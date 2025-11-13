package com.appbuildchat.instaxr.ui.search.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spherical/Cylindrical grid component for XR full space mode
 * Arranges SpatialPanels in a circular pattern around the user
 *
 * @param items List of explore items to display
 * @param onItemClick Callback when an item is clicked
 * @param viewportRotation Current rotation angle in degrees
 * @param focusedItem Currently focused item (if any)
 * @param cylinderRadius Radius of the cylinder in dp
 * @param panelCount Number of panels to arrange in circle
 * @param modifier Modifier for the component
 */
@Composable
fun SphericalExploreGrid(
    items: List<ExploreItem>,
    onItemClick: (ExploreItem) -> Unit,
    viewportRotation: Float,
    focusedItem: ExploreItem?,
    cylinderRadius: Dp = 2000.dp,
    panelCount: Int = 12,
    modifier: Modifier = Modifier
) {
    // Calculate angle between panels
    val angleStep = 360f / panelCount

    // Group items by panel index
    val itemsPerPanel = remember(items.size, panelCount) {
        (items.size / panelCount).coerceAtLeast(6) // At least 6 items per panel
    }

    Subspace {
        SpatialRow {
            // Create panels arranged in a circle
            for (panelIndex in 0 until panelCount) {
                // Calculate angle for this panel
                val baseAngle = panelIndex * angleStep
                val angle = baseAngle - viewportRotation

                // Normalize angle to -180 to 180
                val normalizedAngle = ((angle + 180f) % 360f) - 180f

                // Calculate 3D position (cylindrical coordinates)
                val angleRad = Math.toRadians(normalizedAngle.toDouble())
                val x = (cylinderRadius.value * sin(angleRad)).toFloat()
                val z = (cylinderRadius.value * cos(angleRad)).toFloat()

                // Calculate alpha based on angle (fade out panels behind user)
                val panelAlpha by animateFloatAsState(
                    targetValue = when {
                        normalizedAngle > -70f && normalizedAngle < 70f -> 1f // Visible panels
                        normalizedAngle > -120f && normalizedAngle < 120f -> 0.3f // Partially visible
                        else -> 0f // Hidden
                    },
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "panelAlpha"
                )

                // Only render visible panels
                if (panelAlpha > 0f) {
                    // Get items for this panel
                    val startIndex = panelIndex * itemsPerPanel
                    val endIndex = (startIndex + itemsPerPanel).coerceAtMost(items.size)
                    val panelItems = if (startIndex < items.size) {
                        items.subList(startIndex, endIndex)
                    } else {
                        emptyList()
                    }

                    // Calculate depth offset for focused item
                    val hasFocusedItem = panelItems.any { it.id == focusedItem?.id }
                    val depthOffset by animateDpAsState(
                        targetValue = if (hasFocusedItem) (-500).dp else 0.dp,
                        animationSpec = spring(dampingRatio = 0.7f),
                        label = "depthOffset"
                    )

                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(350.dp)
                            .height(900.dp)
                            .offset(
                                x = x.dp,
                                y = 0.dp,
                                z = z.dp + depthOffset
                            )
                            .alpha(panelAlpha),
                        dragPolicy = MovePolicy(isEnabled = true),
                        resizePolicy = ResizePolicy(isEnabled = false)
                    ) {
                        SphericalPanelContent(
                            items = panelItems,
                            onItemClick = onItemClick,
                            focusedItem = focusedItem
                        )
                    }
                }
            }
        }
    }
}

/**
 * Content for a single spherical panel
 */
@Composable
private fun SphericalPanelContent(
    items: List<ExploreItem>,
    onItemClick: (ExploreItem) -> Unit,
    focusedItem: ExploreItem?
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            ExploreGridItem(
                item = item,
                onClick = { onItemClick(item) },
                isFocused = item.id == focusedItem?.id
            )
        }
    }
}

/**
 * Single item in the spherical grid
 */
@Composable
private fun ExploreGridItem(
    item: ExploreItem,
    onClick: () -> Unit,
    isFocused: Boolean
) {
    val context = LocalContext.current
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "itemScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isFocused) 16.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "itemElevation"
    )

    val resourceId = context.resources.getIdentifier(
        item.thumbnailUrl.replace(".jpg", "").replace(".png", ""),
        "drawable",
        context.packageName
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation.toPx()
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
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
                    .aspectRatio(item.aspectRatio)
            )
        }
    }
}
