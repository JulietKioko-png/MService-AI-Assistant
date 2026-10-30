package com.julietkioko.mserviceai.presentation.search

import com.julietkioko.mserviceai.domain.model.GovService

/**
 * UI state for the dedicated Search screen.
 */
data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<GovService> = emptyList(),
    val popularServices: List<GovService> = emptyList(),
    val quickSuggestions: List<String> = listOf(
        "KRA PIN",
        "Passport",
        "SHIF",
        "National ID",
        "HELB",
        "Business Registration",
        "Land Search",
        "Driving Licence",
        "Good Conduct"
    ),
    val isLoading: Boolean = false
)
