package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProximityEvent(
    val id: String = "",
    val users: List<String> = emptyList(), // Add default value
    val distance: Double = 0.0, // Add default value
    val startTime: Long = 0, // Add default value
    val endTime: Long? = null,
    val location: ProximityLocation = ProximityLocation(), // Add default value
    val status: String = "active",
    val notificationSent: Boolean = false,
    val viewedBy: List<String> = emptyList()
) : Parcelable