package com.appbuildchat.instaxr.data.model

/**
 * Data model representing a message in a chat
 */
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMe: Boolean = false
)
