package com.example.helloandroidxr.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Search feature
 */
class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadSearchResults()
    }

    fun handleAction(action: SearchAction) {
        when (action) {
            is SearchAction.Search -> search(action.query)
            is SearchAction.ClearSearch -> clearSearch()
        }
    }

    private fun loadSearchResults() {
        viewModelScope.launch {
            try {
                _uiState.value = SearchUiState.Loading
                // TODO: Load search results from repository
                _uiState.value = SearchUiState.Success
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun search(query: String) {
        // TODO: Implement search logic
    }

    private fun clearSearch() {
        // TODO: Implement clear search logic
    }
}

/**
 * UI State for Search screen
 */
sealed interface SearchUiState {
    data object Loading : SearchUiState
    data object Success : SearchUiState
    data class Error(val message: String) : SearchUiState
}
