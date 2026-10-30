package com.julietkioko.mserviceai.presentation.assistant

import com.julietkioko.mserviceai.data.repository.InMemoryServiceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AssistantViewModelTest {

    private lateinit var repository: InMemoryServiceRepository
    private lateinit var viewModel: AssistantViewModel

    @Before
    fun setUp() {
        repository = InMemoryServiceRepository()
        viewModel = AssistantViewModel(repository)
    }

    @Test
    fun `initial state contains welcome message and default suggestions`() {
        val state = viewModel.uiState.value

        assertEquals(1, state.messages.size)
        val welcome = state.messages.first()
        assertFalse(welcome.isFromUser)
        assertTrue(welcome.content.contains("MService AI assistant"))
        assertFalse(state.isTyping)
        assertEquals("", state.inputText)
        assertTrue(state.suggestedQuestions.isNotEmpty())
    }

    @Test
    fun `onInputChanged updates inputText in uiState`() {
        viewModel.onInputChanged("How do I renew my driving licence?")

        val state = viewModel.uiState.value
        assertEquals("How do I renew my driving licence?", state.inputText)
    }

    @Test
    fun `onSendMessage with blank text does not add messages`() {
        val initialCount = viewModel.uiState.value.messages.size

        viewModel.onInputChanged("   ")
        viewModel.onSendMessage()

        val state = viewModel.uiState.value
        assertEquals(initialCount, state.messages.size)
    }

    @Test
    fun `greeting query returns polite assistant greeting`() {
        viewModel.onInputChanged("Hello there!")
        viewModel.onSendMessage()

        val state = viewModel.uiState.value
        assertEquals(3, state.messages.size) // Welcome + User + Assistant

        val userMsg = state.messages[1]
        assertTrue(userMsg.isFromUser)
        assertEquals("Hello there!", userMsg.content)

        val botMsg = state.messages[2]
        assertFalse(botMsg.isFromUser)
        assertTrue(botMsg.content.contains("Hello!") || botMsg.content.contains("help"))
        assertFalse(state.isTyping)
        assertEquals("", state.inputText)
    }

    @Test
    fun `service query for KRA PIN returns matching service details with relatedServiceId`() {
        viewModel.onInputChanged("How to get KRA PIN")
        viewModel.onSendMessage()

        val state = viewModel.uiState.value
        assertEquals(3, state.messages.size)

        val botMsg = state.messages.last()
        assertFalse(botMsg.isFromUser)
        assertEquals("kra-pin-registration", botMsg.relatedServiceId)
        assertTrue(botMsg.content.contains("KRA PIN"))
        assertTrue(state.suggestedQuestions.isNotEmpty())
    }

    @Test
    fun `specific intent for fees returns cost information`() {
        viewModel.onInputChanged("What are the fees for passport application?")
        viewModel.onSendMessage()

        val state = viewModel.uiState.value
        val botMsg = state.messages.last()

        assertFalse(botMsg.isFromUser)
        assertEquals("passport-application-renewal", botMsg.relatedServiceId)
        assertTrue(botMsg.content.lowercase().contains("fee") || botMsg.content.contains("Ksh"))
    }

    @Test
    fun `specific intent for requirements returns requirements list`() {
        viewModel.onInputChanged("What do I need for good conduct certificate?")
        viewModel.onSendMessage()

        val state = viewModel.uiState.value
        val botMsg = state.messages.last()

        assertFalse(botMsg.isFromUser)
        assertEquals("police-clearance-good-conduct", botMsg.relatedServiceId)
        assertTrue(botMsg.content.lowercase().contains("requirement") || botMsg.content.contains("•"))
    }

    @Test
    fun `unrecognized query returns fallback helpful response`() {
        viewModel.onInputChanged("xyzunknownrandomquery12345")
        viewModel.onSendMessage()

        val state = viewModel.uiState.value
        val botMsg = state.messages.last()

        assertFalse(botMsg.isFromUser)
        assertTrue(botMsg.content.contains("couldn't find") || botMsg.content.contains("help with"))
        assertTrue(state.suggestedQuestions.isNotEmpty())
    }

    @Test
    fun `onSuggestionSelected sets input and executes search`() {
        val suggestion = "What is SHIF?"
        viewModel.onSuggestionSelected(suggestion)

        val state = viewModel.uiState.value
        assertEquals(3, state.messages.size)
        assertEquals(suggestion, state.messages[1].content)
        assertFalse(state.messages[2].isFromUser)
    }
}
