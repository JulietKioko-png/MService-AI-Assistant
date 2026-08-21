package com.julietkioko.mserviceai.presentation.services

import com.julietkioko.mserviceai.domain.model.GovService
import com.julietkioko.mserviceai.domain.model.ServiceCategory

/**
 * UI State for the Services Discovery and Browsing screen.
 */
data class ServicesUiState(
    val selectedCategory: ServiceCategory? = null,
    val searchQuery: String = "",
    val services: List<GovService> = emptyList(),
    val categoryCounts: Map<ServiceCategory, Int> = emptyMap(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)
