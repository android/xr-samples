package com.example.helloandroidxr.data.model

/**
 * Data model representing a user
 */
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false
)
