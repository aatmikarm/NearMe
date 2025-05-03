// Update UserProfileActivity.kt
package com.aatmik.nearme.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aatmik.nearme.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

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
            loadUserProfile()
        }
    }

    private fun loadUserProfile() {
        // Load user profile implementation
    }
}