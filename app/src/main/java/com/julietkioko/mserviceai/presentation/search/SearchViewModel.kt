package com.julietkioko.mserviceai.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.julietkioko.mserviceai.data.repository.InMemoryServiceRepository
import com.julietkioko.mserviceai.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing search input, suggestions, and instantaneous query filtering.
 */
class SearchViewModel(
    private val repository: ServiceRepository = InMemoryServiceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(isLoading = true))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        performSearch(query)
    }

    fun onSuggestionSelected(suggestion: String) {
        _uiState.update { it.copy(searchQuery = suggestion) }
        performSearch(suggestion)
    }

    fun clearSearch() {
        if (_uiState.value.searchQuery.isEmpty()) return
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList()
            )
        }
    }

    private fun loadInitialData() {
        val allServices = repository.getAllServices()
        val popular = allServices.filter { it.isPopular }
        _uiState.update {
            it.copy(
                popularServices = popular,
                isLoading = false
            )
        }
    }

    private fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        val results = repository.searchServices(trimmed)
        _uiState.update { it.copy(searchResults = results) }
    }

    companion object {
        fun provideFactory(
            repository: ServiceRepository = InMemoryServiceRepository()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(repository) as T
            }
        }
    }
}
