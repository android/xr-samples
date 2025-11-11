package com.example.helloandroidxr.ui.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Top-level composable for the Messages feature screen
 */
@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MessagesContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Internal composable for Messages screen content
 */
@Composable
internal fun MessagesContent(
    uiState: MessagesUiState,
    onAction: (MessagesAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is MessagesUiState.Loading -> {
                Text("Loading Messages...")
            }
            is MessagesUiState.Success -> {
                Text("Messages Screen - Coming Soon")
            }
            is MessagesUiState.Error -> {
                Text("Error: ${uiState.message}")
            }
        }
    }
}

/**
 * User actions for Messages screen
 */
sealed interface MessagesAction {
    data object Refresh : MessagesAction
    data class OpenChat(val chatId: String) : MessagesAction
}
