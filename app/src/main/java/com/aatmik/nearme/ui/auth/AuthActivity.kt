package com.aatmik.nearme.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityAuthBinding
import com.aatmik.nearme.ui.main.MainActivity
import com.aatmik.nearme.ui.profile.CreateProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Send verification code button
        binding.btnSendCode.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                viewModel.sendVerificationCode(phoneNumber)
                showVerificationCodeLayout()
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        // Verify code button
        binding.btnVerifyCode.setOnClickListener {
            val code = binding.etVerificationCode.text.toString().trim()
            if (code.isNotEmpty()) {
                viewModel.verifyCode(code)
            } else {
                Toast.makeText(this, "Please enter verification code", Toast.LENGTH_SHORT).show()
            }
        }

        // Resend code button
        binding.btnResendCode.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                viewModel.sendVerificationCode(phoneNumber)
            }
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSendCode.isEnabled = !isLoading
            binding.btnVerifyCode.isEnabled = !isLoading
            binding.btnResendCode.isEnabled = !isLoading
        }

        // Observe verification state
        viewModel.verificationSent.observe(this) { sent ->
            if (sent) {
                Toast.makeText(this, "Verification code sent", Toast.LENGTH_SHORT).show()
                startResendTimer()
            }
        }

        // Observe verification failures
        viewModel.verificationError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observe authentication result
        viewModel.authResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    // Check if user profile exists
                    viewModel.checkUserProfileExists()
                } else {
                    Toast.makeText(this, "Verification failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observe user profile check result
        viewModel.userProfileExists.observe(this) { exists ->
            if (exists) {
                // Profile exists, go to main activity
                navigateToMainActivity()
            } else {
                // Profile doesn't exist, go to create profile
                navigateToCreateProfileActivity()
            }
        }
    }

    private fun showVerificationCodeLayout() {
        binding.layoutPhoneNumber.visibility = View.GONE
        binding.layoutVerificationCode.visibility = View.VISIBLE
    }

    private fun startResendTimer() {
        // Start a countdown timer for resend button
        binding.btnResendCode.isEnabled = false
        binding.tvResendTimer.visibility = View.VISIBLE

        // In a real app, implement a countdown timer here
        // For simplicity, we'll just enable the button after a delay
        binding.btnResendCode.postDelayed({
            binding.btnResendCode.isEnabled = true
            binding.tvResendTimer.visibility = View.GONE
        }, 60000) // 60 seconds
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToCreateProfileActivity() {
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
}