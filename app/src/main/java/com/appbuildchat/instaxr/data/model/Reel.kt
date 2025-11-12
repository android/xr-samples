package com.appbuildchat.instaxr.data.model

/**
 * Data model representing a reel (short video)
 */
data class Reel(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String? = null,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val caption: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val viewCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
