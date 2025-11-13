package com.appbuildchat.instaxr.data.model

/**
 * Data model representing a chat conversation
 */
data class Chat(
    val id: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val profileImage: String,
    val lastMessage: LastMessage,
    val unreadCount: Int = 0
)

/**
 * Data model representing the last message in a chat
 */
data class LastMessage(
    val sender: String, // "me" or username
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
