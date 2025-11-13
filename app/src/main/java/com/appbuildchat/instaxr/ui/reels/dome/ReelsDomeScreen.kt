package com.appbuildchat.instaxr.ui.reels.dome

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.appbuildchat.instaxr.data.model.Reel
import com.appbuildchat.instaxr.ui.reels.components.ReelInfoPanel
import com.appbuildchat.instaxr.ui.reels.components.ReelVideoPanel
import kotlin.math.cos
import kotlin.math.sin

/**
 * EXPERIMENTAL: Dome-style Reels screen with curved carousel
 * This is a separate implementation to test immersive dome UX
 * Can be deleted entirely without affecting the main Reels feature
 */
@Composable
fun ReelsDomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ReelsDomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReelsDomeContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Spatial content for Dome Reels in XR mode
 */
@SuppressLint("RestrictedApi")
@Composable
fun ReelsDomeContent(
    uiState: ReelsDomeUiState,
    onAction: (ReelsDomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Subspace {
        if (uiState.isLoading) {
            // Loading state
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(800.dp)
                    .height(600.dp),
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
            // Error state
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(800.dp)
                    .height(600.dp),
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
        } else if (uiState.selectedReel != null) {
            // Expanded view: Show selected reel + info panel
            ReelExpandedView(
                uiState = uiState,
                onAction = onAction
            )
        } else {
            // Carousel view: Show curved dome of thumbnails
            ReelCarouselDome(
                uiState = uiState,
                onAction = onAction
            )
        }
    }
}

/**
 * ONE unified curved cinema screen - CONCAVE theater style
 * Center farthest, edges wrap TOWARD user like IMAX
 * Seamless vertical panels create illusion of one continuous screen
 * 12 columns x 5 rows = 60 reel cells
 */
@SuppressLint("RestrictedApi")
@Composable
fun ReelCarouselDome(
    uiState: ReelsDomeUiState,
    onAction: (ReelsDomeAction) -> Unit
) {
    val visibleReels = uiState.visibleReels

    // Large theater screen parameters
    val columns = 12
    val rows = 5
    val cellWidth = 300.dp
    val cellHeight = 400.dp

    // CONCAVE curvature parameters
    val curveRadius = 1600.dp // Distance to CENTER of screen

    // --- START FIX ---
    // Calculate the angle (in radians) that ONE panel of 300dp
    // subtends (takes up) at a 1600dp radius.
    // We use the formula: angle = 2 * arcsin( (width/2) / radius )
    val anglePerPanelRad = 2.0 * kotlin.math.asin(
        (cellWidth.value / 2.0) / curveRadius.value
    )
    // Convert to degrees for the rotation
    val anglePerPanelDeg = Math.toDegrees(anglePerPanelRad).toFloat()
    // --- END FIX ---

    // Calculate total screen height
    val screenHeight = (cellHeight.value * rows).dp

    // Create seamless vertical column panels in CONCAVE curve
    (0 until columns).forEach { col ->
        // --- START FIX ---
        // Calculate the center angle for *this* column.
        // We find the panel's "index" from the center (e.g., -5.5 to +5.5)
        // and multiply by the angle per panel.
        val centerAngleOffsetDeg = (col.toFloat() - (columns - 1) / 2f) * anglePerPanelDeg
        val angleRadians = Math.toRadians(centerAngleOffsetDeg.toDouble())
        // --- END FIX ---

        // CONCAVE curvature - CENTER farthest, edges TOWARD user
        val xPos = (curveRadius.value * sin(angleRadians)).dp
        val zPos = -(curveRadius.value * cos(angleRadians)).dp

        // --- START FIX ---
        // ADD ROTATION: This is critical.
        // Each panel must be rotated to face the user (at 0,0,0)
        // so their edges meet seamlessly.
        val yRotation = -centerAngleOffsetDeg // Negate angle to "turn towards" center

        // NOTE: The .rotation() modifier might not exist on SubspaceModifier.
        // If it doesn't, you may need to wrap this SpatialPanel in another
        // composable that *can* be rotated, or look for a different XR API.
        // This 'faceted' look without rotation is the next problem to solve.
        // For now, let's assume a rotation modifier exists for this example.
        //
        // If you CANNOT rotate, this new math will still place the panels
        // edge-to-edge, but they will be flat and create a "faceted" look
        // instead of a smooth curve. It will still be much better.
        // --- END FIX ---

        // Create seamless vertical panel
        SpatialPanel(
            modifier = SubspaceModifier
                .width(cellWidth)
                .height(screenHeight)
                .offset(x = xPos, y = 0.dp, z = zPos),
                // .rotation(y = yRotation) // <-- Add this if your API supports it
            dragPolicy = MovePolicy(isEnabled = false),
            resizePolicy = ResizePolicy(isEnabled = false)
        ) {
            // Seamless surface - NO gaps, padding, or elevation
            Surface(
                color = Color.Black.copy(alpha = 0.98f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Stack cells vertically with NO spacing
                    for (row in 0 until rows) {
                        val index = row * columns + col

                        if (index < visibleReels.size) {
                            val reel = visibleReels[index]

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                            ) {
                                // Grid cell content - NO padding
                                GridCell(
                                    reel = reel,
                                    onClick = { onAction(ReelsDomeAction.SelectReel(reel.id)) }
                                )

                                // Thin grid lines to show divisions
                                // Right border (except last column)
                                if (col < columns - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .fillMaxHeight()
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .align(Alignment.CenterEnd)
                                    )
                                }
                                // Bottom border (except last row)
                                if (row < rows - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Navigation controls - positioned at bottom center
    val navButtonY = -(screenHeight.value / 2 + 150).dp // Below the grid
    val navButtonZ = -curveRadius // Same distance as screen

    if (uiState.canScrollLeft) {
        SpatialPanel(
            modifier = SubspaceModifier
                .width(120.dp)
                .height(120.dp)
                .offset(x = -150.dp, y = navButtonY, z = navButtonZ),
            dragPolicy = MovePolicy(isEnabled = false),
            resizePolicy = ResizePolicy(isEnabled = false)
        ) {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onAction(ReelsDomeAction.ScrollLeft) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous page",
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }

    if (uiState.canScrollRight) {
        SpatialPanel(
            modifier = SubspaceModifier
                .width(120.dp)
                .height(120.dp)
                .offset(x = 150.dp, y = navButtonY, z = navButtonZ),
            dragPolicy = MovePolicy(isEnabled = false),
            resizePolicy = ResizePolicy(isEnabled = false)
        ) {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onAction(ReelsDomeAction.ScrollRight) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next page",
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Grid cell within the unified curved screen
 * Part of the seamless cinema screen aesthetic
 */
@Composable
fun GridCell(
    reel: Reel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(4.dp), // Minimal padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // Play icon
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .padding(12.dp)
                    .size(40.dp)
            )

            // Username
            Text(
                text = reel.username,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )

            // Caption preview
            Text(
                text = reel.caption ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )

            // Stats
            Text(
                text = "${formatCount(reel.viewCount)} views",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Individual reel thumbnail (kept for backward compatibility)
 */
@Composable
fun ReelThumbnail(
    reel: Reel,
    isCenter: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GridCell(reel = reel, onClick = onClick, modifier = modifier)
}

/**
 * Expanded view when a reel is selected
 * Shows video + info panel (no background rendering for performance)
 */
@SuppressLint("RestrictedApi")
@Composable
fun ReelExpandedView(
    uiState: ReelsDomeUiState,
    onAction: (ReelsDomeAction) -> Unit
) {
    // Foreground: Video + Info panel (removed background blur for performance)
    SpatialRow {
        // Video Panel
        SpatialPanel(
            modifier = SubspaceModifier
                .width(800.dp)
                .height(1000.dp)
                .offset(x = 0.dp, y = 0.dp, z = 100.dp), // Bring forward
            dragPolicy = MovePolicy(isEnabled = true),
            resizePolicy = ResizePolicy(isEnabled = true)
        ) {
            Surface {
                ReelVideoPanel(
                    reel = uiState.selectedReel,
                    hasNext = false, // Disable scrolling in dome mode
                    hasPrevious = false,
                    onScrollNext = {},
                    onScrollPrevious = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Info Panel
        SpatialPanel(
            modifier = SubspaceModifier
                .width(450.dp)
                .height(1000.dp)
                .offset(x = 50.dp, y = 0.dp, z = 70.dp), // Bring forward, slightly angled
            dragPolicy = MovePolicy(isEnabled = true),
            resizePolicy = ResizePolicy(isEnabled = false)
        ) {
            Surface {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Back button
                    IconButton(
                        onClick = { onAction(ReelsDomeAction.DeselectReel) },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to carousel"
                        )
                    }

                    // Info panel
                    ReelInfoPanel(
                        reel = uiState.selectedReel,
                        onLikeClick = { onAction(ReelsDomeAction.ToggleLike) },
                        onShareClick = { onAction(ReelsDomeAction.ShareReel) },
                        onMoreClick = { onAction(ReelsDomeAction.ShowMoreActions) },
                        onCommentLikeClick = { commentId ->
                            onAction(ReelsDomeAction.LikeComment(commentId))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * NOTE: Background blur removed for performance optimization
 * No longer rendering duplicate grid when viewing a reel
 * This saves significant rendering overhead
 */

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
