package com.appbuildchat.instaxr.data.local

import android.content.Context
import com.appbuildchat.instaxr.data.model.Chat
import com.appbuildchat.instaxr.data.model.Comment
import com.appbuildchat.instaxr.data.model.LastMessage
import com.appbuildchat.instaxr.data.model.Message
import com.appbuildchat.instaxr.data.model.Post
import com.appbuildchat.instaxr.data.model.Reel
import com.appbuildchat.instaxr.data.model.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Data classes for JSON deserialization
 */
@Serializable
data class MockUser(
    val id: String,
    val username: String,
    val displayName: String,
    val profileImage: String,
    val bio: String,
    val followers: Int,
    val following: Int
)

@Serializable
data class MockPost(
    val id: String,
    val userId: String,
    val imageUrl: String,
    val description: String,
    val likes: Int,
    val comments: Int,
    val timestamp: String,
    val location: String? = null
)

@Serializable
data class MockChat(
    val id: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val profileImage: String,
    val lastMessage: MockLastMessage,
    val unreadCount: Int = 0
)

@Serializable
data class MockLastMessage(
    val sender: String,
    val message: String,
    val timestamp: Long
)

@Serializable
data class MockMessage(
    val id: String,
    val chatId: String,
    val senderId: String,
    val message: String,
    val timestamp: Long,
    val isMe: Boolean
)

@Serializable
data class MockReel(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageUrl: String?,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val caption: String?,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean,
    val timestamp: Long
)

/**
 * Utility class to load mock data from JSON files in assets
 */
object MockDataLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Load mock users from JSON file
     */
    fun loadUsers(context: Context): List<User> {
        return try {
            val jsonString = context.assets.open("mock_data/users.json")
                .bufferedReader()
                .use { it.readText() }

            val mockUsers = json.decodeFromString<List<MockUser>>(jsonString)

            mockUsers.map { mockUser ->
                User(
                    id = mockUser.id,
                    username = mockUser.username,
                    displayName = mockUser.displayName,
                    profileImageUrl = mockUser.profileImage,
                    bio = mockUser.bio,
                    followerCount = mockUser.followers,
                    followingCount = mockUser.following,
                    postCount = 0,
                    isVerified = false,
                    isFollowing = false
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Load mock posts from JSON file
     */
    fun loadPosts(context: Context): List<Post> {
        return try {
            val jsonString = context.assets.open("mock_data/posts.json")
                .bufferedReader()
                .use { it.readText() }

            val mockPosts = json.decodeFromString<List<MockPost>>(jsonString)
            val users = loadUsers(context).associateBy { it.id }

            mockPosts.map { mockPost ->
                val user = users[mockPost.userId]
                Post(
                    id = mockPost.id,
                    userId = mockPost.userId,
                    username = user?.username ?: "unknown",
                    userProfileImageUrl = user?.profileImageUrl,
                    imageUrl = mockPost.imageUrl,
                    caption = mockPost.description,
                    likeCount = mockPost.likes,
                    commentCount = mockPost.comments,
                    isLiked = false,
                    isSaved = false,
                    timestamp = parseTimestamp(mockPost.timestamp),
                    comments = generateMockComments(mockPost.id, users.values.toList())
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Parse ISO 8601 timestamp to milliseconds
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            // Simple parsing - convert ISO 8601 to millis
            // For production, use a proper date library
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Generate mock comments for a post
     */
    private fun generateMockComments(postId: String, users: List<User>): List<Comment> {
        if (users.isEmpty()) return emptyList()

        val commentTexts = listOf(
            "Amazing shot! 😍",
            "Love this!",
            "This is incredible!",
            "So beautiful! 🔥",
            "Great work!",
            "Stunning! 💯",
            "Wow! Just wow!",
            "This is perfect!",
            "Absolutely gorgeous!",
            "Can't stop looking at this! ❤️"
        )

        return List(kotlin.random.Random.nextInt(2, 6)) { index ->
            val randomUser = users.random()
            Comment(
                id = "${postId}_comment_$index",
                postId = postId,
                userId = randomUser.id,
                username = randomUser.username,
                userProfileImageUrl = randomUser.profileImageUrl,
                text = commentTexts.random(),
                likeCount = kotlin.random.Random.nextInt(0, 50),
                isLiked = false,
                timestamp = System.currentTimeMillis() - kotlin.random.Random.nextLong(1000000)
            )
        }
    }

    /**
     * Load mock chats from JSON file
     */
    fun loadChats(context: Context): List<Chat> {
        return try {
            val jsonString = context.assets.open("mock_data/chats.json")
                .bufferedReader()
                .use { it.readText() }

            val mockChats = json.decodeFromString<List<MockChat>>(jsonString)

            mockChats.map { mockChat ->
                Chat(
                    id = mockChat.id,
                    userId = mockChat.userId,
                    username = mockChat.username,
                    displayName = mockChat.displayName,
                    profileImage = mockChat.profileImage,
                    lastMessage = LastMessage(
                        sender = mockChat.lastMessage.sender,
                        message = mockChat.lastMessage.message,
                        timestamp = mockChat.lastMessage.timestamp
                    ),
                    unreadCount = mockChat.unreadCount
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Load mock messages for a specific chat
     */
    fun loadMessages(context: Context, userId: String): List<Message> {
        return try {
            val jsonString = context.assets.open("mock_data/chat_messages_$userId.json")
                .bufferedReader()
                .use { it.readText() }

            val mockMessages = json.decodeFromString<List<MockMessage>>(jsonString)

            mockMessages.map { mockMessage ->
                Message(
                    id = mockMessage.id,
                    chatId = mockMessage.chatId,
                    senderId = mockMessage.senderId,
                    message = mockMessage.message,
                    timestamp = mockMessage.timestamp,
                    isMe = mockMessage.isMe
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Load mock reels from JSON file
     */
    fun loadReels(context: Context): List<Reel> {
        return try {
            val jsonString = context.assets.open("mock_data/reels.json")
                .bufferedReader()
                .use { it.readText() }

            val mockReels = json.decodeFromString<List<MockReel>>(jsonString)

            mockReels.map { mockReel ->
                Reel(
                    id = mockReel.id,
                    userId = mockReel.userId,
                    username = mockReel.username,
                    userProfileImageUrl = mockReel.userProfileImageUrl,
                    videoUrl = mockReel.videoUrl,
                    thumbnailUrl = mockReel.thumbnailUrl,
                    caption = mockReel.caption,
                    likeCount = mockReel.likeCount,
                    commentCount = mockReel.commentCount,
                    viewCount = mockReel.viewCount,
                    isLiked = mockReel.isLiked,
                    isSaved = mockReel.isSaved,
                    timestamp = mockReel.timestamp
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}
