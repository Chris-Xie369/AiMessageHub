package com.aimessagehub.app.ai

import com.aimessagehub.app.domain.SuggestionRequest
import com.aimessagehub.app.domain.SuggestionResult

class SuggestionEngine(
    private val client: AIClient,
) {
    suspend fun suggest(
        request: SuggestionRequest,
        config: AIClientConfig,
    ): SuggestionResult {
        return try {
            val raw = client.chatCompletion(
                system = PromptBuilder.buildSystem(request.persona, request.instructions),
                user = PromptBuilder.buildUser(request),
                config = config,
            )
            val variants = raw
                .flatMap { it.split("---") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .take(request.variantCount)
            if (variants.isEmpty()) {
                SuggestionResult(emptyList(), "AI 没有返回可用回复")
            } else {
                SuggestionResult(variants)
            }
        } catch (error: Exception) {
            SuggestionResult(emptyList(), error.message ?: "AI 请求失败")
        }
    }
}

