package com.appbuildchat.instaxr.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appbuildchat.instaxr.ui.home.HomeScreen
import com.appbuildchat.instaxr.ui.messages.MessagesScreen
import com.appbuildchat.instaxr.ui.profile.ProfileScreen
import com.appbuildchat.instaxr.ui.profile.ProfileViewModel
import com.appbuildchat.instaxr.ui.reels.ReelsScreen
import com.appbuildchat.instaxr.ui.reels.dome.ReelsDomeScreen
import com.appbuildchat.instaxr.ui.search.SearchScreen
import com.appbuildchat.instaxr.ui.settings.*

/**
 * Main navigation routes for the app
 */
object AppRoutes {
    const val HOME = "home"
    const val REELS = "reels"
    const val REELS_DOME = "reels_dome" // Experimental dome carousel
    const val MY_PAGE = "mypage"
    const val SEARCH = "search"
    const val MESSAGES = "messages"
    const val SETTINGS = "settings"
    const val ADD_POST = "addpost"

    // Settings sub-routes
    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PASSWORD = "change_password"
    const val PRIVACY_SETTINGS = "privacy_settings"
    const val HELP_CENTER = "help"
    const val ABOUT = "about"
    const val TERMS = "terms"
    const val PRIVACY_POLICY = "privacy_policy"
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

        // Reels Screen
        composable(route = AppRoutes.REELS) {
            ReelsScreen()
        }

        // Reels Dome Screen (Experimental)
        composable(route = AppRoutes.REELS_DOME) {
            ReelsDomeScreen()
        }

        // My Page (Profile) Screen
        composable(route = AppRoutes.MY_PAGE) {
            val activity = LocalActivity.current as? ComponentActivity
                ?: error("ProfileScreen requires an activity context")
            val profileViewModel: ProfileViewModel = viewModel(viewModelStoreOwner = activity)
            ProfileScreen(viewModel = profileViewModel)
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
            SettingsScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        // Settings Sub-Screens
        composable(route = AppRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoutes.CHANGE_PASSWORD) {
            PrivacySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoutes.PRIVACY_SETTINGS) {
            PrivacySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoutes.HELP_CENTER) {
            HelpCenterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoutes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoutes.TERMS) {
            TermsOfServiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = AppRoutes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
