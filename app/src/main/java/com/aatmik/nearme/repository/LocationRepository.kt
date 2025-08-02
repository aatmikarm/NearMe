package com.aatmik.nearme.repository

import android.util.Log
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
    val firestore: FirebaseFirestore,
    private val notificationUtil: NotificationUtil,
    private val userRepository: UserRepository
) {
    private val TAG = "NearMe_LocationRepo"

    private val locationsCollection = firestore.collection("locations")
    private val proximityEventsCollection = firestore.collection("proximityEvents")

    /**
     * Update a user's location in Firestore
     */
    suspend fun updateUserLocation(userLocation: UserLocation) {
        Log.d(TAG, "Updating location for user: ${userLocation.userId}, lat: ${userLocation.latitude}, lon: ${userLocation.longitude}")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.w(TAG, "Cannot update location - user is not authenticated")
            return
        }

        try {
            val startTime = System.currentTimeMillis()

            // Get user display name and photo URL for efficiency (denormalization)
            Log.d(TAG, "Fetching user profile for efficient denormalization")
            val userProfile = userRepository.getUserProfile(currentUser.uid)

            // Create location document data
            val locationData = hashMapOf<String, Any>(
                "uid" to userLocation.userId,
                "displayName" to (userProfile?.displayName ?: ""),
                "primaryPhotoUrl" to (userProfile?.photos?.firstOrNull { it.isPrimary }?.url ?: ""),
                "currentLocation" to hashMapOf<String, Any>(
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
            Log.d(TAG, "Saving location to Firestore")
            locationsCollection.document(userLocation.userId)
                .set(locationData)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Location updated successfully for user: ${userLocation.userId} in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location for user: ${userLocation.userId}", e)
        }
    }

    suspend fun getUserLocationById(userId: String): UserLocation? {
        Log.d(TAG, "Getting location for user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            val document = locationsCollection.document(userId).get().await()

            // Parse and return the location data
            if (document.exists()) {
                Log.d(TAG, "Location document found for user: $userId")
                // Parse the document into a UserLocation
                // ...

                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "Location retrieved for user: $userId in $duration ms")
                // Return the parsed location
            } else {
                Log.w(TAG, "Location document not found for user: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching location for user: $userId", e)
        }
        return null
    }

    /**
     * Find users within a specified radius
     */
    suspend fun findNearbyUsers(userLocation: UserLocation, radiusInMeters: Double = 100.0): List<UserLocation> {
        Log.d(TAG, "Finding nearby users within radius: $radiusInMeters meters")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.w(TAG, "Cannot find nearby users - user is not authenticated")
            return emptyList()
        }

        val nearbyUsers = mutableListOf<UserLocation>()

        try {
            val startTime = System.currentTimeMillis()

            // Get geohash query bounds for the radius
            Log.d(TAG, "Calculating geohash bounds for search")
            val bounds = GeoHashUtil.getGeohashQueryBounds(
                userLocation.latitude,
                userLocation.longitude,
                radiusInMeters
            )

            Log.d(TAG, "Found ${bounds.size} geohash bounds to search")

            // Query Firestore for each bound
            for ((startHash, endHash) in bounds) {
                Log.d(TAG, "Querying bound: $startHash to $endHash")

                val query = locationsCollection
                    .orderBy("currentLocation.geohash")
                    .startAt(startHash)
                    .endAt(endHash)
                    .whereEqualTo("isVisible", true)

                val snapshot = query.get().await()
                Log.d(TAG, "Found ${snapshot.size()} potential users in bound")

                // Process results and filter by actual distance
                for (document in snapshot.documents) {
                    val uid = document.getString("uid")
                    if (uid == null) {
                        Log.w(TAG, "Skipping document with missing user ID")
                        continue
                    }

                    // Skip current user
                    if (uid == currentUser.uid) {
                        Log.d(TAG, "Skipping current user")
                        continue
                    }

                    val geoPoint = document.get("currentLocation.geopoint") as? com.google.firebase.firestore.GeoPoint
                    if (geoPoint == null) {
                        Log.w(TAG, "Skipping document with missing geopoint for user: $uid")
                        continue
                    }

                    val distance = GeoHashUtil.distanceBetween(
                        userLocation.latitude,
                        userLocation.longitude,
                        geoPoint.latitude,
                        geoPoint.longitude
                    )

                    // If within radius, add to nearby users
                    if (distance <= radiusInMeters) {
                        Log.d(TAG, "User $uid is within radius at distance: $distance meters")

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
                    } else {
                        Log.d(TAG, "User $uid is outside radius at distance: $distance meters")
                    }
                }
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Found ${nearbyUsers.size} nearby users in $duration ms")

            return nearbyUsers
        } catch (e: Exception) {
            Log.e(TAG, "Error finding nearby users", e)
            return emptyList()
        }
    }

    /**
     * Process proximity events for nearby users
     */
    suspend fun processProximityEvents(
        userLocation: UserLocation,
        nearbyUsers: List<UserLocation>
    ) {
        Log.d(TAG, "Processing proximity events for ${nearbyUsers.size} nearby users")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.w(TAG, "Cannot process proximity events - user is not authenticated")
            return
        }

        try {
            val startTime = System.currentTimeMillis()

            var newEvents = 0
            var updatedEvents = 0

            for (nearbyUser in nearbyUsers) {
                Log.d(TAG, "Processing proximity with user: ${nearbyUser.userId}")

                val userIds = listOf(currentUser.uid, nearbyUser.userId).sorted()

                // Check if there's an active proximity event for these users
                Log.d(TAG, "Checking for existing proximity events")
                val existingEvents = proximityEventsCollection
                    .whereEqualTo("users", userIds)
                    .whereEqualTo("status", "active")
                    .get()
                    .await()

                if (existingEvents.isEmpty) {
                    Log.d(TAG, "Creating new proximity event with user: ${nearbyUser.userId}")
                    // Create new proximity event
                    createProximityEvent(userLocation, nearbyUser)
                    newEvents++
                } else {
                    Log.d(TAG, "Updating existing proximity event with user: ${nearbyUser.userId}")
                    // Update existing proximity event
                    val event = existingEvents.documents.first()
                    updateProximityEvent(event.id, userLocation, nearbyUser)
                    updatedEvents++
                }
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Processed proximity events: $newEvents new, $updatedEvents updated in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing proximity events", e)
        }
    }

    /**
     * Create a new proximity event
     */
    private suspend fun createProximityEvent(userLocation: UserLocation, nearbyUser: UserLocation) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.w(TAG, "Cannot create proximity event - user is not authenticated")
            return
        }

        try {
            val startTime = System.currentTimeMillis()

            // Calculate distance
            val distance = GeoHashUtil.distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                nearbyUser.latitude,
                nearbyUser.longitude
            )
            Log.d(TAG, "Distance between users: $distance meters")

            // Create reduced precision geohash for general area (not exact location)
            val areaGeohash = GeoHashUtil.encode(
                userLocation.latitude,
                userLocation.longitude,
                precision = 6 // Lower precision for privacy
            )
            Log.d(TAG, "Created area geohash with reduced precision: $areaGeohash")

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
            Log.d(TAG, "Saving proximity event to Firestore")
            val eventRef = proximityEventsCollection.add(proximityEvent).await()
            Log.d(TAG, "Created proximity event with ID: ${eventRef.id}")

            // Send notification to both users
            Log.d(TAG, "Sending proximity notifications to both users")
            sendProximityNotification(eventRef.id, currentUser.uid, nearbyUser.userId, distance)

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Created proximity event in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating proximity event", e)
        }
    }

    /**
     * Update an existing proximity event
     */
    private suspend fun updateProximityEvent(
        eventId: String,
        userLocation: UserLocation,
        nearbyUser: UserLocation
    ) {
        Log.d(TAG, "Updating proximity event: $eventId")

        try {
            val startTime = System.currentTimeMillis()

            // Calculate new distance
            val distance = GeoHashUtil.distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                nearbyUser.latitude,
                nearbyUser.longitude
            )
            Log.d(TAG, "New distance between users: $distance meters")

            // Update proximity event with new distance
            Log.d(TAG, "Updating Firestore document")
            proximityEventsCollection.document(eventId)
                .update(
                    "distance", distance,
                    "lastUpdated", System.currentTimeMillis()
                )
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Updated proximity event in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating proximity event: $eventId", e)
        }
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
        Log.d(TAG, "Sending proximity notifications for event: $eventId")

        try {
            val startTime = System.currentTimeMillis()

            // Mark notification as sent
            Log.d(TAG, "Marking notification as sent")
            proximityEventsCollection.document(eventId)
                .update("notificationSent", true)
                .await()

            // Get user profiles
            Log.d(TAG, "Fetching user profiles for notification content")
            val user1Profile = userRepository.getUserProfile(userId1)
            val user2Profile = userRepository.getUserProfile(userId2)

            // Send notification to first user
            user2Profile?.let { profile ->
                Log.d(TAG, "Sending notification to user: $userId1 about user: $userId2")
                notificationUtil.sendProximityNotification(
                    userId1,
                    profile.displayName,
                    distance.toInt(),
                    eventId
                )
            }

            // Send notification to second user
            user1Profile?.let { profile ->
                Log.d(TAG, "Sending notification to user: $userId2 about user: $userId1")
                notificationUtil.sendProximityNotification(
                    userId2,
                    profile.displayName,
                    distance.toInt(),
                    eventId
                )
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Sent proximity notifications in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending proximity notifications for event: $eventId", e)
        }
    }

    /**
     * Get proximity event by ID
     */
    suspend fun getProximityEvent(eventId: String): ProximityEvent? {
        Log.d(TAG, "Getting proximity event by ID: $eventId")

        try {
            val startTime = System.currentTimeMillis()

            val document = proximityEventsCollection.document(eventId).get().await()
            val event = document.toObject(ProximityEvent::class.java)?.copy(id = document.id)

            val duration = System.currentTimeMillis() - startTime
            if (event != null) {
                Log.d(TAG, "Retrieved proximity event: $eventId in $duration ms")
            } else {
                Log.w(TAG, "Proximity event not found: $eventId")
            }

            return event
        } catch (e: Exception) {
            Log.e(TAG, "Error getting proximity event: $eventId", e)
            return null
        }
    }

    /**
     * Get active proximity events for a user
     */
    suspend fun getActiveProximityEvents(userId: String): List<ProximityEvent> {
        Log.d(TAG, "Getting active proximity events for user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            val events = proximityEventsCollection
                .whereArrayContains("users", userId)
                .whereEqualTo("status", "active")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val proximityEvents = events.documents.mapNotNull { document ->
                document.toObject(ProximityEvent::class.java)?.copy(id = document.id)
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Retrieved ${proximityEvents.size} active proximity events in $duration ms")

            return proximityEvents
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active proximity events for user: $userId", e)
            return emptyList()
        }
    }

    /**
     * Update proximity event status (e.g., to "matched" or "ignored")
     */
    suspend fun updateProximityEventStatus(eventId: String, status: String) {
        Log.d(TAG, "Updating proximity event status: $eventId to $status")

        try {
            val startTime = System.currentTimeMillis()

            proximityEventsCollection.document(eventId)
                .update("status", status)
                .await()

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Updated proximity event status to $status in $duration ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating proximity event status: $eventId", e)
        }
    }

    /**
     * Mark proximity event as viewed by a user
     */
    suspend fun markProximityEventAsViewed(eventId: String, userId: String) {
        Log.d(TAG, "Marking proximity event as viewed: $eventId by user: $userId")

        try {
            val startTime = System.currentTimeMillis()

            val event = getProximityEvent(eventId)
            if (event == null) {
                Log.w(TAG, "Cannot mark as viewed - proximity event not found: $eventId")
                return
            }

            // If not already viewed by this user
            if (!event.viewedBy.contains(userId)) {
                Log.d(TAG, "Adding user to viewedBy list")
                proximityEventsCollection.document(eventId)
                    .update("viewedBy", event.viewedBy + userId)
                    .await()

                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "Marked proximity event as viewed in $duration ms")
            } else {
                Log.d(TAG, "Event already viewed by user, no update needed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking proximity event as viewed: $eventId", e)
        }
    }
}