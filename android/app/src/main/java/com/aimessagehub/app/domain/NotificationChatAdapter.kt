package com.aimessagehub.app.domain

import android.view.accessibility.AccessibilityNodeInfo

abstract class NotificationChatAdapter(
    final override val source: AppSource,
) : ChatAdapter {

    override val capabilities: Set<AdapterCapability> = setOf(
        AdapterCapability.NOTIFICATION_PARSE,
        AdapterCapability.CONVERSATION_READ,
        AdapterCapability.ONE_TAP_REPLY,
    )

    override fun parseNotification(envelope: NotificationEnvelope): ChatMessage? {
        if (source != AppSource.GENERIC && source.packageName != envelope.packageName) return null
        val text = envelope.text?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val title = envelope.title?.trim() ?: "未知联系人"
        val conversationId = conversationIdFor(title, envelope.postTime)
        return ChatMessage(
            id = envelope.notificationKey ?: "notify:${envelope.postTime}:${text.hashCode()}",
            app = source,
            conversationId = conversationId,
            conversationTitle = title,
            sender = title,
            text = text,
            timestamp = envelope.postTime,
            direction = MessageDirection.INCOMING,
        )
    }

    override fun extractConversation(root: AccessibilityNodeInfo): ChatContext? {
        val texts = AccessibilityNodes.collectText(root)
        val latest = texts.lastOrNull()?.take(120) ?: return null
        val conversationId = "${source.name}:screen:${latest.hashCode()}"
        val message = ChatMessage(
            id = "screen:${System.currentTimeMillis()}:${latest.hashCode()}",
            app = source,
            conversationId = conversationId,
            conversationTitle = source.displayName,
            sender = source.displayName,
            text = latest,
            timestamp = System.currentTimeMillis(),
            direction = MessageDirection.INCOMING,
        )
        return ChatContext(conversationId, source.displayName, listOf(message))
    }

    override fun prepareReply(root: AccessibilityNodeInfo): ReplyTarget? {
        val input = AccessibilityNodes.findEditText(root) ?: return null
        val send = AccessibilityNodes.findSendButton(root)
        return ReplyTarget(input, send)
    }

    override fun sendReply(
        root: AccessibilityNodeInfo,
        target: ReplyTarget,
        text: String,
    ): Boolean {
        val input = target.inputNode ?: return false
        val bundle = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        if (!input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)) return false
        val send = target.sendNode ?: AccessibilityNodes.findSendButton(root) ?: return false
        return send.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    protected open fun conversationIdFor(title: String, timestamp: Long): String =
        "${source.name}:$title"
}
