package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Match(
    val id: String = "",
    val users: List<String> = emptyList(), // UIDs of users involved
    val matchedAt: Long = 0, // When match occurred
    val proximityEventId: String = "", // Reference to proximity event
    val status: String = "active", // "active", "deleted"
    val instagramShared: Map<String, Boolean> = emptyMap(), // First user shared Instagram
    val lastInteraction: Long = 0, // Last activity between users
    val lastMessage: MessagePreview? = null // Preview of last message (for UI)
) : Parcelable

@Parcelize
data class MessagePreview(
    val text: String = "", // Message preview text
    val sentAt: Long = 0, // When message was sent
    val senderId: String = "" // Who sent the message
) : Parcelable
