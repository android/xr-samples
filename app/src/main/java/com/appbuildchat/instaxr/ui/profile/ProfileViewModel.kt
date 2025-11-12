package com.appbuildchat.instaxr.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appbuildchat.instaxr.data.local.MockDataLoader
import com.appbuildchat.instaxr.data.model.Post
import com.appbuildchat.instaxr.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile feature
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String = "user_1" // Default to sarah_travels
    private var allPosts: List<Post> = emptyList()

    init {
        loadProfile()
    }

    fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.Refresh -> loadProfile()
            is ProfileAction.EditProfile -> editProfile()
            is ProfileAction.SelectPost -> selectPost(action.post)
            is ProfileAction.DeselectPost -> deselectPost()
            is ProfileAction.ChangeTab -> changeTab(action.tab)
            is ProfileAction.ToggleFollow -> toggleFollow()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading

                val users = MockDataLoader.loadUsers(getApplication())
                val currentUser = users.firstOrNull { it.id == currentUserId }

                if (currentUser == null) {
                    _uiState.value = ProfileUiState.Error("User not found")
                    return@launch
                }

                val posts = MockDataLoader.loadPosts(getApplication())
                allPosts = posts

                // Filter posts by current user
                val userPosts = posts.filter { it.userId == currentUserId }

                _uiState.value = ProfileUiState.Success(
                    user = currentUser,
                    posts = userPosts,
                    selectedTab = ProfileTab.POSTS,
                    selectedPost = null,
                    isExpanded = false
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun editProfile() {
        // TODO: Implement edit profile logic
        // For now, just a placeholder
    }

    private fun selectPost(post: Post) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            _uiState.value = currentState.copy(
                selectedPost = post,
                isExpanded = true
            )
        }
    }

    private fun deselectPost() {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            _uiState.value = currentState.copy(
                selectedPost = null,
                isExpanded = false
            )
        }
    }

    private fun changeTab(tab: ProfileTab) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            _uiState.value = currentState.copy(
                selectedTab = tab,
                selectedPost = null,
                isExpanded = false
            )
        }
    }

    private fun toggleFollow() {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            val updatedUser = currentState.user.copy(
                isFollowing = !currentState.user.isFollowing
            )
            _uiState.value = currentState.copy(user = updatedUser)
        }
    }
}

/**
 * UI State for Profile screen
 */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val user: User,
        val posts: List<Post>,
        val selectedTab: ProfileTab,
        val selectedPost: Post?,
        val isExpanded: Boolean
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

/**
 * Actions for Profile screen
 */
sealed interface ProfileAction {
    data object Refresh : ProfileAction
    data object EditProfile : ProfileAction
    data class SelectPost(val post: Post) : ProfileAction
    data object DeselectPost : ProfileAction
    data class ChangeTab(val tab: ProfileTab) : ProfileAction
    data object ToggleFollow : ProfileAction
}

/**
 * Profile tabs
 */
enum class ProfileTab {
    POSTS,
    REELS,
    TAGGED
}
