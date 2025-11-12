package com.appbuildchat.instaxr.data.repository

import com.appbuildchat.instaxr.data.model.Story
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface for Story data
 */
interface StoryRepository {
    fun getStories(): Flow<List<Story>>
    suspend fun getStoryById(storyId: String): Story?
    suspend fun markStoryAsViewed(storyId: String): Result<Unit>
    suspend fun deleteStory(storyId: String): Result<Unit>
}

/**
 * Default implementation of StoryRepository
 * TODO: Connect to actual data sources (Room database, API)
 */
class DefaultStoryRepository : StoryRepository {

    override fun getStories(): Flow<List<Story>> {
        // TODO: Implement actual data fetching from local/remote sources
        return flowOf(emptyList())
    }

    override suspend fun getStoryById(storyId: String): Story? {
        // TODO: Implement actual data fetching
        return null
    }

    override suspend fun markStoryAsViewed(storyId: String): Result<Unit> {
        // TODO: Implement mark as viewed logic
        return Result.success(Unit)
    }

    override suspend fun deleteStory(storyId: String): Result<Unit> {
        // TODO: Implement delete story logic
        return Result.success(Unit)
    }
}
