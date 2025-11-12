package com.appbuildchat.instaxr.data.model

/**
 * Data model representing a comment on a post
 */
data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String? = null,
    val text: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
