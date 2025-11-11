package com.example.helloandroidxr.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Top-level composable for the Settings feature screen
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Internal composable for Settings screen content
 */
@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is SettingsUiState.Loading -> {
                Text("Loading Settings...")
            }
            is SettingsUiState.Success -> {
                Text("Settings Screen - Coming Soon")
            }
            is SettingsUiState.Error -> {
                Text("Error: ${uiState.message}")
            }
        }
    }
}

/**
 * User actions for Settings screen
 */
sealed interface SettingsAction {
    data class ToggleSetting(val settingId: String) : SettingsAction
    data object Logout : SettingsAction
}
