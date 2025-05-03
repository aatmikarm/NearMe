package com.aatmik.nearme.repository

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
    private val matchesCollection = firestore.collection("matches")

    /**
     * Get all matches for a user
     */
    suspend fun getMatches(userId: String): List<Match> {
        val snapshot = matchesCollection
            .whereArrayContains("users", userId)
            .whereEqualTo("status", "active")
            .orderBy("matchedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(Match::class.java)?.copy(id = document.id)
        }
    }

    /**
     * Get new matches for a user (past 24 hours)
     */
    suspend fun getNewMatches(userId: String): List<Match> {
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        val snapshot = matchesCollection
            .whereArrayContains("users", userId)
            .whereEqualTo("status", "active")
            .whereGreaterThan("matchedAt", oneDayAgo)
            .orderBy("matchedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(Match::class.java)?.copy(id = document.id)
        }
    }

    /**
     * Get match by ID
     */
    suspend fun getMatch(matchId: String): Match? {
        val document = matchesCollection.document(matchId).get().await()
        return document.toObject(Match::class.java)?.copy(id = document.id)
    }

    /**
     * Create or update a match between two users
     */
    suspend fun createOrUpdateMatch(userId1: String, userId2: String, proximityEventId: String): String {
        val users = listOf(userId1, userId2).sorted()

        // Check if match already exists
        val existingMatches = matchesCollection
            .whereEqualTo("users", users)
            .get()
            .await()

        if (existingMatches.documents.isNotEmpty()) {
            // Update existing match
            val matchId = existingMatches.documents.first().id
            matchesCollection.document(matchId)
                .update(
                    mapOf(
                        "status" to "active",
                        "lastInteraction" to System.currentTimeMillis()
                    )
                )
                .await()

            return matchId
        } else {
            // Create new match
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

            // Send match notifications
            sendMatchNotifications(matchRef.id, userId1, userId2)

            return matchRef.id
        }
    }

    /**
     * Share Instagram with match
     */
    suspend fun shareInstagram(matchId: String, userId: String): Boolean {
        return try {
            val match = getMatch(matchId) ?: return false

            // Update Instagram sharing status
            val updates = mutableMapOf<String, Any>()
            updates["instagramShared.$userId"] = true

            matchesCollection.document(matchId)
                .update(updates)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete a match
     */
    suspend fun deleteMatch(matchId: String): Boolean {
        return try {
            // Update match status to deleted
            matchesCollection.document(matchId)
                .update("status", "deleted")
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Send match notifications to both users
     */
    private suspend fun sendMatchNotifications(matchId: String, userId1: String, userId2: String) {
        // Get user profiles
        val user1Profile = userRepository.getUserProfile(userId1)
        val user2Profile = userRepository.getUserProfile(userId2)

        // Send notification to first user
        user2Profile?.let { profile ->
            notificationUtil.sendMatchNotification(
                userId1,
                profile.displayName,
                matchId
            )
        }

        // Send notification to second user
        user1Profile?.let { profile ->
            notificationUtil.sendMatchNotification(
                userId2,
                profile.displayName,
                matchId
            )
        }
    }

    suspend fun updateLastMessage(matchId: String, text: String, senderId: String) {
        try {
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
        } catch (e: Exception) {
            // Handle error
        }
    }
}
