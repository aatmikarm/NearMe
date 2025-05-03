package com.aatmik.nearme.util

import android.util.Log
import com.aatmik.nearme.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUtil @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Add a TAG for logging
    private val TAG = "NearMe_NotificationUtil"

    private val notificationsCollection = firestore.collection("notifications")

    /**
     * Send proximity notification
     */
    suspend fun sendProximityNotification(
        recipientId: String,
        userName: String,
        distance: Int,
        proximityEventId: String
    ) {
        Log.d(TAG, "Sending proximity notification to user: $recipientId about user: $userName at distance: $distance meters")

        try {
            val startTime = System.currentTimeMillis()

            // Create notification object
            Log.d(TAG, "Creating notification object")
            val notification = Notification(
                recipientId = recipientId,
                type = "proximity",
                title = "Someone Nearby!",
                body = "$userName is $distance meters away",
                data = mapOf(
                    "actionType" to "proximity_event",
                    "entityId" to proximityEventId
                ),
                createdAt = System.currentTimeMillis(),
                isRead = false,
                sentToDevice = false
            )

            // Save to Firestore
            Log.d(TAG, "Saving notification to Firestore")
            val notificationRef = notificationsCollection.add(notification).await()
            Log.d(TAG, "Created notification with ID: ${notificationRef.id}")

            // Request cloud function to send FCM notification
            val cloudFunctionData = hashMapOf(
                "notificationId" to notificationRef.id,
                "recipientId" to recipientId
            )

            Log.d(TAG, "Would call Cloud Function to send FCM notification")
            // This would typically call a Firebase Cloud Function
            // In a real implementation, you would use Firebase Cloud Functions
            // to send the actual FCM notification to the device

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Proximity notification process completed in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending proximity notification to user: $recipientId", e)
        }
    }

    /**
     * Send match notification
     */
    suspend fun sendMatchNotification(
        recipientId: String,
        userName: String,
        matchId: String
    ) {
        Log.d(TAG, "Sending match notification to user: $recipientId about matching with: $userName")

        try {
            val startTime = System.currentTimeMillis()

            // Create notification object
            Log.d(TAG, "Creating notification object")
            val notification = Notification(
                recipientId = recipientId,
                type = "match",
                title = "New Match!",
                body = "You matched with $userName",
                data = mapOf(
                    "actionType" to "match",
                    "entityId" to matchId
                ),
                createdAt = System.currentTimeMillis(),
                isRead = false,
                sentToDevice = false
            )

            // Save to Firestore
            Log.d(TAG, "Saving notification to Firestore")
            val notificationRef = notificationsCollection.add(notification).await()
            Log.d(TAG, "Created notification with ID: ${notificationRef.id}")

            // Request cloud function to send FCM notification
            val cloudFunctionData = hashMapOf(
                "notificationId" to notificationRef.id,
                "recipientId" to recipientId
            )

            Log.d(TAG, "Would call Cloud Function to send FCM notification")
            // This would typically call a Firebase Cloud Function

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Match notification process completed in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending match notification to user: $recipientId", e)
        }
    }

    /**
     * Send message notification
     */
    suspend fun sendMessageNotification(
        recipientId: String,
        senderName: String,
        messageText: String,
        conversationId: String
    ) {
        Log.d(TAG, "Sending message notification to user: $recipientId from: $senderName")

        try {
            val startTime = System.currentTimeMillis()

            // Create notification object
            Log.d(TAG, "Creating notification object")
            val notification = Notification(
                recipientId = recipientId,
                type = "message",
                title = senderName,
                body = messageText,
                data = mapOf(
                    "actionType" to "message",
                    "entityId" to conversationId
                ),
                createdAt = System.currentTimeMillis(),
                isRead = false,
                sentToDevice = false
            )

            // Save to Firestore
            Log.d(TAG, "Saving notification to Firestore")
            val notificationRef = notificationsCollection.add(notification).await()
            Log.d(TAG, "Created notification with ID: ${notificationRef.id}")

            // Request cloud function to send FCM notification
            val cloudFunctionData = hashMapOf(
                "notificationId" to notificationRef.id,
                "recipientId" to recipientId
            )

            Log.d(TAG, "Would call Cloud Function to send FCM notification")
            // This would typically call a Firebase Cloud Function

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Message notification process completed in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message notification to user: $recipientId", e)
        }
    }

    /**
     * Get user's unread notifications
     */
    suspend fun getUnreadNotifications(userId: String): List<Notification> {
        Log.d(TAG, "Getting unread notifications for user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            val snapshot = notificationsCollection
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("isRead", false)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val notifications = snapshot.documents.mapNotNull { document ->
                document.toObject(Notification::class.java)?.copy(id = document.id)
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Retrieved ${notifications.size} unread notifications for user: $userId in $duration ms")

            return notifications
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving unread notifications for user: $userId", e)
            return emptyList()
        }
    }

    /**
     * Mark notification as read
     */
    suspend fun markNotificationAsRead(notificationId: String) {
        Log.d(TAG, "Marking notification as read: $notificationId")

        try {
            val startTime = System.currentTimeMillis()

            notificationsCollection.document(notificationId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "readAt" to System.currentTimeMillis()
                    )
                )
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Marked notification as read: $notificationId in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: $notificationId", e)
        }
    }

    /**
     * Register FCM token for user
     */
    suspend fun registerFcmToken(userId: String, token: String) {
        Log.d(TAG, "Registering FCM token for user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            firestore.collection("users").document(userId)
                .update("fcmTokens", com.google.firebase.firestore.FieldValue.arrayUnion(token))
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Registered FCM token for user: $userId in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering FCM token for user: $userId", e)
        }
    }

    /**
     * Unregister FCM token for user
     */
    suspend fun unregisterFcmToken(userId: String, token: String) {
        Log.d(TAG, "Unregistering FCM token for user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            firestore.collection("users").document(userId)
                .update("fcmTokens", com.google.firebase.firestore.FieldValue.arrayRemove(token))
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Unregistered FCM token for user: $userId in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering FCM token for user: $userId", e)
        }
    }
}