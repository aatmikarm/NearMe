package com.aatmik.nearme.ui.messages.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityChatBinding
import com.aatmik.nearme.util.loadUserPhoto
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var messageAdapter: MessageAdapter

    private var friendId: String? = null

    companion object {
        private const val EXTRA_FRIEND_ID = "friend_id"

        fun createIntent(context: Context, friendId: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_FRIEND_ID, friendId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendId = intent.getStringExtra(EXTRA_FRIEND_ID)
        if (friendId == null) {
            Toast.makeText(this, "Invalid conversation", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        loadConversation()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Send button
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.etMessage.text?.clear()
            }
        }

        // Instagram button (show profile)
        binding.ivInstagram.setOnClickListener {
            viewModel.getOtherUserInstagram()?.let { instagramId ->
                // Open Instagram profile (in a real app, you'd use Instagram deep linking)
                Toast.makeText(this, "Instagram: $instagramId", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe conversation
        viewModel.otherUser.observe(this) { user ->
            user?.let {
                // Set title
                binding.tvName.text = it.displayName

                // Load profile photo with our new method
                binding.ivProfilePhoto.loadUserPhoto(
                    it.photos.firstOrNull { photo -> photo.isPrimary }?.url,
                    it.uid
                )

                // Show Instagram button if shared
                binding.ivInstagram.visibility = if (viewModel.isInstagramShared()) View.VISIBLE else View.GONE
            }
        }

        // Observe messages
        viewModel.messages.observe(this) { messages ->
            messageAdapter.submitList(messages)
            if (messages != null && messages.isNotEmpty()) {
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
        }

        // Observe send message result
        viewModel.sendMessageResult.observe(this) { result ->
            result?.let {
                if (it.isFailure) {
                    Toast.makeText(
                        this,
                        "Failed to send message: ${it.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadConversation() {
        friendId?.let {
            viewModel.loadConversation(it)
        }
    }

    private fun sendMessage(text: String) {
        friendId?.let {
            viewModel.sendMessage(it, text)
        }
    }
}