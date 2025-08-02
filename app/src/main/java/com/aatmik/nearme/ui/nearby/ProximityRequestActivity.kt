package com.aatmik.nearme.ui.nearby

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityProximityRequestBinding
import com.aatmik.nearme.util.formatDistance
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProximityRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProximityRequestBinding
    private val viewModel: ProximityRequestViewModel by viewModels()

    private var eventId: String? = null

    companion object {
        private const val EXTRA_EVENT_ID = "event_id"

        fun createIntent(context: Context, eventId: String): Intent {
            return Intent(context, ProximityRequestActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProximityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        if (eventId == null) {
            Toast.makeText(this, "Invalid proximity event", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        observeViewModel()
        loadProximityEvent()
    }

    private fun setupListeners() {
        // Send Friend Request button
        binding.btnConnect.setOnClickListener {
            viewModel.sendFriendRequest()
        }

        // Skip button
        binding.btnSkip.setOnClickListener {
            viewModel.skipRequest()
            finish()
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
            binding.btnConnect.isEnabled = !isLoading
            binding.btnSkip.isEnabled = !isLoading
        }

        // Observe proximity event
        viewModel.proximityEvent.observe(this) { event ->
            event?.let {
                binding.tvDistance.text = formatDistance(it.distance)
            }
        }

        // Observe other user profile
        viewModel.otherUserProfile.observe(this) { profile ->
            profile?.let {
                binding.tvNameAge.text = getString(R.string.name_age_format, it.displayName, it.age)

                // Load profile photo
                val photoUrl = it.photos.firstOrNull { photo -> photo.isPrimary }?.url
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivProfilePhoto)
                }
            }
        }

        // Observe friend request result
        viewModel.requestResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send friend request: ${it.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadProximityEvent() {
        eventId?.let {
            viewModel.loadProximityEvent(it)
        }
    }
}