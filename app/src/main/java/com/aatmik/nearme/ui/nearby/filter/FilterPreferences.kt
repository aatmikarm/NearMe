package com.aatmik.nearme.ui.nearby.filter

/**
 * UI model for filter preferences
 */
data class FilterPreferences(
    val distanceProgress: Int,
    val minAge: Int,
    val maxAge: Int,
    val genderPreferences: Set<String>
)