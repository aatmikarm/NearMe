package com.aatmik.nearme.repository

import android.app.Activity
import android.util.Log
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
    private val TAG = "NearMe_AuthRepository"

    // For storing the verification ID
    private var storedVerificationId: String? = null

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        val isLoggedIn = firebaseAuth.currentUser != null
        Log.d(TAG, "Checking if user is logged in: $isLoggedIn")
        return isLoggedIn
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            Log.d(TAG, "Current user ID: $uid")
        } else {
            Log.w(TAG, "Current user ID is null (user not authenticated)")
        }
        return uid
    }

    /**
     * Send verification code
     */
    fun sendVerificationCode(
        activity: Activity, // Add the activity parameter
        phoneNumber: String,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (Exception) -> Unit,
        onCodeSent: (String) -> Unit
    ) {
        Log.d(TAG, "Sending verification code to phone number: $phoneNumber")

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.i(TAG, "Verification automatically completed")
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.e(TAG, "Verification failed: ${exception.message}", exception)
                onVerificationFailed(exception)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // Save verification ID for later use
                Log.i(TAG, "Verification code sent successfully")
                storedVerificationId = verificationId
                onCodeSent(verificationId)
            }
        }

        try {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity) // Pass the activity here
                .setCallbacks(callback)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
            Log.d(TAG, "Verification request sent to Firebase")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending verification code: ${e.message}", e)
            onVerificationFailed(e)
        }
    }

    /**
     * Verify OTP code
     */
    suspend fun verifyCode(code: String): Result<String> {
        Log.d(TAG, "Verifying OTP code")

        val verificationId = storedVerificationId
        if (verificationId == null) {
            Log.e(TAG, "Verification ID not found")
            return Result.failure(Exception("Verification ID not found"))
        }

        return try {
            val startTime = System.currentTimeMillis()

            Log.d(TAG, "Creating credential with verification ID and code")
            val credential = PhoneAuthProvider.getCredential(verificationId, code)

            Log.d(TAG, "Signing in with credential")
            val result = firebaseAuth.signInWithCredential(credential).await()

            val uid = result.user?.uid
            val duration = System.currentTimeMillis() - startTime

            if (uid != null) {
                Log.i(TAG, "Verification successful for user: $uid (took $duration ms)")
                Result.success(uid)
            } else {
                Log.w(TAG, "Verification successful but user ID is null (took $duration ms)")
                Result.success("")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying code: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Create user account after verification
     */
    suspend fun createUserAccount(userProfile: UserProfile): Result<Boolean> {
        Log.d(TAG, "Creating user account for user ID: ${userProfile.uid}")

        return try {
            val startTime = System.currentTimeMillis()

            val created = userRepository.createUserProfile(userProfile)

            val duration = System.currentTimeMillis() - startTime

            if (created) {
                Log.i(TAG, "User account created successfully for user ID: ${userProfile.uid} (took $duration ms)")
            } else {
                Log.w(TAG, "Failed to create user account for user ID: ${userProfile.uid} (took $duration ms)")
            }

            Result.success(created)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user account: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if user profile exists
     */
    suspend fun checkUserProfileExists(userId: String): Boolean {
        Log.d(TAG, "Checking if user profile exists for user ID: $userId")

        try {
            val startTime = System.currentTimeMillis()

            val profile = userRepository.getUserProfile(userId)

            val duration = System.currentTimeMillis() - startTime
            val exists = profile != null

            Log.d(TAG, "User profile ${if (exists) "exists" else "does not exist"} for user ID: $userId (check took $duration ms)")

            return exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user profile exists: ${e.message}", e)
            return false
        }
    }

    /**
     * Sign out user
     */
    fun signOut() {
        Log.d(TAG, "Signing out user: ${getCurrentUserId()}")

        try {
            firebaseAuth.signOut()
            Log.i(TAG, "User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out user: ${e.message}", e)
        }
    }
}