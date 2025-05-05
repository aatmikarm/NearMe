package com.aatmik.nearme.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
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

    private val TAG = "AuthActivity"
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    // CountDownTimer for resend button
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default country code to India
        binding.ccp.setDefaultCountryUsingNameCode("IN")
        binding.ccp.resetToDefaultCountry()

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Send verification code button
        binding.btnSendCode.setOnClickListener {
            val phoneNumber = getFullPhoneNumber()
            if (phoneNumber.isNotEmpty()) {
                viewModel.sendVerificationCode(this, phoneNumber)
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
            val phoneNumber = getFullPhoneNumber()
            if (phoneNumber.isNotEmpty()) {
                viewModel.sendVerificationCode(this, phoneNumber)
                startResendTimer()
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Get the full phone number with country code
     */
    private fun getFullPhoneNumber(): String {
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        if (phoneNumber.isEmpty()) {
            return ""
        }

        // Get selected country code with +
        val countryCode = binding.ccp.selectedCountryCodeWithPlus

        // Remove any plus sign if user added it
        val cleanPhoneNumber = if (phoneNumber.startsWith("+")) {
            phoneNumber.substring(1)
        } else {
            phoneNumber
        }

        // For India, validate the phone number format (should be 10 digits)
        if (binding.ccp.selectedCountryNameCode == "IN") {
            // Indian mobile numbers are 10 digits and start with 6, 7, 8, or 9
            val isValidIndianNumber = cleanPhoneNumber.matches(Regex("^[6-9][0-9]{9}$"))
            if (!isValidIndianNumber) {
                Toast.makeText(this, "Please enter a valid 10-digit Indian mobile number", Toast.LENGTH_SHORT).show()
                return ""
            }
        }

        // Combine country code and phone number
        val fullNumber = countryCode + cleanPhoneNumber
        Log.d(TAG, "Full phone number with country code: $fullNumber")
        return fullNumber
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
        // Cancel any existing timer
        countDownTimer?.cancel()

        // Start a countdown timer for resend button
        binding.btnResendCode.isEnabled = false
        binding.tvResendTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvResendTimer.text = "Resend in ${secondsRemaining}s"
            }

            override fun onFinish() {
                binding.btnResendCode.isEnabled = true
                binding.tvResendTimer.visibility = View.GONE
            }
        }.start()
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

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the countdown timer to prevent memory leaks
        countDownTimer?.cancel()
    }
}