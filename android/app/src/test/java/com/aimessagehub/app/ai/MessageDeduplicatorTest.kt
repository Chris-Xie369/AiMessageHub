package com.aimessagehub.app.ai

import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.MessageDirection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageDeduplicatorTest {
    @Test
    fun duplicateMessageIsRejected() {
        val deduplicator = MessageDeduplicator()
        val message = ChatMessage(
            id = "msg-1",
            app = AppSource.WECHAT,
            conversationId = "WECHAT:Alice",
            conversationTitle = "Alice",
            sender = "Alice",
            text = "hello",
            timestamp = 1_000L,
            direction = MessageDirection.INCOMING,
        )

        assertFalse(deduplicator.isDuplicate(message))
        assertTrue(deduplicator.isDuplicate(message))
    }

    @Test
    fun differentConversationsAreIndependent() {
        val deduplicator = MessageDeduplicator()
        val first = ChatMessage(
            id = "msg-1",
            app = AppSource.WECHAT,
            conversationId = "WECHAT:Alice",
            conversationTitle = "Alice",
            sender = "Alice",
            text = "hello",
            timestamp = 1_000L,
            direction = MessageDirection.INCOMING,
        )
        val second = first.copy(conversationId = "WECHAT:Bob", conversationTitle = "Bob")

        assertFalse(deduplicator.isDuplicate(first))
        assertFalse(deduplicator.isDuplicate(second))
    }
}

