package com.appbuildchat.instaxr.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings feature
 */
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val settingsMap = mutableMapOf<String, Boolean>()

    init {
        loadSettings()
    }

    fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ToggleSetting -> toggleSetting(action.settingId)
            is SettingsAction.NavigateToDetail -> {
                // Navigation is handled in UI layer
            }
            is SettingsAction.Logout -> logout()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = SettingsUiState.Loading

                // Initialize default toggle states
                settingsMap["account_status"] = false
                settingsMap["push_notifications"] = true
                settingsMap["email_notifications"] = false

                // Load mock settings data
                val sections = getMockSettingsSections()
                _uiState.value = SettingsUiState.Success(sections)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun toggleSetting(settingId: String) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            // Toggle the setting value
            val currentValue = settingsMap[settingId] ?: false
            settingsMap[settingId] = !currentValue

            // Update UI with new toggle state
            val updatedSections = currentState.sections.map { section ->
                section.copy(
                    items = section.items.map { item ->
                        if (item.id == settingId) {
                            item.copy(isToggled = settingsMap[settingId] ?: false)
                        } else {
                            item
                        }
                    }
                )
            }
            _uiState.value = SettingsUiState.Success(updatedSections)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            // TODO: Implement actual logout logic
            // For now, just show a mock success
            println("Logout action triggered")
        }
    }

    private fun getMockSettingsSections(): List<SettingsSection> {
        return listOf(
            // Account Section
            SettingsSection(
                header = "계정",
                items = listOf(
                    SettingsItem(
                        id = "edit_profile",
                        title = "프로필 편집",
                        subtitle = "사진, 이름, 소개 등을 변경하세요",
                        icon = Icons.Default.Person,
                        type = SettingsItemType.Navigation
                    ),
                    SettingsItem(
                        id = "change_password",
                        title = "비밀번호 변경",
                        icon = Icons.Default.Lock,
                        type = SettingsItemType.Navigation
                    ),
                    SettingsItem(
                        id = "privacy_settings",
                        title = "개인정보 설정",
                        subtitle = "계정 공개 범위 및 개인정보를 관리하세요",
                        icon = Icons.Default.Settings,
                        type = SettingsItemType.Navigation
                    ),
                    SettingsItem(
                        id = "account_status",
                        title = "계정 상태",
                        subtitle = "계정 활성화 상태",
                        icon = Icons.Default.AccountCircle,
                        type = SettingsItemType.Toggle,
                        isToggled = settingsMap["account_status"] ?: false
                    )
                )
            ),
            // Notifications Section
            SettingsSection(
                header = "알림",
                items = listOf(
                    SettingsItem(
                        id = "push_notifications",
                        title = "푸시 알림",
                        subtitle = "새 메시지 및 활동 알림 받기",
                        icon = Icons.Default.Notifications,
                        type = SettingsItemType.Toggle,
                        isToggled = settingsMap["push_notifications"] ?: true
                    ),
                    SettingsItem(
                        id = "email_notifications",
                        title = "이메일 알림",
                        subtitle = "이메일로 알림 받기",
                        icon = Icons.Default.Email,
                        type = SettingsItemType.Toggle,
                        isToggled = settingsMap["email_notifications"] ?: false
                    )
                )
            ),
            // About & Support Section
            SettingsSection(
                header = "정보 및 지원",
                items = listOf(
                    SettingsItem(
                        id = "about",
                        title = "InstaXR 정보",
                        subtitle = "버전 및 앱 정보",
                        icon = Icons.Default.Info,
                        type = SettingsItemType.Navigation
                    ),
                    SettingsItem(
                        id = "help",
                        title = "고객센터",
                        subtitle = "도움말 및 자주 묻는 질문",
                        icon = Icons.Default.Info,
                        type = SettingsItemType.Navigation
                    ),
                    SettingsItem(
                        id = "terms",
                        title = "서비스 약관",
                        icon = Icons.Default.Settings,
                        type = SettingsItemType.Navigation
                    ),
                    SettingsItem(
                        id = "privacy_policy",
                        title = "개인정보 처리방침",
                        icon = Icons.Default.Settings,
                        type = SettingsItemType.Navigation
                    )
                )
            ),
            // Account Actions Section
            SettingsSection(
                header = "계정 관리",
                items = listOf(
                    SettingsItem(
                        id = "logout",
                        title = "로그아웃",
                        icon = Icons.Default.ExitToApp,
                        type = SettingsItemType.Action,
                        isDestructive = true
                    )
                )
            )
        )
    }
}

/**
 * UI State for Settings screen
 */
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val sections: List<SettingsSection>) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}
