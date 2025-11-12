/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appbuildchat.instaxr.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.spatial.ApplicationSubspace
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width

/**
 * Main InstaXR App composable
 * Uses ApplicationSubspace at the top level
 */
@SuppressLint("RestrictedApi")
@Composable
fun InstaXRApp() {
    val spatialConfiguration = LocalSpatialConfiguration.current

    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        // XR Mode - with ApplicationSubspace at top level
        ApplicationSubspace {
            SpatialContent(
                onRequestHomeSpaceMode = spatialConfiguration::requestHomeSpaceMode
            )
        }
    } else {
        // 2D Mode - regular navigation with mode switch button
        My2DContent(
            onRequestFullSpaceMode = spatialConfiguration::requestFullSpaceMode
        )
    }
}

/**
 * Spatial content for XR mode
 * Observes HomeViewModel (activity-scoped via Hilt) to switch layouts
 */
@SuppressLint("RestrictedApi")
@Composable
fun SpatialContent(onRequestHomeSpaceMode: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Get the activity to scope ViewModel to activity level
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity

    // Check if we're on home route
    val isHomeRoute = currentRoute == AppRoutes.HOME

    // Get activity-scoped HomeViewModel (same instance as HomeScreen uses)
    val homeViewModel: com.appbuildchat.instaxr.ui.home.HomeViewModel? =
        if (isHomeRoute && activity != null) {
            androidx.hilt.navigation.compose.hiltViewModel(viewModelStoreOwner = activity)
        } else null

    val homeUiState = homeViewModel?.uiState?.collectAsState()?.value
    val hasSelectedPost = (homeUiState as? com.appbuildchat.instaxr.ui.home.HomeUiState.Success)?.selectedPost != null

    // If on home with selected post, show three spatial panels
    if (isHomeRoute && hasSelectedPost && homeViewModel != null && homeUiState != null) {
        // EXPANDED STATE: Three separate spatial panels
        com.appbuildchat.instaxr.ui.home.HomeScreenSpatialPanelsAnimated(
            uiState = homeUiState,
            onAction = homeViewModel::handleAction
        )

        // Still show navigation orbiter
        Orbiter(
            position = ContentEdge.Bottom,
            offset = 100.dp,
            alignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationItem(Icons.Default.Home, "Home", true) {
                        homeViewModel.handleAction(com.appbuildchat.instaxr.ui.home.HomeAction.DeselectPost)
                    }
                    NavigationItem(Icons.Default.Search, "Search", false) {
                        homeViewModel.handleAction(com.appbuildchat.instaxr.ui.home.HomeAction.DeselectPost)
                        navController.navigateSingleTopTo(AppRoutes.SEARCH)
                    }
                    NavigationItem(Icons.Default.Add, "Add", false) {
                        homeViewModel.handleAction(com.appbuildchat.instaxr.ui.home.HomeAction.DeselectPost)
                        navController.navigateSingleTopTo(AppRoutes.ADD_POST)
                    }
                    NavigationItem(Icons.Default.Email, "Messages", false) {
                        homeViewModel.handleAction(com.appbuildchat.instaxr.ui.home.HomeAction.DeselectPost)
                        navController.navigateSingleTopTo(AppRoutes.MESSAGES)
                    }
                    NavigationItem(Icons.Default.Person, "My Page", false) {
                        homeViewModel.handleAction(com.appbuildchat.instaxr.ui.home.HomeAction.DeselectPost)
                        navController.navigateSingleTopTo(AppRoutes.MY_PAGE)
                    }
                    NavigationItem(Icons.Default.Settings, "Settings", false) {
                        homeViewModel.handleAction(com.appbuildchat.instaxr.ui.home.HomeAction.DeselectPost)
                        navController.navigateSingleTopTo(AppRoutes.SETTINGS)
                    }
                }
            }
        }
    } else {
        // NORMAL STATE: Main spatial panel with navigation
        SpatialPanel(
        modifier = SubspaceModifier
            .width(680.dp)
            .height(800.dp),
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = true)
    ) {
        Surface {
            AppNavigation(navController = navController)
        }

        // Home Space Mode Button (Top Right)
        Orbiter(
            position = ContentEdge.Bottom,
            offset = 20.dp,
            alignment = Alignment.End
        ) {
            HomeSpaceModeIconButton(
                onClick = onRequestHomeSpaceMode,
                modifier = Modifier.size(56.dp)
            )
        }

        // Bottom Navigation Orbiter
        Orbiter(
            position = ContentEdge.Bottom,
            offset = 100.dp,
            alignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationItem(Icons.Default.Home, "Home", currentRoute == AppRoutes.HOME) {
                        navController.navigateSingleTopTo(AppRoutes.HOME)
                    }
                    NavigationItem(Icons.Default.Search, "Search", currentRoute == AppRoutes.SEARCH) {
                        navController.navigateSingleTopTo(AppRoutes.SEARCH)
                    }
                    NavigationItem(Icons.Default.Add, "Add", currentRoute == AppRoutes.ADD_POST) {
                        navController.navigateSingleTopTo(AppRoutes.ADD_POST)
                    }
                    NavigationItem(Icons.Default.Email, "Messages", currentRoute == AppRoutes.MESSAGES) {
                        navController.navigateSingleTopTo(AppRoutes.MESSAGES)
                    }
                    NavigationItem(Icons.Default.Person, "My Page", currentRoute == AppRoutes.MY_PAGE) {
                        navController.navigateSingleTopTo(AppRoutes.MY_PAGE)
                    }
                    NavigationItem(Icons.Default.Settings, "Settings", currentRoute == AppRoutes.SETTINGS) {
                        navController.navigateSingleTopTo(AppRoutes.SETTINGS)
                    }
                }
            }
        }
        }
    }
}

/**
 * 2D content for Home Space mode
 */
@SuppressLint("RestrictedApi")
@Composable
fun My2DContent(onRequestFullSpaceMode: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main content area
            Box(modifier = Modifier.weight(1f)) {
                AppNavigation(navController = navController)

                // Full Space Mode button at top right
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    FullSpaceModeIconButton(
                        onClick = onRequestFullSpaceMode,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Bottom Navigation Bar
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationItem(Icons.Default.Home, "Home", currentRoute == AppRoutes.HOME) {
                        navController.navigateSingleTopTo(AppRoutes.HOME)
                    }
                    NavigationItem(Icons.Default.Search, "Search", currentRoute == AppRoutes.SEARCH) {
                        navController.navigateSingleTopTo(AppRoutes.SEARCH)
                    }
                    NavigationItem(Icons.Default.Add, "Add", currentRoute == AppRoutes.ADD_POST) {
                        navController.navigateSingleTopTo(AppRoutes.ADD_POST)
                    }
                    NavigationItem(Icons.Default.Email, "Messages", currentRoute == AppRoutes.MESSAGES) {
                        navController.navigateSingleTopTo(AppRoutes.MESSAGES)
                    }
                    NavigationItem(Icons.Default.Person, "My Page", currentRoute == AppRoutes.MY_PAGE) {
                        navController.navigateSingleTopTo(AppRoutes.MY_PAGE)
                    }
                    NavigationItem(Icons.Default.Settings, "Settings", currentRoute == AppRoutes.SETTINGS) {
                        navController.navigateSingleTopTo(AppRoutes.SETTINGS)
                    }
                }
            }
        }
    }
}

/**
 * Navigation item component
 */
@Composable
private fun NavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
        isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .hoverable(
                interactionSource = remember { MutableInteractionSource() }
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Home Space Mode button
 */
@Composable
fun HomeSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Switch to Home Space Mode"
        )
    }
}

/**
 * Full Space Mode button
 */
@Composable
fun FullSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Switch to Full Space Mode"
        )
    }
}
