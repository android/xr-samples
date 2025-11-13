package com.appbuildchat.instaxr.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import com.appbuildchat.instaxr.ui.icons.CommentBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.appbuildchat.instaxr.data.model.Post

/**
 * Top-level composable for the Home feature screen
 * Uses Hilt ViewModel scoped to Activity for sharing with InstaXRApp
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    // Get activity-scoped ViewModel (shared with InstaXRApp)
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val viewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(
        viewModelStoreOwner = activity ?: error("Activity required")
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Spatial content for XR mode with two states:
 * 1. Shrinked state: Single SpatialPanel with posts list (680dp width)
 * 2. Expanded state: Three separate SpatialPanels (compact list, image, comments)
 *
 * When transitioning from shrinked to expanded, the single panel smoothly animates
 * to become the compact list panel with animated width shrinking.
 *
 * IMPORTANT: Wraps everything in Subspace to create spatial context
 */
@SuppressLint("RestrictedApi")
@Composable
internal fun HomeSpatialContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit
) {
    // CRITICAL: Wrap in Subspace to create spatial context for SpatialPanels
    Subspace {
        when (uiState) {
            is HomeUiState.Loading -> {
                // Show loading in a simple spatial panel
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(680.dp)
                        .height(800.dp),
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
            is HomeUiState.Success -> {
                if (uiState.selectedPost != null) {
                    // EXPANDED STATE: Three separate spatial panels with animation
                    HomeScreenSpatialPanelsAnimated(
                        uiState = uiState,
                        onAction = onAction
                    )
                } else {
                    // SHRINKED STATE: Single spatial panel with posts list
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(680.dp)
                            .height(800.dp),
                        dragPolicy = MovePolicy(isEnabled = true),
                        resizePolicy = ResizePolicy(isEnabled = true)
                    ) {
                        Surface {
                            PostsList(
                                posts = uiState.posts,
                                onPostImageClick = { postId -> onAction(HomeAction.SelectPost(postId)) },
                                onLikeClick = { postId -> onAction(HomeAction.LikePost(postId)) },
                                onCommentClick = { postId -> onAction(HomeAction.SelectPostForComments(postId)) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            is HomeUiState.Error -> {
                // Show error in a simple spatial panel
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(680.dp)
                        .height(800.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: ${uiState.message}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Internal composable for Home screen content (2D mode)
 */
@SuppressLint("RestrictedApi")
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Note: Spatial panels are shown at the InstaXRApp level, not here
    // This composable only shows 2D content
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator()
            }
            is HomeUiState.Success -> {
                if (uiState.selectedPost != null) {
                    // 2D three-panel layout
                    ThreePanelLayout(
                        posts = uiState.posts,
                        selectedPost = uiState.selectedPost,
                        onPostClick = { postId -> onAction(HomeAction.SelectPost(postId)) },
                        onLikeClick = { postId -> onAction(HomeAction.LikePost(postId)) },
                        onClosePreview = { onAction(HomeAction.DeselectPost) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Normal posts list
                    PostsList(
                        posts = uiState.posts,
                        onPostImageClick = { postId -> onAction(HomeAction.SelectPost(postId)) },
                        onLikeClick = { postId -> onAction(HomeAction.LikePost(postId)) },
                        onCommentClick = { postId -> onAction(HomeAction.SelectPostForComments(postId)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is HomeUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Three separate spatial panels with animated width transition for the left panel
 * When entering from shrinked state, the left panel animates from 680dp to 250dp
 * This creates a smooth shrinking effect as the layout expands to three panels
 */
@SuppressLint("RestrictedApi")
@Composable
fun HomeScreenSpatialPanelsAnimated(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit
) {
    if (uiState is HomeUiState.Success && uiState.selectedPost != null) {
        // Animate the left panel width from initial (680dp) to compact (250dp)
        val leftPanelWidth by animateDpAsState(
            targetValue = 250.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "leftPanelWidth"
        )

        // Animate alpha for center and right panels (fade in effect)
        val animatedAlpha = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            launch {
                animatedAlpha.animateTo(
                    1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            }
        }

        SpatialRow {
            // Left panel - Compact posts list with animated width shrinking (height stays constant)
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(leftPanelWidth)
                    .height(700.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CompactPostsList(
                        posts = uiState.posts,
                        selectedPostId = uiState.selectedPost.id,
                        onPostClick = { postId ->
                            if (uiState.expandedForComments) {
                                onAction(HomeAction.SelectPostForComments(postId))
                            } else {
                                onAction(HomeAction.SelectPost(postId))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Center/right panels reordered based on expandedForComments
            if (uiState.expandedForComments) {
                // When expanded for comments: Comments panel comes first (center), then image (right)

                // Center panel - Description and comments with fade in animation
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(400.dp)
                        .height(700.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = false)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize().alpha(animatedAlpha.value)
                    ) {
                        DescriptionAndCommentsPanel(
                            post = uiState.selectedPost,
                            onLikeClick = { postId -> onAction(HomeAction.LikePost(postId)) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Floating close button (Orbiter)
                    Orbiter(
                        position = androidx.xr.compose.spatial.ContentEdge.Top,
                        offset = 16.dp,
                        alignment = androidx.compose.ui.Alignment.End
                    ) {
                        FilledTonalIconButton(
                            onClick = { onAction(HomeAction.DeselectPost) },
                            modifier = Modifier.size(48.dp).alpha(animatedAlpha.value)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close expanded view"
                            )
                        }
                    }
                }

                // Right panel - Image preview (bigger)
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(900.dp)
                        .height(700.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize().alpha(animatedAlpha.value)
                    ) {
                        CentralImagePreview(
                            post = uiState.selectedPost,
                            onClose = { onAction(HomeAction.DeselectPost) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                // Normal mode: Image comes first (center), then comments (right)

                // Central panel - Large image preview with fade in animation
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(900.dp)
                        .height(700.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize().alpha(animatedAlpha.value)
                    ) {
                        CentralImagePreview(
                            post = uiState.selectedPost,
                            onClose = { onAction(HomeAction.DeselectPost) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Right panel - Description and comments with fade in animation
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(400.dp)
                        .height(700.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = false)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize().alpha(animatedAlpha.value)
                    ) {
                        DescriptionAndCommentsPanel(
                            post = uiState.selectedPost,
                            onLikeClick = { postId -> onAction(HomeAction.LikePost(postId)) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Floating close button (Orbiter)
                    Orbiter(
                        position = androidx.xr.compose.spatial.ContentEdge.Top,
                        offset = 16.dp,
                        alignment = androidx.compose.ui.Alignment.End
                    ) {
                        FilledTonalIconButton(
                            onClick = { onAction(HomeAction.DeselectPost) },
                            modifier = Modifier.size(48.dp).alpha(animatedAlpha.value)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close expanded view"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Three separate spatial panels for XR mode using SpatialRow (non-animated version)
 * Each panel is independently positioned and can be moved/dragged separately
 * This is called directly from ApplicationSubspace - NOT wrapped in nested Subspace
 */
@SuppressLint("RestrictedApi")
@Composable
fun HomeScreenSpatialPanels(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit
) {
    if (uiState is HomeUiState.Success && uiState.selectedPost != null) {
        SpatialRow {
            // Left panel - Compact posts list (small)
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(250.dp)
                    .height(700.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = false)
            ) {
                Surface {
                    CompactPostsList(
                        posts = uiState.posts,
                        selectedPostId = uiState.selectedPost.id,
                        onPostClick = { postId -> onAction(HomeAction.SelectPost(postId)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Central panel - Large image preview (biggest, in the center)
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(900.dp)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    CentralImagePreview(
                        post = uiState.selectedPost,
                        onClose = { onAction(HomeAction.DeselectPost) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Right panel - Description and comments (medium)
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(400.dp)
                    .height(700.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = false)
            ) {
                Surface {
                    DescriptionAndCommentsPanel(
                        post = uiState.selectedPost,
                        onLikeClick = { postId -> onAction(HomeAction.LikePost(postId)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Scrollable list of posts
 */
@Composable
private fun PostsList(
    posts: List<Post>,
    onPostImageClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            PostItem(
                post = post,
                onPostImageClick = { onPostImageClick(post.id) },
                onLikeClick = { onLikeClick(post.id) },
                onCommentClick = { onCommentClick(post.id) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Individual post item
 */
@Composable
private fun PostItem(
    post: Post,
    onPostImageClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        // Post header with user info
        PostHeader(
            username = post.username,
            profileImageUrl = post.userProfileImageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        // Post image (full width)
        PostImage(
            imageUrl = post.imageUrl,
            onClick = onPostImageClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Post actions (like and comment counts)
        PostActions(
            isLiked = post.isLiked,
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * Post header with user profile image and username
 */
@Composable
private fun PostHeader(
    username: String,
    profileImageUrl: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile image (oval/circle)
        if (profileImageUrl != null) {
            val context = LocalContext.current
            val resourceId = context.resources.getIdentifier(
                profileImageUrl.substringBeforeLast("."),
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Profile picture of $username",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.first().uppercaseChar().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            // Fallback placeholder with first letter
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Username
        Text(
            text = username,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Post image
 */
@Composable
private fun PostImage(
    imageUrl: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        imageUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    if (resourceId != 0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 500.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            // Loading indicator shown while image loads
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .size(800) // Limit size to prevent bitmap too large crash
                    .crossfade(true)
                    .memoryCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (onClick != null) Modifier.clickable(onClick = onClick)
                        else Modifier
                    ),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        // Fallback placeholder
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Image not found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Post actions with like and comment counts
 */
@Composable
private fun PostActions(
    isLiked: Boolean,
    likeCount: Int,
    commentCount: Int,
    onLikeClick: () -> Unit,
    onCommentClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Like button with count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onLikeClick)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatCount(likeCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Comment icon with count (clickable)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (onCommentClick != null) Modifier.clickable(onClick = onCommentClick) else Modifier
        ) {
            Icon(
                imageVector = Icons.Filled.CommentBubble,
                contentDescription = "Comments",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatCount(commentCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


/**
 * Three-panel layout: shrinked posts list, central image preview, and comments panel
 * (2D fallback version for non-XR mode)
 */
@Composable
private fun ThreePanelLayout(
    posts: List<Post>,
    selectedPost: Post,
    onPostClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onClosePreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // Left panel - shrinked posts list
        CompactPostsList(
            posts = posts,
            selectedPostId = selectedPost.id,
            onPostClick = onPostClick,
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
        )

        // Central panel - large image preview
        CentralImagePreview(
            post = selectedPost,
            onClose = onClosePreview,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        // Right panel - description and comments
        DescriptionAndCommentsPanel(
            post = selectedPost,
            onLikeClick = onLikeClick,
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight()
        )
    }
}

/**
 * Compact posts list for left panel
 */
@Composable
private fun CompactPostsList(
    posts: List<Post>,
    selectedPostId: String,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            CompactPostItem(
                post = post,
                isSelected = post.id == selectedPostId,
                onClick = { onPostClick(post.id) }
            )
        }
    }
}

/**
 * Compact post item for left panel
 */
@Composable
private fun CompactPostItem(
    post: Post,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        post.imageUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface
            )
            .padding(8.dp)
    ) {
        // Small thumbnail with loading indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            // Loading indicator shown while image loads
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp)
            )

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .size(600) // Reasonable thumbnail size
                    .crossfade(true)
                    .memoryCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Post thumbnail",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Username
        Text(
            text = post.username,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

/**
 * Central image preview panel
 */
@Composable
private fun CentralImagePreview(
    post: Post,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        post.imageUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Loading indicator shown while image loads
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(resourceId)
                .size(1920, 1920) // Limit max size to prevent memory issues
                .crossfade(true)
                .build(),
            contentDescription = "Large post image",
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Description and comments panel
 */
@Composable
private fun DescriptionAndCommentsPanel(
    post: Post,
    onLikeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Caption/Description with username
        if (!post.caption.isNullOrBlank()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = post.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Post actions
        PostActions(
            isLiked = post.isLiked,
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            onLikeClick = { onLikeClick(post.id) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Comments section
        Text(
            text = "Comments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Comments list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(post.comments, key = { it.id }) { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}

/**
 * Individual comment item
 */
@Composable
private fun CommentItem(
    comment: com.appbuildchat.instaxr.data.model.Comment,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Profile image
        if (comment.userProfileImageUrl != null) {
            val resourceId = context.resources.getIdentifier(
                comment.userProfileImageUrl.substringBeforeLast("."),
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Profile picture of ${comment.username}",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comment.username.first().uppercaseChar().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.username.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Comment content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (comment.likeCount > 0) {
                    Text(
                        text = "${comment.likeCount} likes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Format count for display (e.g., 1.2K, 15.3K, etc.)
 */
private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%.0fK", count / 1000.0)
    }
}

/**
 * User actions for Home screen
 */
sealed interface HomeAction {
    data object Refresh : HomeAction
    data class LikePost(val postId: String) : HomeAction
    data class SelectPost(val postId: String) : HomeAction
    data class SelectPostForComments(val postId: String) : HomeAction
    data object DeselectPost : HomeAction
}
