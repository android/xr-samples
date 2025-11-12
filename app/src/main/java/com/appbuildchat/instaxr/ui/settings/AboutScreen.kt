package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun AboutScreen(
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
                    AboutContent(
                        onNavigateBack = onNavigateBack,
                        modifier = modifier
                    )
                }
            }
        }
    } else {
        AboutContent(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "InstaXR 정보",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Logo/Icon Placeholder
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.large,
                color = Color(0xFF0095F6)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "XR",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // App Name
            Text(
                text = "InstaXR",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Version
            Text(
                text = "버전 1.0.0",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = "InstaXR은 확장 현실(XR) 기술을 활용한\n차세대 소셜 미디어 플랫폼입니다.\n\n공간 UI와 몰입형 환경에서\n새로운 방식으로 소통하고 공유하세요.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Copyright
            Text(
                text = "© 2025 InstaXR\nAll rights reserved.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            // Additional Info
            Divider(
                color = Color(0xFF333333),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow("빌드 번호", "2025.01.001")
                InfoRow("최소 Android 버전", "API 24 이상")
                InfoRow("개발사", "AppBuildChat")
                InfoRow("라이선스", "MIT License")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
