package com.aatmik.nearme.ui.nearby

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI model for nearby users
 */
@Parcelize
data class NearbyUserModel(
    val userId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bio: String,
    val photoUrl: String,
    val distance: Double,
    val lastActive: Long,
    val hasInstagram: Boolean
) : Parcelable
