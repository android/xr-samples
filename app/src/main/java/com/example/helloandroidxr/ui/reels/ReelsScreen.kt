package com.example.helloandroidxr.ui.reels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Top-level composable for the Reels feature screen
 */
@Composable
fun ReelsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReelsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReelsContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Internal composable for Reels screen content
 */
@Composable
internal fun ReelsContent(
    uiState: ReelsUiState,
    onAction: (ReelsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is ReelsUiState.Loading -> {
                Text("Loading Reels...")
            }
            is ReelsUiState.Success -> {
                Text("Reels Screen - Coming Soon")
            }
            is ReelsUiState.Error -> {
                Text("Error: ${uiState.message}")
            }
        }
    }
}

/**
 * User actions for Reels screen
 */
sealed interface ReelsAction {
    data object Refresh : ReelsAction
    data class PlayReel(val reelId: String) : ReelsAction
}
