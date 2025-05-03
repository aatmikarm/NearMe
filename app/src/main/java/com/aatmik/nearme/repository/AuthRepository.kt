package com.aatmik.nearme.repository

import android.app.Activity
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.PreferenceManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) {
    // For storing the verification ID
    private var storedVerificationId: String? = null

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Send verification code
     */
    // In AuthRepository.kt
    fun sendVerificationCode(
        activity: Activity, // Add the activity parameter
        phoneNumber: String,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (Exception) -> Unit,
        onCodeSent: (String) -> Unit
    ) {
        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                onVerificationFailed(exception)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // Save verification ID for later use
                storedVerificationId = verificationId
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity) // Pass the activity here
            .setCallbacks(callback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    /**
     * Verify OTP code
     */
    suspend fun verifyCode(code: String): Result<String> {
        val verificationId = storedVerificationId ?: return Result.failure(Exception("Verification ID not found"))

        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create user account after verification
     */
    suspend fun createUserAccount(userProfile: UserProfile): Result<Boolean> {
        return try {
            val created = userRepository.createUserProfile(userProfile)
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if user profile exists
     */
    suspend fun checkUserProfileExists(userId: String): Boolean {
        val profile = userRepository.getUserProfile(userId)
        return profile != null
    }

    /**
     * Sign out user
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
}