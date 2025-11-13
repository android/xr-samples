package com.appbuildchat.instaxr.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appbuildchat.instaxr.data.local.MockDataLoader
import com.appbuildchat.instaxr.data.model.Chat
import com.appbuildchat.instaxr.data.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Messages feature
 * Uses Hilt for dependency injection and activity-scoped sharing
 */
@HiltViewModel
class MessagesViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadChats()
    }

    fun handleAction(action: MessagesAction) {
        when (action) {
            is MessagesAction.Refresh -> loadChats()
            is MessagesAction.SelectChat -> selectChat(action.chatId)
            is MessagesAction.DeselectChat -> deselectChat()
            is MessagesAction.SendMessage -> sendMessage(action.message)
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            try {
                _uiState.value = MessagesUiState.Loading
                val chats = MockDataLoader.loadChats(getApplication())
                _uiState.value = MessagesUiState.Success(chats)
            } catch (e: Exception) {
                _uiState.value = MessagesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun selectChat(chatId: String) {
        android.util.Log.d("MessagesViewModel", "selectChat called with chatId=$chatId")
        val currentState = _uiState.value
        if (currentState is MessagesUiState.Success) {
            val selectedChat = currentState.chats.find { it.id == chatId }
            android.util.Log.d("MessagesViewModel", "Found chat: $selectedChat")
            
            // Load messages for this chat
            if (selectedChat != null) {
                viewModelScope.launch {
                    try {
                        val messages = MockDataLoader.loadMessages(getApplication(), selectedChat.userId)
                        _uiState.value = currentState.copy(
                            selectedChat = selectedChat,
                            messages = messages
                        )
                        android.util.Log.d("MessagesViewModel", "Loaded ${messages.size} messages")
                    } catch (e: Exception) {
                        android.util.Log.e("MessagesViewModel", "Error loading messages", e)
                    }
                }
            }
        }
    }

    private fun deselectChat() {
        val currentState = _uiState.value
        if (currentState is MessagesUiState.Success) {
            _uiState.value = currentState.copy(selectedChat = null, messages = emptyList())
        }
    }

    private fun sendMessage(messageText: String) {
        val currentState = _uiState.value
        if (currentState is MessagesUiState.Success && currentState.selectedChat != null) {
            // Create new message
            val newMessage = Message(
                id = "msg_${System.currentTimeMillis()}",
                chatId = currentState.selectedChat.id,
                senderId = "me",
                message = messageText,
                timestamp = System.currentTimeMillis(),
                isMe = true
            )

            // Add to messages list
            val updatedMessages = currentState.messages + newMessage
            _uiState.value = currentState.copy(messages = updatedMessages)
        }
    }
}

/**
 * UI State for Messages screen
 */
sealed interface MessagesUiState {
    data object Loading : MessagesUiState
    data class Success(
        val chats: List<Chat>,
        val selectedChat: Chat? = null,
        val messages: List<Message> = emptyList()
    ) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
}

/**
 * Actions for Messages screen
 */
sealed interface MessagesAction {
    data object Refresh : MessagesAction
    data class SelectChat(val chatId: String) : MessagesAction
    data object DeselectChat : MessagesAction
    data class SendMessage(val message: String) : MessagesAction
}
