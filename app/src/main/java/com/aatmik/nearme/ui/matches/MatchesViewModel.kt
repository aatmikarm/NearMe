// Update MatchesViewModel.kt
package com.aatmik.nearme.ui.matches

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.Match
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.MatchRepository
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _newMatches = MutableLiveData<List<Pair<Match, UserProfile>>>()
    val newMatches: LiveData<List<Pair<Match, UserProfile>>> = _newMatches

    private val _allMatches = MutableLiveData<List<Pair<Match, UserProfile>>>()
    val allMatches: LiveData<List<Pair<Match, UserProfile>>> = _allMatches

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userId = userRepository.getCurrentUserId() ?: return@launch

                // Get new matches (last 24 hours)
                val newMatchesList = matchRepository.getNewMatches(userId)
                val newMatchesWithProfiles = mutableListOf<Pair<Match, UserProfile>>()

                for (match in newMatchesList) {
                    val otherUserId = match.users.firstOrNull { it != userId } ?: continue
                    val otherUserProfile = userRepository.getUserProfile(otherUserId) ?: continue
                    newMatchesWithProfiles.add(Pair(match, otherUserProfile))
                }

                _newMatches.value = newMatchesWithProfiles

                // Get all matches
                val allMatchesList = matchRepository.getMatches(userId)
                val allMatchesWithProfiles = mutableListOf<Pair<Match, UserProfile>>()

                for (match in allMatchesList) {
                    val otherUserId = match.users.firstOrNull { it != userId } ?: continue
                    val otherUserProfile = userRepository.getUserProfile(otherUserId) ?: continue
                    allMatchesWithProfiles.add(Pair(match, otherUserProfile))
                }

                _allMatches.value = allMatchesWithProfiles

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}