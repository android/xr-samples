package com.appbuildchat.instaxr.data.model

/**
 * Data model representing a story
 */
data class Story(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String? = null,
    val mediaUrl: String,
    val mediaType: MediaType = MediaType.IMAGE,
    val isViewed: Boolean = false,
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
    val timestamp: Long = System.currentTimeMillis()
)

enum class MediaType {
    IMAGE,
    VIDEO
}
