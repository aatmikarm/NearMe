package com.aatmik.nearme.ui.matches

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.MatchRepository
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchConfirmationViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _matchUsers = MutableLiveData<Pair<UserProfile, UserProfile>?>()
    val matchUsers: LiveData<Pair<UserProfile, UserProfile>?> = _matchUsers

    private val _instagramShareResult = MutableLiveData<Result<Boolean>?>()
    val instagramShareResult: LiveData<Result<Boolean>?> = _instagramShareResult

    private var matchId: String? = null

    /**
     * Load match details
     */
    fun loadMatch(mId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            matchId = mId

            try {
                val match = matchRepository.getMatch(mId) ?: throw Exception("Match not found")
                val currentUserId = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")

                // Get other user's ID
                val otherUserId = match.users.firstOrNull { it != currentUserId } ?: throw Exception("Other user not found")

                // Get both user profiles
                val currentUserProfile = userRepository.getUserProfile(currentUserId) ?: throw Exception("Current user profile not found")
                val otherUserProfile = userRepository.getUserProfile(otherUserId) ?: throw Exception("Other user profile not found")

                // Update match users LiveData
                _matchUsers.value = Pair(currentUserProfile, otherUserProfile)

            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Share Instagram with match
     */
    fun shareInstagram() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val mId = matchId ?: throw Exception("Match ID not found")
                val currentUserId = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")

                val result = matchRepository.shareInstagram(mId, currentUserId)
                _instagramShareResult.value = Result.success(result)

            } catch (e: Exception) {
                _instagramShareResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}