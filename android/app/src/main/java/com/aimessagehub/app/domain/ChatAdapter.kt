package com.aimessagehub.app.domain

import android.view.accessibility.AccessibilityNodeInfo

data class NotificationEnvelope(
    val packageName: String,
    val notificationKey: String?,
    val title: String?,
    val text: String?,
    val postTime: Long,
)

data class ChatContext(
    val conversationId: String,
    val conversationTitle: String,
    val messages: List<ChatMessage>,
)

data class ReplyTarget(
    val inputNode: AccessibilityNodeInfo?,
    val sendNode: AccessibilityNodeInfo?,
)

enum class AdapterCapability {
    NOTIFICATION_PARSE,
    CONVERSATION_READ,
    ONE_TAP_REPLY,
}

interface ChatAdapter {
    val source: AppSource
    val capabilities: Set<AdapterCapability>

    fun parseNotification(envelope: NotificationEnvelope): ChatMessage?
    fun extractConversation(root: AccessibilityNodeInfo): ChatContext?
    fun prepareReply(root: AccessibilityNodeInfo): ReplyTarget?
    fun sendReply(root: AccessibilityNodeInfo, target: ReplyTarget, text: String): Boolean
}

