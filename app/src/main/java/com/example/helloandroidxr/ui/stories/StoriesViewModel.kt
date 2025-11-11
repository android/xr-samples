package com.example.helloandroidxr.ui.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Stories feature
 */
class StoriesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<StoriesUiState>(StoriesUiState.Loading)
    val uiState: StateFlow<StoriesUiState> = _uiState.asStateFlow()

    init {
        loadStories()
    }

    fun handleAction(action: StoriesAction) {
        when (action) {
            is StoriesAction.Refresh -> loadStories()
            is StoriesAction.ViewStory -> viewStory(action.storyId)
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            try {
                _uiState.value = StoriesUiState.Loading
                // TODO: Load stories from repository
                _uiState.value = StoriesUiState.Success
            } catch (e: Exception) {
                _uiState.value = StoriesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun viewStory(storyId: String) {
        // TODO: Implement view story logic
    }
}

/**
 * UI State for Stories screen
 */
sealed interface StoriesUiState {
    data object Loading : StoriesUiState
    data object Success : StoriesUiState
    data class Error(val message: String) : StoriesUiState
}
