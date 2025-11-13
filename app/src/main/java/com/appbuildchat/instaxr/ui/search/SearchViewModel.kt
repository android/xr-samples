package com.appbuildchat.instaxr.ui.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbuildchat.instaxr.data.local.MockDataLoader
import com.appbuildchat.instaxr.data.model.ExploreItem
import com.appbuildchat.instaxr.ui.components.LoadDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Search/Explore feature
 * Manages infinite grid of mixed posts and reels
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Cache of all available content
    private var allPosts: List<ExploreItem.PostItem> = emptyList()
    private var allReels: List<ExploreItem.ReelItem> = emptyList()

    init {
        loadExploreContent()
    }

    /**
     * Handle user actions
     */
    fun handleAction(action: SearchAction) {
        when (action) {
            is SearchAction.SelectItem -> selectItem(action.item)
            is SearchAction.ClearSelection -> clearSelection()
            is SearchAction.LoadMore -> loadMore(action.direction)
            is SearchAction.ToggleLike -> toggleLike(action.itemId)
            is SearchAction.RotateViewport -> rotateViewport(action.angleDelta)
            is SearchAction.ScrollVertical -> scrollVertical(action.delta)
            is SearchAction.FocusItem -> focusItem(action.item)
            is SearchAction.UnfocusItem -> unfocusItem()
        }
    }

    /**
     * Load initial explore content (posts + reels mixed)
     */
    private fun loadExploreContent() {
        viewModelScope.launch {
            try {
                _uiState.value = SearchUiState.Loading

                // Try to load from mock data, fallback to hardcoded samples
                try {
                    val posts = MockDataLoader.loadPosts(context)
                    val reels = MockDataLoader.loadReels(context)

                    allPosts = posts.map { ExploreItem.PostItem(it) }
                    allReels = reels.map { ExploreItem.ReelItem(it) }
                } catch (e: Exception) {
                    // Fallback to hardcoded sample data
                    android.util.Log.e("SearchViewModel", "Failed to load mock data, using hardcoded samples", e)
                    allPosts = emptyList()
                    allReels = createHardcodedReels()
                }

                // Mix and create initial grid (30 items)
                val initialItems = if (allReels.isEmpty() && allPosts.isEmpty()) {
                    createHardcodedReels()
                } else {
                    createMixedItems(30)
                }

                _uiState.value = SearchUiState.Success(
                    exploreItems = initialItems,
                    selectedItem = null
                )
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "Error loading explore content", e)
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Create hardcoded sample reels for testing
     */
    private fun createHardcodedReels(): List<ExploreItem.ReelItem> {
        val sampleReels = (1..20).map { index ->
            ExploreItem.ReelItem(
                com.appbuildchat.instaxr.data.model.Reel(
                    id = "reel_$index",
                    userId = "user_$index",
                    username = "user$index",
                    userProfileImageUrl = null,
                    thumbnailUrl = "reel_$index.jpg",
                    videoUrl = "video_$index.mp4",
                    caption = "Sample reel #$index",
                    likeCount = (100..10000).random(),
                    commentCount = (10..500).random(),
                    viewCount = (1000..100000).random(),
                    isLiked = false,
                    isSaved = false,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return sampleReels
    }

    /**
     * Create a mixed list of posts and reels
     * Ratio: ~60% reels, ~40% posts (Instagram-like)
     */
    private fun createMixedItems(count: Int): List<ExploreItem> {
        val mixed = mutableListOf<ExploreItem>()
        var postIndex = 0
        var reelIndex = 0

        repeat(count) {
            // 60% chance for reel, 40% for post
            val useReel = kotlin.random.Random.nextFloat() < 0.6f

            if (useReel && allReels.isNotEmpty()) {
                mixed.add(allReels[reelIndex % allReels.size])
                reelIndex++
            } else if (allPosts.isNotEmpty()) {
                mixed.add(allPosts[postIndex % allPosts.size])
                postIndex++
            }
        }

        return mixed
    }

    /**
     * Select an item to view in expanded mode
     */
    private fun selectItem(item: ExploreItem) {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                currentState.copy(selectedItem = item)
            } else {
                currentState
            }
        }
    }

    /**
     * Clear selection and return to grid view
     */
    private fun clearSelection() {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                currentState.copy(selectedItem = null)
            } else {
                currentState
            }
        }
    }

    /**
     * Load more content when scrolling reaches edges
     */
    private fun loadMore(direction: LoadDirection) {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                val additionalItems = createMixedItems(15) // Add 15 more items

                val updatedItems = when (direction) {
                    LoadDirection.TOP -> additionalItems + currentState.exploreItems
                    LoadDirection.BOTTOM -> currentState.exploreItems + additionalItems
                    LoadDirection.LEFT, LoadDirection.RIGHT -> {
                        // For horizontal loading, we add items to support cylindrical wrapping
                        currentState.exploreItems + additionalItems
                    }
                }

                currentState.copy(exploreItems = updatedItems)
            } else {
                currentState
            }
        }
    }

    /**
     * Rotate viewport for cylindrical/spherical navigation
     */
    private fun rotateViewport(angleDelta: Float) {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                val newRotation = (currentState.viewportRotation + angleDelta) % 360f
                currentState.copy(viewportRotation = newRotation)

                // Trigger loading if rotating near edges
                if (newRotation > 300f || newRotation < 60f) {
                    loadMore(if (angleDelta > 0) LoadDirection.RIGHT else LoadDirection.LEFT)
                }

                currentState.copy(viewportRotation = newRotation)
            } else {
                currentState
            }
        }
    }

    /**
     * Scroll vertically in XR space
     */
    private fun scrollVertical(delta: Float) {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                val newOffset = currentState.verticalOffset + delta
                currentState.copy(verticalOffset = newOffset)

                // Trigger loading at edges
                if (newOffset > 1000f) {
                    loadMore(LoadDirection.BOTTOM)
                } else if (newOffset < -1000f) {
                    loadMore(LoadDirection.TOP)
                }

                currentState.copy(verticalOffset = newOffset)
            } else {
                currentState
            }
        }
    }

    /**
     * Focus on an item in XR mode (zoom forward)
     */
    private fun focusItem(item: ExploreItem) {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                currentState.copy(focusedItem = item)
            } else {
                currentState
            }
        }
    }

    /**
     * Unfocus and return to grid view
     */
    private fun unfocusItem() {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                currentState.copy(focusedItem = null)
            } else {
                currentState
            }
        }
    }

    /**
     * Toggle like status for an item
     */
    private fun toggleLike(itemId: String) {
        _uiState.update { currentState ->
            if (currentState is SearchUiState.Success) {
                val updatedItems = currentState.exploreItems.map { item ->
                    when (item) {
                        is ExploreItem.PostItem -> {
                            if (item.id == itemId) {
                                val updatedPost = item.post.copy(
                                    isLiked = !item.post.isLiked,
                                    likeCount = if (item.post.isLiked)
                                        item.post.likeCount - 1
                                    else
                                        item.post.likeCount + 1
                                )
                                ExploreItem.PostItem(updatedPost)
                            } else item
                        }
                        is ExploreItem.ReelItem -> {
                            if (item.id == itemId) {
                                val updatedReel = item.reel.copy(
                                    isLiked = !item.reel.isLiked,
                                    likeCount = if (item.reel.isLiked)
                                        item.reel.likeCount - 1
                                    else
                                        item.reel.likeCount + 1
                                )
                                ExploreItem.ReelItem(updatedReel)
                            } else item
                        }
                    }
                }

                val updatedSelectedItem = currentState.selectedItem?.let { selected ->
                    updatedItems.find { it.id == selected.id }
                }

                currentState.copy(
                    exploreItems = updatedItems,
                    selectedItem = updatedSelectedItem
                )
            } else {
                currentState
            }
        }
    }
}

/**
 * UI State for Search/Explore screen
 */
sealed interface SearchUiState {
    data object Loading : SearchUiState

    data class Success(
        val exploreItems: List<ExploreItem>,
        val selectedItem: ExploreItem? = null,
        // XR-specific state
        val viewportRotation: Float = 0f, // Rotation angle in degrees
        val focusedItem: ExploreItem? = null, // Item in XR focus mode
        val verticalOffset: Float = 0f, // Vertical scroll offset
        val isFullSpaceMode: Boolean = true // Whether in Full Space Mode
    ) : SearchUiState

    data class Error(val message: String) : SearchUiState
}

/**
 * Actions for Search/Explore screen
 */
sealed interface SearchAction {
    data class SelectItem(val item: ExploreItem) : SearchAction
    data object ClearSelection : SearchAction
    data class LoadMore(val direction: LoadDirection) : SearchAction
    data class ToggleLike(val itemId: String) : SearchAction

    // New XR-specific actions
    data class RotateViewport(val angleDelta: Float) : SearchAction
    data class ScrollVertical(val delta: Float) : SearchAction
    data class FocusItem(val item: ExploreItem) : SearchAction
    data object UnfocusItem : SearchAction
}
