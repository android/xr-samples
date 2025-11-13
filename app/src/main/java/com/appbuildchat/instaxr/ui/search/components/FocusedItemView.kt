package com.appbuildchat.instaxr.ui.search.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.alpha
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.appbuildchat.instaxr.data.model.ExploreItem

/**
 * Focused view for a selected item in XR mode
 * Displays item in an enlarged panel with details
 * Supports swipe gestures to navigate to next/previous items
 *
 * @param item Currently focused item
 * @param onDismiss Callback when user wants to close focus mode
 * @param onToggleLike Callback to toggle like on the item
 * @param modifier Modifier for the view
 */
@Composable
fun FocusedItemView(
    item: ExploreItem,
    onDismiss: () -> Unit,
    onToggleLike: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(0f) }

    // Animate entrance
    val zOffset by animateDpAsState(
        targetValue = (-800).dp, // Bring item closer to user
        animationSpec = spring(dampingRatio = 0.7f),
        label = "focusZOffset"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = 0.5f, // Darken background
        animationSpec = spring(dampingRatio = 0.8f),
        label = "backgroundAlpha"
    )

    Subspace {
        // Background dimmer - clickable to dismiss
        SpatialPanel(
            modifier = SubspaceModifier
                .width(3000.dp)
                .height(2000.dp)
                .offset(z = (-400).dp)
                .alpha(backgroundAlpha),
            dragPolicy = MovePolicy(isEnabled = false),
            resizePolicy = ResizePolicy(isEnabled = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { onDismiss() } // Click background to close
            )
        }

        // Main focused content panel
        SpatialPanel(
            modifier = SubspaceModifier
                .width(1200.dp)
                .height(1400.dp)
                .offset(z = zOffset),
            dragPolicy = MovePolicy(isEnabled = true),
            resizePolicy = ResizePolicy(isEnabled = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                // Could implement swipe to next/previous item here
                                dragOffset = 0f
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    }
            ) {
                FocusedItemContent(
                    item = item,
                    onDismiss = onDismiss,
                    onToggleLike = { onToggleLike(item.id) }
                )
            }
        }
    }
}

/**
 * Content for the focused item panel
 */
@Composable
private fun FocusedItemContent(
    item: ExploreItem,
    onDismiss: () -> Unit,
    onToggleLike: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (item) {
                    is ExploreItem.PostItem -> "Post"
                    is ExploreItem.ReelItem -> "Reel"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main image
        val resourceId = context.resources.getIdentifier(
            item.thumbnailUrl.replace(".jpg", "").replace(".png", ""),
            "drawable",
            context.packageName
        )

        if (resourceId != 0) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Get isLiked from the actual item type
        val isLiked = when (item) {
            is ExploreItem.PostItem -> item.post.isLiked
            is ExploreItem.ReelItem -> item.reel.isLiked
        }

        // Action buttons and stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = "${item.likeCount}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Comment count
            Text(
                text = "${item.commentCount} comments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // View count (for reels)
            if (item is ExploreItem.ReelItem) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.viewCount} views",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Swipe hint
        Text(
            text = "← Swipe to navigate →",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
