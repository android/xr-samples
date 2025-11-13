package com.appbuildchat.instaxr.data.model

/**
 * Data model representing a post
 */
data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String? = null,
    val imageUrls: List<String>, // Multiple images support
    val caption: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val comments: List<Comment> = emptyList()
) {
    // Convenience property for backward compatibility
    val imageUrl: String
        get() = imageUrls.firstOrNull() ?: ""
}
