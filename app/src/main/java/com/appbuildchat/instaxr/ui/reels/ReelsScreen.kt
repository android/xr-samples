package com.appbuildchat.instaxr.ui.reels

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
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

/**
 * Spatial content for XR mode with two floating panels:
 * 1. Main panel (center): Video player - displays only the video, scrollable vertically
 * 2. Right panel: Information (author, date, description) + Comments + Action buttons
 *
 * IMPORTANT: Wraps everything in Subspace to create spatial context
 */
@SuppressLint("RestrictedApi")
@Composable
fun ReelsSpatialContent(
    uiState: ReelsUiState,
    onAction: (ReelsAction) -> Unit
) {
    // CRITICAL: Wrap in Subspace to create spatial context for SpatialPanels
    Subspace {
        if (uiState.isLoading) {
            // Show loading in a simple spatial panel
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(800.dp)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        } else if (uiState.error != null) {
            // Show error in a simple spatial panel
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(800.dp)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            // Main content - Two floating spatial panels with spacing
            SpatialRow {
                // Main Panel (Center) - Video Player
                // This is the primary focus, displaying only the video
                // Vertically scrollable to navigate between reels
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(800.dp)
                        .height(1000.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface {
                        ReelVideoPanel(
                            reel = uiState.currentReel,
                            hasNext = uiState.hasNext,
                            hasPrevious = uiState.hasPrevious,
                            onScrollNext = { onAction(ReelsAction.ScrollToNext) },
                            onScrollPrevious = { onAction(ReelsAction.ScrollToPrevious) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Right Panel - Information, Comments, and Actions
                // Split into three sections from top to bottom:
                // 1. Reel info (author, date, description)
                // 2. Comments section (scrollable list)
                // 3. Action buttons (Like, Share, More)
                // Offset adds spacing and slight angle for better viewing
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(450.dp)
                        .height(1000.dp)
                        .offset(x = 50.dp, y = 0.dp, z = -30.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = false)
                ) {
                    Surface {
                        ReelInfoPanel(
                            reel = uiState.currentReel,
                            onLikeClick = { onAction(ReelsAction.ToggleLike) },
                            onShareClick = { onAction(ReelsAction.ShareReel) },
                            onMoreClick = { onAction(ReelsAction.ShowMoreActions) },
                            onCommentLikeClick = { commentId ->
                                onAction(ReelsAction.LikeComment(commentId))
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
