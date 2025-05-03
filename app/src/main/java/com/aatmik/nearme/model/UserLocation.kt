package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserLocation(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val geohash: String,
    val timestamp: Long,
    val isVisible: Boolean,
    val appState: String = "foreground" // "foreground", "background", "inactive"
) : Parcelable
