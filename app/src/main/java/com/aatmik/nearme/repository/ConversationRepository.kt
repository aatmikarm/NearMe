package com.aatmik.nearme.repository

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
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val notificationUtil: NotificationUtil
) {
    private val conversationsCollection = firestore.collection("conversations")

    /**
     * Get or create a conversation for a match
     */
    suspend fun getOrCreateConversation(matchId: String): Conversation {
        // Check if conversation exists
        val query = conversationsCollection
            .whereEqualTo("matchId", matchId)
            .limit(1)
            .get()
            .await()

        if (!query.isEmpty) {
            // Return existing conversation
            val document = query.documents.first()
            return document.toObject(Conversation::class.java)?.copy(id = document.id)
                ?: throw Exception("Failed to parse conversation")
        }

        // Get match details to create conversation
        val matchDocument = firestore.collection("matches").document(matchId).get().await()
        val users = matchDocument.get("users") as? List<String> ?: throw Exception("Invalid match data")

        // Create new conversation
        val conversation = Conversation(
            matchId = matchId,
            participants = users,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastReadBy = users.associateWith { 0L }
        )

        // Save to Firestore
        val conversationRef = conversationsCollection.add(conversation).await()

        return conversation.copy(id = conversationRef.id)
    }

    /**
     * Get messages for a conversation as a Flow
     */
    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val query = conversationsCollection
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val subscription = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Message::class.java)?.copy(id = document.id)
            } ?: emptyList()

            trySend(messages)
        }

        awaitClose { subscription.remove() }
    }

    /**
     * Send a message
     */
    suspend fun sendMessage(conversationId: String, senderId: String, text: String): String {
        // Get conversation to check participants
        val conversation = conversationsCollection.document(conversationId).get().await()
            .toObject(Conversation::class.java) ?: throw Exception("Conversation not found")

        // Create message
        val message = Message(
            senderId = senderId,
            text = text,
            type = "text",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        // Add to Firestore
        val messageRef = conversationsCollection
            .document(conversationId)
            .collection("messages")
            .add(message)
            .await()

        // Update conversation last update timestamp
        conversationsCollection.document(conversationId)
            .update("updatedAt", System.currentTimeMillis())
            .await()

        // Send notification to other participants
        val otherParticipants = conversation.participants.filter { it != senderId }
        for (recipientId in otherParticipants) {
            sendMessageNotification(conversationId, senderId, recipientId, text)
        }

        return messageRef.id
    }

    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(conversationId: String, userId: String) {
        // Update last read timestamp
        conversationsCollection.document(conversationId)
            .update(
                mapOf(
                    "lastReadBy.$userId" to System.currentTimeMillis()
                )
            )
            .await()

        // Mark unread messages as read
        val messages = conversationsCollection.document(conversationId)
            .collection("messages")
            .whereNotEqualTo("senderId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()

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
        // Get sender profile
        val senderProfile = userRepository.getUserProfile(senderId)

        senderProfile?.let { profile ->
            notificationUtil.sendMessageNotification(
                recipientId,
                profile.displayName,
                messageText,
                conversationId
            )
        }
    }
}
