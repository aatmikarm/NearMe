package com.aatmik.nearme.ui.nearby.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aatmik.nearme.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _preferences = MutableLiveData<FilterPreferences>()
    val preferences: LiveData<FilterPreferences> = _preferences

    /**
     * Load current preferences
     */
    fun loadPreferences() {
        viewModelScope.launch {
            // Get proximity range
            val proximityRange = preferenceManager.getProximityRange()
            val distanceProgress = distanceToProgress(proximityRange)

            // Get age range
            val (minAge, maxAge) = preferenceManager.getAgeRangePreference()

            // Get gender preferences
            val genderPreferences = preferenceManager.getGenderPreference()

            // Set preferences
            _preferences.value = FilterPreferences(
                distanceProgress = distanceProgress,
                minAge = minAge,
                maxAge = maxAge,
                genderPreferences = genderPreferences
            )
        }
    }

    /**
     * Reset to default preferences
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            // Default preferences
            val defaultPreferences = FilterPreferences(
                distanceProgress = distanceToProgress(DEFAULT_PROXIMITY_RANGE),
                minAge = DEFAULT_AGE_MIN,
                maxAge = DEFAULT_AGE_MAX,
                genderPreferences = DEFAULT_GENDER_PREFERENCE
            )

            _preferences.value = defaultPreferences
        }
    }

    /**
     * Save preferences
     */
    fun savePreferences(
        distanceProgress: Int,
        minAge: Int,
        maxAge: Int,
        genderPreferences: Set<String>
    ) {
        viewModelScope.launch {
            // Convert distance progress to meters
            val proximityRange = progressToDistance(distanceProgress)

            // Save preferences
            preferenceManager.setProximityRange(proximityRange)
            preferenceManager.setAgeRangePreference(minAge, maxAge)
            preferenceManager.setGenderPreference(genderPreferences)

            // Update local preferences
            _preferences.value = FilterPreferences(
                distanceProgress = distanceProgress,
                minAge = minAge,
                maxAge = maxAge,
                genderPreferences = genderPreferences
            )
        }
    }

    /**
     * Convert distance progress to meters
     */
    fun progressToDistance(progress: Int): Int {
        return when (progress) {
            0 -> 10    // Minimum range is 10 meters
            MAX_PROGRESS -> 5000 // Maximum range is 5 kilometers
            else -> {
                // Exponential scale for better UX
                // 0 -> 10m, 25 -> ~100m, 50 -> ~500m, 75 -> ~1500m, 100 -> 5000m
                val factor = progress.toFloat() / MAX_PROGRESS
                val exponential = Math.pow(factor.toDouble(), 2.0)
                val range = MIN_RANGE + (exponential * (MAX_RANGE - MIN_RANGE)).toInt()
                range
            }
        }
    }

    /**
     * Convert distance in meters to progress
     */
    private fun distanceToProgress(distance: Int): Int {
        return when {
            distance <= MIN_RANGE -> 0
            distance >= MAX_RANGE -> MAX_PROGRESS
            else -> {
                // Inverse of the exponential scale
                val normalizedDistance = (distance - MIN_RANGE).toFloat() / (MAX_RANGE - MIN_RANGE)
                val factor = Math.sqrt(normalizedDistance.toDouble()).toFloat()
                (factor * MAX_PROGRESS).toInt()
            }
        }
    }

    companion object {
        private const val MAX_PROGRESS = 100
        private const val MIN_RANGE = 10 // 10 meters
        private const val MAX_RANGE = 5000 // 5 kilometers

        // Default values
        private const val DEFAULT_PROXIMITY_RANGE = 100 // 100 meters
        private const val DEFAULT_AGE_MIN = 18
        private const val DEFAULT_AGE_MAX = 99
        private val DEFAULT_GENDER_PREFERENCE = setOf("male", "female", "non-binary")
    }
}