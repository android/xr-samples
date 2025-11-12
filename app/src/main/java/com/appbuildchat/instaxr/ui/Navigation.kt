package com.appbuildchat.instaxr.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appbuildchat.instaxr.ui.home.HomeScreen
import com.appbuildchat.instaxr.ui.messages.MessagesScreen
import com.appbuildchat.instaxr.ui.profile.ProfileScreen
import com.appbuildchat.instaxr.ui.reels.ReelsScreen
import com.appbuildchat.instaxr.ui.search.SearchScreen
import com.appbuildchat.instaxr.ui.settings.SettingsScreen
import com.appbuildchat.instaxr.ui.stories.StoriesScreen

/**
 * Main navigation routes for the app
 */
object AppRoutes {
    const val HOME = "home"
    const val REELS = "reels"
    const val STORIES = "stories"
    const val SEARCH = "search"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

/**
 * Main navigation composable for the app
 * Defines the navigation graph and screen destinations
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = modifier
    ) {
        // Home Screen
        composable(route = AppRoutes.HOME) {
            @Composable {
                HomeScreen()
            }
        }

        // Reels Screen
        composable(route = AppRoutes.REELS) {
            @Composable {
                ReelsScreen()
            }
        }

        // Stories Screen
        composable(route = AppRoutes.STORIES) {
            @Composable {
                StoriesScreen()
            }
        }

        // Search Screen
        composable(route = AppRoutes.SEARCH) {
            @Composable {
                SearchScreen()
            }
        }

        // Messages Screen
        composable(route = AppRoutes.MESSAGES) {
            @Composable {
                MessagesScreen()
            }
        }

        // Profile Screen
        composable(route = AppRoutes.PROFILE) {
            @Composable {
                ProfileScreen()
            }
        }

        // Settings Screen
        composable(route = AppRoutes.SETTINGS) {
            @Composable {
                SettingsScreen()
            }
        }
    }
}

/**
 * Extension function to navigate to routes safely
 */
fun NavHostController.navigateSingleTopTo(route: String) {
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        popUpTo(this@navigateSingleTopTo.graph.startDestinationId) {
            saveState = true
        }
        // Avoid multiple copies of the same destination
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}
