package com.aimessagehub.app.ai

import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.MessageDirection
import com.aimessagehub.app.domain.SuggestionRequest

object PromptBuilder {
    fun buildSystem(persona: String, instructions: String): String {
        val style = persona.trim().ifEmpty { "自然、简洁、符合中文日常聊天习惯" }
        val rules = instructions.trim().ifEmpty {
            "不要暴露你是 AI；不要编造事实；回复控制在 80 字以内；不要使用 Markdown。"
        }
        return """
            你是用户个人聊天助手，帮助用户理解消息并草拟回复。
            回复风格：$style
            额外要求：$rules
            只返回回复草稿本身，每条候选之间用「---」分隔。
        """.trimIndent()
    }

    fun buildUser(request: SuggestionRequest): String {
        val history = request.history
            .sortedBy { it.timestamp }
            .takeLast(20)
            .joinToString(separator = "\n") { message ->
                val role = if (message.direction == MessageDirection.OUTGOING) "我" else message.sender
                "$role：${message.text}"
            }
        val contact = request.contactName.ifBlank { "对方" }
        return """
            正在与「$contact」聊天。
            最近消息：
            ${history.ifBlank { "（暂无历史消息）" }}

            请给出 ${request.variantCount} 个不同角度的回复候选，使用「---」分隔。
        """.trimIndent()
    }
}

