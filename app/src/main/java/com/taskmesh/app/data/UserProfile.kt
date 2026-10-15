package com.taskmesh.app.data

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val providerId: String = "email",
    val generalLearnedCount: Int = 0,
    val owspLearnedCount: Int = 0,
    val bookmarksCount: Int = 0,
    val generalDay: Int = 1,
    val owspDay: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
