package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrivacyPolicyContent(
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyPolicyContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        text = "개인정보 처리방침",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }
        )

        HorizontalDivider()

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "개인정보 처리방침",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "InstaXR은 사용자의 개인정보를 중요하게 생각하며, 관련 법규를 준수합니다.",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1. 수집하는 개인정보",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "• 이메일 주소\n• 사용자 이름\n• 프로필 정보",
                fontSize = 14.sp
            )

            Text(
                text = "2. 개인정보의 사용 목적",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "수집된 정보는 서비스 제공, 사용자 인증, 콘텐츠 제공 등의 목적으로만 사용됩니다.",
                fontSize = 14.sp
            )

            Text(
                text = "3. 개인정보 보관 기간",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "사용자가 서비스를 탈퇴하거나 개인정보 삭제를 요청할 때까지 보관됩니다.",
                fontSize = 14.sp
            )
        }
    }
}
