package com.aimessagehub.app.data

import android.view.accessibility.AccessibilityNodeInfo
import com.aimessagehub.app.ai.MessageDeduplicator
import com.aimessagehub.app.ai.SettingsStore
import com.aimessagehub.app.ai.SuggestionEngine
import com.aimessagehub.app.data.local.AppDatabase
import com.aimessagehub.app.data.local.ContactPolicyEntity
import com.aimessagehub.app.data.local.ConversationEntity
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.ContactPolicy
import com.aimessagehub.app.domain.Conversation
import com.aimessagehub.app.domain.ExecutionPolicy
import com.aimessagehub.app.domain.MessageDirection
import com.aimessagehub.app.domain.NotificationEnvelope
import com.aimessagehub.app.domain.SuggestionRequest
import com.aimessagehub.app.domain.SuggestionStatus
import com.aimessagehub.app.domain.UiSuggestion
import com.aimessagehub.app.domain.AdapterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

interface ReplyExecutor {
    fun canExecute(app: AppSource): Boolean
    fun execute(app: AppSource, conversationId: String, text: String): Boolean
}

class HubRepository(
    private val database: AppDatabase,
    private val registry: AdapterRegistry,
    private val engine: SuggestionEngine,
    private val settings: SettingsStore,
    private val scope: CoroutineScope,
) {
    private val deduplicator = MessageDeduplicator()
    private val messageDao = database.messageDao()
    private val conversationDao = database.conversationDao()
    private val policyDao = database.contactPolicyDao()

    private val suggestionLocks = mutableSetOf<String>()
    private val autoCancellations = mutableMapOf<String, Job>()
    private val _suggestions = MutableStateFlow<List<UiSuggestion>>(emptyList())

    @Volatile
    var replyExecutor: ReplyExecutor? = null

    val suggestions: StateFlow<List<UiSuggestion>> = _suggestions.asStateFlow()

    val conversations: Flow<List<Conversation>> =
        conversationDao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    val contactPolicies: Flow<List<ContactPolicy>> =
        policyDao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    fun messagesFor(conversationId: String): Flow<List<ChatMessage>> =
        messageDao.observeForConversation(conversationId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    fun onNotification(envelope: NotificationEnvelope) {
        if (!settings.isCaptureEnabled(envelope.packageName)) return
        val adapter = registry.forPackage(envelope.packageName)
        val message = adapter.parseNotification(envelope) ?: return
        if (deduplicator.isDuplicate(message)) return
        persistAndMaybeSuggest(message)
    }

    fun onAccessibilityRoot(packageName: String?, root: AccessibilityNodeInfo) {
        if (!settings.isCaptureEnabled(packageName)) return
        val adapter = registry.forPackage(packageName)
        val context = adapter.extractConversation(root) ?: return
        context.messages.forEach { message ->
            if (!deduplicator.isDuplicate(message)) {
                scope.launch {
                    persistMessage(message)
                }
            }
        }
    }

    fun requestSuggestion(conversationId: String) {
        scope.launch {
            val history = messageDao.latestMessages(conversationId, 20).map { it.toDomain() }
            val last = history.lastOrNull() ?: return@launch
            launchSuggestion(
                conversationId = conversationId,
                app = last.app,
                contactName = last.conversationTitle,
                history = history,
            )
        }
    }

    fun sendSuggestion(suggestionId: String, variantIndex: Int) {
        executeSuggestion(suggestionId, variantIndex)
    }

    fun cancelAutoReply(suggestionId: String) {
        autoCancellations.remove(suggestionId)?.cancel()
    }

    fun ignoreSuggestion(suggestionId: String) {
        updateStatus(suggestionId, SuggestionStatus.IGNORED)
    }

    fun setContactAutoReply(
        app: AppSource,
        contactId: String,
        contactName: String,
        enabled: Boolean,
    ) {
        scope.launch {
            policyDao.upsert(
                ContactPolicyEntity(
                    app = app.name,
                    contactId = contactId,
                    contactName = contactName,
                    autoReplyEnabled = enabled,
                ),
            )
        }
    }

    fun deleteConversation(conversationId: String) {
        scope.launch {
            conversationDao.delete(conversationId)
        }
    }

    private fun persistAndMaybeSuggest(message: ChatMessage) {
        scope.launch {
            persistMessage(message)
            val history = messageDao.latestMessages(message.conversationId, 20).map { it.toDomain() }
            launchSuggestion(
                conversationId = message.conversationId,
                app = message.app,
                contactName = message.conversationTitle,
                history = history,
            )
        }
    }

    private suspend fun persistMessage(message: ChatMessage) {
        messageDao.insert(message.toEntity())
        conversationDao.upsert(
            ConversationEntity(
                id = message.conversationId,
                app = message.app.name,
                title = message.conversationTitle,
                lastMessageAt = message.timestamp,
                lastPreview = message.text.take(60),
            ),
        )
    }

    private fun launchSuggestion(
        conversationId: String,
        app: AppSource,
        contactName: String,
        history: List<ChatMessage>,
    ) {
        val lockKey = "$app:$conversationId"
        synchronized(suggestionLocks) {
            if (lockKey in suggestionLocks) return
            suggestionLocks += lockKey
        }
        scope.launch {
            val id = UUID.randomUUID().toString()
            _suggestions.value += UiSuggestion(
                id = id,
                app = app,
                conversationId = conversationId,
                conversationTitle = contactName,
                variants = emptyList(),
                status = SuggestionStatus.GENERATING,
                createdAt = System.currentTimeMillis(),
            )
            val request = SuggestionRequest(
                conversationId = conversationId,
                app = app,
                contactName = contactName,
                history = history,
                persona = settings.state.value.persona,
                instructions = settings.state.value.instructions,
            )
            val result = engine.suggest(request, settings.state.value.toAIConfig())
            val next = _suggestions.value.map { suggestion ->
                if (suggestion.id == id) {
                    suggestion.copy(
                        variants = result.variants,
                        status = if (result.isSuccess) SuggestionStatus.READY else SuggestionStatus.ERROR,
                        error = result.error,
                    )
                } else {
                    suggestion
                }
            }
            _suggestions.value = next

            if (result.isSuccess && settings.state.value.defaultPolicy == ExecutionPolicy.AUTO_WHITELIST) {
                val policy = policyDao.get(app.name, conversationId)
                if (policy?.autoReplyEnabled == true) {
                    scheduleAutoSend(id, result.variants.first())
                }
            }
            synchronized(suggestionLocks) {
                suggestionLocks -= lockKey
            }
        }
    }

    private fun scheduleAutoSend(suggestionId: String, text: String) {
        autoCancellations.remove(suggestionId)?.cancel()
        val job = scope.launch {
            delay(5_000)
            val current = _suggestions.value.firstOrNull { it.id == suggestionId } ?: return@launch
            if (current.status != SuggestionStatus.READY) return@launch
            executeSuggestion(suggestionId, 0)
        }
        autoCancellations[suggestionId] = job
    }

    private fun executeSuggestion(suggestionId: String, variantIndex: Int) {
        scope.launch {
            val suggestion = _suggestions.value.firstOrNull { it.id == suggestionId } ?: return@launch
            val text = suggestion.variants.getOrNull(variantIndex) ?: return@launch
            autoCancellations.remove(suggestionId)?.cancel()
            updateStatus(suggestionId, SuggestionStatus.SENDING)
            val executor = replyExecutor
            val executed = executor != null &&
                executor.canExecute(suggestion.app) &&
                executor.execute(suggestion.app, suggestion.conversationId, text)
            if (executed) {
                updateStatus(suggestionId, SuggestionStatus.SENT)
                val outgoing = ChatMessage(
                    id = "sent:${System.currentTimeMillis()}:${text.hashCode()}",
                    app = suggestion.app,
                    conversationId = suggestion.conversationId,
                    conversationTitle = suggestion.conversationTitle,
                    sender = "我",
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    direction = MessageDirection.OUTGOING,
                )
                persistMessage(outgoing)
            } else {
                updateStatus(
                    suggestionId,
                    SuggestionStatus.READY,
                    "请先打开对应聊天窗口后重试",
                )
            }
        }
    }

    private fun updateStatus(
        suggestionId: String,
        status: SuggestionStatus,
        error: String? = null,
    ) {
        _suggestions.value = _suggestions.value.map { suggestion ->
            if (suggestion.id == suggestionId) {
                suggestion.copy(status = status, error = error)
            } else {
                suggestion
            }
        }
    }
}
