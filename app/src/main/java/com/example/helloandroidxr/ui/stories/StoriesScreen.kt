package com.example.helloandroidxr.ui.stories

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
 * Top-level composable for the Stories feature screen
 */
@Composable
fun StoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: StoriesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StoriesContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Internal composable for Stories screen content
 */
@Composable
internal fun StoriesContent(
    uiState: StoriesUiState,
    onAction: (StoriesAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is StoriesUiState.Loading -> {
                Text("Loading Stories...")
            }
            is StoriesUiState.Success -> {
                Text("Stories Screen - Coming Soon")
            }
            is StoriesUiState.Error -> {
                Text("Error: ${uiState.message}")
            }
        }
    }
}

/**
 * User actions for Stories screen
 */
sealed interface StoriesAction {
    data object Refresh : StoriesAction
    data class ViewStory(val storyId: String) : StoriesAction
}
