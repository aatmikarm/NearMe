// Update UserProfileActivity.kt
package com.aatmik.nearme.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityProfileBinding
import com.aatmik.nearme.util.loadUserPhoto
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: UserProfileViewModel by viewModels()

    private var userId: String? = null

    companion object {
        private const val EXTRA_USER_ID = "user_id"

        fun createIntent(context: Context, userId: String): Intent {
            return Intent(context, UserProfileActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra(EXTRA_USER_ID)
        if (userId != null) {
            setupToolbar()
            observeViewModel()
            loadUserProfile()
        } else {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                // Set name and age
                binding.tvNameAge.text = "${it.displayName}, ${it.age}"

                // Set bio
                binding.tvBio.text = it.bio.ifEmpty { "No bio provided" }

                // Set profile photo
                // Set profile photo with our new method
                val photoUrl = it.photos.firstOrNull { photo -> photo.isPrimary }?.url
                binding.ivProfilePhoto.loadUserPhoto(photoUrl, it.uid)


                // Set interests
                binding.chipGroupInterests.removeAllViews()
                it.interests.forEach { interest ->
                    val chip = Chip(this)
                    chip.text = interest
                    binding.chipGroupInterests.addView(chip)
                }
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserProfile() {
        userId?.let {
            viewModel.loadUserProfile(it)
        }
    }
}