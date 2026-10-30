package com.julietkioko.mserviceai.presentation.details

import com.julietkioko.mserviceai.domain.model.GovService

/**
 * UI State for the Service Details screen.
 */
data class DetailsUiState(
    val service: GovService? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
