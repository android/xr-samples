package com.appbuildchat.instaxr.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.layout.offset
import com.appbuildchat.instaxr.data.model.ExploreItem
import com.appbuildchat.instaxr.ui.components.CompactInfiniteGrid
import com.appbuildchat.instaxr.ui.components.InfiniteGrid
import com.appbuildchat.instaxr.ui.components.SearchBar
import com.appbuildchat.instaxr.ui.search.components.ExploreGridItem
import com.appbuildchat.instaxr.ui.search.components.SphericalExploreGrid
import com.appbuildchat.instaxr.ui.search.components.FocusedItemView
import com.appbuildchat.instaxr.ui.search.xrGestures

/**
 * Top-level composable for the Search/Explore feature screen
 * Uses activity-scoped ViewModel shared with InstaXRApp
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier
) {
    // Get activity-scoped ViewModel (shared with InstaXRApp)
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val viewModel: SearchViewModel = androidx.hilt.navigation.compose.hiltViewModel(
        viewModelStoreOwner = activity ?: error("Activity required")
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spatialCapabilities = LocalSpatialCapabilities.current

    // Check if we're in XR spatial mode
    val isSpatialMode = spatialCapabilities.isSpatialUiEnabled

    if (isSpatialMode) {
        SearchSpatialContent(
            uiState = uiState,
            onAction = viewModel::handleAction,
            modifier = modifier
        )
    } else {
        SearchContent(
            uiState = uiState,
            onAction = viewModel::handleAction,
            modifier = modifier
        )
    }
}

/**
 * 2D Mode: Standard screen content with grid
 */
@Composable
internal fun SearchContent(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is SearchUiState.Loading -> {
                CircularProgressIndicator()
            }
            is SearchUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Decorative search bar
                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    // Grid of explore items
                    val gridState = rememberLazyGridState()
                    InfiniteGrid(
                        items = uiState.exploreItems,
                        onLoadMore = { direction ->
                            onAction(SearchAction.LoadMore(direction))
                        },
                        state = gridState,
                        modifier = Modifier.fillMaxSize()
                    ) { item ->
                        ExploreGridItem(
                            item = item,
                            onClick = { onAction(SearchAction.SelectItem(item)) }
                        )
                    }
                }
            }
            is SearchUiState.Error -> {
                Text("Error: ${uiState.message}")
            }
        }
    }
}

/**
 * Full Space Mode content rendered directly in ApplicationSubspace from InstaXRApp
 * Similar to HomeScreenSpatialPanelsAnimated
 */
@Composable
fun SearchFullSpaceContent(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val spatialConfiguration = LocalSpatialConfiguration.current

    // Request Full Space Mode
    androidx.compose.runtime.LaunchedEffect(Unit) {
        android.util.Log.d("SearchScreen", "SearchFullSpaceContent: Requesting Full Space Mode")
        spatialConfiguration.requestFullSpaceMode()
    }

    when (uiState) {
        is SearchUiState.Loading -> {
            // Show loading in a simple spatial panel
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(600.dp)
                    .height(400.dp),
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
        }
        is SearchUiState.Success -> {
            val hasFocusedItem = uiState.focusedItem != null

            // Render Full Space content
            when {
                hasFocusedItem -> {
                    FullSpaceExploreView(
                        exploreItems = uiState.exploreItems,
                        focusedItem = uiState.focusedItem!!,
                        viewportRotation = uiState.viewportRotation,
                        onAction = onAction,
                        modifier = modifier
                    )
                }
                else -> {
                    FullSpaceSphericalView(
                        exploreItems = uiState.exploreItems,
                        viewportRotation = uiState.viewportRotation,
                        focusedItem = uiState.focusedItem,
                        onAction = onAction,
                        modifier = modifier
                    )
                }
            }
        }
        is SearchUiState.Error -> {
            // Show error in a simple spatial panel
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(600.dp)
                    .height(400.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${uiState.message}")
                    }
                }
            }
        }
    }
}

/**
 * XR Spatial Mode: Floating panels with expand/collapse states OR full space spherical mode
 */
@Composable
internal fun SearchSpatialContent(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
    enableFullSpaceMode: Boolean = true // Re-enabled with simplified version
) {
    val spatialConfiguration = LocalSpatialConfiguration.current

    // Request Full Space Mode when enabled
    androidx.compose.runtime.LaunchedEffect(enableFullSpaceMode) {
        if (enableFullSpaceMode) {
            android.util.Log.d("SearchScreen", "Requesting Full Space Mode")
            spatialConfiguration.requestFullSpaceMode()
        }
    }

    when (uiState) {
        is SearchUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is SearchUiState.Success -> {
            val isExpanded = uiState.selectedItem != null
            val hasFocusedItem = uiState.focusedItem != null

            // CRITICAL FIX: Only wrap in Subspace when NOT in Full Space Mode
            // Full Space Mode renders directly in ApplicationSubspace from InstaXRApp
            if (enableFullSpaceMode) {
                // Full Space Mode: Render directly without additional Subspace wrapper
                android.util.Log.d("SearchScreen", "Rendering in Full Space Mode (no Subspace)")
                when {
                    hasFocusedItem -> {
                        FullSpaceExploreView(
                            exploreItems = uiState.exploreItems,
                            focusedItem = uiState.focusedItem!!,
                            viewportRotation = uiState.viewportRotation,
                            onAction = onAction,
                            modifier = modifier
                        )
                    }
                    else -> {
                        FullSpaceSphericalView(
                            exploreItems = uiState.exploreItems,
                            viewportRotation = uiState.viewportRotation,
                            focusedItem = uiState.focusedItem,
                            onAction = onAction,
                            modifier = modifier
                        )
                    }
                }
            } else {
                // Home Space Mode: Wrap in Subspace as normal
                Subspace {
                    when {
                        // Traditional mode: 3-panel expanded layout
                        isExpanded -> {
                            ExpandedExploreView(
                                exploreItems = uiState.exploreItems,
                                selectedItem = uiState.selectedItem!!,
                                onAction = onAction,
                                modifier = modifier
                            )
                        }
                        // Traditional mode: Single panel collapsed
                        else -> {
                            CollapsedExploreView(
                                exploreItems = uiState.exploreItems,
                                onAction = onAction,
                                modifier = modifier
                            )
                        }
                    }
                }
            }
        }
        is SearchUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${uiState.message}")
            }
        }
    }
}

/**
 * Collapsed state: Single panel with explore grid
 */
@Composable
private fun CollapsedExploreView(
    exploreItems: List<ExploreItem>,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("SearchScreen", "CollapsedExploreView rendering with ${exploreItems.size} items")

    SpatialPanel(
        modifier = SubspaceModifier
            .width(800.dp)
            .height(900.dp),
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = true)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Debug text
                Text(
                    text = "Search Screen - ${exploreItems.size} items",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Decorative search bar
                SearchBar(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                // Infinite grid of explore items
                if (exploreItems.isNotEmpty()) {
                    val gridState = rememberLazyGridState()
                    InfiniteGrid(
                        items = exploreItems,
                        onLoadMore = { direction ->
                            onAction(SearchAction.LoadMore(direction))
                        },
                        state = gridState,
                        modifier = Modifier.fillMaxSize()
                    ) { item ->
                        ExploreGridItem(
                            item = item,
                            onClick = { onAction(SearchAction.SelectItem(item)) }
                        )
                    }
                } else {
                    Text(
                        text = "No items to display",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Expanded state: 3-panel layout (compact grid, large preview, details)
 */
@Composable
private fun ExpandedExploreView(
    exploreItems: List<ExploreItem>,
    selectedItem: ExploreItem,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val compactPanelWidth by animateDpAsState(targetValue = 300.dp, label = "compactPanelWidth")
    val centerPanelWidth by animateDpAsState(targetValue = 900.dp, label = "centerPanelWidth")
    val detailPanelWidth by animateDpAsState(targetValue = 400.dp, label = "detailPanelWidth")

    SpatialRow {
        // Left: Compact grid
        AnimatedVisibility(visible = true) {
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(compactPanelWidth)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = false)
            ) {
                Surface {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // Back button
                        IconButton(onClick = { onAction(SearchAction.ClearSelection) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to grid"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Compact 2-column grid
                        val compactGridState = rememberLazyGridState()
                        CompactInfiniteGrid(
                            items = exploreItems,
                            onLoadMore = { direction ->
                                onAction(SearchAction.LoadMore(direction))
                            },
                            state = compactGridState,
                            modifier = Modifier.fillMaxSize()
                        ) { item ->
                            ExploreGridItem(
                                item = item,
                                onClick = { onAction(SearchAction.SelectItem(item)) }
                            )
                        }
                    }
                }
            }
        }

        // Center: Large image preview
        AnimatedVisibility(visible = true) {
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(centerPanelWidth)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    LargeItemPreview(
                        item = selectedItem,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Right: Details panel
        AnimatedVisibility(visible = true) {
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(detailPanelWidth)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = false)
            ) {
                Surface {
                    ItemDetailsPanel(
                        item = selectedItem,
                        onAction = onAction,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Large preview of selected item (center panel)
 */
@Composable
private fun LargeItemPreview(
    item: ExploreItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        item.thumbnailUrl.replace(".jpg", "").replace(".png", ""),
        "drawable",
        context.packageName
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (resourceId != 0) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("Image not found", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/**
 * Details panel for selected item (right panel)
 */
@Composable
private fun ItemDetailsPanel(
    item: ExploreItem,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User info
        when (item) {
            is ExploreItem.PostItem -> {
                Text(
                    text = item.post.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                item.post.caption?.let { caption ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is ExploreItem.ReelItem -> {
                Text(
                    text = item.reel.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                item.reel.caption?.let { caption ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLiked = when (item) {
                is ExploreItem.PostItem -> item.post.isLiked
                is ExploreItem.ReelItem -> item.reel.isLiked
            }

            IconButton(onClick = { onAction(SearchAction.ToggleLike(item.id)) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${item.likeCount} likes",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        if (item is ExploreItem.ReelItem) {
            Text(
                text = "${item.viewCount} views",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${item.commentCount} comments",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Full Space Spherical View: Cylindrical grid arranged around the user
 * Supports omnidirectional scrolling and gesture navigation
 */
@Composable
private fun FullSpaceSphericalView(
    exploreItems: List<ExploreItem>,
    viewportRotation: Float,
    focusedItem: ExploreItem?,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("SearchScreen", "FullSpaceSphericalView with ${exploreItems.size} items")

    // Full Space Mode: Larger panel, minimal offset
    // In Full Space, coordinate system is relative to user's head position
    SpatialPanel(
        modifier = SubspaceModifier
            .width(800.dp)
            .height(600.dp),
            // No offset - render at origin (directly in front of user)
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = true)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = "✨ FULL SPACE MODE ✨",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "${exploreItems.size} items loaded",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "✅ Panel is rendering!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No Subspace wrapper",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Size: 800x600dp | Offset: None",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Full Space Explore View: Spherical grid with focused item overlay
 */
@Composable
private fun FullSpaceExploreView(
    exploreItems: List<ExploreItem>,
    focusedItem: ExploreItem,
    viewportRotation: Float,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Render both the background grid and focused item
    // They're both in the same Subspace context
    SphericalExploreGrid(
        items = exploreItems,
        onItemClick = { item ->
            onAction(SearchAction.FocusItem(item))
        },
        viewportRotation = viewportRotation,
        focusedItem = focusedItem,
        cylinderRadius = 2000.dp,
        panelCount = 12
    )

    // Focused item overlay
    FocusedItemView(
        item = focusedItem,
        onDismiss = {
            onAction(SearchAction.UnfocusItem)
        },
        onToggleLike = { itemId ->
            onAction(SearchAction.ToggleLike(itemId))
        }
    )
}
