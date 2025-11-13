package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Data model for settings items
 */
sealed class SettingsItemType {
    data object Navigation : SettingsItemType()
    data object Toggle : SettingsItemType()
    data object Action : SettingsItemType()
}

data class SettingsItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val type: SettingsItemType,
    val isToggled: Boolean = false,
    val isDestructive: Boolean = false,
    val isPrimary: Boolean = false,
    val showIcon: Boolean = true
)

data class SettingsSection(
    val header: String,
    val items: List<SettingsItem>
)

/**
 * Top-level composable for the Settings feature screen
 */
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onNavigate = onNavigate,
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
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (uiState) {
            is SettingsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is SettingsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Header
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "설정 및 활동",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Settings Sections
                    uiState.sections.forEachIndexed { index, section ->
                        // Add thick divider before each section (except first)
                        if (index > 0) {
                            item {
                                HorizontalDivider(
                                    thickness = 8.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }

                        item {
                            SettingSectionHeader(section.header)
                        }

                        items(section.items) { item ->
                            when (item.type) {
                                is SettingsItemType.Navigation -> {
                                    SettingNavigationItem(
                                        item = item,
                                        onClick = {
                                            onAction(SettingsAction.NavigateToDetail(item.id))
                                            onNavigate(item.id)
                                        }
                                    )
                                }
                                is SettingsItemType.Toggle -> {
                                    SettingToggleItem(
                                        item = item,
                                        onToggle = {
                                            onAction(SettingsAction.ToggleSetting(item.id))
                                        }
                                    )
                                }
                                is SettingsItemType.Action -> {
                                    SettingActionItem(
                                        item = item,
                                        onClick = {
                                            if (item.id == "logout") {
                                                showLogoutDialog = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is SettingsUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "오류가 발생했습니다",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("로그아웃")
            },
            text = {
                Text("정말 로그아웃 하시겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(SettingsAction.Logout)
                        showLogoutDialog = false
                    }
                ) {
                    Text("로그아웃", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * Section Header Component
 */
@Composable
private fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

/**
 * Navigation Item Component (with chevron)
 */
@Composable
private fun SettingNavigationItem(
    item: SettingsItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.showIcon) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = when {
                        item.isPrimary -> MaterialTheme.colorScheme.primary
                        item.isDestructive -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = when {
                        item.isPrimary -> MaterialTheme.colorScheme.primary
                        item.isDestructive -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "이동",
                tint = when {
                    item.isPrimary -> MaterialTheme.colorScheme.primary
                    item.isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Toggle Item Component (with switch)
 */
@Composable
private fun SettingToggleItem(
    item: SettingsItem,
    onToggle: (Boolean) -> Unit
) {
    var isChecked by remember(item.isToggled) { mutableStateOf(item.isToggled) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onToggle(it)
                }
            )
        }
    }
}

/**
 * Action Item Component (for destructive actions like logout)
 */
@Composable
private fun SettingActionItem(
    item: SettingsItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.showIcon) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = when {
                        item.isPrimary -> MaterialTheme.colorScheme.primary
                        item.isDestructive -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = item.title,
                fontSize = 16.sp,
                color = when {
                    item.isPrimary -> MaterialTheme.colorScheme.primary
                    item.isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * User actions for Settings screen
 */
sealed interface SettingsAction {
    data class ToggleSetting(val settingId: String) : SettingsAction
    data class NavigateToDetail(val screenId: String) : SettingsAction
    data object Logout : SettingsAction
}
