package com.aatmik.nearme.ui.nearby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.ProximityEvent
import com.aatmik.nearme.model.UserLocation
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.LocationRepository
import com.aatmik.nearme.repository.UserRepository
import com.aatmik.nearme.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _nearbyUsers = MutableLiveData<List<NearbyUserModel>>()
    val nearbyUsers: LiveData<List<NearbyUserModel>> = _nearbyUsers

    private val _activeProximityEvents = MutableLiveData<List<ProximityEvent>>()
    val activeProximityEvents: LiveData<List<ProximityEvent>> = _activeProximityEvents

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadNearbyUsers()
        loadActiveProximityEvents()
        startPeriodicUpdates()
    }

    /**
     * Load nearby users
     */
    fun loadNearbyUsers() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val currentUserId = userRepository.getCurrentUserId() ?: return@launch
                val currentLocation = getUserLocation(currentUserId)

                // If we don't have a location yet, return empty list
                if (currentLocation == null) {
                    _nearbyUsers.value = emptyList()
                    return@launch
                }

                // Get proximity range from preferences
                val proximityRange = preferenceManager.getProximityRange().toDouble()

                // Find nearby users
                val nearby = locationRepository.findNearbyUsers(currentLocation, proximityRange)

                // Convert to UI model
                val nearbyUsersModel = mutableListOf<NearbyUserModel>()

                for (nearbyUser in nearby) {
                    val userProfile = userRepository.getUserProfile(nearbyUser.userId)
                    if (userProfile != null) {
                        // Check if user matches preferences
                        if (matchesPreferences(userProfile)) {
                            val model = NearbyUserModel(
                                userId = nearbyUser.userId,
                                name = userProfile.displayName,
                                age = userProfile.age,
                                gender = userProfile.gender,
                                bio = userProfile.bio,
                                photoUrl = userProfile.photos.firstOrNull { it.isPrimary }?.url ?: "",
                                distance = calculateDistance(currentLocation, nearbyUser),
                                lastActive = nearbyUser.timestamp,
                                hasInstagram = userProfile.instagramConnected
                            )

                            nearbyUsersModel.add(model)
                        }
                    }
                }

                // Sort by distance
                nearbyUsersModel.sortBy { it.distance }

                _nearbyUsers.value = nearbyUsersModel

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load active proximity events
     */
    fun loadActiveProximityEvents() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId() ?: return@launch

                val events = locationRepository.getActiveProximityEvents(currentUserId)
                _activeProximityEvents.value = events

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Start periodic updates
     */
    private fun startPeriodicUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(UPDATE_INTERVAL_MS)
                loadNearbyUsers()
                loadActiveProximityEvents()
            }
        }
    }

    /**
     * Get user location
     */
    private suspend fun getUserLocation(userId: String): UserLocation? {
        return try {
            val locationsCollection = locationRepository.firestore.collection("locations")
            val document = locationsCollection.document(userId).get().await()

            if (document.exists()) {
                val locationData = document.data ?: return null

                val currentLocation = locationData["currentLocation"] as? Map<String, Any> ?: return null
                val geoPoint = currentLocation["geopoint"] as? com.google.firebase.firestore.GeoPoint ?: return null
                val geohash = currentLocation["geohash"] as? String ?: ""
                val accuracy = (currentLocation["accuracy"] as? Number)?.toFloat() ?: 0f

                UserLocation(
                    userId = userId,
                    latitude = geoPoint.latitude,
                    longitude = geoPoint.longitude,
                    accuracy = accuracy,
                    geohash = geohash,
                    timestamp = (locationData["lastUpdated"] as? Number)?.toLong() ?: 0L,
                    isVisible = locationData["isVisible"] as? Boolean ?: true,
                    appState = locationData["appState"] as? String ?: "foreground"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate distance between current user and nearby user
     */
    private fun calculateDistance(currentLocation: UserLocation, nearbyUser: UserLocation): Double {
        return com.firebase.geofire.GeoFireUtils.getDistanceBetween(
            com.firebase.geofire.GeoLocation(currentLocation.latitude, currentLocation.longitude),
            com.firebase.geofire.GeoLocation(nearbyUser.latitude, nearbyUser.longitude)
        )
    }

    /**
     * Check if user matches preferences
     */
    private fun matchesPreferences(userProfile: UserProfile): Boolean {
        // Get gender preference
        val genderPreference = preferenceManager.getGenderPreference()
        if (!genderPreference.contains(userProfile.gender)) {
            return false
        }

        // Get age preference
        val (minAge, maxAge) = preferenceManager.getAgeRangePreference()
        if (userProfile.age < minAge || userProfile.age > maxAge) {
            return false
        }

        // Check if user is in blocklist
        val currentUserId = userRepository.getCurrentUserId() ?: return false
        val currentUser = userRepository.getUserProfile(currentUserId) ?: return false
        if (currentUser.blockedUsers.contains(userProfile.uid)) {
            return false
        }

        return true
    }

    /**
     * Handle connect action
     */
    fun connectWithUser(userId: String) {
        viewModelScope.launch {
            // Find active proximity event with this user
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            val userIds = listOf(currentUserId, userId).sorted()

            val events = _activeProximityEvents.value ?: emptyList()
            val event = events.firstOrNull { it.users.containsAll(userIds) }

            if (event != null) {
                // Open proximity match screen
                // In a real implementation, you'd use LiveData or a callback to notify the UI
            } else {
                _error.value = "No active proximity event found"
            }
        }
    }

    /**
     * Handle skip action
     */
    fun skipUser(userId: String) {
        viewModelScope.launch {
            // Find active proximity event with this user
            val currentUserId = userRepository.getCurrentUserId() ?: return@launch
            val userIds = listOf(currentUserId, userId).sorted()

            val events = _activeProximityEvents.value ?: emptyList()
            val event = events.firstOrNull { it.users.containsAll(userIds) }

            if (event != null) {
                // Update event status to ignored
                locationRepository.updateProximityEventStatus(event.id, "ignored")

                // Refresh lists
                loadActiveProximityEvents()
                loadNearbyUsers()
            }
        }
    }

    companion object {
        private const val UPDATE_INTERVAL_MS = 60000L // 1 minute
    }
}
