package com.aimessagehub.app.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAICompatibleClientTest {
    @Test
    fun blankApiKeyFailsFast() = runTest {
        val client = OpenAICompatibleClient()
        val error = try {
            client.chatCompletion("system", "user", AIClientConfig(apiKey = ""))
            null
        } catch (exception: Exception) {
            exception
        }

        assertTrue(error?.message.orEmpty().contains("API Key"))
    }
}
