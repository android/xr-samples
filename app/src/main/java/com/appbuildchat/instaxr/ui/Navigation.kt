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
import com.appbuildchat.instaxr.ui.search.SearchScreen
import com.appbuildchat.instaxr.ui.settings.SettingsScreen

/**
 * Main navigation routes for the app
 */
object AppRoutes {
    const val HOME = "home"
    const val MY_PAGE = "mypage"
    const val SEARCH = "search"
    const val MESSAGES = "messages"
    const val SETTINGS = "settings"
    const val ADD_POST = "addpost"
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
            HomeScreen()
        }

        // My Page (Profile) Screen
        composable(route = AppRoutes.MY_PAGE) {
            ProfileScreen()
        }

        // Search Screen
        composable(route = AppRoutes.SEARCH) {
            SearchScreen()
        }

        // Messages Screen
        composable(route = AppRoutes.MESSAGES) {
            MessagesScreen()
        }

        // Settings Screen
        composable(route = AppRoutes.SETTINGS) {
            SettingsScreen()
        }

        // Add Post Screen (placeholder for now)
        composable(route = AppRoutes.ADD_POST) {
            HomeScreen() // TODO: Create AddPostScreen
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
