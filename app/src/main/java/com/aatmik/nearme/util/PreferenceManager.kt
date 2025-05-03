package com.aatmik.nearme.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app preferences and settings
 */
@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    /**
     * Check if it's the first time the app is launched
     */
    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_LAUNCH, true)
    }

    /**
     * Set first time launch to false after onboarding
     */
    fun setFirstTimeLaunchCompleted() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME_LAUNCH, false).apply()
    }

    /**
     * Check if location sharing is enabled
     */
    fun isLocationSharingEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOCATION_SHARING, true)
    }

    /**
     * Set location sharing preference
     */
    fun setLocationSharing(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_LOCATION_SHARING, enabled).apply()
    }

    /**
     * Get proximity range in meters
     */
    fun getProximityRange(): Int {
        return sharedPreferences.getInt(KEY_PROXIMITY_RANGE, DEFAULT_PROXIMITY_RANGE)
    }

    /**
     * Set proximity range in meters
     */
    fun setProximityRange(range: Int) {
        sharedPreferences.edit().putInt(KEY_PROXIMITY_RANGE, range).apply()
    }

    /**
     * Check if background location is enabled
     */
    fun isBackgroundLocationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BACKGROUND_LOCATION, false)
    }

    /**
     * Set background location preference
     */
    fun setBackgroundLocation(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BACKGROUND_LOCATION, enabled).apply()
    }

    /**
     * Get Instagram sharing preference
     */
    fun getInstagramSharing(): String {
        return sharedPreferences.getString(KEY_INSTAGRAM_SHARING, "matches") ?: "matches"
    }

    /**
     * Set Instagram sharing preference
     */
    fun setInstagramSharing(preference: String) {
        sharedPreferences.edit().putString(KEY_INSTAGRAM_SHARING, preference).apply()
    }

    /**
     * Get notification preference
     */
    fun isNotificationEnabled(type: String): Boolean {
        return when (type) {
            NOTIFICATION_PROXIMITY -> sharedPreferences.getBoolean(KEY_NOTIFICATION_PROXIMITY, true)
            NOTIFICATION_MATCH -> sharedPreferences.getBoolean(KEY_NOTIFICATION_MATCH, true)
            NOTIFICATION_MESSAGE -> sharedPreferences.getBoolean(KEY_NOTIFICATION_MESSAGE, true)
            else -> true
        }
    }

    /**
     * Set notification preference
     */
    fun setNotificationEnabled(type: String, enabled: Boolean) {
        when (type) {
            NOTIFICATION_PROXIMITY -> sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_PROXIMITY, enabled).apply()
            NOTIFICATION_MATCH -> sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_MATCH, enabled).apply()
            NOTIFICATION_MESSAGE -> sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_MESSAGE, enabled).apply()
        }
    }

    /**
     * Get gender preference
     */
    fun getGenderPreference(): Set<String> {
        return sharedPreferences.getStringSet(KEY_GENDER_PREFERENCE, DEFAULT_GENDER_PREFERENCE) ?: DEFAULT_GENDER_PREFERENCE
    }

    /**
     * Set gender preference
     */
    fun setGenderPreference(preferences: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_GENDER_PREFERENCE, preferences).apply()
    }

    /**
     * Get age range preference
     */
    fun getAgeRangePreference(): Pair<Int, Int> {
        val min = sharedPreferences.getInt(KEY_AGE_MIN, DEFAULT_AGE_MIN)
        val max = sharedPreferences.getInt(KEY_AGE_MAX, DEFAULT_AGE_MAX)
        return Pair(min, max)
    }

    /**
     * Set age range preference
     */
    fun setAgeRangePreference(min: Int, max: Int) {
        sharedPreferences.edit()
            .putInt(KEY_AGE_MIN, min)
            .putInt(KEY_AGE_MAX, max)
            .apply()
    }

    /**
     * Check if user is premium
     */
    fun isPremiumUser(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PREMIUM, false)
    }

    /**
     * Set premium status
     */
    fun setPremiumStatus(isPremium: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
    }

    /**
     * Save FCM token
     */
    fun saveFcmToken(token: String) {
        sharedPreferences.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    /**
     * Get saved FCM token
     */
    fun getFcmToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    companion object {
        private const val PREF_NAME = "nearme_preferences"

        // Keys
        private const val KEY_FIRST_TIME_LAUNCH = "first_time_launch"
        private const val KEY_LOCATION_SHARING = "location_sharing"
        private const val KEY_PROXIMITY_RANGE = "proximity_range"
        private const val KEY_BACKGROUND_LOCATION = "background_location"
        private const val KEY_INSTAGRAM_SHARING = "instagram_sharing"
        private const val KEY_NOTIFICATION_PROXIMITY = "notification_proximity"
        private const val KEY_NOTIFICATION_MATCH = "notification_match"
        private const val KEY_NOTIFICATION_MESSAGE = "notification_message"
        private const val KEY_GENDER_PREFERENCE = "gender_preference"
        private const val KEY_AGE_MIN = "age_min"
        private const val KEY_AGE_MAX = "age_max"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_FCM_TOKEN = "fcm_token"

        // Default values
        private const val DEFAULT_PROXIMITY_RANGE = 100 // 100 meters
        private const val DEFAULT_AGE_MIN = 18
        private const val DEFAULT_AGE_MAX = 99
        private val DEFAULT_GENDER_PREFERENCE = setOf("male", "female", "non-binary")

        // Notification types
        const val NOTIFICATION_PROXIMITY = "proximity"
        const val NOTIFICATION_MATCH = "match"
        const val NOTIFICATION_MESSAGE = "message"
    }
}
