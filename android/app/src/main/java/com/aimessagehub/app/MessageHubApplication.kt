package com.aimessagehub.app

import android.app.Application
import android.content.Context
import com.aimessagehub.app.ai.OpenAICompatibleClient
import com.aimessagehub.app.ai.SettingsStore
import com.aimessagehub.app.ai.SuggestionEngine
import com.aimessagehub.app.data.HubRepository
import com.aimessagehub.app.data.local.AppDatabase
import com.aimessagehub.app.domain.AdapterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MessageHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}

object ServiceLocator {
    lateinit var appContext: Context
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var settings: SettingsStore
        private set
    lateinit var repository: HubRepository
        private set
    lateinit var registry: AdapterRegistry
        private set

    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun init(context: Context) {
        appContext = context.applicationContext
        registry = AdapterRegistry()
        settings = SettingsStore(appContext)
        database = AppDatabase.get(appContext)
        repository = HubRepository(
            database = database,
            registry = registry,
            engine = SuggestionEngine(OpenAICompatibleClient()),
            settings = settings,
            scope = applicationScope,
        )
    }
}

