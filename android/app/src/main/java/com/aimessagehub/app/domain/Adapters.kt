package com.aimessagehub.app.domain

class WeChatAdapter : NotificationChatAdapter(AppSource.WECHAT)

class QQAdapter : NotificationChatAdapter(AppSource.QQ)

class SoulAdapter : NotificationChatAdapter(AppSource.SOUL)

class XiaohongshuAdapter : NotificationChatAdapter(AppSource.XIAOHONGSHU)

class GenericAdapter : NotificationChatAdapter(AppSource.GENERIC) {
    override fun parseNotification(envelope: NotificationEnvelope): ChatMessage? {
        val knownPackages = AppSource.entries
            .filter { it != AppSource.GENERIC }
            .map { it.packageName }
        if (envelope.packageName in knownPackages) return null
        return super.parseNotification(envelope)
    }
}

class AdapterRegistry {
    private val adapters = listOf(
        WeChatAdapter(),
        QQAdapter(),
        SoulAdapter(),
        XiaohongshuAdapter(),
        GenericAdapter(),
    )

    fun forPackage(packageName: String?): ChatAdapter {
        val source = AppSource.fromPackageName(packageName)
        return adapters.first { it.source == source }
    }

    fun forSource(source: AppSource): ChatAdapter = adapters.first { it.source == source }
}

