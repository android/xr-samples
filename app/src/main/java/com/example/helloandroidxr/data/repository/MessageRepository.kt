package com.example.helloandroidxr.data.repository

import com.example.helloandroidxr.data.model.Chat
import com.example.helloandroidxr.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface for Message and Chat data
 */
interface MessageRepository {
    fun getChats(): Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, content: String): Result<Message>
    suspend fun markMessageAsRead(messageId: String): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}

/**
 * Default implementation of MessageRepository
 * TODO: Connect to actual data sources (Room database, API)
 */
class DefaultMessageRepository : MessageRepository {

    override fun getChats(): Flow<List<Chat>> {
        // TODO: Implement actual data fetching from local/remote sources
        return flowOf(emptyList())
    }

    override fun getMessages(chatId: String): Flow<List<Message>> {
        // TODO: Implement actual data fetching
        return flowOf(emptyList())
    }

    override suspend fun sendMessage(chatId: String, content: String): Result<Message> {
        // TODO: Implement send message logic
        return Result.failure(NotImplementedError("Not yet implemented"))
    }

    override suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        // TODO: Implement mark as read logic
        return Result.success(Unit)
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        // TODO: Implement delete message logic
        return Result.success(Unit)
    }
}
