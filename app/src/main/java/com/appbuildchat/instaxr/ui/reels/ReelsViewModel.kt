package com.appbuildchat.instaxr.ui.reels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbuildchat.instaxr.data.model.Comment
import com.appbuildchat.instaxr.data.model.Reel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Reels feature
 */
class ReelsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReelsUiState())
    val uiState: StateFlow<ReelsUiState> = _uiState.asStateFlow()

    init {
        loadReels()
    }

    fun handleAction(action: ReelsAction) {
        when (action) {
            is ReelsAction.Refresh -> loadReels()
            is ReelsAction.ScrollToNext -> scrollToNext()
            is ReelsAction.ScrollToPrevious -> scrollToPrevious()
            is ReelsAction.ToggleLike -> toggleLike()
            is ReelsAction.ShareReel -> shareReel()
            is ReelsAction.ShowMoreActions -> showMoreActions()
            is ReelsAction.LikeComment -> likeComment(action.commentId)
        }
    }

    private fun loadReels() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Generate dummy reels data
                val dummyReels = generateDummyReels()

                _uiState.update {
                    it.copy(
                        reels = dummyReels,
                        currentReelIndex = 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun scrollToNext() {
        _uiState.update { state ->
            val nextIndex = (state.currentReelIndex + 1).coerceAtMost(state.reels.size - 1)
            state.copy(currentReelIndex = nextIndex)
        }
    }

    private fun scrollToPrevious() {
        _uiState.update { state ->
            val previousIndex = (state.currentReelIndex - 1).coerceAtLeast(0)
            state.copy(currentReelIndex = previousIndex)
        }
    }

    private fun toggleLike() {
        _uiState.update { state ->
            val currentReel = state.currentReel ?: return@update state
            val updatedReels = state.reels.toMutableList()
            val index = state.currentReelIndex

            updatedReels[index] = currentReel.copy(
                isLiked = !currentReel.isLiked,
                likeCount = if (currentReel.isLiked)
                    currentReel.likeCount - 1
                else
                    currentReel.likeCount + 1
            )

            state.copy(reels = updatedReels)
        }
    }

    private fun shareReel() {
        // TODO: Implement share functionality
    }

    private fun showMoreActions() {
        _uiState.update { it.copy(showMoreActions = !it.showMoreActions) }
    }

    private fun likeComment(commentId: String) {
        _uiState.update { state ->
            val currentReel = state.currentReel ?: return@update state
            val updatedComments = currentReel.comments.map { comment ->
                if (comment.id == commentId) {
                    comment.copy(
                        isLiked = !comment.isLiked,
                        likeCount = if (comment.isLiked)
                            comment.likeCount - 1
                        else
                            comment.likeCount + 1
                    )
                } else {
                    comment
                }
            }

            val updatedReels = state.reels.toMutableList()
            updatedReels[state.currentReelIndex] = currentReel.copy(comments = updatedComments)

            state.copy(reels = updatedReels)
        }
    }

    private fun generateDummyReels(): List<Reel> {
        return listOf(
            Reel(
                id = "reel_1",
                userId = "user_1",
                username = "jane_doe",
                userProfileImageUrl = null,
                videoUrl = "https://example.com/video1.mp4",
                thumbnailUrl = null,
                caption = "Beautiful sunset at the beach! #sunset #nature #peaceful",
                likeCount = 1250,
                commentCount = 45,
                viewCount = 12500,
                isLiked = false,
                isSaved = false,
                timestamp = System.currentTimeMillis() - 3600000,
                comments = listOf(
                    Comment(
                        id = "comment_1",
                        postId = "reel_1",
                        userId = "user_2",
                        username = "john_smith",
                        text = "Amazing view!",
                        likeCount = 23,
                        timestamp = System.currentTimeMillis() - 3000000
                    ),
                    Comment(
                        id = "comment_2",
                        postId = "reel_1",
                        userId = "user_3",
                        username = "sarah_wilson",
                        text = "Where is this place?",
                        likeCount = 12,
                        timestamp = System.currentTimeMillis() - 2500000
                    ),
                    Comment(
                        id = "comment_3",
                        postId = "reel_1",
                        userId = "user_4",
                        username = "mike_jones",
                        text = "Absolutely stunning! I need to visit this place",
                        likeCount = 45,
                        timestamp = System.currentTimeMillis() - 2000000
                    )
                )
            ),
            Reel(
                id = "reel_2",
                userId = "user_5",
                username = "chef_marco",
                userProfileImageUrl = null,
                videoUrl = "https://example.com/video2.mp4",
                thumbnailUrl = null,
                caption = "Making the perfect pasta carbonara #cooking #pasta #italianfood",
                likeCount = 3400,
                commentCount = 128,
                viewCount = 45000,
                isLiked = true,
                isSaved = true,
                timestamp = System.currentTimeMillis() - 7200000,
                comments = listOf(
                    Comment(
                        id = "comment_4",
                        postId = "reel_2",
                        userId = "user_6",
                        username = "food_lover_99",
                        text = "This looks delicious! Can you share the recipe?",
                        likeCount = 67,
                        timestamp = System.currentTimeMillis() - 6000000
                    ),
                    Comment(
                        id = "comment_5",
                        postId = "reel_2",
                        userId = "user_7",
                        username = "italian_nonna",
                        text = "Perfect technique! Just like my grandmother used to make",
                        likeCount = 89,
                        timestamp = System.currentTimeMillis() - 5500000
                    )
                )
            ),
            Reel(
                id = "reel_3",
                userId = "user_8",
                username = "fitness_coach_alex",
                userProfileImageUrl = null,
                videoUrl = "https://example.com/video3.mp4",
                thumbnailUrl = null,
                caption = "5-minute morning workout routine #fitness #workout #motivation",
                likeCount = 5600,
                commentCount = 234,
                viewCount = 89000,
                isLiked = false,
                isSaved = true,
                timestamp = System.currentTimeMillis() - 86400000,
                comments = listOf(
                    Comment(
                        id = "comment_6",
                        postId = "reel_3",
                        userId = "user_9",
                        username = "morning_runner",
                        text = "Great routine! Doing this every day now",
                        likeCount = 156,
                        timestamp = System.currentTimeMillis() - 80000000
                    ),
                    Comment(
                        id = "comment_7",
                        postId = "reel_3",
                        userId = "user_10",
                        username = "gym_enthusiast",
                        text = "How many reps do you recommend?",
                        likeCount = 34,
                        timestamp = System.currentTimeMillis() - 75000000
                    ),
                    Comment(
                        id = "comment_8",
                        postId = "reel_3",
                        userId = "user_8",
                        username = "fitness_coach_alex",
                        text = "@gym_enthusiast Start with 10-15 reps, 3 sets!",
                        likeCount = 78,
                        timestamp = System.currentTimeMillis() - 74000000
                    )
                )
            )
        )
    }
}

/**
 * UI State for Reels screen
 */
data class ReelsUiState(
    val reels: List<Reel> = emptyList(),
    val currentReelIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMoreActions: Boolean = false
) {
    val currentReel: Reel?
        get() = reels.getOrNull(currentReelIndex)

    val hasNext: Boolean
        get() = currentReelIndex < reels.size - 1

    val hasPrevious: Boolean
        get() = currentReelIndex > 0
}

/**
 * Actions that can be performed on the Reels screen
 */
sealed interface ReelsAction {
    data object Refresh : ReelsAction
    data object ScrollToNext : ReelsAction
    data object ScrollToPrevious : ReelsAction
    data object ToggleLike : ReelsAction
    data object ShareReel : ReelsAction
    data object ShowMoreActions : ReelsAction
    data class LikeComment(val commentId: String) : ReelsAction
}
