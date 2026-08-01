package com.aimessagehub.app.ai

import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.MessageDirection
import com.aimessagehub.app.domain.SuggestionRequest
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {
    @Test
    fun userPromptContainsContactAndHistory() {
        val request = SuggestionRequest(
            conversationId = "WECHAT:Alice",
            app = AppSource.WECHAT,
            contactName = "Alice",
            history = listOf(
                ChatMessage(
                    id = "1",
                    app = AppSource.WECHAT,
                    conversationId = "WECHAT:Alice",
                    conversationTitle = "Alice",
                    sender = "Alice",
                    text = "周末有空吗",
                    timestamp = 1L,
                    direction = MessageDirection.INCOMING,
                ),
            ),
            persona = "简洁",
            instructions = "不要用 Markdown",
        )

        val prompt = PromptBuilder.buildUser(request)

        assertTrue(prompt.contains("Alice"))
        assertTrue(prompt.contains("周末有空吗"))
    }

    @Test
    fun systemPromptUsesDefaultRulesWhenBlank() {
        val prompt = PromptBuilder.buildSystem("", "")

        assertTrue(prompt.contains("不要暴露你是 AI"))
        assertTrue(prompt.contains("不要使用 Markdown"))
    }
}

