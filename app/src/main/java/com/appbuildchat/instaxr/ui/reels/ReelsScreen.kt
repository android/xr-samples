package com.appbuildchat.instaxr.ui.reels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appbuildchat.instaxr.ui.reels.components.ReelInfoPanel
import com.appbuildchat.instaxr.ui.reels.components.ReelVideoPanel

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
        modifier = modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Main content - Two panel layout
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left Panel - Video Player (Main Screen)
                ReelVideoPanel(
                    reel = uiState.currentReel,
                    hasNext = uiState.hasNext,
                    hasPrevious = uiState.hasPrevious,
                    onScrollNext = { onAction(ReelsAction.ScrollToNext) },
                    onScrollPrevious = { onAction(ReelsAction.ScrollToPrevious) },
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Right Panel - Information, Comments, and Actions
                ReelInfoPanel(
                    reel = uiState.currentReel,
                    onLikeClick = { onAction(ReelsAction.ToggleLike) },
                    onShareClick = { onAction(ReelsAction.ShareReel) },
                    onMoreClick = { onAction(ReelsAction.ShowMoreActions) },
                    onCommentLikeClick = { commentId ->
                        onAction(ReelsAction.LikeComment(commentId))
                    },
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                )
            }
        }
    }
}
