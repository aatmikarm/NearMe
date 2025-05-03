package com.aatmik.nearme.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.Match
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.ConversationRepository
import com.aatmik.nearme.repository.MatchRepository
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _otherUser = MutableLiveData<UserProfile?>()
    val otherUser: LiveData<UserProfile?> = _otherUser

    private val _sendMessageResult = MutableLiveData<Result<String>?>()
    val sendMessageResult: LiveData<Result<String>?> = _sendMessageResult

    private var conversationId: String? = null
    private var match: Match? = null
    private var otherUserId: String? = null

    /**
     * Load conversation for a match
     */
    fun loadConversation(matchId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Get match
                match = matchRepository.getMatch(matchId) ?: throw Exception("Match not found")

                // Get current user ID
                val currentUserId = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")

                // Get other user ID
                otherUserId = match?.users?.firstOrNull { it != currentUserId } ?: throw Exception("Other user not found")

                // Get other user profile
                val otherUserProfile = userRepository.getUserProfile(otherUserId!!) ?: throw Exception("Other user profile not found")
                _otherUser.value = otherUserProfile

                // Get or create conversation
                val conversation = conversationRepository.getOrCreateConversation(matchId)
                conversationId = conversation.id

                // Subscribe to messages
                conversationRepository.getMessages(conversation.id).collectLatest { messageList ->
                    _messages.value = messageList

                    // Mark messages as read
                    conversationRepository.markMessagesAsRead(conversation.id, currentUserId)
                }

            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Send a message
     */
    fun sendMessage(matchId: String, text: String) {
        viewModelScope.launch {
            try {
                val convId = conversationId ?: throw Exception("Conversation not loaded")
                val currentUserId = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")

                val messageId = conversationRepository.sendMessage(convId, currentUserId, text)
                _sendMessageResult.value = Result.success(messageId)

                // Update last message in match
                matchRepository.updateLastMessage(matchId, text, currentUserId)

            } catch (e: Exception) {
                _sendMessageResult.value = Result.failure(e)
            }
        }
    }

    /**
     * Check if Instagram is shared
     */
    fun isInstagramShared(): Boolean {
        val currentUserId = userRepository.getCurrentUserId() ?: return false
        val otherUser = otherUserId ?: return false

        return match?.instagramShared?.get(otherUser) == true
    }

    /**
     * Get other user's Instagram ID
     */
    fun getOtherUserInstagram(): String? {
        if (!isInstagramShared()) return null
        return _otherUser.value?.instagramId
    }
}