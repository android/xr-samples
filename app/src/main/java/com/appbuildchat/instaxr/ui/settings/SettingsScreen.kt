package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.layout.height

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
    val isDestructive: Boolean = false
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
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSpatialUiEnabled = LocalSpatialCapabilities.current.isSpatialUiEnabled

    if (isSpatialUiEnabled) {
        // XR Mode - Spatial Panel
        Subspace {
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(680.dp)
                    .height(800.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    SettingsContent(
                        uiState = uiState,
                        onAction = viewModel::handleAction,
                        onNavigate = onNavigate,
                        modifier = modifier
                    )
                }
            }
        }
    } else {
        // 2D Mode
        SettingsContent(
            uiState = uiState,
            onAction = viewModel::handleAction,
            onNavigate = onNavigate,
            modifier = modifier
        )
    }
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
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (uiState) {
            is SettingsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            is SettingsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Header
                    item {
                        Text(
                            text = "설정",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }

                    // Settings Sections
                    uiState.sections.forEach { section ->
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

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
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
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
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
                Text(
                    "로그아웃",
                    color = Color.White
                )
            },
            text = {
                Text(
                    "정말 로그아웃 하시겠습니까?",
                    color = Color.White.copy(alpha = 0.9f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(SettingsAction.Logout)
                        showLogoutDialog = false
                    }
                ) {
                    Text("로그아웃", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소", color = Color.White)
                }
            },
            containerColor = Color(0xFF1C1C1C)
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
        color = Color.White.copy(alpha = 0.6f),
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
            .clickable(onClick = onClick),
        color = Color.Black
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
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "이동",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    HorizontalDivider(
        color = Color(0xFF333333),
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
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
            .height(56.dp),
        color = Color.Black
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
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onToggle(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF0095F6),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF333333)
                )
            )
        }
    }

    HorizontalDivider(
        color = Color(0xFF333333),
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
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
            .clickable(onClick = onClick),
        color = Color.Black
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
                tint = if (item.isDestructive) Color.Red else Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.title,
                fontSize = 16.sp,
                color = if (item.isDestructive) Color.Red else Color.White,
                fontWeight = FontWeight.Normal
            )
        }
    }

    HorizontalDivider(
        color = Color(0xFF333333),
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

/**
 * User actions for Settings screen
 */
sealed interface SettingsAction {
    data class ToggleSetting(val settingId: String) : SettingsAction
    data class NavigateToDetail(val screenId: String) : SettingsAction
    data object Logout : SettingsAction
}
