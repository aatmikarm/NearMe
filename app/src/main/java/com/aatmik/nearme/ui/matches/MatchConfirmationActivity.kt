package com.aatmik.nearme.ui.matches

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityMatchConfirmationBinding
import com.aatmik.nearme.ui.messages.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchConfirmationBinding
    private val viewModel: MatchConfirmationViewModel by viewModels()

    private var matchId: String? = null

    companion object {
        private const val EXTRA_MATCH_ID = "match_id"

        fun createIntent(context: Context, matchId: String): Intent {
            return Intent(context, MatchConfirmationActivity::class.java).apply {
                putExtra(EXTRA_MATCH_ID, matchId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMatchConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matchId = intent.getStringExtra(EXTRA_MATCH_ID)
        if (matchId == null) {
            Toast.makeText(this, "Invalid match", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        observeViewModel()
        loadMatch()
    }

    private fun setupListeners() {
        // Share Instagram button
        binding.btnShareInstagram.setOnClickListener {
            viewModel.shareInstagram()
        }

        // Don't Share button
        binding.btnDontShare.setOnClickListener {
            binding.layoutInstagramShare.visibility = View.GONE
            binding.layoutActions.visibility = View.VISIBLE
        }

        // Message button
        binding.btnMessage.setOnClickListener {
            navigateToChatScreen()
        }

        // Close button
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnShareInstagram.isEnabled = !isLoading
            binding.btnDontShare.isEnabled = !isLoading
            binding.btnMessage.isEnabled = !isLoading
        }

        // Observe match data
        viewModel.matchUsers.observe(this) { users ->
            users?.let { (currentUser, otherUser) ->
                // Set current user photo
                Glide.with(this)
                    .load(currentUser.photos.firstOrNull { it.isPrimary }?.url)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivCurrentUserPhoto)

                // Set other user photo
                Glide.with(this)
                    .load(otherUser.photos.firstOrNull { it.isPrimary }?.url)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivOtherUserPhoto)

                // Set text
                binding.tvMatchText.text = getString(R.string.matched_with_name, otherUser.displayName)
            }
        }

        // Observe Instagram share result
        viewModel.instagramShareResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(this, R.string.instagram_shared, Toast.LENGTH_SHORT).show()
                    binding.layoutInstagramShare.visibility = View.GONE
                    binding.layoutActions.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        this,
                        "Failed to share Instagram: ${it.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadMatch() {
        matchId?.let {
            viewModel.loadMatch(it)
        }
    }

    private fun navigateToChatScreen() {
        matchId?.let {
            val intent = ChatActivity.createIntent(this, it)
            startActivity(intent)
            finish()
        }
    }
}
