package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FriendRequest(
    val id: String = "",
    val users: List<String> = emptyList(), // UIDs of users involved
    val requestedAt: Long = 0, // When request was sent
    val proximityEventId: String = "", // Reference to proximity event
    val status: String = "pending", // "pending", "accepted", "rejected"
    val requestedBy: String = "", // Who sent the friend request
    val acceptedAt: Long? = null, // When request was accepted
    val rejectedAt: Long? = null, // When request was rejected
    val lastInteraction: Long = 0 // Last activity between users
) : Parcelable

@Parcelize
data class Friend(
    val id: String = "",
    val users: List<String> = emptyList(), // UIDs of users involved
    val friendsSince: Long = 0, // When they became friends
    val proximityEventId: String = "", // Reference to original proximity event
    val instagramShared: Map<String, Boolean> = emptyMap(), // Instagram sharing status
    val lastInteraction: Long = 0, // Last activity between users
    val lastMessage: MessagePreview? = null // Preview of last message (for UI)
) : Parcelable

@Parcelize
data class MessagePreview(
    val text: String = "", // Message preview text
    val sentAt: Long = 0, // When message was sent
    val senderId: String = "" // Who sent the message
) : Parcelable