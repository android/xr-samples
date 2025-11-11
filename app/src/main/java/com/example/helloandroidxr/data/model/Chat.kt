package com.example.helloandroidxr.data.model

/**
 * Data model representing a chat conversation
 */
data class Chat(
    val id: String,
    val participantIds: List<String>,
    val participantNames: List<String>,
    val participantProfileImages: List<String?>,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val groupName: String? = null
)
