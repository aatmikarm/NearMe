package com.aatmik.nearme.repository

import com.aatmik.nearme.model.ProximityEvent
import com.aatmik.nearme.model.ProximityLocation
import com.aatmik.nearme.model.UserLocation
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.GeoHashUtil
import com.aatmik.nearme.util.NotificationUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationUtil: NotificationUtil,
    private val userRepository: UserRepository
) {

    private val locationsCollection = firestore.collection("locations")
    private val proximityEventsCollection = firestore.collection("proximityEvents")

    /**
     * Update a user's location in Firestore
     */
    suspend fun updateUserLocation(userLocation: UserLocation) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        // Get user display name and photo URL for efficiency (denormalization)
        val userProfile = userRepository.getUserProfile(currentUser.uid)

        // Create location document data
        val locationData = hashMapOf(
            "uid" to userLocation.userId,
            "displayName" to userProfile?.displayName ?: "",
            "primaryPhotoUrl" to userProfile?.photos?.firstOrNull { it.isPrimary }?.url ?: "",
            "currentLocation" to hashMapOf(
                "geohash" to userLocation.geohash,
                "geopoint" to com.google.firebase.firestore.GeoPoint(
                    userLocation.latitude,
                    userLocation.longitude
                ),
                "accuracy" to userLocation.accuracy
            ),
            "lastUpdated" to userLocation.timestamp,
            "isVisible" to userLocation.isVisible,
            "appState" to userLocation.appState
        )

        // Update user location in Firestore
        locationsCollection.document(userLocation.userId)
            .set(locationData)
            .await()
    }

    /**
     * Find users within a specified radius
     */
    suspend fun findNearbyUsers(userLocation: UserLocation, radiusInMeters: Double = 100.0): List<UserLocation> {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return emptyList()
        val nearbyUsers = mutableListOf<UserLocation>()

        // Get geohash query bounds for the radius
        val bounds = GeoHashUtil.getGeohashQueryBounds(
            userLocation.latitude,
            userLocation.longitude,
            radiusInMeters
        )

        // Query Firestore for each bound
        for ((startHash, endHash) in bounds) {
            val query = locationsCollection
                .orderBy("currentLocation.geohash")
                .startAt(startHash)
                .endAt(endHash)
                .whereEqualTo("isVisible", true)

            val snapshot = query.get().await()

            // Process results and filter by actual distance
            for (document in snapshot.documents) {
                val uid = document.getString("uid") ?: continue

                // Skip current user
                if (uid == currentUser.uid) continue

                val geoPoint = document.get("currentLocation.geopoint") as? com.google.firebase.firestore.GeoPoint
                    ?: continue

                val distance = GeoHashUtil.distanceBetween(
                    userLocation.latitude,
                    userLocation.longitude,
                    geoPoint.latitude,
                    geoPoint.longitude
                )

                // If within radius, add to nearby users
                if (distance <= radiusInMeters) {
                    val nearbyUserLocation = UserLocation(
                        userId = uid,
                        latitude = geoPoint.latitude,
                        longitude = geoPoint.longitude,
                        accuracy = (document.get("currentLocation.accuracy") as? Number)?.toFloat() ?: 0f,
                        geohash = document.getString("currentLocation.geohash") ?: "",
                        timestamp = (document.get("lastUpdated") as? Number)?.toLong() ?: 0L,
                        isVisible = document.getBoolean("isVisible") ?: false,
                        appState = document.getString("appState") ?: "inactive"
                    )

                    nearbyUsers.add(nearbyUserLocation)
                }
            }
        }

        return nearbyUsers
    }

    /**
     * Process proximity events for nearby users
     */
    suspend fun processProximityEvents(
        userLocation: UserLocation,
        nearbyUsers: List<UserLocation>
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        for (nearbyUser in nearbyUsers) {
            val userIds = listOf(currentUser.uid, nearbyUser.userId).sorted()

            // Check if there's an active proximity event for these users
            val existingEvents = proximityEventsCollection
                .whereEqualTo("users", userIds)
                .whereEqualTo("status", "active")
                .get()
                .await()

            if (existingEvents.isEmpty) {
                // Create new proximity event
                createProximityEvent(userLocation, nearbyUser)
            } else {
                // Update existing proximity event
                val event = existingEvents.documents.first()
                updateProximityEvent(event.id, userLocation, nearbyUser)
            }
        }
    }

    /**
     * Create a new proximity event
     */
    private suspend fun createProximityEvent(userLocation: UserLocation, nearbyUser: UserLocation) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        // Calculate distance
        val distance = GeoHashUtil.distanceBetween(
            userLocation.latitude,
            userLocation.longitude,
            nearbyUser.latitude,
            nearbyUser.longitude
        )

        // Create reduced precision geohash for general area (not exact location)
        val areaGeohash = GeoHashUtil.encode(
            userLocation.latitude,
            userLocation.longitude,
            precision = 6 // Lower precision for privacy
        )

        // Create proximity event
        val proximityEvent = ProximityEvent(
            users = listOf(currentUser.uid, nearbyUser.userId).sorted(),
            distance = distance,
            startTime = System.currentTimeMillis(),
            location = ProximityLocation(geohash = areaGeohash),
            status = "active",
            notificationSent = false
        )

        // Add to Firestore
        val eventRef = proximityEventsCollection.add(proximityEvent).await()

        // Send notification to both users
        sendProximityNotification(eventRef.id, currentUser.uid, nearbyUser.userId, distance)
    }

    /**
     * Update an existing proximity event
     */
    private suspend fun updateProximityEvent(
        eventId: String,
        userLocation: UserLocation,
        nearbyUser: UserLocation
    ) {
        // Calculate new distance
        val distance = GeoHashUtil.distanceBetween(
            userLocation.latitude,
            userLocation.longitude,
            nearbyUser.latitude,
            nearbyUser.longitude
        )

        // Update proximity event with new distance
        proximityEventsCollection.document(eventId)
            .update(
                "distance", distance,
                "lastUpdated", System.currentTimeMillis()
            )
            .await()
    }

    /**
     * Send notification about proximity to both users
     */
    private suspend fun sendProximityNotification(
        eventId: String,
        userId1: String,
        userId2: String,
        distance: Double
    ) {
        // Mark notification as sent
        proximityEventsCollection.document(eventId)
            .update("notificationSent", true)
            .await()

        // Get user profiles
        val user1Profile = userRepository.getUserProfile(userId1)
        val user2Profile = userRepository.getUserProfile(userId2)

        // Send notification to first user
        user2Profile?.let { profile ->
            notificationUtil.sendProximityNotification(
                userId1,
                profile.displayName,
                distance.toInt(),
                eventId
            )
        }

        // Send notification to second user
        user1Profile?.let { profile ->
            notificationUtil.sendProximityNotification(
                userId2,
                profile.displayName,
                distance.toInt(),
                eventId
            )
        }
    }

    /**
     * Get proximity event by ID
     */
    suspend fun getProximityEvent(eventId: String): ProximityEvent? {
        val document = proximityEventsCollection.document(eventId).get().await()
        return document.toObject(ProximityEvent::class.java)?.copy(id = document.id)
    }

    /**
     * Get active proximity events for a user
     */
    suspend fun getActiveProximityEvents(userId: String): List<ProximityEvent> {
        val events = proximityEventsCollection
            .whereArrayContains("users", userId)
            .whereEqualTo("status", "active")
            .orderBy("startTime", Query.Direction.DESCENDING)
            .get()
            .await()

        return events.documents.mapNotNull { document ->
            document.toObject(ProximityEvent::class.java)?.copy(id = document.id)
        }
    }

    /**
     * Update proximity event status (e.g., to "matched" or "ignored")
     */
    suspend fun updateProximityEventStatus(eventId: String, status: String) {
        proximityEventsCollection.document(eventId)
            .update("status", status)
            .await()
    }

    /**
     * Mark proximity event as viewed by a user
     */
    suspend fun markProximityEventAsViewed(eventId: String, userId: String) {
        val event = getProximityEvent(eventId) ?: return

        // If not already viewed by this user
        if (!event.viewedBy.contains(userId)) {
            proximityEventsCollection.document(eventId)
                .update("viewedBy", event.viewedBy + userId)
                .await()
        }
    }
}