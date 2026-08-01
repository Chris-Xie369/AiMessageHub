package com.aimessagehub.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationAdapterTest {
    @Test
    fun wechatNotificationParsesTitleAndText() {
        val adapter = WeChatAdapter()
        val message = adapter.parseNotification(
            NotificationEnvelope(
                packageName = "com.tencent.mm",
                notificationKey = "notify-1",
                title = "Alice",
                text = "晚上一起吃饭吗",
                postTime = 1_700_000_000_000,
            ),
        )

        assertNotNull(message)
        message?.let {
            assertEquals(AppSource.WECHAT, it.app)
            assertEquals("Alice", it.conversationTitle)
            assertEquals("晚上一起吃饭吗", it.text)
            assertEquals(MessageDirection.INCOMING, it.direction)
        }
    }

    @Test
    fun wrongPackageIsIgnored() {
        val message = WeChatAdapter().parseNotification(
            NotificationEnvelope(
                packageName = "com.tencent.mobileqq",
                notificationKey = "notify-2",
                title = "Bob",
                text = "hello",
                postTime = 1L,
            ),
        )

        assertNull(message)
    }

    @Test
    fun genericAdapterSkipsKnownPackages() {
        val message = GenericAdapter().parseNotification(
            NotificationEnvelope(
                packageName = "com.tencent.mm",
                notificationKey = "notify-3",
                title = "Alice",
                text = "hi",
                postTime = 1L,
            ),
        )

        assertNull(message)
    }
}

