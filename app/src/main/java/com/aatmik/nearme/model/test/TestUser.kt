package com.aatmik.nearme.model.test

/**
 * Simple data class for testing Firestore connectivity
 */
data class TestUser(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val createdAt: Long = 0
)