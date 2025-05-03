package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val uid: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val createdAt: Long = 0,
    val lastActive: Long = 0,
    val displayName: String = "",
    val age: Int = 0,
    val gender: String = "",
    val bio: String = "",
    val interests: List<String> = emptyList(),
    val photos: List<UserPhoto> = emptyList(),
    val instagramId: String = "",
    val instagramConnected: Boolean = false,
    val verificationStatus: String = "none", // "none", "pending", "verified"
    val location: UserLocationPreference = UserLocationPreference(),
    val preferences: DiscoveryPreferences = DiscoveryPreferences(),
    val privacy: PrivacySettings = PrivacySettings(),
    val statistics: UserStatistics = UserStatistics(),
    val subscription: SubscriptionInfo = SubscriptionInfo(),
    val isOnline: Boolean = false,
    val blockedUsers: List<String> = emptyList()
) : Parcelable

@Parcelize
data class UserPhoto(
    val id: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
    val isPrimary: Boolean = false
) : Parcelable

@Parcelize
data class UserLocationPreference(
    val isLocationEnabled: Boolean = true,
    val lastLocationTimestamp: Long = 0
) : Parcelable

@Parcelize
data class DiscoveryPreferences(
    val discoveryDistance: Int = 100, // in meters (10-500)
    val ageRangeMin: Int = 18,
    val ageRangeMax: Int = 99,
    val genderPreference: List<String> = listOf("male", "female", "non-binary"),
    val showInstagramTo: String = "matches" // "all", "matches", "none"
) : Parcelable

@Parcelize
data class PrivacySettings(
    val profileVisibility: String = "visible", // "visible", "hidden"
    val locationSharing: String = "always", // "always", "app_open", "never"
    val allowBackgroundLocation: Boolean = false
) : Parcelable

@Parcelize
data class UserStatistics(
    val proximityEvents: Int = 0,
    val matches: Int = 0,
    val profileViews: Int = 0
) : Parcelable

@Parcelize
data class SubscriptionInfo(
    val isPremium: Boolean = false,
    val plan: String = "none", // "none", "monthly", "semi_annual"
    val expiresAt: Long = 0,
    val paymentMethod: String = ""
) : Parcelable
