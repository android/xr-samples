package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PrivacySetting(
    val title: String,
    val description: String,
    val isEnabled: Boolean = false
)

@Composable
fun PrivacySettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrivacySettingsContent(
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySettingsContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val privacySettings = remember {
        listOf(
            PrivacySetting("계정 공개", "모든 사용자가 내 프로필을 볼 수 있습니다", true),
            PrivacySetting("활동 상태", "내 활동 상태를 다른 사용자에게 표시", false),
            PrivacySetting("스토리 공유", "친구에게만 스토리 공개", true),
            PrivacySetting("메시지 수신", "모든 사용자로부터 메시지 받기", false),
            PrivacySetting("위치 정보", "게시물에 위치 정보 포함", false)
        )
    }

    var settings by remember { mutableStateOf(privacySettings) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "개인정보 설정",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }
        )

        HorizontalDivider()

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(settings) { setting ->
                val index = settings.indexOf(setting)
                PrivacySettingItem(
                    setting = setting,
                    onToggle = {
                        settings = settings.toMutableList().also {
                            it[index] = setting.copy(isEnabled = !setting.isEnabled)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PrivacySettingItem(
    setting: PrivacySetting,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setting.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = setting.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = setting.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }

    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
}
