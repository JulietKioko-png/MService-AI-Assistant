package com.julietkioko.mserviceai.presentation.assistant

/**
 * UI state for the AI Assistant chat screen.
 *
 * @property messages           Chronological list of chat messages (user + assistant).
 * @property inputText          Current text in the compose input field.
 * @property isTyping           True while the assistant "thinks" (drives typing indicator).
 * @property suggestedQuestions Context-aware quick-reply chips shown below the latest assistant message.
 */
data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false,
    val suggestedQuestions: List<String> = listOf(
        "How do I get a KRA PIN?",
        "What documents do I need for a passport?",
        "How do I register for SHIF?",
        "How do I apply for HELB?",
        "What services are available?"
    )
)
