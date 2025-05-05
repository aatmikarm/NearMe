package com.aatmik.nearme.util

/**
 * Validation utility for form fields
 */
object ValidationUtil {

    // Updated phone regex to support international formats with country code
    private val PHONE_REGEX = Regex("^\\+[0-9]{1,4}[0-9]{6,14}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PASSWORD_REGEX = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")

    /**
     * Validate phone number with country code
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.isNotEmpty() && PHONE_REGEX.matches(phone)
    }

    /**
     * Validate phone number for specific country
     */
    fun isValidCountryPhoneNumber(phone: String, countryCode: String): Boolean {
        // For India (+91)
        if (countryCode == "+91") {
            // Remove country code if present
            val localNumber = if (phone.startsWith("+91")) phone.substring(3) else phone
            // Indian numbers are 10 digits starting with 6, 7, 8, or 9
            return localNumber.matches(Regex("^[6-9][0-9]{9}$"))
        }

        // For other countries, use general validation
        return isValidPhoneNumber(phone)
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