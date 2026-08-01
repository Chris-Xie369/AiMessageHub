package com.aimessagehub.app.domain

enum class MessageDirection {
    INCOMING,
    OUTGOING,
}

enum class ExecutionPolicy {
    SUGGEST,
    ONE_TAP,
    AUTO_WHITELIST,
}

data class ChatMessage(
    val id: String,
    val app: AppSource,
    val conversationId: String,
    val conversationTitle: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val direction: MessageDirection,
)

data class Conversation(
    val id: String,
    val app: AppSource,
    val title: String,
    val lastMessageAt: Long,
    val lastPreview: String,
)

data class ContactPolicy(
    val app: AppSource,
    val contactId: String,
    val contactName: String,
    val autoReplyEnabled: Boolean,
)

data class SuggestionRequest(
    val conversationId: String,
    val app: AppSource,
    val contactName: String,
    val history: List<ChatMessage>,
    val persona: String,
    val instructions: String,
    val variantCount: Int = 3,
)

data class SuggestionResult(
    val variants: List<String>,
    val error: String? = null,
) {
    val isSuccess: Boolean
        get() = error == null && variants.isNotEmpty()
}

enum class SuggestionStatus {
    GENERATING,
    READY,
    ERROR,
    SENDING,
    SENT,
    IGNORED,
}

data class UiSuggestion(
    val id: String,
    val app: AppSource,
    val conversationId: String,
    val conversationTitle: String,
    val variants: List<String>,
    val status: SuggestionStatus,
    val error: String? = null,
    val createdAt: Long,
)

