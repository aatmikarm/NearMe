package com.aatmik.nearme.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityAuthBinding
import com.aatmik.nearme.ui.main.MainActivity
import com.aatmik.nearme.ui.profile.CreateProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val TAG = "AuthActivity"
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    // CountDownTimer for resend button
    private var countDownTimer: CountDownTimer? = null

    // Array to store OTP input fields
    private lateinit var otpFields: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default country code to India
        binding.ccp.setDefaultCountryUsingNameCode("IN")
        binding.ccp.resetToDefaultCountry()

        // Initialize OTP fields array
        otpFields = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        setupOtpFieldsLogic()
        setupListeners()
        observeViewModel()
        setFocusToPhoneNumberField()
    }

    private fun setFocusToPhoneNumberField() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            binding.etPhoneNumber.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etPhoneNumber, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupOtpFieldsLogic() {
        // Add text watchers to each OTP field
        for (i in otpFields.indices) {
            // Text change listener
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // If text is entered, move to next field
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    }

                    // Update the verification code field (for backward compatibility)
                    updateVerificationCodeField()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Handle backspace key to move to previous field
            otpFields[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (otpFields[i].text.isEmpty() && i > 0) {
                        otpFields[i - 1].text = null
                        otpFields[i - 1].requestFocus()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }

        // Set keyboard action for the last OTP field
        otpFields.last().setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyOtp()
                true
            } else {
                false
            }
        }
    }

    private fun updateVerificationCodeField() {
        // Combine all OTP fields into one string
        val otp = otpFields.joinToString("") { it.text.toString() }
        binding.etVerificationCode.setText(otp)
    }

    private fun getOtpCode(): String {
        return otpFields.joinToString("") { it.text.toString() }
    }

    private fun setupListeners() {
        // Set up keyboard action listener for phone number field
        binding.etPhoneNumber.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                // Same action as send code button
                val phoneNumber = getFullPhoneNumber()
                if (phoneNumber.isNotEmpty()) {
                    viewModel.sendVerificationCode(this, phoneNumber)
                    showVerificationCodeLayout(phoneNumber)
                    return@setOnEditorActionListener true
                } else {
                    Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            false
        }

        // Send verification code button
        binding.btnSendCode.setOnClickListener {
            val phoneNumber = getFullPhoneNumber()
            if (phoneNumber.isNotEmpty()) {
                viewModel.sendVerificationCode(this, phoneNumber)
                showVerificationCodeLayout(phoneNumber)
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        // Verify code button
        binding.btnVerifyCode.setOnClickListener {
            verifyOtp()
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

    private fun verifyOtp() {
        val otp = getOtpCode()
        if (otp.length == 6) {
            viewModel.verifyCode(otp)
        } else {
            Toast.makeText(this, "Please enter the 6-digit verification code", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(
                    this,
                    "Please enter a valid 10-digit Indian mobile number",
                    Toast.LENGTH_SHORT
                ).show()
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
                    Toast.makeText(
                        this,
                        "Verification failed: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
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

    private fun showVerificationCodeLayout(phoneNumber: String) {
        binding.layoutPhoneNumber.visibility = View.GONE
        binding.layoutVerificationCode.visibility = View.VISIBLE
        binding.tvPhoneDisplay.text = phoneNumber

        // Clear all OTP fields
        otpFields.forEach { it.text = null }

        // Request focus on first OTP field
        otpFields[0].requestFocus()

        // Show keyboard automatically
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(otpFields[0], InputMethodManager.SHOW_IMPLICIT)
    }

    private fun startResendTimer() {
        // Cancel any existing timer
        countDownTimer?.cancel()

        // Start a countdown timer for resend button
        binding.btnResendCode.isEnabled = false
        binding.tvResendTimer.visibility = View.VISIBLE
        binding.btnResendCode.visibility = View.GONE

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvResendTimer.text = getString(R.string.resend_in_seconds, secondsRemaining)
            }

            override fun onFinish() {
                binding.btnResendCode.isEnabled = true
                binding.tvResendTimer.visibility = View.GONE
                binding.btnResendCode.visibility = View.VISIBLE
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