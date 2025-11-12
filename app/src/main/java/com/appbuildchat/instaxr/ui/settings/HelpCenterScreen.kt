package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

data class HelpTopic(
    val title: String,
    val description: String
)

@Composable
fun HelpCenterScreen(
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
                    HelpCenterContent(
                        onNavigateBack = onNavigateBack,
                        modifier = modifier
                    )
                }
            }
        }
    } else {
        HelpCenterContent(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpCenterContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val helpTopics = remember {
        listOf(
            HelpTopic(
                "시작하기",
                "InstaXR 사용 방법을 알아보세요"
            ),
            HelpTopic(
                "게시물 업로드",
                "사진과 동영상을 공유하는 방법"
            ),
            HelpTopic(
                "개인정보 보호",
                "계정과 개인정보를 보호하는 방법"
            ),
            HelpTopic(
                "XR 기능 사용",
                "공간 UI 및 몰입형 환경 사용법"
            ),
            HelpTopic(
                "계정 관리",
                "프로필 편집 및 설정 변경"
            ),
            HelpTopic(
                "문제 해결",
                "일반적인 문제 및 해결 방법"
            ),
            HelpTopic(
                "커뮤니티 가이드라인",
                "안전하고 존중하는 커뮤니티 만들기"
            ),
            HelpTopic(
                "문의하기",
                "추가 도움이 필요하신가요?"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "고객센터",
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
            items(helpTopics) { topic ->
                HelpTopicItem(topic = topic)
            }
        }
    }
}

@Composable
private fun HelpTopicItem(topic: HelpTopic) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to detail */ },
        color = Color.Black
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "자세히 보기",
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
