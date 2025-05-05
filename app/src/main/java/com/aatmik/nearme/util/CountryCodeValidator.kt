package com.aatmik.nearme.util

/**
 * Utility class for validating phone numbers with country codes
 */
object CountryCodeValidator {

    /**
     * Validate full phone number (with country code)
     * @param phoneNumber Full phone number with country code (e.g., +917891638838)
     * @return True if valid, false otherwise
     */
    fun isValidFullPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation for international format
        return phoneNumber.matches(Regex("^\\+[0-9]{1,4}[0-9]{6,14}$"))
    }

    /**
     * Validate India phone number specifically
     * @param phoneNumber Phone number without country code
     * @return True if valid Indian mobile number, false otherwise
     */
    fun isValidIndianPhoneNumber(phoneNumber: String): Boolean {
        // Indian mobile numbers are 10 digits and start with 6, 7, 8, or 9
        return phoneNumber.matches(Regex("^[6-9][0-9]{9}$"))
    }

    /**
     * Ensure phone number has proper country code format
     * @param phoneNumber The input phone number which may or may not have country code
     * @param countryCode The country code (e.g., "+91")
     * @return Properly formatted phone number with country code
     */
    fun ensureCountryCode(phoneNumber: String, countryCode: String): String {
        // Remove any existing + from the country code if provided
        val cleanCountryCode = if (countryCode.startsWith("+")) {
            countryCode
        } else {
            "+$countryCode"
        }

        // Check if phone already has country code
        if (phoneNumber.startsWith("+")) {
            return phoneNumber
        }

        // Remove leading zeros from phone number if any
        val cleanPhoneNumber = phoneNumber.trimStart('0')

        // Combine country code and phone number
        return cleanCountryCode + cleanPhoneNumber
    }
}