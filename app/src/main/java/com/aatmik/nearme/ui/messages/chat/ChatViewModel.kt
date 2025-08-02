package com.aatmik.nearme.ui.messages.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.Friend
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.repository.ConversationRepository
import com.aatmik.nearme.repository.FriendRepository
import com.aatmik.nearme.model.Message
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val friendRepository: FriendRepository,
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
    private var friend: Friend? = null
    private var otherUserId: String? = null

    /**
     * Load conversation for a friend
     */
    fun loadConversation(friendId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Get friend
                friend = friendRepository.getFriend(friendId) ?: throw Exception("Friend not found")

                // Get current user ID
                val currentUserId = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")

                // Get other user ID
                otherUserId = friend?.users?.firstOrNull { it != currentUserId } ?: throw Exception("Other user not found")

                // Get other user profile
                val otherUserProfile = userRepository.getUserProfile(otherUserId!!) ?: throw Exception("Other user profile not found")
                _otherUser.value = otherUserProfile

                // Get or create conversation
                val conversation = conversationRepository.getOrCreateConversation(friendId)
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
    fun sendMessage(friendId: String, text: String) {
        viewModelScope.launch {
            try {
                val convId = conversationId ?: throw Exception("Conversation not loaded")
                val currentUserId = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")

                val messageId = conversationRepository.sendMessage(convId, currentUserId, text)
                _sendMessageResult.value = Result.success(messageId)

                // Update last message in friend
                friendRepository.updateLastMessage(friendId, text, currentUserId)

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

        return friend?.instagramShared?.get(otherUser) == true
    }

    /**
     * Get other user's Instagram ID
     */
    fun getOtherUserInstagram(): String? {
        if (!isInstagramShared()) return null
        return _otherUser.value?.instagramId
    }
}