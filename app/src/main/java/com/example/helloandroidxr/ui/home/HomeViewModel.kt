package com.example.helloandroidxr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home feature
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.Refresh -> loadHomeFeed()
            is HomeAction.LikePost -> likePost(action.postId)
        }
    }

    private fun loadHomeFeed() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                // TODO: Load home feed from repository
                _uiState.value = HomeUiState.Success
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun likePost(postId: String) {
        // TODO: Implement like post logic
    }
}

/**
 * UI State for Home screen
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Success : HomeUiState
    data class Error(val message: String) : HomeUiState
}
