package com.aatmik.nearme.repository

import android.util.Log
import com.aatmik.nearme.model.Friend
import com.aatmik.nearme.model.FriendRequest
import com.aatmik.nearme.util.NotificationUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val notificationUtil: NotificationUtil
) {

    private val TAG = "NearMe_FriendRepository"

    private val friendRequestsCollection = firestore.collection("friendRequests")
    private val friendsCollection = firestore.collection("friends")

    /**
     * Get sent friend requests
     */
    suspend fun getSentRequests(userId: String): List<FriendRequest> {
        Log.d(TAG, "Getting sent requests for user: $userId")

        try {
            val snapshot = friendRequestsCollection
                .whereEqualTo("requestedBy", userId)
                .whereEqualTo("status", "pending")
                .orderBy("requestedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            return snapshot.documents.mapNotNull { document ->
                document.toObject(FriendRequest::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sent requests: $e")
            return emptyList()
        }
    }

    /**
     * Get received friend requests
     */
    suspend fun getReceivedRequests(userId: String): List<FriendRequest> {
        Log.d(TAG, "Getting received requests for user: $userId")

        try {
            val snapshot = friendRequestsCollection
                .whereArrayContains("users", userId)
                .whereNotEqualTo("requestedBy", userId)
                .whereEqualTo("status", "pending")
                .orderBy("requestedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            return snapshot.documents.mapNotNull { document ->
                document.toObject(FriendRequest::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting received requests: $e")
            return emptyList()
        }
    }

    /**
     * Get friends list
     */
    suspend fun getFriends(userId: String): List<Friend> {
        Log.d(TAG, "Getting friends for user: $userId")

        try {
            val snapshot = friendsCollection
                .whereArrayContains("users", userId)
                .orderBy("friendsSince", Query.Direction.DESCENDING)
                .get()
                .await()

            return snapshot.documents.mapNotNull { document ->
                document.toObject(Friend::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting friends: $e")
            return emptyList()
        }
    }

    /**
     * Send friend request
     */
    suspend fun sendFriendRequest(fromUserId: String, toUserId: String, proximityEventId: String): String {
        Log.d(TAG, "Sending friend request from $fromUserId to $toUserId")

        try {
            val request = FriendRequest(
                users = listOf(fromUserId, toUserId).sorted(),
                requestedAt = System.currentTimeMillis(),
                proximityEventId = proximityEventId,
                requestedBy = fromUserId,
                status = "pending",
                lastInteraction = System.currentTimeMillis()
            )

            val requestRef = friendRequestsCollection.add(request).await()
            Log.d(TAG, "Friend request sent with ID: ${requestRef.id}")
            return requestRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error sending friend request: $e")
            throw e
        }
    }

    /**
     * Accept friend request
     */
    suspend fun acceptFriendRequest(requestId: String): Boolean {
        Log.d(TAG, "Accepting friend request: $requestId")

        try {
            // Get the request
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val request = requestDoc.toObject(FriendRequest::class.java) ?: return false

            // Create friendship
            val friend = Friend(
                users = request.users,
                friendsSince = System.currentTimeMillis(),
                proximityEventId = request.proximityEventId,
                instagramShared = request.users.associateWith { false },
                lastInteraction = System.currentTimeMillis()
            )

            friendsCollection.add(friend).await()

            // Update request status
            friendRequestsCollection.document(requestId)
                .update("status", "accepted", "acceptedAt", System.currentTimeMillis())
                .await()

            Log.d(TAG, "Friend request accepted: $requestId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting friend request: $e")
            return false
        }
    }

    /**
     * Reject friend request
     */
    suspend fun rejectFriendRequest(requestId: String): Boolean {
        Log.d(TAG, "Rejecting friend request: $requestId")

        try {
            friendRequestsCollection.document(requestId)
                .update("status", "rejected", "rejectedAt", System.currentTimeMillis())
                .await()

            Log.d(TAG, "Friend request rejected: $requestId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting friend request: $e")
            return false
        }
    }

    /**
     * Remove friend
     */
    suspend fun removeFriend(friendId: String): Boolean {
        Log.d(TAG, "Removing friend: $friendId")

        try {
            friendsCollection.document(friendId).delete().await()
            Log.d(TAG, "Friend removed: $friendId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing friend: $e")
            return false
        }
    }

    /**
     * Update last message for friend
     */
    suspend fun updateLastMessage(friendId: String, text: String, senderId: String) {
        try {
            friendsCollection.document(friendId)
                .update(
                    mapOf(
                        "lastMessage" to mapOf(
                            "text" to text,
                            "sentAt" to System.currentTimeMillis(),
                            "senderId" to senderId
                        ),
                        "lastInteraction" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last message: $e")
        }
    }

    /**
     * Get friend by ID
     */
    suspend fun getFriend(friendId: String): Friend? {
        Log.d(TAG, "Getting friend by ID: $friendId")

        try {
            val document = friendsCollection.document(friendId).get().await()
            return document.toObject(Friend::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting friend by ID: $friendId", e)
            return null
        }
    }


    /**
     * Get friend by proximity event ID
     */
    suspend fun getFriendByProximityEventId(proximityEventId: String): Friend? {
        try {
            val snapshot = friendsCollection
                .whereEqualTo("proximityEventId", proximityEventId)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                return document.toObject(Friend::class.java)?.copy(id = document.id)
            }

            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting friend by proximity event ID: $proximityEventId", e)
            return null
        }
    }
}