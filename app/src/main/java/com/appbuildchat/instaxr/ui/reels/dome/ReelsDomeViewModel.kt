package com.appbuildchat.instaxr.ui.reels.dome

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
 * ViewModel for the experimental Dome Reels feature
 * This is a separate implementation to test dome/curved carousel UX
 */
class ReelsDomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReelsDomeUiState())
    val uiState: StateFlow<ReelsDomeUiState> = _uiState.asStateFlow()

    init {
        loadReels()
    }

    fun handleAction(action: ReelsDomeAction) {
        when (action) {
            is ReelsDomeAction.Refresh -> loadReels()
            is ReelsDomeAction.ScrollLeft -> scrollLeft()
            is ReelsDomeAction.ScrollRight -> scrollRight()
            is ReelsDomeAction.SelectReel -> selectReel(action.reelId)
            is ReelsDomeAction.DeselectReel -> deselectReel()
            is ReelsDomeAction.ToggleLike -> toggleLike()
            is ReelsDomeAction.ShareReel -> shareReel()
            is ReelsDomeAction.ShowMoreActions -> showMoreActions()
            is ReelsDomeAction.LikeComment -> likeComment(action.commentId)
        }
    }

    private fun loadReels() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Generate dummy reels data (more than original for testing carousel)
                val dummyReels = generateDummyReels()

                _uiState.update {
                    it.copy(
                        reels = dummyReels,
                        carouselStartIndex = 0,
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

    private fun scrollLeft() {
        _uiState.update { state ->
            val newIndex = (state.carouselStartIndex - 1).coerceAtLeast(0)
            state.copy(carouselStartIndex = newIndex)
        }
    }

    private fun scrollRight() {
        _uiState.update { state ->
            val maxIndex = (state.reels.size - VISIBLE_THUMBNAILS).coerceAtLeast(0)
            val newIndex = (state.carouselStartIndex + 1).coerceAtMost(maxIndex)
            state.copy(carouselStartIndex = newIndex)
        }
    }

    private fun selectReel(reelId: String) {
        _uiState.update { state ->
            val selectedReel = state.reels.find { it.id == reelId }
            state.copy(selectedReel = selectedReel)
        }
    }

    private fun deselectReel() {
        _uiState.update { it.copy(selectedReel = null) }
    }

    private fun toggleLike() {
        _uiState.update { state ->
            val selectedReel = state.selectedReel ?: return@update state
            val updatedReel = selectedReel.copy(
                isLiked = !selectedReel.isLiked,
                likeCount = if (selectedReel.isLiked)
                    selectedReel.likeCount - 1
                else
                    selectedReel.likeCount + 1
            )

            val updatedReels = state.reels.map { reel ->
                if (reel.id == selectedReel.id) updatedReel else reel
            }

            state.copy(
                reels = updatedReels,
                selectedReel = updatedReel
            )
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
            val selectedReel = state.selectedReel ?: return@update state
            val updatedComments = selectedReel.comments.map { comment ->
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

            val updatedReel = selectedReel.copy(comments = updatedComments)
            val updatedReels = state.reels.map { reel ->
                if (reel.id == selectedReel.id) updatedReel else reel
            }

            state.copy(
                reels = updatedReels,
                selectedReel = updatedReel
            )
        }
    }

    private fun generateDummyReels(): List<Reel> {
        // Generate 100 dummy reels for testing the large grid
        return (1..100).map { index ->
            val reelId = "dome_reel_$index"
            Reel(
                id = reelId,
                userId = "user_$index",
                username = getUsernameForIndex(index),
                userProfileImageUrl = null,
                videoUrl = "https://example.com/video$index.mp4",
                thumbnailUrl = null,
                caption = getCaptionForIndex(index),
                likeCount = (100..5000).random(),
                commentCount = (10..200).random(),
                viewCount = (1000..100000).random(),
                isLiked = false,
                isSaved = false,
                timestamp = System.currentTimeMillis() - (index * 3600000L),
                comments = generateCommentsForReel(reelId, index)
            )
        }
    }

    private fun getUsernameForIndex(index: Int): String {
        val usernames = listOf(
            "travel_explorer", "foodie_adventures", "tech_guru",
            "fitness_motivation", "art_creative", "music_lover",
            "nature_photographer", "comedy_central", "fashion_style", "gaming_pro"
        )
        return usernames.getOrElse(index - 1) { "user_$index" }
    }

    private fun getCaptionForIndex(index: Int): String {
        val captions = listOf(
            "Amazing sunset at the beach! #sunset #nature",
            "Cooking the perfect pasta carbonara #cooking #food",
            "Latest tech gadget review #tech #review",
            "Morning workout routine #fitness #health",
            "Creating digital art #art #creative",
            "Live music performance #music #concert",
            "Wildlife photography adventure #nature #animals",
            "Funny comedy sketch #comedy #lol",
            "Fashion trends 2025 #fashion #style",
            "Epic gaming moments #gaming #esports"
        )
        return captions.getOrElse(index - 1) { "Reel #$index" }
    }

    private fun generateCommentsForReel(reelId: String, reelIndex: Int): List<Comment> {
        return (1..3).map { commentIndex ->
            Comment(
                id = "comment_${reelId}_$commentIndex",
                postId = reelId,
                userId = "commenter_$commentIndex",
                username = "user_$commentIndex",
                text = "Great content! Love this reel #$reelIndex",
                likeCount = (5..100).random(),
                isLiked = false,
                timestamp = System.currentTimeMillis() - (commentIndex * 1000000L)
            )
        }
    }

    companion object {
        const val VISIBLE_THUMBNAILS = 60 // Number of thumbnails visible at once (12 cols x 5 rows)
    }
}

/**
 * UI State for Dome Reels screen
 */
data class ReelsDomeUiState(
    val reels: List<Reel> = emptyList(),
    val carouselStartIndex: Int = 0,
    val selectedReel: Reel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMoreActions: Boolean = false
) {
    val visibleReels: List<Reel>
        get() {
            val endIndex = (carouselStartIndex + ReelsDomeViewModel.VISIBLE_THUMBNAILS)
                .coerceAtMost(reels.size)
            return reels.subList(carouselStartIndex, endIndex)
        }

    val canScrollLeft: Boolean
        get() = carouselStartIndex > 0

    val canScrollRight: Boolean
        get() = carouselStartIndex < reels.size - ReelsDomeViewModel.VISIBLE_THUMBNAILS
}

/**
 * Actions for Dome Reels screen
 */
sealed interface ReelsDomeAction {
    data object Refresh : ReelsDomeAction
    data object ScrollLeft : ReelsDomeAction
    data object ScrollRight : ReelsDomeAction
    data class SelectReel(val reelId: String) : ReelsDomeAction
    data object DeselectReel : ReelsDomeAction
    data object ToggleLike : ReelsDomeAction
    data object ShareReel : ReelsDomeAction
    data object ShowMoreActions : ReelsDomeAction
    data class LikeComment(val commentId: String) : ReelsDomeAction
}
