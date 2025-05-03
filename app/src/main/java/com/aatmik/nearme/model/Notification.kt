package com.aatmik.nearme.model

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val type: String = "", // "proximity", "match", "message", "system"
    val title: String = "",
    val body: String = "",
    val data: Map<String, String> = emptyMap(),
    val createdAt: Long = 0,
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val sentToDevice: Boolean = false
)