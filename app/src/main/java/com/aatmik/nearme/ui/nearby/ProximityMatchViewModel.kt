package com.aatmik.nearme.ui.nearby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.ProximityEvent
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.LocationRepository
import com.aatmik.nearme.repository.MatchRepository
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProximityMatchViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _proximityEvent = MutableLiveData<ProximityEvent?>()
    val proximityEvent: LiveData<ProximityEvent?> = _proximityEvent

    private val _otherUserProfile = MutableLiveData<UserProfile?>()
    val otherUserProfile: LiveData<UserProfile?> = _otherUserProfile

    private val _matchResult = MutableLiveData<Result<String>?>()
    val matchResult: LiveData<Result<String>?> = _matchResult

    /**
     * Load proximity event details
     */
    fun loadProximityEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Get proximity event
                val event = locationRepository.getProximityEvent(eventId)
                _proximityEvent.value = event

                // Mark event as viewed
                event?.let {
                    val currentUserId = userRepository.getCurrentUserId() ?: return@let
                    locationRepository.markProximityEventAsViewed(eventId, currentUserId)

                    // Get other user's profile
                    val otherUserId = event.users.firstOrNull { it != currentUserId } ?: return@let
                    val otherUser = userRepository.getUserProfile(otherUserId)
                    _otherUserProfile.value = otherUser
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Connect with the other user
     */
    fun connectWithUser() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val event = proximityEvent.value ?: throw Exception("Proximity event not found")
                val eventId = event.id

                // Update event status
                locationRepository.updateProximityEventStatus(eventId, "matched")

                // Create match (or update existing match with connect)
                val currentUserId = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")
                val otherUserId = event.users.firstOrNull { it != currentUserId } ?: throw Exception("Other user not found")

                val matchId = matchRepository.createOrUpdateMatch(currentUserId, otherUserId, eventId)
                _matchResult.value = Result.success(matchId)

                // Increment match statistics
                userRepository.incrementStatistic(currentUserId, "matches")

            } catch (e: Exception) {
                _matchResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Skip this proximity match
     */
    fun skipMatch() {
        viewModelScope.launch {
            try {
                val event = proximityEvent.value ?: return@launch
                locationRepository.updateProximityEventStatus(event.id, "ignored")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
