package com.example.helloandroidxr.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile feature
 */
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.Refresh -> loadProfile()
            is ProfileAction.EditProfile -> editProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading
                // TODO: Load profile from repository
                _uiState.value = ProfileUiState.Success
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun editProfile() {
        // TODO: Implement edit profile logic
    }
}

/**
 * UI State for Profile screen
 */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Success : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
