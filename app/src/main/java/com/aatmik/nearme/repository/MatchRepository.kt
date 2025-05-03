package com.aatmik.nearme.repository

import android.util.Log
import com.aatmik.nearme.model.Match
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.NotificationUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val notificationUtil: NotificationUtil
) {
    // Add a TAG for logging
    private val TAG = "NearMe_MatchRepository"

    private val matchesCollection = firestore.collection("matches")

    /**
     * Get all matches for a user
     */
    suspend fun getMatches(userId: String): List<Match> {
        Log.d(TAG, "Getting all matches for user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            val snapshot = matchesCollection
                .whereArrayContains("users", userId)
                .whereEqualTo("status", "active")
                .orderBy("matchedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val matches = snapshot.documents.mapNotNull { document ->
                document.toObject(Match::class.java)?.copy(id = document.id)
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Retrieved ${matches.size} matches for user: $userId in $duration ms")

            return matches
        } catch (e: Exception) {
            Log.e(TAG, "Error getting matches for user: $userId", e)
            return emptyList()
        }
    }

    /**
     * Get new matches for a user (past 24 hours)
     */
    suspend fun getNewMatches(userId: String): List<Match> {
        Log.d(TAG, "Getting new matches (past 24h) for user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            Log.d(TAG, "Timestamp for 24 hours ago: $oneDayAgo")

            val snapshot = matchesCollection
                .whereArrayContains("users", userId)
                .whereEqualTo("status", "active")
                .whereGreaterThan("matchedAt", oneDayAgo)
                .orderBy("matchedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val matches = snapshot.documents.mapNotNull { document ->
                document.toObject(Match::class.java)?.copy(id = document.id)
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Retrieved ${matches.size} new matches for user: $userId in $duration ms")

            return matches
        } catch (e: Exception) {
            Log.e(TAG, "Error getting new matches for user: $userId", e)
            return emptyList()
        }
    }

    /**
     * Get match by ID
     */
    suspend fun getMatch(matchId: String): Match? {
        Log.d(TAG, "Getting match by ID: $matchId")

        try {
            val startTime = System.currentTimeMillis()

            val document = matchesCollection.document(matchId).get().await()
            val match = document.toObject(Match::class.java)?.copy(id = document.id)

            val duration = System.currentTimeMillis() - startTime
            if (match != null) {
                Log.d(TAG, "Retrieved match: $matchId in $duration ms")
            } else {
                Log.w(TAG, "Match not found: $matchId")
            }

            return match
        } catch (e: Exception) {
            Log.e(TAG, "Error getting match by ID: $matchId", e)
            return null
        }
    }

    /**
     * Create or update a match between two users
     */
    suspend fun createOrUpdateMatch(userId1: String, userId2: String, proximityEventId: String): String {
        Log.d(TAG, "Creating or updating match between users: $userId1 and $userId2")

        try {
            val startTime = System.currentTimeMillis()

            val users = listOf(userId1, userId2).sorted()
            Log.d(TAG, "Sorted user IDs: $users")

            // Check if match already exists
            Log.d(TAG, "Checking if match already exists")
            val existingMatches = matchesCollection
                .whereEqualTo("users", users)
                .get()
                .await()

            if (existingMatches.documents.isNotEmpty()) {
                // Update existing match
                val matchId = existingMatches.documents.first().id
                Log.d(TAG, "Updating existing match: $matchId")

                matchesCollection.document(matchId)
                    .update(
                        mapOf(
                            "status" to "active",
                            "lastInteraction" to System.currentTimeMillis()
                        )
                    )
                    .await()

                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "Updated existing match in $duration ms")
                return matchId
            } else {
                // Create new match
                Log.d(TAG, "Creating new match")
                val match = Match(
                    users = users,
                    matchedAt = System.currentTimeMillis(),
                    proximityEventId = proximityEventId,
                    status = "active",
                    instagramShared = mapOf(
                        userId1 to false,
                        userId2 to false
                    ),
                    lastInteraction = System.currentTimeMillis()
                )

                val matchRef = matchesCollection.add(match).await()
                Log.d(TAG, "Created new match with ID: ${matchRef.id}")

                // Send match notifications
                Log.d(TAG, "Sending match notifications")
                sendMatchNotifications(matchRef.id, userId1, userId2)

                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "Created new match in $duration ms")
                return matchRef.id
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating or updating match between users: $userId1 and $userId2", e)
            throw e
        }
    }

    /**
     * Share Instagram with match
     */
    suspend fun shareInstagram(matchId: String, userId: String): Boolean {
        Log.d(TAG, "Sharing Instagram for user: $userId in match: $matchId")

        return try {
            val startTime = System.currentTimeMillis()

            val match = getMatch(matchId)
            if (match == null) {
                Log.w(TAG, "Match not found: $matchId, can't share Instagram")
                return false
            }

            // Update Instagram sharing status
            Log.d(TAG, "Updating Instagram sharing status")
            val updates = mutableMapOf<String, Any>()
            updates["instagramShared.$userId"] = true

            matchesCollection.document(matchId)
                .update(updates)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Shared Instagram for user: $userId in match: $matchId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing Instagram for user: $userId in match: $matchId", e)
            false
        }
    }

    /**
     * Delete a match
     */
    suspend fun deleteMatch(matchId: String): Boolean {
        Log.d(TAG, "Deleting match: $matchId")

        return try {
            val startTime = System.currentTimeMillis()

            // Update match status to deleted
            matchesCollection.document(matchId)
                .update("status", "deleted")
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Deleted match: $matchId in $duration ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting match: $matchId", e)
            false
        }
    }

    /**
     * Send match notifications to both users
     */
    private suspend fun sendMatchNotifications(matchId: String, userId1: String, userId2: String) {
        Log.d(TAG, "Sending match notifications for match: $matchId to users: $userId1 and $userId2")

        try {
            val startTime = System.currentTimeMillis()

            // Get user profiles
            Log.d(TAG, "Fetching user profiles for notifications")
            val user1Profile = userRepository.getUserProfile(userId1)
            val user2Profile = userRepository.getUserProfile(userId2)

            if (user1Profile == null) {
                Log.w(TAG, "User profile not found for userId: $userId1, can't send notification")
            }

            if (user2Profile == null) {
                Log.w(TAG, "User profile not found for userId: $userId2, can't send notification")
            }

            // Send notification to first user
            user2Profile?.let { profile ->
                Log.d(TAG, "Sending match notification to user: $userId1 about user: $userId2")
                notificationUtil.sendMatchNotification(
                    userId1,
                    profile.displayName,
                    matchId
                )
            }

            // Send notification to second user
            user1Profile?.let { profile ->
                Log.d(TAG, "Sending match notification to user: $userId2 about user: $userId1")
                notificationUtil.sendMatchNotification(
                    userId2,
                    profile.displayName,
                    matchId
                )
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Sent match notifications in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending match notifications for match: $matchId", e)
        }
    }

    suspend fun updateLastMessage(matchId: String, text: String, senderId: String) {
        Log.d(TAG, "Updating last message for match: $matchId from user: $senderId")

        try {
            val startTime = System.currentTimeMillis()

            matchesCollection.document(matchId)
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

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Updated last message for match: $matchId in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last message for match: $matchId", e)
        }
    }
}