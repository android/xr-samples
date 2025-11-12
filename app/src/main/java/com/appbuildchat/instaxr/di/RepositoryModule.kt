package com.appbuildchat.instaxr.di

import com.appbuildchat.instaxr.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: DefaultUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: DefaultPostRepository
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindStoryRepository(
        impl: DefaultStoryRepository
    ): StoryRepository

    @Binds
    @Singleton
    abstract fun bindReelRepository(
        impl: DefaultReelRepository
    ): ReelRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: DefaultMessageRepository
    ): MessageRepository
}
