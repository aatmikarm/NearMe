package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProximityLocation(
    val geohash: String = "",
    val placeName: String? = null // General location name (if available)
) : Parcelable