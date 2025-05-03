package com.aatmik.nearme.util

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Get user-friendly error message from Firebase exceptions
 */
object FirebaseExceptions {

    /**
     * Get friendly message for Firebase Auth exceptions
     */
    fun getAuthErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_CREDENTIAL" -> "Invalid credentials. Please try again."
                    "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid verification code. Please try again."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many requests. Please try again later."
                    "ERROR_SESSION_EXPIRED" -> "Verification session expired. Please request a new code."
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Account already exists with different credentials."
                    "ERROR_REQUIRES_RECENT_LOGIN" -> "This operation requires recent authentication. Please log in again."
                    else -> "Authentication error: ${exception.message}"
                }
            }
            else -> exception.message ?: "An unknown error occurred"
        }
    }

    /**
     * Get friendly message for Firestore exceptions
     */
    fun getFirestoreErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied. You don't have access to this resource."
                    FirebaseFirestoreException.Code.UNAVAILABLE -> "Service unavailable. Please check your internet connection."
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "Request timed out. Please try again."
                    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> "Too many requests. Please try again later."
                    FirebaseFirestoreException.Code.NOT_FOUND -> "Requested document doesn't exist."
                    FirebaseFirestoreException.Code.CANCELLED -> "Operation cancelled."
                    else -> "Database error: ${exception.message}"
                }
            }
            else -> exception.message ?: "An unknown error occurred"
        }
    }

    /**
     * Get friendly message for general Firebase exceptions
     */
    fun getFirebaseErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthException -> getAuthErrorMessage(exception)
            is FirebaseFirestoreException -> getFirestoreErrorMessage(exception)
            is FirebaseException -> "Firebase error: ${exception.message}"
            else -> exception.message ?: "An unknown error occurred"
        }
    }
}