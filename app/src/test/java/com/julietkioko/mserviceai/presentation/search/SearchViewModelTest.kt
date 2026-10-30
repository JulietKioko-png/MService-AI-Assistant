package com.julietkioko.mserviceai.presentation.search

import com.julietkioko.mserviceai.data.repository.InMemoryServiceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchViewModelTest {

    private lateinit var repository: InMemoryServiceRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        repository = InMemoryServiceRepository()
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun `initial state loads popular services and empty query`() {
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.popularServices.isEmpty())
        assertTrue(state.popularServices.all { it.isPopular })
        assertFalse(state.isLoading)
    }

    @Test
    fun `onSearchQueryChanged updates query and search results`() {
        viewModel.onSearchQueryChanged("KRA PIN")

        val state = viewModel.uiState.value
        assertEquals("KRA PIN", state.searchQuery)
        assertFalse(state.searchResults.isEmpty())
        assertTrue(state.searchResults.any { it.id == "kra-pin-registration" })
    }

    @Test
    fun `onSearchQueryChanged with empty text clears search results`() {
        viewModel.onSearchQueryChanged("Passport")
        assertFalse(viewModel.uiState.value.searchResults.isEmpty())

        viewModel.onSearchQueryChanged("")
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
    }

    @Test
    fun `onSuggestionSelected sets query and filters results`() {
        viewModel.onSuggestionSelected("HELB")

        val state = viewModel.uiState.value
        assertEquals("HELB", state.searchQuery)
        assertFalse(state.searchResults.isEmpty())
        assertTrue(state.searchResults.any { it.id == "helb-loan-application" })
    }

    @Test
    fun `clearSearch resets query and search results`() {
        viewModel.onSearchQueryChanged("Driving Licence")
        assertFalse(viewModel.uiState.value.searchResults.isEmpty())

        viewModel.clearSearch()
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
    }
}
