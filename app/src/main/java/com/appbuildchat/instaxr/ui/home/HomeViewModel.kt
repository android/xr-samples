package com.appbuildchat.instaxr.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appbuildchat.instaxr.data.local.MockDataLoader
import com.appbuildchat.instaxr.data.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home feature
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.Refresh -> loadHomeFeed()
            is HomeAction.LikePost -> likePost(action.postId)
            is HomeAction.SelectPost -> selectPost(action.postId)
            is HomeAction.DeselectPost -> deselectPost()
        }
    }

    private fun loadHomeFeed() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                val posts = MockDataLoader.loadPosts(getApplication())
                _uiState.value = HomeUiState.Success(posts)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun likePost(postId: String) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    post.copy(
                        isLiked = !post.isLiked,
                        likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                    )
                } else {
                    post
                }
            }
            val updatedSelectedPost = if (currentState.selectedPost?.id == postId) {
                updatedPosts.find { it.id == postId }
            } else {
                currentState.selectedPost
            }
            _uiState.value = HomeUiState.Success(updatedPosts, updatedSelectedPost)
        }
    }

    private fun selectPost(postId: String) {
        android.util.Log.d("HomeViewModel", "selectPost called with postId=$postId")
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val selectedPost = currentState.posts.find { it.id == postId }
            android.util.Log.d("HomeViewModel", "Found post: $selectedPost")
            _uiState.value = currentState.copy(selectedPost = selectedPost)
            android.util.Log.d("HomeViewModel", "Updated state, selectedPost=${(_uiState.value as? HomeUiState.Success)?.selectedPost?.id}")
        }
    }

    private fun deselectPost() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(selectedPost = null)
        }
    }
}

/**
 * UI State for Home screen
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val posts: List<Post>,
        val selectedPost: Post? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
