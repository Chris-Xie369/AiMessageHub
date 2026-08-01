package com.aimessagehub.app.ai

import android.content.Context
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ExecutionPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("aimh_settings", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(read())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_state.value)
        persist(next)
        _state.value = next
    }

    fun isCaptureEnabled(packageName: String?): Boolean {
        val app = AppSource.fromPackageName(packageName)
        val enabled = _state.value.captureApps
        if (app != AppSource.GENERIC) return app in enabled
        return packageName.isNullOrBlank().not() && AppSource.GENERIC in enabled
    }

    private fun read(): AppSettings {
        return AppSettings(
            baseUrl = prefs.getString(KEY_BASE_URL, AppSettings().baseUrl).orEmpty(),
            apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
            model = prefs.getString(KEY_MODEL, AppSettings().model).orEmpty(),
            temperature = prefs.getFloat(KEY_TEMPERATURE, AppSettings().temperature.toFloat()).toDouble(),
            maxTokens = prefs.getInt(KEY_MAX_TOKENS, AppSettings().maxTokens),
            persona = prefs.getString(KEY_PERSONA, "").orEmpty(),
            instructions = prefs.getString(KEY_INSTRUCTIONS, "").orEmpty(),
            defaultPolicy = runCatching {
                ExecutionPolicy.valueOf(prefs.getString(KEY_POLICY, ExecutionPolicy.ONE_TAP.name).orEmpty())
            }.getOrDefault(ExecutionPolicy.ONE_TAP),
            captureApps = prefs.getStringSet(KEY_CAPTURE_APPS, emptySet())
                .orEmpty()
                .mapNotNull { name -> runCatching { AppSource.valueOf(name) }.getOrNull() }
                .toSet(),
            bubbleEnabled = prefs.getBoolean(KEY_BUBBLE_ENABLED, true),
            groupChatsEnabled = prefs.getBoolean(KEY_GROUP_CHATS, false),
        )
    }

    private fun persist(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_BASE_URL, settings.baseUrl)
            .putString(KEY_API_KEY, settings.apiKey)
            .putString(KEY_MODEL, settings.model)
            .putFloat(KEY_TEMPERATURE, settings.temperature.toFloat())
            .putInt(KEY_MAX_TOKENS, settings.maxTokens)
            .putString(KEY_PERSONA, settings.persona)
            .putString(KEY_INSTRUCTIONS, settings.instructions)
            .putString(KEY_POLICY, settings.defaultPolicy.name)
            .putStringSet(KEY_CAPTURE_APPS, settings.captureApps.map { it.name }.toSet())
            .putBoolean(KEY_BUBBLE_ENABLED, settings.bubbleEnabled)
            .putBoolean(KEY_GROUP_CHATS, settings.groupChatsEnabled)
            .apply()
    }

    private companion object {
        const val KEY_BASE_URL = "base_url"
        const val KEY_API_KEY = "api_key"
        const val KEY_MODEL = "model"
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_MAX_TOKENS = "max_tokens"
        const val KEY_PERSONA = "persona"
        const val KEY_INSTRUCTIONS = "instructions"
        const val KEY_POLICY = "policy"
        const val KEY_CAPTURE_APPS = "capture_apps"
        const val KEY_BUBBLE_ENABLED = "bubble_enabled"
        const val KEY_GROUP_CHATS = "group_chats"
    }
}

