package com.appbuildchat.instaxr.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun TermsOfServiceScreen(
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
                    TermsOfServiceContent(
                        onNavigateBack = onNavigateBack,
                        modifier = modifier
                    )
                }
            }
        }
    } else {
        TermsOfServiceContent(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TermsOfServiceContent(
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
                    "서비스 약관",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "최종 업데이트: 2025년 1월",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            TermsSection(
                title = "1. 서비스 이용 약관",
                content = "InstaXR 서비스(이하 '서비스')를 이용해 주셔서 감사합니다. 본 약관은 귀하와 InstaXR 간의 관계를 규율하며, 서비스 이용 시 적용됩니다."
            )

            TermsSection(
                title = "2. 사용자 계정",
                content = "서비스를 이용하려면 계정을 만들어야 합니다. 계정 정보의 보안을 유지하고 계정에서 발생하는 모든 활동에 대한 책임은 귀하에게 있습니다."
            )

            TermsSection(
                title = "3. 콘텐츠",
                content = "귀하는 게시하는 콘텐츠에 대한 모든 권리를 보유합니다. 다만, 귀하가 콘텐츠를 게시할 때 InstaXR에 해당 콘텐츠를 사용, 수정, 배포할 수 있는 라이선스를 부여합니다."
            )

            TermsSection(
                title = "4. 금지 행위",
                content = "서비스 이용 시 다음 행위는 금지됩니다:\n• 불법적이거나 유해한 콘텐츠 게시\n• 타인의 권리 침해\n• 스팸 또는 악의적인 소프트웨어 배포\n• 서비스의 보안 기능 우회"
            )

            TermsSection(
                title = "5. XR 기능 사용",
                content = "확장 현실(XR) 기능을 사용할 때는 주변 환경에 주의를 기울이고 안전하게 사용하시기 바랍니다. 신체적 불편함을 느끼면 즉시 사용을 중단하세요."
            )

            TermsSection(
                title = "6. 서비스 변경 및 종료",
                content = "InstaXR은 언제든지 서비스를 변경하거나 종료할 수 있는 권리를 보유합니다. 중요한 변경 사항이 있을 경우 사전에 통지하겠습니다."
            )

            TermsSection(
                title = "7. 면책 조항",
                content = "서비스는 '있는 그대로' 제공됩니다. InstaXR은 서비스의 정확성, 신뢰성, 가용성에 대해 보증하지 않습니다."
            )

            TermsSection(
                title = "8. 준거법",
                content = "본 약관은 대한민국 법률에 따라 규율되며 해석됩니다."
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "문의사항이 있으시면 고객센터로 연락해 주시기 바랍니다.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun TermsSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}
