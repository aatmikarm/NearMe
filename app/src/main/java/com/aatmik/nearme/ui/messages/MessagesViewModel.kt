// Update MessagesViewModel.kt
package com.aatmik.nearme.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.model.Conversation
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.model.Match
import com.aatmik.nearme.repository.ConversationRepository
import com.aatmik.nearme.repository.MatchRepository
import com.aatmik.nearme.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _conversations = MutableLiveData<List<Pair<Conversation, UserProfile>>>()
    val conversations: LiveData<List<Pair<Conversation, UserProfile>>> = _conversations

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadConversations()
    }

    // Update MessagesViewModel.kt to include match data
    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userId = userRepository.getCurrentUserId() ?: return@launch

                // Get all active matches
                val matches = matchRepository.getMatches(userId)
                val conversationsWithProfiles = mutableListOf<Pair<Conversation, UserProfile>>()

                for (match in matches) {
                    try {
                        // Get or create conversation for this match
                        val conversation = conversationRepository.getOrCreateConversation(match.id)

                        // Get other user profile
                        val otherUserId = match.users.firstOrNull { it != userId } ?: continue
                        val otherUserProfile = userRepository.getUserProfile(otherUserId) ?: continue

                        // Add match last message data to the conversation
                        conversation.lastMessage = match.lastMessage

                        conversationsWithProfiles.add(Pair(conversation, otherUserProfile))
                    } catch (e: Exception) {
                        // Skip this conversation if there's an error
                        continue
                    }
                }

                // Sort conversations by last update time (most recent first)
                conversationsWithProfiles.sortByDescending {
                    val match = matches.find { m -> m.id == it.first.matchId }
                    match?.lastInteraction ?: it.first.updatedAt
                }

                _conversations.value = conversationsWithProfiles

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}