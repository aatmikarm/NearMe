package com.aatmik.nearme.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.Friend
import com.aatmik.nearme.model.FriendRequest
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.FriendRepository
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _receivedRequests = MutableLiveData<List<Pair<FriendRequest, UserProfile>>>()
    val receivedRequests: LiveData<List<Pair<FriendRequest, UserProfile>>> = _receivedRequests

    private val _sentRequests = MutableLiveData<List<Pair<FriendRequest, UserProfile>>>()
    val sentRequests: LiveData<List<Pair<FriendRequest, UserProfile>>> = _sentRequests

    private val _friends = MutableLiveData<List<Pair<Friend, UserProfile>>>()
    val friends: LiveData<List<Pair<Friend, UserProfile>>> = _friends

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId() ?: return@launch

                // Load received requests
                val received = friendRepository.getReceivedRequests(userId)
                _receivedRequests.value = received.mapNotNull { request ->
                    val otherUserId = request.users.firstOrNull { it != userId }
                    val profile = otherUserId?.let { userRepository.getUserProfile(it) }
                    if (profile != null) Pair(request, profile) else null
                }

                // Load sent requests
                val sent = friendRepository.getSentRequests(userId)
                _sentRequests.value = sent.mapNotNull { request ->
                    val otherUserId = request.users.firstOrNull { it != userId }
                    val profile = otherUserId?.let { userRepository.getUserProfile(it) }
                    if (profile != null) Pair(request, profile) else null
                }

                // Load friends
                val friendsList = friendRepository.getFriends(userId)
                _friends.value = friendsList.mapNotNull { friend ->
                    val otherUserId = friend.users.firstOrNull { it != userId }
                    val profile = otherUserId?.let { userRepository.getUserProfile(it) }
                    if (profile != null) Pair(friend, profile) else null
                }

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            try {
                friendRepository.acceptFriendRequest(requestId)
                loadAllData()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            try {
                friendRepository.rejectFriendRequest(requestId)
                loadAllData()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}