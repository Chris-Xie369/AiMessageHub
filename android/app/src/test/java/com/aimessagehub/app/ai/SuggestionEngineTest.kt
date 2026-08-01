package com.aimessagehub.app.ai

import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.MessageDirection
import com.aimessagehub.app.domain.SuggestionRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionEngineTest {
    @Test
    fun successfulResponseReturnsVariants() = runTest {
        val engine = SuggestionEngine(
            client = object : AIClient {
                override suspend fun chatCompletion(
                    system: String,
                    user: String,
                    config: AIClientConfig,
                ): List<String> = listOf("好的呀\n---\n当然可以")
            },
        )
        val result = engine.suggest(
            request = SuggestionRequest(
                conversationId = "WECHAT:Alice",
                app = AppSource.WECHAT,
                contactName = "Alice",
                history = emptyList(),
                persona = "",
                instructions = "",
            ),
            config = AIClientConfig(apiKey = "test"),
        )

        assertTrue(result.isSuccess)
        assertEquals(2, result.variants.size)
    }

    @Test
    fun failureReturnsError() = runTest {
        val engine = SuggestionEngine(
            client = object : AIClient {
                override suspend fun chatCompletion(
                    system: String,
                    user: String,
                    config: AIClientConfig,
                ): List<String> = error("HTTP 401")
            },
        )
        val result = engine.suggest(
            request = SuggestionRequest(
                conversationId = "WECHAT:Alice",
                app = AppSource.WECHAT,
                contactName = "Alice",
                history = emptyList(),
                persona = "",
                instructions = "",
            ),
            config = AIClientConfig(apiKey = "bad"),
        )

        assertTrue(!result.isSuccess)
        assertTrue(result.error.orEmpty().contains("HTTP 401"))
    }
}

