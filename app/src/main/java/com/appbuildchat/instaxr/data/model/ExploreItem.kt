package com.appbuildchat.instaxr.data.model

/**
 * Sealed interface representing items that can appear in the explore/search grid
 * Wraps both Posts and Reels for unified display
 */
sealed interface ExploreItem {
    val id: String
    val thumbnailUrl: String
    val aspectRatio: Float
    val likeCount: Int
    val commentCount: Int

    /**
     * Post type explore item (1:1 aspect ratio)
     */
    data class PostItem(
        val post: Post,
        override val aspectRatio: Float = 1f // Square posts
    ) : ExploreItem {
        override val id: String get() = post.id
        override val thumbnailUrl: String get() = post.imageUrl
        override val likeCount: Int get() = post.likeCount
        override val commentCount: Int get() = post.commentCount
    }

    /**
     * Reel type explore item (9:16 aspect ratio)
     */
    data class ReelItem(
        val reel: Reel,
        override val aspectRatio: Float = 9f / 16f // Vertical reels
    ) : ExploreItem {
        override val id: String get() = reel.id
        override val thumbnailUrl: String get() = reel.thumbnailUrl ?: reel.videoUrl
        override val likeCount: Int get() = reel.likeCount
        override val commentCount: Int get() = reel.commentCount

        val viewCount: Int get() = reel.viewCount
    }
}
