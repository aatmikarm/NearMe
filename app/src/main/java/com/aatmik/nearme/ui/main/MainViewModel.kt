package com.aatmik.nearme.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.ProximityEvent
import com.aatmik.nearme.model.UserLocation
import com.aatmik.nearme.repository.LocationRepository
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    // Expose location permission state to fragments
    private val _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean> = _locationPermissionGranted

    // Expose current user location to fragments
    private val _currentUserLocation = MutableLiveData<UserLocation?>()
    val currentUserLocation: LiveData<UserLocation?> = _currentUserLocation

    // Track active proximity events that need attention
    private val _activeProximityEvents = MutableLiveData<List<ProximityEvent>>()
    val activeProximityEvents: LiveData<List<ProximityEvent>> = _activeProximityEvents

    // Notify fragments when a specific proximity event is selected
    private val _selectedProximityEvent = MutableLiveData<ProximityEvent?>()
    val selectedProximityEvent: LiveData<ProximityEvent?> = _selectedProximityEvent

    init {
        startLocationUpdates()
        monitorActiveProximityEvents()
    }

    /**
     * Called by MainActivity when location permissions change
     */
    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
        if (granted) {
            startLocationUpdates()
        }
    }

    /**
     * Start periodic location updates
     */
    private fun startLocationUpdates() {
        viewModelScope.launch {
            // Only start if we have permission
            if (_locationPermissionGranted.value != true) return@launch

            try {
                val currentUserId = userRepository.getCurrentUserId() ?: return@launch
                updateCurrentLocation(currentUserId)

                // Start periodic updates
                while (_locationPermissionGranted.value == true) {
                    delay(UPDATE_INTERVAL_MS)
                    updateCurrentLocation(currentUserId)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Update the user's current location
     */
    private suspend fun updateCurrentLocation(userId: String) {
        try {
            val locationsCollection = locationRepository.firestore.collection("locations")
            val document = locationsCollection.document(userId).get().await()

            if (document.exists()) {
                // Parse location data from Firestore
                val locationData = document.data ?: return
                val currentLocation = locationData["currentLocation"] as? Map<String, Any> ?: return
                val geoPoint = currentLocation["geopoint"] as? com.google.firebase.firestore.GeoPoint ?: return

                // Create UserLocation object
                val userLocation = UserLocation(
                    userId = userId,
                    latitude = geoPoint.latitude,
                    longitude = geoPoint.longitude,
                    accuracy = (currentLocation["accuracy"] as? Number)?.toFloat() ?: 0f,
                    geohash = currentLocation["geohash"] as? String ?: "",
                    timestamp = (locationData["lastUpdated"] as? Number)?.toLong() ?: 0L,
                    isVisible = locationData["isVisible"] as? Boolean ?: true,
                    appState = locationData["appState"] as? String ?: "foreground"
                )

                // Update LiveData with the new location
                _currentUserLocation.postValue(userLocation)

                // Update user's active timestamp
                userRepository.updateLastActive(userId)
            }
        } catch (e: Exception) {
            // Handle error - post null to indicate location unavailable
            _currentUserLocation.postValue(null)
        }
    }

    /**
     * Monitor active proximity events that need attention
     */
    private fun monitorActiveProximityEvents() {
        viewModelScope.launch {
            while (true) {
                try {
                    val currentUserId = userRepository.getCurrentUserId() ?: continue
                    val events = locationRepository.getActiveProximityEvents(currentUserId)

                    // Filter for events that need attention (unviewed)
                    val unviewedEvents = events.filter {
                        !it.viewedBy.contains(currentUserId)
                    }

                    _activeProximityEvents.postValue(unviewedEvents)
                } catch (e: Exception) {
                    // Handle error
                }

                delay(PROXIMITY_CHECK_INTERVAL_MS)
            }
        }
    }

    /**
     * Called when user selects a proximity event to view
     */
    fun selectProximityEvent(event: ProximityEvent) {
        _selectedProximityEvent.value = event

        // Mark as viewed after selection
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId() ?: return@launch
                locationRepository.markProximityEventAsViewed(event.id, currentUserId)

                // Refresh the list after marking as viewed
                val events = locationRepository.getActiveProximityEvents(currentUserId)
                _activeProximityEvents.postValue(events)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Clear selected proximity event
     */
    fun clearSelectedProximityEvent() {
        _selectedProximityEvent.value = null
    }

    companion object {
        private const val UPDATE_INTERVAL_MS = 30000L // 30 seconds
        private const val PROXIMITY_CHECK_INTERVAL_MS = 15000L // 15 seconds
    }
}