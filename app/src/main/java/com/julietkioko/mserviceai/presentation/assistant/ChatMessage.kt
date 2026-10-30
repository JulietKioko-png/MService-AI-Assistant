package com.julietkioko.mserviceai.presentation.assistant

import java.util.UUID

/**
 * Represents a single message in the AI Assistant conversation.
 *
 * @property id        Unique identifier for stable LazyColumn keys.
 * @property content   The display text of the message.
 * @property isFromUser True for user-sent messages, false for assistant responses.
 * @property timestamp Epoch millis when the message was created.
 * @property relatedServiceId Optional service ID enabling "tap to view details" on assistant messages.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedServiceId: String? = null
)
