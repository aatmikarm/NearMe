package com.aatmik.nearme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Update in Conversation.kt
@Parcelize
data class Conversation(
    val id: String = "",
    val matchId: String = "",
    val participants: List<String> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val lastReadBy: Map<String, Long> = emptyMap(), // User ID to timestamp
    var lastMessage: MessagePreview? = null // Added this field
) : Parcelable

@Parcelize
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: String = "text", // "text", "image", "location"
    val mediaUrl: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false,
    val readAt: Long = 0,
    val status: String = "sent" // "sent", "delivered", "read"
) : Parcelable