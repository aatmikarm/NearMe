package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProximityEvent(
    val id: String = "",
    val users: List<String> = emptyList(),
    val distance: Double = 0.0,
    val startTime: Long = 0,
    val endTime: Long? = null,
    val location: ProximityLocation = ProximityLocation(),
    val status: String = "active",
    val notificationSent: Boolean = false,
    val viewedBy: List<String> = emptyList()
) : Parcelable