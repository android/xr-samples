package com.example.helloandroidxr.data.model

/**
 * Data model representing a post
 */
data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String? = null,
    val imageUrl: String,
    val caption: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
