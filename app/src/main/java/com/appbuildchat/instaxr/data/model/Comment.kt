package com.appbuildchat.instaxr.data.model

data class Comment(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String? = null,
    val text: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val replies: List<Comment> = emptyList()
)
