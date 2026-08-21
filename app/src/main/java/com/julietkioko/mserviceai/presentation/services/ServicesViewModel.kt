package com.julietkioko.mserviceai.presentation.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.julietkioko.mserviceai.data.repository.InMemoryServiceRepository
import com.julietkioko.mserviceai.domain.model.ServiceCategory
import com.julietkioko.mserviceai.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing discovery, category filtering, and reactive searching of Kenyan public services.
 */
class ServicesViewModel(
    private val repository: ServiceRepository = InMemoryServiceRepository(),
    initialCategory: ServiceCategory? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ServicesUiState(
            selectedCategory = initialCategory,
            isLoading = true
        )
    )
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    init {
        loadServices(category = initialCategory)
    }

    /**
     * Updates selected category and re-applies any active search filter.
     * Passing null selects "All".
     */
    fun onCategorySelected(category: ServiceCategory?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    /**
     * Re-filters services reactively based on user search query.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    /**
     * Clears only the search input while preserving the active category.
     */
    fun clearSearch() {
        if (_uiState.value.searchQuery.isEmpty()) return
        _uiState.update { it.copy(searchQuery = "") }
        applyFilters()
    }

    /**
     * Resets both search query and category filters to show all services.
     */
    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                searchQuery = ""
            )
        }
        applyFilters()
    }

    private fun loadServices(category: ServiceCategory?) {
        val all = repository.getAllServices()
        val counts = ServiceCategory.entries.associateWith { cat ->
            all.count { it.category == cat }
        }
        val initialFiltered = if (category != null) {
            repository.getServicesByCategory(category)
        } else {
            all
        }

        _uiState.update {
            it.copy(
                services = initialFiltered,
                categoryCounts = counts,
                totalCount = all.size,
                isLoading = false
            )
        }
    }

    private fun applyFilters() {
        val currentCategory = _uiState.value.selectedCategory
        val query = _uiState.value.searchQuery.trim()

        val baseList = if (currentCategory != null) {
            repository.getServicesByCategory(currentCategory)
        } else {
            repository.getAllServices()
        }

        val filtered = if (query.isEmpty()) {
            baseList
        } else {
            baseList.filter { service ->
                service.title.contains(query, ignoreCase = true) ||
                service.agency.contains(query, ignoreCase = true) ||
                service.summary.contains(query, ignoreCase = true) ||
                service.description.contains(query, ignoreCase = true) ||
                service.requirements.any { it.contains(query, ignoreCase = true) }
            }
        }

        _uiState.update { it.copy(services = filtered) }
    }

    companion object {
        fun provideFactory(
            repository: ServiceRepository = InMemoryServiceRepository(),
            initialCategory: ServiceCategory? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ServicesViewModel(repository, initialCategory) as T
            }
        }
    }
}
