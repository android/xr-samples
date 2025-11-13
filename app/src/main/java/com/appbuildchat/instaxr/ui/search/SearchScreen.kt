package com.appbuildchat.instaxr.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.Dp
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
import androidx.xr.compose.subspace.layout.alpha
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.layout.offset
import com.appbuildchat.instaxr.data.model.ExploreItem
import com.appbuildchat.instaxr.ui.components.CompactInfiniteGrid
import com.appbuildchat.instaxr.ui.components.InfiniteGrid
import com.appbuildchat.instaxr.ui.components.LoadDirection
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
 * Full Space Spherical View: 3 large curved panels for immersive explore experience
 * Mimics Instagram explore grid with vertical levels
 *
 * Layout:
 * - Upper panel: +35° elevation, 600dp height
 * - Middle panel: 0° elevation (eye level), 900dp height
 * - Lower panel: -35° elevation, 600dp height
 */
@Composable
private fun FullSpaceSphericalView(
    exploreItems: List<ExploreItem>,
    viewportRotation: Float,
    focusedItem: ExploreItem?,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("SearchScreen", "FullSpaceSphericalView with ${exploreItems.size} items - 3 large curved panels")

    // 3 large curved panels configuration
    val panels = listOf(
        CurvedPanelConfig(name = "Upper", elevation = 35f, height = 600.dp, width = 2400.dp),
        CurvedPanelConfig(name = "Middle", elevation = 0f, height = 900.dp, width = 2400.dp),
        CurvedPanelConfig(name = "Lower", elevation = -35f, height = 600.dp, width = 2400.dp)
    )

    val distanceFromUser = 1200.dp // Distance in front of user

    // Divide items across 3 panels
    val itemsPerPanel = (exploreItems.size / 3).coerceAtLeast(10)

    panels.forEachIndexed { index, panelConfig ->
        // Calculate Y position based on elevation
        val elevationRad = Math.toRadians(panelConfig.elevation.toDouble())
        val yOffset = (distanceFromUser.value * kotlin.math.sin(elevationRad)).toFloat()
        val zOffset = -(distanceFromUser.value * kotlin.math.cos(elevationRad)).toFloat() // Negative Z = in front

        // Get items for this panel
        val startIndex = index * itemsPerPanel
        val endIndex = ((index + 1) * itemsPerPanel).coerceAtMost(exploreItems.size)
        val panelItems = if (startIndex < exploreItems.size) {
            exploreItems.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        android.util.Log.d("SearchScreen", "${panelConfig.name} panel: ${panelItems.size} items, Y=$yOffset, Z=$zOffset")

        SpatialPanel(
            modifier = SubspaceModifier
                .width(panelConfig.width)
                .height(panelConfig.height)
                .offset(
                    x = 0.dp,
                    y = yOffset.dp,
                    z = zOffset.dp
                ),
            dragPolicy = MovePolicy(isEnabled = true),
            resizePolicy = ResizePolicy(isEnabled = true)
        ) {
            CurvedPanelContent(
                items = panelItems,
                onItemClick = { item -> onAction(SearchAction.FocusItem(item)) },
                onLoadMore = { direction -> onAction(SearchAction.LoadMore(direction)) },
                panelName = panelConfig.name
            )
        }
    }
}

/**
 * Configuration for a single curved panel
 */
private data class CurvedPanelConfig(
    val name: String,       // Panel name (Upper/Middle/Lower)
    val elevation: Float,   // Elevation angle in degrees (+up, -down)
    val height: Dp,         // Panel height
    val width: Dp           // Panel width
)

/**
 * Content for a single curved panel with InfiniteGrid
 */
@Composable
private fun CurvedPanelContent(
    items: List<ExploreItem>,
    onItemClick: (ExploreItem) -> Unit,
    onLoadMore: (LoadDirection) -> Unit,
    panelName: String
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Explore - $panelName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Infinite grid of explore items
            if (items.isNotEmpty()) {
                val gridState = rememberLazyGridState()
                InfiniteGrid(
                    items = items,
                    onLoadMore = onLoadMore,
                    state = gridState,
                    modifier = Modifier.fillMaxSize()
                ) { item ->
                    ExploreGridItem(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items in $panelName panel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
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
