package com.aatmik.nearme.repository

import android.net.Uri
import com.aatmik.nearme.model.UserPhoto
import com.aatmik.nearme.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.aatmik.nearme.model.SubscriptionInfo
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Get a user's profile
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        val document = usersCollection.document(userId).get().await()
        return document.toObject(UserProfile::class.java)
    }


    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
    /**
     * Create a new user profile
     */
    suspend fun createUserProfile(userProfile: UserProfile): Boolean {
        return try {
            usersCollection.document(userProfile.uid)
                .set(userProfile)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update a user's profile
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            usersCollection.document(userId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update user's last active timestamp
     */
    suspend fun updateLastActive(userId: String) {
        usersCollection.document(userId)
            .update("lastActive", System.currentTimeMillis())
            .await()
    }

    /**
     * Upload a profile photo
     */
    suspend fun uploadProfilePhoto(userId: String, photoUri: Uri, isPrimary: Boolean = false): UserPhoto? {
        val photoId = UUID.randomUUID().toString()
        val photoRef = storage.reference.child("profile_photos/$userId/original/$photoId.jpg")
        val thumbnailRef = storage.reference.child("profile_photos/$userId/thumbnails/${photoId}_thumb.jpg")

        try {
            // Upload original photo
            photoRef.putFile(photoUri).await()
            val photoUrl = photoRef.downloadUrl.await().toString()

            // Upload thumbnail (in a real app, you'd resize the image first)
            thumbnailRef.putFile(photoUri).await()
            val thumbnailUrl = thumbnailRef.downloadUrl.await().toString()

            // Create UserPhoto object
            val userPhoto = UserPhoto(
                id = photoId,
                url = photoUrl,
                thumbnailUrl = thumbnailUrl,
                isPrimary = isPrimary
            )

            // Update user's photos in Firestore
            val userProfile = getUserProfile(userId)
            val updatedPhotos = userProfile?.photos?.toMutableList() ?: mutableListOf()

            // If this is primary, set all others to non-primary
            if (isPrimary) {
                updatedPhotos.forEach { photo ->
                    photo.copy(isPrimary = false)
                }
            }

            // Add the new photo
            updatedPhotos.add(userPhoto)

            // Update Firestore
            usersCollection.document(userId)
                .update("profile.photos", updatedPhotos)
                .await()

            return userPhoto
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Delete a profile photo
     */
    suspend fun deleteProfilePhoto(userId: String, photoId: String): Boolean {
        try {
            // Delete from Storage
            val photoRef = storage.reference.child("profile_photos/$userId/original/$photoId.jpg")
            val thumbnailRef = storage.reference.child("profile_photos/$userId/thumbnails/${photoId}_thumb.jpg")

            photoRef.delete().await()
            thumbnailRef.delete().await()

            // Update user's photos in Firestore
            val userProfile = getUserProfile(userId)
            val updatedPhotos = userProfile?.photos?.toMutableList() ?: return false

            // Remove the photo
            updatedPhotos.removeAll { it.id == photoId }

            // If we removed the primary photo, make the first one primary
            if (updatedPhotos.isNotEmpty() && !updatedPhotos.any { it.isPrimary }) {
                updatedPhotos[0] = updatedPhotos[0].copy(isPrimary = true)
            }

            // Update Firestore
            usersCollection.document(userId)
                .update("profile.photos", updatedPhotos)
                .await()

            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Connect Instagram account
     */
    suspend fun connectInstagram(userId: String, instagramId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "profile.instagramId" to instagramId,
                        "profile.instagramConnected" to true
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Disconnect Instagram account
     */
    suspend fun disconnectInstagram(userId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "profile.instagramConnected" to false
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update discovery preferences
     */
    suspend fun updateDiscoveryPreferences(userId: String, preferences: Map<String, Any>): Boolean {
        val prefixedPreferences = preferences.mapKeys { "preferences.${it.key}" }

        return try {
            usersCollection.document(userId)
                .update(prefixedPreferences)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update privacy settings
     */
    suspend fun updatePrivacySettings(userId: String, settings: Map<String, Any>): Boolean {
        val prefixedSettings = settings.mapKeys { "privacy.${it.key}" }

        return try {
            usersCollection.document(userId)
                .update(prefixedSettings)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Block a user
     */
    suspend fun blockUser(userId: String, blockedUserId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("blockedUsers", com.google.firebase.firestore.FieldValue.arrayUnion(blockedUserId))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Unblock a user
     */
    suspend fun unblockUser(userId: String, blockedUserId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("blockedUsers", com.google.firebase.firestore.FieldValue.arrayRemove(blockedUserId))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get blocked users list
     */
    suspend fun getBlockedUsers(userId: String): List<String> {
        val document = usersCollection.document(userId).get().await()
        return document.get("blockedUsers") as? List<String> ?: emptyList()
    }

    /**
     * Update online status
     */
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        usersCollection.document(userId)
            .update("isOnline", isOnline)
            .await()
    }

    /**
     * Increment user statistic
     */
    suspend fun incrementStatistic(userId: String, statisticField: String, amount: Int = 1) {
        usersCollection.document(userId)
            .update(
                "statistics.$statisticField",
                com.google.firebase.firestore.FieldValue.increment(amount.toLong())
            )
            .await()
    }

    /**
     * Update subscription information
     */
    suspend fun updateSubscription(userId: String, subscription: SubscriptionInfo): Boolean {
        return try {
            usersCollection.document(userId)
                .update("subscription", subscription)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Search for users by name (for admin purposes)
     */
    suspend fun searchUsersByName(query: String, limit: Int = 20): List<UserProfile> {
        val snapshot = usersCollection
            .orderBy("profile.displayName")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(UserProfile::class.java)
        }
    }

    /**
     * Delete user account (complete account deletion)
     */
    suspend fun deleteUserAccount(userId: String): Boolean {
        return try {
            // Delete from Authentication (would typically be done via Cloud Function)

            // Delete user's photos
            val userProfile = getUserProfile(userId) ?: return false

            // Delete photos from storage
            for (photo in userProfile.photos) {
                storage.reference.child("profile_photos/$userId/original/${photo.id}.jpg").delete().await()
                storage.reference.child("profile_photos/$userId/thumbnails/${photo.id}_thumb.jpg").delete().await()
            }

            // Delete from Firestore
            usersCollection.document(userId).delete().await()

            true
        } catch (e: Exception) {
            false
        }
    }
}