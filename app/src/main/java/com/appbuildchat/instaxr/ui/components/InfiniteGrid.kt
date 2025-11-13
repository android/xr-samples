package com.appbuildchat.instaxr.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Direction for loading more content in infinite grid
 * Extended to support omnidirectional scrolling for XR spherical grid
 */
enum class LoadDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

/**
 * A grid component that supports infinite bidirectional scrolling
 * Loads more content when user reaches top or bottom edges
 *
 * @param items List of items to display
 * @param columns Number of columns in the grid
 * @param onLoadMore Callback triggered when more content needs to be loaded
 * @param modifier Modifier for the grid
 * @param contentPadding Padding for grid content
 * @param state LazyGridState for controlling scroll position
 * @param content Composable lambda for rendering each item
 */
@Composable
fun <T> InfiniteGrid(
    items: List<T>,
    columns: Int = 3,
    onLoadMore: (LoadDirection) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    state: LazyGridState = rememberLazyGridState(),
    content: @Composable (T) -> Unit
) {
    // Determine when to load more content
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0

            // Load more at bottom when reaching last 6 items (2 rows of 3)
            val shouldLoadBottom = lastVisibleItemIndex >= totalItemsCount - 6 && totalItemsCount > 0

            // Load more at top when reaching first 6 items
            val shouldLoadTop = firstVisibleItemIndex <= 5 && totalItemsCount > 0

            when {
                shouldLoadBottom -> LoadDirection.BOTTOM
                shouldLoadTop -> LoadDirection.TOP
                else -> null
            }
        }
    }

    // Trigger load more when scroll position changes
    LaunchedEffect(state) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { direction ->
                direction?.let { onLoadMore(it) }
            }
    }

    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = state,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                count = items.size,
                key = { index -> items.getOrNull(index)?.hashCode() ?: index }
            ) { index ->
                items.getOrNull(index)?.let { item ->
                    content(item)
                }
            }
        }
    }
}

/**
 * Compact version of InfiniteGrid with 2 columns for side panels
 */
@Composable
fun <T> CompactInfiniteGrid(
    items: List<T>,
    onLoadMore: (LoadDirection) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(2.dp),
    state: LazyGridState = rememberLazyGridState(),
    content: @Composable (T) -> Unit
) {
    InfiniteGrid(
        items = items,
        columns = 2,
        onLoadMore = onLoadMore,
        modifier = modifier,
        contentPadding = contentPadding,
        state = state,
        content = content
    )
}
