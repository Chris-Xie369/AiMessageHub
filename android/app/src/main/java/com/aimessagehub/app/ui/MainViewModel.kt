package com.aimessagehub.app.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aimessagehub.app.ServiceLocator
import com.aimessagehub.app.ai.AppSettings
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.ExecutionPolicy
import com.aimessagehub.app.service.SuggestionBubbleService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = getApplication<Application>()
    private val repository = ServiceLocator.repository
    private val settingsStore = ServiceLocator.settings

    val settings: StateFlow<AppSettings> = settingsStore.state

    val conversations: StateFlow<List<com.aimessagehub.app.domain.Conversation>> =
        repository.conversations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    val contacts: StateFlow<List<com.aimessagehub.app.domain.ContactPolicy>> =
        repository.contactPolicies.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    val suggestions: StateFlow<List<com.aimessagehub.app.domain.UiSuggestion>> =
        repository.suggestions

    fun messagesFor(conversationId: String): Flow<List<ChatMessage>> =
        repository.messagesFor(conversationId)

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        settingsStore.update(transform)
    }

    fun toggleAppCapture(app: AppSource, enabled: Boolean) {
        updateSettings { current ->
            current.copy(
                captureApps = if (enabled) {
                    current.captureApps + app
                } else {
                    current.captureApps - app
                },
            )
        }
    }

    fun setDefaultPolicy(policy: ExecutionPolicy) {
        updateSettings { it.copy(defaultPolicy = policy) }
    }

    fun setBubbleEnabled(enabled: Boolean) {
        updateSettings { it.copy(bubbleEnabled = enabled) }
        val intent = Intent(app, SuggestionBubbleService::class.java)
        if (enabled) {
            app.startForegroundService(intent)
        } else {
            app.stopService(intent)
        }
    }

    fun requestSuggestion(conversationId: String) {
        repository.requestSuggestion(conversationId)
    }

    fun sendSuggestion(suggestionId: String, variantIndex: Int) {
        repository.sendSuggestion(suggestionId, variantIndex)
    }

    fun cancelAutoReply(suggestionId: String) {
        repository.cancelAutoReply(suggestionId)
    }

    fun ignoreSuggestion(suggestionId: String) {
        repository.ignoreSuggestion(suggestionId)
    }

    fun copySuggestion(text: String) {
        if (text.isBlank()) return
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("AI 回复", text))
    }

    fun setContactAutoReply(
        appSource: AppSource,
        contactId: String,
        contactName: String,
        enabled: Boolean,
    ) {
        repository.setContactAutoReply(appSource, contactId, contactName, enabled)
    }

    fun deleteConversation(conversationId: String) {
        repository.deleteConversation(conversationId)
    }
}

