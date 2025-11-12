package com.appbuildchat.instaxr.ui.profile

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import com.appbuildchat.instaxr.ui.icons.CommentBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.appbuildchat.instaxr.data.model.Post
import com.appbuildchat.instaxr.data.model.User

/**
 * Top-level composable for the Profile feature screen
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Spatial content for XR mode
 */
@SuppressLint("RestrictedApi")
@Composable
fun ProfileSpatialContent(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit
) {
    Subspace {
        when (uiState) {
            is ProfileUiState.Loading -> {
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
            is ProfileUiState.Success -> {
                if (uiState.selectedPost != null) {
                    // EXPANDED STATE: Two panels (profile info + post detail)
                    ProfileScreenSpatialPanelsAnimated(
                        uiState = uiState,
                        onAction = onAction
                    )
                } else {
                    // NORMAL STATE: Single panel with profile
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(680.dp)
                            .height(900.dp),
                        dragPolicy = MovePolicy(isEnabled = true),
                        resizePolicy = ResizePolicy(isEnabled = true)
                    ) {
                        Surface {
                            ProfileMainContent(
                                user = uiState.user,
                                posts = uiState.posts,
                                selectedTab = uiState.selectedTab,
                                onAction = onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Orbiter for tab navigation
                        Orbiter(
                            position = androidx.xr.compose.spatial.ContentEdge.Bottom,
                            offset = 20.dp,
                            alignment = Alignment.CenterHorizontally
                        ) {
                            TabNavigationOrbiter(
                                selectedTab = uiState.selectedTab,
                                onTabChange = { tab -> onAction(ProfileAction.ChangeTab(tab)) }
                            )
                        }
                    }
                }
            }
            is ProfileUiState.Error -> {
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
 * Two spatial panels for expanded state (profile info + post detail)
 */
@SuppressLint("RestrictedApi")
@Composable
fun ProfileScreenSpatialPanelsAnimated(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit
) {
    if (uiState is ProfileUiState.Success && uiState.selectedPost != null) {
        val leftPanelWidth by animateDpAsState(
            targetValue = 400.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "leftPanelWidth"
        )

        SpatialRow {
            // Left panel - Profile info with post grid
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(leftPanelWidth)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = false)
            ) {
                Surface {
                    ProfileMainContent(
                        user = uiState.user,
                        posts = uiState.posts,
                        selectedTab = uiState.selectedTab,
                        onAction = onAction,
                        isCompact = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Right panel - Selected post detail
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    expandFrom = Alignment.Start
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 300)
                ) + shrinkHorizontally(
                    animationSpec = tween(durationMillis = 300),
                    shrinkTowards = Alignment.Start
                )
            ) {
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(900.dp)
                        .height(900.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface {
                        PostDetailView(
                            post = uiState.selectedPost,
                            onClose = { onAction(ProfileAction.DeselectPost) },
                            onLikeClick = { /* Handle like */ },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Close button orbiter
                    Orbiter(
                        position = androidx.xr.compose.spatial.ContentEdge.Top,
                        offset = 16.dp,
                        alignment = Alignment.End
                    ) {
                        FilledTonalIconButton(
                            onClick = { onAction(ProfileAction.DeselectPost) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Internal composable for Profile screen content (2D mode)
 */
@Composable
internal fun ProfileContent(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator()
            }
            is ProfileUiState.Success -> {
                if (uiState.selectedPost != null) {
                    // Two panel layout
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left: Profile info
                        ProfileMainContent(
                            user = uiState.user,
                            posts = uiState.posts,
                            selectedTab = uiState.selectedTab,
                            onAction = onAction,
                            isCompact = true,
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxHeight()
                        )

                        // Right: Post detail
                        PostDetailView(
                            post = uiState.selectedPost,
                            onClose = { onAction(ProfileAction.DeselectPost) },
                            onLikeClick = { /* Handle like */ },
                            modifier = Modifier
                                .weight(0.7f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    // Normal profile view
                    ProfileMainContent(
                        user = uiState.user,
                        posts = uiState.posts,
                        selectedTab = uiState.selectedTab,
                        onAction = onAction,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is ProfileUiState.Error -> {
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
 * Main profile content with header and post grid
 */
@Composable
private fun ProfileMainContent(
    user: User,
    posts: List<Post>,
    selectedTab: ProfileTab,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            ProfileHeader(
                user = user,
                onEditProfile = { onAction(ProfileAction.EditProfile) },
                isCompact = isCompact
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Tab navigation
            if (!isCompact) {
                TabNavigation(
                    selectedTab = selectedTab,
                    onTabChange = { tab -> onAction(ProfileAction.ChangeTab(tab)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            // Post grid
            PostGrid(
                posts = posts,
                onPostClick = { post -> onAction(ProfileAction.SelectPost(post)) },
                columns = if (isCompact) 2 else 3
            )
        }
    }
}

/**
 * Profile header with avatar, stats, and edit button
 */
@Composable
private fun ProfileHeader(
    user: User,
    onEditProfile: () -> Unit,
    isCompact: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile picture
        ProfileAvatar(
            profileImageUrl = user.profileImageUrl,
            username = user.username,
            size = if (isCompact) 80.dp else 120.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username and display name
        Text(
            text = user.username,
            style = if (isCompact) MaterialTheme.typography.titleMedium
                   else MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!user.displayName.isNullOrBlank() && user.displayName != user.username) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        if (!isCompact) {
            StatsRow(
                postCount = user.postCount,
                followerCount = user.followerCount,
                followingCount = user.followingCount
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bio
        if (!user.bio.isNullOrBlank() && !isCompact) {
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Edit Profile button
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 8.dp else 32.dp)
        ) {
            Text("Edit Profile")
        }
    }
}

/**
 * Profile avatar with circular shape
 */
@Composable
private fun ProfileAvatar(
    profileImageUrl: String?,
    username: String,
    size: Dp
) {
    val context = LocalContext.current

    if (profileImageUrl != null) {
        val resourceId = context.resources.getIdentifier(
            profileImageUrl.substringBeforeLast("."),
            "drawable",
            context.packageName
        )

        if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            PlaceholderAvatar(username = username, size = size)
        }
    } else {
        PlaceholderAvatar(username = username, size = size)
    }
}

/**
 * Placeholder avatar with initial letter
 */
@Composable
private fun PlaceholderAvatar(username: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.first().uppercaseChar().toString(),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Stats row showing posts, followers, following
 */
@Composable
private fun StatsRow(
    postCount: Int,
    followerCount: Int,
    followingCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(label = "Posts", count = postCount)
        StatItem(label = "Followers", count = followerCount)
        StatItem(label = "Following", count = followingCount)
    }
}

/**
 * Individual stat item
 */
@Composable
private fun StatItem(label: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tab navigation for Posts, Reels, Tagged
 */
@Composable
private fun TabNavigation(
    selectedTab: ProfileTab,
    onTabChange: (ProfileTab) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Tab(
            selected = selectedTab == ProfileTab.POSTS,
            onClick = { onTabChange(ProfileTab.POSTS) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            text = { Text("Posts") }
        )
        Tab(
            selected = selectedTab == ProfileTab.REELS,
            onClick = { onTabChange(ProfileTab.REELS) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
            text = { Text("Reels") }
        )
        Tab(
            selected = selectedTab == ProfileTab.TAGGED,
            onClick = { onTabChange(ProfileTab.TAGGED) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            text = { Text("Tagged") }
        )
    }
}

/**
 * Tab navigation as orbiter (for XR mode)
 */
@Composable
private fun TabNavigationOrbiter(
    selectedTab: ProfileTab,
    onTabChange: (ProfileTab) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onTabChange(ProfileTab.POSTS) },
                colors = if (selectedTab == ProfileTab.POSTS) {
                    IconButtonDefaults.filledIconButtonColors()
                } else {
                    IconButtonDefaults.iconButtonColors()
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Posts")
            }
            IconButton(
                onClick = { onTabChange(ProfileTab.REELS) },
                colors = if (selectedTab == ProfileTab.REELS) {
                    IconButtonDefaults.filledIconButtonColors()
                } else {
                    IconButtonDefaults.iconButtonColors()
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Reels")
            }
            IconButton(
                onClick = { onTabChange(ProfileTab.TAGGED) },
                colors = if (selectedTab == ProfileTab.TAGGED) {
                    IconButtonDefaults.filledIconButtonColors()
                } else {
                    IconButtonDefaults.iconButtonColors()
                }
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Tagged")
            }
        }
    }
}

/**
 * Post grid (Instagram-style 3-column grid)
 */
@Composable
private fun PostGrid(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    columns: Int = 3
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No posts yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column {
            // Calculate rows needed
            val rows = (posts.size + columns - 1) / columns

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (col in 0 until columns) {
                        val index = row * columns + col
                        if (index < posts.size) {
                            PostThumbnail(
                                post = posts[index],
                                onClick = { onPostClick(posts[index]) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * Post thumbnail for grid
 */
@Composable
private fun PostThumbnail(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        post.imageUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    if (resourceId != 0) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(resourceId)
                .size(400)
                .crossfade(true)
                .build(),
            contentDescription = "Post thumbnail",
            modifier = modifier
                .aspectRatio(1f)
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Post detail view
 */
@Composable
private fun PostDetailView(
    post: Post,
    onClose: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        post.imageUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }

        // Post image
        if (resourceId != 0) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .size(1920, 1920)
                    .crossfade(true)
                    .build(),
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Post info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                profileImageUrl = post.userProfileImageUrl,
                username = post.username,
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = post.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Caption
        if (!post.caption.isNullOrBlank()) {
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Like and comment counts
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onLikeClick)
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Filled.Favorite
                                 else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) MaterialTheme.colorScheme.error
                          else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatCount(post.likeCount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CommentBubble,
                    contentDescription = "Comments",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatCount(post.commentCount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Format count for display
 */
private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%.0fK", count / 1000.0)
    }
}
