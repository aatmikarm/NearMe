package com.aatmik.nearme.util

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
        // Create notification object
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
        val notificationRef = notificationsCollection.add(notification).await()

        // Request cloud function to send FCM notification
        val cloudFunctionData = hashMapOf(
            "notificationId" to notificationRef.id,
            "recipientId" to recipientId
        )

        // This would typically call a Firebase Cloud Function
        // In a real implementation, you would use Firebase Cloud Functions
        // to send the actual FCM notification to the device
    }

    /**
     * Send match notification
     */
    suspend fun sendMatchNotification(
        recipientId: String,
        userName: String,
        matchId: String
    ) {
        // Create notification object
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
        val notificationRef = notificationsCollection.add(notification).await()

        // Request cloud function to send FCM notification
        val cloudFunctionData = hashMapOf(
            "notificationId" to notificationRef.id,
            "recipientId" to recipientId
        )

        // This would typically call a Firebase Cloud Function
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
        // Create notification object
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
        val notificationRef = notificationsCollection.add(notification).await()

        // Request cloud function to send FCM notification
        val cloudFunctionData = hashMapOf(
            "notificationId" to notificationRef.id,
            "recipientId" to recipientId
        )

        // This would typically call a Firebase Cloud Function
    }

    /**
     * Get user's unread notifications
     */
    suspend fun getUnreadNotifications(userId: String): List<Notification> {
        val snapshot = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .whereEqualTo("isRead", false)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(Notification::class.java)?.copy(id = document.id)
        }
    }

    /**
     * Mark notification as read
     */
    suspend fun markNotificationAsRead(notificationId: String) {
        notificationsCollection.document(notificationId)
            .update(
                mapOf(
                    "isRead" to true,
                    "readAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    /**
     * Register FCM token for user
     */
    suspend fun registerFcmToken(userId: String, token: String) {
        firestore.collection("users").document(userId)
            .update("fcmTokens", com.google.firebase.firestore.FieldValue.arrayUnion(token))
            .await()
    }

    /**
     * Unregister FCM token for user
     */
    suspend fun unregisterFcmToken(userId: String, token: String) {
        firestore.collection("users").document(userId)
            .update("fcmTokens", com.google.firebase.firestore.FieldValue.arrayRemove(token))
            .await()
    }
}
