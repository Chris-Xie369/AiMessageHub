package com.aimessagehub.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aimessagehub.app.ai.AIClient
import com.aimessagehub.app.ai.AIClientConfig
import com.aimessagehub.app.ai.SettingsStore
import com.aimessagehub.app.ai.SuggestionEngine
import com.aimessagehub.app.data.local.AppDatabase
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.NotificationEnvelope
import com.aimessagehub.app.domain.AdapterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HubRepositoryInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var settings: SettingsStore
    private lateinit var scope: CoroutineScope

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settings = SettingsStore(context)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun notificationPersistsConversation() = runBlocking {
        settings.update { it.copy(captureApps = setOf(AppSource.WECHAT)) }
        val repository = HubRepository(
            database = database,
            registry = AdapterRegistry(),
            engine = SuggestionEngine(FakeClient()),
            settings = settings,
            scope = scope,
        )

        repository.onNotification(
            NotificationEnvelope(
                packageName = "com.tencent.mm",
                notificationKey = "notify-1",
                title = "Alice",
                text = "你好",
                postTime = 1_700_000_000_000,
            ),
        )
        delay(200)

        val conversations = repository.conversations.first()
        assertEquals(1, conversations.size)
        assertEquals("Alice", conversations.first().title)
    }
}

private class FakeClient : AIClient {
    override suspend fun chatCompletion(
        system: String,
        user: String,
        config: AIClientConfig,
    ): List<String> = listOf("你好呀")
}

