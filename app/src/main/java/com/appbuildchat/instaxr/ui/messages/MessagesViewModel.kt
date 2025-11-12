package com.appbuildchat.instaxr.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Messages feature
 */
class MessagesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    fun handleAction(action: MessagesAction) {
        when (action) {
            is MessagesAction.Refresh -> loadMessages()
            is MessagesAction.OpenChat -> openChat(action.chatId)
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                _uiState.value = MessagesUiState.Loading
                // TODO: Load messages from repository
                _uiState.value = MessagesUiState.Success
            } catch (e: Exception) {
                _uiState.value = MessagesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun openChat(chatId: String) {
        // TODO: Implement open chat logic
    }
}

/**
 * UI State for Messages screen
 */
sealed interface MessagesUiState {
    data object Loading : MessagesUiState
    data object Success : MessagesUiState
    data class Error(val message: String) : MessagesUiState
}
