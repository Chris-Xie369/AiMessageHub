package com.aimessagehub.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

interface AIClient {
    suspend fun chatCompletion(
        system: String,
        user: String,
        config: AIClientConfig,
    ): List<String>
}

class OpenAICompatibleClient : AIClient {
    override suspend fun chatCompletion(
        system: String,
        user: String,
        config: AIClientConfig,
    ): List<String> = withContext(Dispatchers.IO) {
        if (config.apiKey.isBlank()) {
            throw IOException("请先在设置中填写 API Key")
        }
        val url = URL(config.baseUrl.trimEnd('/') + "/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 60_000
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${config.apiKey}")

            val messages = JSONArray()
                .put(
                    JSONObject()
                        .put("role", "system")
                        .put("content", system),
                )
                .put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", user),
                )
            val payload = JSONObject()
                .put("model", config.model)
                .put("temperature", config.temperature)
                .put("max_tokens", config.maxTokens)
                .put("messages", messages)

            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val body = if (status in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            if (status !in 200..299) {
                throw IOException("AI 请求失败 (HTTP $status): ${body.take(300)}")
            }

            val root = JSONObject(body)
            val choices = root.optJSONArray("choices") ?: JSONArray()
            val texts = (0 until choices.length())
                .mapNotNull { index ->
                    val choice = choices.optJSONObject(index) ?: return@mapNotNull null
                    choice.optJSONObject("message")
                        ?.optString("content")
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                }
                .take(3)
            if (texts.isEmpty()) throw IOException("AI 没有返回可用回复")
            texts
        } finally {
            connection.disconnect()
        }
    }
}

