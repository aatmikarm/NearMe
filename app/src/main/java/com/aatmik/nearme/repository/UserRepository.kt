package com.aatmik.nearme.repository

import android.net.Uri
import android.util.Log
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

    private val TAG = "NearMe_UserRepository"

    private val usersCollection = firestore.collection("users")

    suspend fun getUserProfile(userId: String): UserProfile? {
        Log.d(TAG, "Getting user profile for userId: $userId")
        try {
            val startTime = System.currentTimeMillis()
            val document = usersCollection.document(userId).get().await()
            val profile = document.toObject(UserProfile::class.java)

            val duration = System.currentTimeMillis() - startTime
            if (profile != null) {
                Log.d(TAG, "Retrieved profile for user $userId in $duration ms")
            } else {
                Log.w(TAG, "Profile not found for user $userId (took $duration ms)")
            }

            return profile
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile for $userId: ${e.message}", e)
            return null
        }
    }

    fun getCurrentUserId(): String? {
        Log.d(TAG, "Getting current user ID")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            Log.d(TAG, "Current user ID: $uid")
        } else {
            Log.w(TAG, "Current user ID is null (user not authenticated)")
        }
        return uid
    }

    suspend fun createUserProfile(userProfile: UserProfile): Boolean {
        Log.d(TAG, "Creating user profile for uid: ${userProfile.uid}")
        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userProfile.uid)
                .set(userProfile)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "User profile created successfully for uid: ${userProfile.uid} in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user profile for uid: ${userProfile.uid}", e)
            false
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Boolean {
        Log.d(TAG, "Updating user profile for userId: $userId with fields: ${updates.keys}")
        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update(updates)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "User profile updated successfully for userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile for userId: $userId", e)
            false
        }
    }

    suspend fun updateLastActive(userId: String) {
        Log.d(TAG, "Updating last active timestamp for userId: $userId")
        try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update("lastActive", System.currentTimeMillis())
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Last active timestamp updated for userId: $userId in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last active timestamp for userId: $userId", e)
        }
    }

    suspend fun uploadProfilePhoto(userId: String, photoUri: Uri, isPrimary: Boolean = false): UserPhoto? {
        Log.d(TAG, "Uploading profile photo for userId: $userId, isPrimary: $isPrimary")
        val photoId = UUID.randomUUID().toString()
        val photoRef = storage.reference.child("profile_photos/$userId/original/$photoId.jpg")
        val thumbnailRef = storage.reference.child("profile_photos/$userId/thumbnails/${photoId}_thumb.jpg")

        try {
            val startTime = System.currentTimeMillis()

            // Upload original photo (which is now compressed)
            Log.d(TAG, "Uploading original photo to storage")
            photoRef.putFile(photoUri).await()

            // Get the download URL
            val photoUrl = photoRef.downloadUrl.await().toString()
            Log.d(TAG, "Original photo uploaded, URL: $photoUrl")

            // For thumbnail, we can use the same compressed image since it's already small
            Log.d(TAG, "Uploading thumbnail photo to storage")
            thumbnailRef.putFile(photoUri).await()

            // Get the thumbnail download URL
            val thumbnailUrl = thumbnailRef.downloadUrl.await().toString()
            Log.d(TAG, "Thumbnail uploaded, URL: $thumbnailUrl")

            // Create UserPhoto object
            val userPhoto = UserPhoto(
                id = photoId,
                url = photoUrl,
                thumbnailUrl = thumbnailUrl,
                isPrimary = isPrimary
            )

            // Update user's photos in Firestore
            Log.d(TAG, "Fetching existing user profile to update photos list")
            val userProfile = getUserProfile(userId)
            val updatedPhotos = userProfile?.photos?.toMutableList() ?: mutableListOf()
            Log.d(TAG, "Current photo count: ${updatedPhotos.size}")

            // If this is primary, set all others to non-primary
            if (isPrimary) {
                Log.d(TAG, "Setting other photos to non-primary")
                for (i in 0 until updatedPhotos.size) {
                    if (updatedPhotos[i].isPrimary) {
                        updatedPhotos[i] = updatedPhotos[i].copy(isPrimary = false)
                    }
                }
            }

            // Add the new photo
            updatedPhotos.add(userPhoto)
            Log.d(TAG, "New photo count: ${updatedPhotos.size}")

            // Update Firestore
            Log.d(TAG, "Updating user document with new photo list")
            usersCollection.document(userId)
                .update("photos", updatedPhotos)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Profile photo upload completed for userId: $userId in $duration ms")
            return userPhoto
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile photo for userId: $userId", e)
            return null
        }
    }

    suspend fun deleteProfilePhoto(userId: String, photoId: String): Boolean {
        Log.d(TAG, "Deleting profile photo for userId: $userId, photoId: $photoId")
        try {
            val startTime = System.currentTimeMillis()

            // Delete from Storage
            Log.d(TAG, "Deleting photo files from storage")
            val photoRef = storage.reference.child("profile_photos/$userId/original/$photoId.jpg")
            val thumbnailRef = storage.reference.child("profile_photos/$userId/thumbnails/${photoId}_thumb.jpg")

            photoRef.delete().await()
            thumbnailRef.delete().await()
            Log.d(TAG, "Photo files deleted from storage")

            // Update user's photos in Firestore
            Log.d(TAG, "Updating user profile photos list")
            val userProfile = getUserProfile(userId)
            val updatedPhotos = userProfile?.photos?.toMutableList() ?: run {
                Log.w(TAG, "User profile not found, can't update photos")
                return false
            }

            // Remove the photo
            val photoCount = updatedPhotos.size
            updatedPhotos.removeAll { it.id == photoId }
            Log.d(TAG, "Removed photo from list (before: $photoCount, after: ${updatedPhotos.size})")

            // If we removed the primary photo, make the first one primary
            if (updatedPhotos.isNotEmpty() && !updatedPhotos.any { it.isPrimary }) {
                Log.d(TAG, "Primary photo was removed, setting first photo as primary")
                updatedPhotos[0] = updatedPhotos[0].copy(isPrimary = true)
            }

            // Update Firestore
            usersCollection.document(userId)
                .update("profile.photos", updatedPhotos)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Profile photo deletion completed for userId: $userId in $duration ms")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting profile photo for userId: $userId, photoId: $photoId", e)
            return false
        }
    }

    suspend fun connectInstagram(userId: String, instagramId: String): Boolean {
        Log.d(TAG, "Connecting Instagram account for userId: $userId, instagramId: $instagramId")
        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update(
                    mapOf(
                        "profile.instagramId" to instagramId,
                        "profile.instagramConnected" to true
                    )
                )
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Instagram connected successfully for userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting Instagram for userId: $userId", e)
            false
        }
    }

    suspend fun disconnectInstagram(userId: String): Boolean {
        Log.d(TAG, "Disconnecting Instagram account for userId: $userId")
        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update(
                    mapOf(
                        "profile.instagramConnected" to false
                    )
                )
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Instagram disconnected for userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting Instagram for userId: $userId", e)
            false
        }
    }

    suspend fun updateDiscoveryPreferences(userId: String, preferences: Map<String, Any>): Boolean {
        Log.d(TAG, "Updating discovery preferences for userId: $userId with fields: ${preferences.keys}")
        val prefixedPreferences = preferences.mapKeys { "preferences.${it.key}" }

        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update(prefixedPreferences)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Discovery preferences updated for userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating discovery preferences for userId: $userId", e)
            false
        }
    }

    suspend fun updatePrivacySettings(userId: String, settings: Map<String, Any>): Boolean {
        Log.d(TAG, "Updating privacy settings for userId: $userId with fields: ${settings.keys}")
        val prefixedSettings = settings.mapKeys { "privacy.${it.key}" }

        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update(prefixedSettings)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Privacy settings updated for userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating privacy settings for userId: $userId", e)
            false
        }
    }

    suspend fun blockUser(userId: String, blockedUserId: String): Boolean {
        Log.d(TAG, "Blocking user: $blockedUserId for userId: $userId")
        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update("blockedUsers", com.google.firebase.firestore.FieldValue.arrayUnion(blockedUserId))
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "User $blockedUserId blocked successfully by userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error blocking user $blockedUserId for userId: $userId", e)
            false
        }
    }

    suspend fun unblockUser(userId: String, blockedUserId: String): Boolean {
        Log.d(TAG, "Unblocking user: $blockedUserId for userId: $userId")
        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update("blockedUsers", com.google.firebase.firestore.FieldValue.arrayRemove(blockedUserId))
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "User $blockedUserId unblocked successfully by userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unblocking user $blockedUserId for userId: $userId", e)
            false
        }
    }

    suspend fun getBlockedUsers(userId: String): List<String> {
        Log.d(TAG, "Getting blocked users list for userId: $userId")
        try {
            val startTime = System.currentTimeMillis()

            val document = usersCollection.document(userId).get().await()
            val blockedUsers = document.get("blockedUsers") as? List<String> ?: emptyList()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Retrieved ${blockedUsers.size} blocked users for userId: $userId in $duration ms")
            return blockedUsers
        } catch (e: Exception) {
            Log.e(TAG, "Error getting blocked users for userId: $userId", e)
            return emptyList()
        }
    }

    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        Log.d(TAG, "Updating online status for userId: $userId to $isOnline")
        try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update("isOnline", isOnline)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Online status updated to $isOnline for userId: $userId in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating online status for userId: $userId", e)
        }
    }

    suspend fun incrementStatistic(userId: String, statisticField: String, amount: Int = 1) {
        Log.d(TAG, "Incrementing statistic '$statisticField' by $amount for userId: $userId")
        try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update(
                    "statistics.$statisticField",
                    com.google.firebase.firestore.FieldValue.increment(amount.toLong())
                )
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Statistic '$statisticField' incremented for userId: $userId in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing statistic '$statisticField' for userId: $userId", e)
        }
    }

    suspend fun updateSubscription(userId: String, subscription: SubscriptionInfo): Boolean {
        Log.d(TAG, "Updating subscription for userId: $userId, plan: ${subscription.plan}")
        return try {
            val startTime = System.currentTimeMillis()

            usersCollection.document(userId)
                .update("subscription", subscription)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Subscription updated for userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subscription for userId: $userId", e)
            false
        }
    }

    /**
     * Search for users by name (for admin purposes)
     */
    suspend fun searchUsersByName(query: String, limit: Int = 20): List<UserProfile> {
        Log.d(TAG, "Searching users by name with query: '$query', limit: $limit")
        try {
            val startTime = System.currentTimeMillis()

            val snapshot = usersCollection
                .orderBy("profile.displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { document ->
                document.toObject(UserProfile::class.java)
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Found ${users.size} users matching query: '$query' in $duration ms")
            return users
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users by name with query: '$query'", e)
            return emptyList()
        }
    }

    /**
     * Delete user account (complete account deletion)
     */
    suspend fun deleteUserAccount(userId: String): Boolean {
        Log.d(TAG, "Deleting user account for userId: $userId")
        return try {
            val startTime = System.currentTimeMillis()

            // Delete from Authentication (would typically be done via Cloud Function)
            Log.d(TAG, "Retrieving user profile for deletion")

            // Delete user's photos
            val userProfile = getUserProfile(userId)
            if (userProfile == null) {
                Log.w(TAG, "User profile not found for userId: $userId, aborting deletion")
                return false
            }

            // Delete photos from storage
            Log.d(TAG, "Deleting ${userProfile.photos.size} photos from storage")
            for (photo in userProfile.photos) {
                Log.d(TAG, "Deleting photo: ${photo.id}")
                storage.reference.child("profile_photos/$userId/original/${photo.id}.jpg").delete().await()
                storage.reference.child("profile_photos/$userId/thumbnails/${photo.id}_thumb.jpg").delete().await()
            }

            // Delete from Firestore
            Log.d(TAG, "Deleting user document from Firestore")
            usersCollection.document(userId).delete().await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "User account deleted successfully for userId: $userId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user account for userId: $userId", e)
            false
        }
    }
}