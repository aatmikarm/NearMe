package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProximityEvent(
    val id: String = "",
    val users: List<String>, // UIDs of users involved
    val distance: Double, // Distance in meters
    val startTime: Long, // When proximity began
    val endTime: Long? = null, // When proximity ended (null if ongoing)
    val location: ProximityLocation,
    val status: String = "active", // "active", "ended", "matched", "ignored"
    val notificationSent: Boolean = false,
    val viewedBy: List<String> = emptyList() // Which users have viewed this event
) : Parcelable