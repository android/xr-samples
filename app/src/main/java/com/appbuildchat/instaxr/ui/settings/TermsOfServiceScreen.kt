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
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TermsOfServiceContent(
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TermsOfServiceContent(
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
                        text = "서비스 약관",
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
                text = "서비스 이용약관",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "InstaXR 서비스 이용약관에 오신 것을 환영합니다.",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "제1조 목적",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "본 약관은 InstaXR이 제공하는 서비스의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.",
                fontSize = 14.sp
            )

            Text(
                text = "제2조 정의",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "1. \"서비스\"라 함은 InstaXR이 제공하는 XR 소셜 미디어 플랫폼을 의미합니다.\n2. \"이용자\"라 함은 본 약관에 따라 서비스를 이용하는 회원 및 비회원을 말합니다.",
                fontSize = 14.sp
            )

            Text(
                text = "제3조 약관의 효력 및 변경",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "본 약관은 서비스를 이용하고자 하는 모든 이용자에게 적용되며, 회사는 필요한 경우 약관을 변경할 수 있습니다.",
                fontSize = 14.sp
            )
        }
    }
}
