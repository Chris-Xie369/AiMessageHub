package com.aimessagehub.app.ai

import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ExecutionPolicy

data class AppSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4o-mini",
    val temperature: Double = 0.7,
    val maxTokens: Int = 512,
    val persona: String = "",
    val instructions: String = "",
    val defaultPolicy: ExecutionPolicy = ExecutionPolicy.ONE_TAP,
    val captureApps: Set<AppSource> = emptySet(),
    val bubbleEnabled: Boolean = true,
    val groupChatsEnabled: Boolean = false,
) {
    fun toAIConfig(): AIClientConfig = AIClientConfig(
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
        temperature = temperature,
        maxTokens = maxTokens,
    )
}

