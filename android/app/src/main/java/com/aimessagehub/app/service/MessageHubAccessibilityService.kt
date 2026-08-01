package com.aimessagehub.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.aimessagehub.app.ServiceLocator
import com.aimessagehub.app.data.ReplyExecutor
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.NotificationEnvelope

class MessageHubAccessibilityService : AccessibilityService(), ReplyExecutor {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        ServiceLocator.repository.replyExecutor = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString()
        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                val text = event.text.joinToString("\n").trim()
                if (text.isNotEmpty()) {
                    ServiceLocator.repository.onNotification(
                        NotificationEnvelope(
                            packageName = packageName.orEmpty(),
                            notificationKey = "accessibility:${event.eventTime}",
                            title = null,
                            text = text,
                            postTime = System.currentTimeMillis(),
                        ),
                    )
                }
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                rootInActiveWindow?.let { root ->
                    ServiceLocator.repository.onAccessibilityRoot(packageName, root)
                }
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun canExecute(app: AppSource): Boolean {
        return rootInActiveWindow?.packageName?.toString() == app.packageName
    }

    override fun execute(
        app: AppSource,
        conversationId: String,
        text: String,
    ): Boolean {
        val root = rootInActiveWindow ?: return false
        val adapter = ServiceLocator.registry.forPackage(root.packageName?.toString())
        val target = adapter.prepareReply(root) ?: return false
        return adapter.sendReply(root, target, text)
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
            ServiceLocator.repository.replyExecutor = null
        }
        super.onDestroy()
    }

    companion object {
        @Volatile
        var instance: MessageHubAccessibilityService? = null
    }
}

