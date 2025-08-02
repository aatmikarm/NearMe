package com.aatmik.nearme.repository

import android.util.Log
import com.aatmik.nearme.model.Conversation
import com.aatmik.nearme.util.NotificationUtil
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.aatmik.nearme.model.Message
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val notificationUtil: NotificationUtil
) {

    private val TAG = "NearMe_ConversationRepo"

    private val conversationsCollection = firestore.collection("conversations")

    /**
     * Get or create a conversation for a friend
     */
    suspend fun getOrCreateConversation(friendId: String): Conversation {
        Log.d(TAG, "Getting or creating conversation for friendId: $friendId")

        try {
            val startTime = System.currentTimeMillis()

            // Check if conversation exists
            Log.d(TAG, "Checking if conversation already exists")
            val query = conversationsCollection
                .whereEqualTo("friendId", friendId)
                .limit(1)
                .get()
                .await()

            if (!query.isEmpty) {
                // Return existing conversation
                Log.d(TAG, "Found existing conversation for friendId: $friendId")
                val document = query.documents.first()
                val conversation = document.toObject(Conversation::class.java)?.copy(id = document.id)

                if (conversation == null) {
                    Log.e(TAG, "Failed to parse existing conversation document")
                    throw Exception("Failed to parse conversation")
                }

                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "Retrieved existing conversation in $duration ms")
                return conversation
            }

            // Get friend details to create conversation
            Log.d(TAG, "No existing conversation found, creating new one")
            Log.d(TAG, "Fetching friend details from Firestore")
            val friendDocument = firestore.collection("friends").document(friendId).get().await()
            val users = friendDocument.get("users") as? List<String>

            if (users == null) {
                Log.e(TAG, "Invalid friend data: users list is null")
                throw Exception("Invalid friend data")
            }

            Log.d(TAG, "Creating new conversation with ${users.size} participants")

            // Create new conversation
            val conversation = Conversation(
                friendId = friendId,
                participants = users,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastReadBy = users.associateWith { 0L }
            )

            // Save to Firestore
            Log.d(TAG, "Saving new conversation to Firestore")
            val conversationRef = conversationsCollection.add(conversation).await()

            val newConversation = conversation.copy(id = conversationRef.id)
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Created new conversation with ID: ${conversationRef.id} in $duration ms")

            return newConversation
        } catch (e: Exception) {
            Log.e(TAG, "Error getting or creating conversation for friendId: $friendId", e)
            throw e
        }
    }

    /**
     * Get messages for a conversation as a Flow
     */
    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        Log.d(TAG, "Setting up messages flow for conversationId: $conversationId")

        try {
            val query = conversationsCollection
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)

            Log.d(TAG, "Adding snapshot listener for messages")
            val subscription = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error in messages snapshot listener: ${exception.message}", exception)
                    close(exception)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { document ->
                    val message = document.toObject(Message::class.java)
                    message?.copy(id = document.id)
                } ?: emptyList()

                Log.d(TAG, "Received ${messages.size} messages update for conversationId: $conversationId")
                trySend(messages)
            }

            awaitClose {
                Log.d(TAG, "Removing messages snapshot listener for conversationId: $conversationId")
                subscription.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up messages flow for conversationId: $conversationId", e)
            close(e)
        }
    }

    /**
     * Send a message
     */
    suspend fun sendMessage(conversationId: String, senderId: String, text: String): String {
        Log.d(TAG, "Sending message from user: $senderId in conversation: $conversationId")

        try {
            val startTime = System.currentTimeMillis()

            // Get conversation to check participants
            Log.d(TAG, "Fetching conversation details")
            val conversation = conversationsCollection.document(conversationId).get().await()
                .toObject(Conversation::class.java)

            if (conversation == null) {
                Log.e(TAG, "Conversation not found: $conversationId")
                throw Exception("Conversation not found")
            }

            // Create message
            Log.d(TAG, "Creating message object")
            val message = Message(
                senderId = senderId,
                text = text,
                type = "text",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            // Add to Firestore
            Log.d(TAG, "Saving message to Firestore")
            val messageRef = conversationsCollection
                .document(conversationId)
                .collection("messages")
                .add(message)
                .await()

            // Update conversation last update timestamp
            Log.d(TAG, "Updating conversation timestamp")
            conversationsCollection.document(conversationId)
                .update("updatedAt", System.currentTimeMillis())
                .await()

            // Send notification to other participants
            val otherParticipants = conversation.participants.filter { it != senderId }
            Log.d(TAG, "Sending notifications to ${otherParticipants.size} other participants")

            for (recipientId in otherParticipants) {
                sendMessageNotification(conversationId, senderId, recipientId, text)
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Message sent with ID: ${messageRef.id} in $duration ms")

            return messageRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message in conversation: $conversationId", e)
            throw e
        }
    }

    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(conversationId: String, userId: String) {
        Log.d(TAG, "Marking messages as read for user: $userId in conversation: $conversationId")

        try {
            val startTime = System.currentTimeMillis()

            // Update last read timestamp
            Log.d(TAG, "Updating last read timestamp")
            conversationsCollection.document(conversationId)
                .update(
                    mapOf(
                        "lastReadBy.$userId" to System.currentTimeMillis()
                    )
                )
                .await()

            // Mark unread messages as read
            Log.d(TAG, "Fetching unread messages")
            val messages = conversationsCollection.document(conversationId)
                .collection("messages")
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            Log.d(TAG, "Found ${messages.size()} unread messages to mark as read")

            var markedCount = 0
            for (document in messages.documents) {
                conversationsCollection.document(conversationId)
                    .collection("messages")
                    .document(document.id)
                    .update(
                        mapOf(
                            "isRead" to true,
                            "readAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                markedCount++
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Marked $markedCount messages as read in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read in conversation: $conversationId", e)
        }
    }

    /**
     * Send message notification
     */
    private suspend fun sendMessageNotification(
        conversationId: String,
        senderId: String,
        recipientId: String,
        messageText: String
    ) {
        Log.d(TAG, "Preparing to send message notification from user: $senderId to: $recipientId")

        try {
            val startTime = System.currentTimeMillis()

            // Get sender profile
            Log.d(TAG, "Fetching sender profile for notification")
            val senderProfile = userRepository.getUserProfile(senderId)

            if (senderProfile == null) {
                Log.w(TAG, "Sender profile not found, cannot send notification")
                return
            }

            Log.d(TAG, "Sending message notification with sender name: ${senderProfile.displayName}")
            notificationUtil.sendMessageNotification(
                recipientId,
                senderProfile.displayName,
                messageText,
                conversationId
            )

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Message notification sent in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message notification", e)
        }
    }
}