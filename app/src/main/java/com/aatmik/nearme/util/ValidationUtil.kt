package com.aatmik.nearme.util

/**
 * Validation utility for form fields
 */
object ValidationUtil {

    private val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PASSWORD_REGEX = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")

    /**
     * Validate phone number
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.isNotEmpty() && PHONE_REGEX.matches(phone)
    }

    /**
     * Validate email
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && EMAIL_REGEX.matches(email)
    }

    /**
     * Validate password
     * Must contain at least one digit, one lowercase letter, one uppercase letter,
     * one special character, no whitespace, and be at least 8 characters long
     */
    fun isValidPassword(password: String): Boolean {
        return password.isNotEmpty() && PASSWORD_REGEX.matches(password)
    }

    /**
     * Validate name
     */
    fun isValidName(name: String): Boolean {
        return name.isNotEmpty() && name.length >= 2
    }

    /**
     * Validate age (must be 18 or older)
     */
    fun isValidAge(age: Int): Boolean {
        return age >= 18
    }

    /**
     * Validate bio (optional but max 500 chars)
     */
    fun isValidBio(bio: String): Boolean {
        return bio.length <= 500
    }

    /**
     * Validate verification code (6 digits)
     */
    fun isValidVerificationCode(code: String): Boolean {
        return code.length == 6 && code.all { it.isDigit() }
    }

    /**
     * Validate Instagram username
     */
    fun isValidInstagramUsername(username: String): Boolean {
        // Instagram usernames can contain letters, numbers, periods and underscores
        // and must be between 1 and 30 characters
        val instagramRegex = Regex("^[a-zA-Z0-9._]{1,30}$")
        return username.isNotEmpty() && instagramRegex.matches(username)
    }
}