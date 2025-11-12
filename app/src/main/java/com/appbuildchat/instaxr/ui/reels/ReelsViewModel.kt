package com.appbuildchat.instaxr.ui.reels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Reels feature
 */
class ReelsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ReelsUiState>(ReelsUiState.Loading)
    val uiState: StateFlow<ReelsUiState> = _uiState.asStateFlow()

    init {
        loadReels()
    }

    fun handleAction(action: ReelsAction) {
        when (action) {
            is ReelsAction.Refresh -> loadReels()
            is ReelsAction.PlayReel -> playReel(action.reelId)
        }
    }

    private fun loadReels() {
        viewModelScope.launch {
            try {
                _uiState.value = ReelsUiState.Loading
                // TODO: Load reels from repository
                _uiState.value = ReelsUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReelsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun playReel(reelId: String) {
        // TODO: Implement play reel logic
    }
}

/**
 * UI State for Reels screen
 */
sealed interface ReelsUiState {
    data object Loading : ReelsUiState
    data object Success : ReelsUiState
    data class Error(val message: String) : ReelsUiState
}
