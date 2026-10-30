package com.julietkioko.mserviceai.presentation.details

import com.julietkioko.mserviceai.data.repository.InMemoryServiceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DetailsViewModelTest {

    private lateinit var repository: InMemoryServiceRepository

    @Before
    fun setUp() {
        repository = InMemoryServiceRepository()
    }

    @Test
    fun `valid serviceId loads correct service details`() {
        val viewModel = DetailsViewModel(
            serviceId = "sha-shif-registration",
            repository = repository
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNotNull(state.service)
        assertEquals("sha-shif-registration", state.service?.id)
        assertEquals("SHIF/SHA Registration & Contribution", state.service?.title)
        assertFalse(state.service?.requirements.isNullOrEmpty())
        assertFalse(state.service?.steps.isNullOrEmpty())
        assertTrue(state.service?.portalUrl?.startsWith("http") == true)
    }

    @Test
    fun `invalid serviceId sets errorMessage and null service`() {
        val viewModel = DetailsViewModel(
            serviceId = "non-existent-service-123",
            repository = repository
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.service)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage?.contains("non-existent-service-123") == true)
    }
}
