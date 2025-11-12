package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.layout.height

data class PrivacyOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isToggled: Boolean
)

@Composable
fun PrivacySettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpatialUiEnabled = LocalSpatialCapabilities.current.isSpatialUiEnabled

    if (isSpatialUiEnabled) {
        Subspace {
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(680.dp)
                    .height(800.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    PrivacySettingsContent(
                        onNavigateBack = onNavigateBack,
                        modifier = modifier
                    )
                }
            }
        }
    } else{
        PrivacySettingsContent(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySettingsContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val privacyOptions = remember {
        listOf(
            PrivacyOption(
                id = "private_account",
                title = "비공개 계정",
                subtitle = "팔로워만 내 콘텐츠를 볼 수 있습니다",
                icon = Icons.Default.Lock,
                isToggled = false
            ),
            PrivacyOption(
                id = "show_activity_status",
                title = "활동 상태 표시",
                subtitle = "다른 사용자에게 활동 상태를 표시합니다",
                icon = Icons.Default.Person,
                isToggled = true
            ),
            PrivacyOption(
                id = "allow_messages",
                title = "메시지 허용",
                subtitle = "모든 사용자로부터 메시지를 받습니다",
                icon = Icons.Default.Email,
                isToggled = true
            ),
            PrivacyOption(
                id = "allow_mentions",
                title = "멘션 허용",
                subtitle = "다른 사용자가 나를 멘션할 수 있습니다",
                icon = Icons.Default.AccountCircle,
                isToggled = true
            ),
            PrivacyOption(
                id = "show_story_sharing",
                title = "스토리 공유 허용",
                subtitle = "다른 사용자가 내 게시물을 스토리에 공유할 수 있습니다",
                icon = Icons.Default.Share,
                isToggled = false
            )
        )
    }

    var privacyStates by remember { mutableStateOf(privacyOptions.associate { it.id to it.isToggled }) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "개인정보 설정",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )

        HorizontalDivider(color = Color(0xFF333333), thickness = 0.5.dp)

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(privacyOptions) { option ->
                PrivacyOptionItem(
                    option = option,
                    isToggled = privacyStates[option.id] ?: option.isToggled,
                    onToggle = { newValue ->
                        privacyStates = privacyStates.toMutableMap().apply {
                            put(option.id, newValue)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PrivacyOptionItem(
    option: PrivacyOption,
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Black
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = option.subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Switch(
                checked = isToggled,
                onCheckedChange = onToggle,
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
