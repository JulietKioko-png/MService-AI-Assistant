package com.julietkioko.mserviceai.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.julietkioko.mserviceai.data.repository.InMemoryServiceRepository
import com.julietkioko.mserviceai.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing the details view for a selected Kenyan government service.
 */
class DetailsViewModel(
    private val serviceId: String,
    private val repository: ServiceRepository = InMemoryServiceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        loadServiceDetails()
    }

    private fun loadServiceDetails() {
        val service = repository.getServiceById(serviceId)
        if (service != null) {
            _uiState.update {
                it.copy(
                    service = service,
                    isLoading = false,
                    errorMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    service = null,
                    isLoading = false,
                    errorMessage = "Service not found with ID: $serviceId"
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            serviceId: String,
            repository: ServiceRepository = InMemoryServiceRepository()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailsViewModel(serviceId, repository) as T
            }
        }
    }
}
