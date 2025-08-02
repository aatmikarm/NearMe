// Update MessagesFragment.kt
package com.aatmik.nearme.ui.messages.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aatmik.nearme.databinding.FragmentMessagesBinding
import com.aatmik.nearme.ui.messages.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConversationsFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConversationsViewModel by viewModels()
    private lateinit var conversationAdapter: ConversationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh conversations when coming back to this fragment
        viewModel.loadConversations()
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter { conversation, _ ->
            navigateToChat(conversation.friendId)
        }

        binding.rvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe conversations
        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            conversationAdapter.submitList(conversations)

            // Show empty state if no conversations
            updateEmptyState(conversations.isEmpty())
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvConversations.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun navigateToChat(friendId: String) {
        val intent = ChatActivity.createIntent(requireContext(), friendId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}