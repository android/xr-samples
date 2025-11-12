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
fun PrivacyPolicyScreen(
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
                    PrivacyPolicyContent(
                        onNavigateBack = onNavigateBack,
                        modifier = modifier
                    )
                }
            }
        }
    } else {
        PrivacyPolicyContent(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyPolicyContent(
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
                    "개인정보 처리방침",
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

            PrivacySection(
                title = "1. 수집하는 정보",
                content = "InstaXR은 서비스 제공을 위해 다음과 같은 정보를 수집합니다:\n\n• 계정 정보: 이름, 이메일 주소, 프로필 사진\n• 콘텐츠: 게시물, 댓글, 메시지\n• 사용 정보: 앱 사용 패턴, 기기 정보\n• 위치 정보: XR 기능 사용 시 공간 데이터"
            )

            PrivacySection(
                title = "2. 정보 사용 목적",
                content = "수집된 정보는 다음 목적으로 사용됩니다:\n\n• 서비스 제공 및 개선\n• 맞춤형 콘텐츠 추천\n• 보안 및 사기 방지\n• 고객 지원 제공\n• 법적 의무 준수"
            )

            PrivacySection(
                title = "3. 정보 공유",
                content = "InstaXR은 다음의 경우를 제외하고는 귀하의 개인정보를 제3자와 공유하지 않습니다:\n\n• 귀하의 동의가 있는 경우\n• 법적 요구사항이 있는 경우\n• 서비스 제공에 필요한 파트너와의 공유 (엄격한 보안 기준 적용)"
            )

            PrivacySection(
                title = "4. XR 데이터 처리",
                content = "확장 현실(XR) 기능 사용 시 수집되는 공간 데이터는:\n\n• 기기 내에서만 처리되며 서버로 전송되지 않습니다\n• 즉시 삭제되며 저장되지 않습니다\n• 개인 식별에 사용되지 않습니다"
            )

            PrivacySection(
                title = "5. 데이터 보안",
                content = "InstaXR은 귀하의 정보를 보호하기 위해 업계 표준 보안 조치를 적용합니다:\n\n• 암호화된 데이터 전송\n• 안전한 서버 저장\n• 정기적인 보안 감사\n• 접근 제어 및 모니터링"
            )

            PrivacySection(
                title = "6. 귀하의 권리",
                content = "귀하는 다음과 같은 권리를 가집니다:\n\n• 개인정보 열람 및 수정\n• 개인정보 삭제 요청\n• 데이터 이동권\n• 마케팅 수신 거부\n• 계정 삭제"
            )

            PrivacySection(
                title = "7. 쿠키 및 추적 기술",
                content = "InstaXR은 서비스 개선을 위해 쿠키 및 유사한 기술을 사용합니다. 설정에서 쿠키 사용을 관리할 수 있습니다."
            )

            PrivacySection(
                title = "8. 아동의 개인정보",
                content = "InstaXR은 만 14세 미만 아동의 개인정보를 고의로 수집하지 않습니다. 부모님이나 보호자는 아동의 개인정보 보호를 위해 주의해 주시기 바랍니다."
            )

            PrivacySection(
                title = "9. 정책 변경",
                content = "본 개인정보 처리방침은 수시로 업데이트될 수 있습니다. 중요한 변경 사항이 있을 경우 앱 내 알림을 통해 공지하겠습니다."
            )

            PrivacySection(
                title = "10. 문의",
                content = "개인정보 보호와 관련된 질문이나 우려사항이 있으시면 다음으로 연락해 주세요:\n\n이메일: privacy@instaxr.com\n고객센터: 앱 내 고객센터"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
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
