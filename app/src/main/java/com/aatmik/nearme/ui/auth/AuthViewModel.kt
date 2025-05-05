package com.aatmik.nearme.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val TAG = "AuthViewModel"

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _verificationSent = MutableLiveData<Boolean>()
    val verificationSent: LiveData<Boolean> = _verificationSent

    private val _verificationError = MutableLiveData<Exception>()
    val verificationError: LiveData<Exception> = _verificationError

    private val _authResult = MutableLiveData<Result<String>?>()
    val authResult: LiveData<Result<String>?> = _authResult

    private val _userProfileExists = MutableLiveData<Boolean>()
    val userProfileExists: LiveData<Boolean> = _userProfileExists

    // Store phone number for later use
    private var phoneNumber: String = ""

    // In AuthViewModel.kt
    fun sendVerificationCode(activity: Activity, phone: String) {
        _isLoading.value = true
        phoneNumber = phone

        // Log the phone number for debugging
        Log.d(TAG, "Sending verification code to: $phoneNumber")

        authRepository.sendVerificationCode(
            activity,
            phoneNumber = phone,
            onVerificationCompleted = { credential ->
                // Auto-verification completed
                _isLoading.value = false
                signInWithCredential(credential)
            },
            onVerificationFailed = { exception ->
                _isLoading.value = false
                _verificationError.value = exception
                Log.e(TAG, "Verification failed: ${exception.message}", exception)
            },
            onCodeSent = { _ ->
                _isLoading.value = false
                _verificationSent.value = true
                Log.d(TAG, "Verification code sent to: $phoneNumber")
            }
        )
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val user = FirebaseAuth.getInstance().signInWithCredential(credential).await().user
                if (user != null) {
                    _authResult.value = Result.success(user.uid)
                    Log.d(TAG, "Authentication successful for user: ${user.uid}")
                } else {
                    _authResult.value = Result.failure(Exception("Authentication failed"))
                    Log.e(TAG, "Authentication failed: user is null")
                }
            } catch (e: Exception) {
                _authResult.value = Result.failure(e)
                Log.e(TAG, "Authentication failed: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Verifying code: $code")

            _authResult.value = authRepository.verifyCode(code)
            _isLoading.value = false
        }
    }

    fun checkUserProfileExists() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            Log.d(TAG, "Checking if profile exists for user: $userId")

            val exists = authRepository.checkUserProfileExists(userId)
            _userProfileExists.value = exists

            Log.d(TAG, "Profile exists: $exists")
        }
    }
}