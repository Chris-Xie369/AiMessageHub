package com.aimessagehub.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.aimessagehub.app.R
import com.aimessagehub.app.ServiceLocator
import com.aimessagehub.app.domain.SuggestionStatus
import com.aimessagehub.app.domain.UiSuggestion
import com.aimessagehub.app.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SuggestionBubbleService : Service() {
    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var lastSuggestionId: String? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        createNotificationChannel()
        startAsForeground()
        addBubbleView()
        collectSuggestions()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        bubbleView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        bubbleView = null
        super.onDestroy()
    }

    private fun startAsForeground() {
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("AI 消息中枢")
            .setContentText("建议悬浮窗运行中")
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "建议悬浮窗",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    private fun addBubbleView() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.bubble_suggestion, null)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16
            y = 180
        }
        windowManager?.addView(view, params)
        bubbleView = view

        view.findViewById<Button>(R.id.bubbleSend).setOnClickListener {
            lastSuggestionId?.let { id ->
                ServiceLocator.repository.sendSuggestion(id, 0)
            }
        }
        view.findViewById<Button>(R.id.bubbleCopy).setOnClickListener {
            val suggestion = currentSuggestion() ?: return@setOnClickListener
            copyToClipboard(suggestion.variants.firstOrNull().orEmpty())
        }
        view.findViewById<Button>(R.id.bubbleDismiss).setOnClickListener {
            lastSuggestionId?.let { id ->
                ServiceLocator.repository.ignoreSuggestion(id)
            }
        }
    }

    private fun collectSuggestions() {
        ServiceLocator.applicationScope.launch(Dispatchers.Main) {
            ServiceLocator.repository.suggestions.collect { suggestions ->
                val suggestion = suggestions.firstOrNull {
                    it.status == SuggestionStatus.READY ||
                        it.status == SuggestionStatus.SENDING ||
                        it.status == SuggestionStatus.GENERATING
                }
                render(suggestion)
            }
        }
    }

    private fun render(suggestion: UiSuggestion?) {
        val view = bubbleView ?: return
        lastSuggestionId = suggestion?.id
        view.visibility = if (suggestion == null) View.GONE else View.VISIBLE
        if (suggestion == null) return
        view.findViewById<TextView>(R.id.bubbleTitle).text = when (suggestion.status) {
            SuggestionStatus.GENERATING -> "正在生成建议…"
            SuggestionStatus.SENDING -> "正在发送…"
            else -> "${suggestion.conversationTitle} · AI 建议"
        }
        val body = when {
            suggestion.error != null -> suggestion.error
            suggestion.variants.isNotEmpty() -> suggestion.variants.first()
            else -> "等待 AI 回复"
        }
        view.findViewById<TextView>(R.id.bubbleBody).text = body
    }

    private fun currentSuggestion(): UiSuggestion? {
        return ServiceLocator.repository.suggestions.value.firstOrNull {
            it.status == SuggestionStatus.READY
        }
    }

    private fun copyToClipboard(text: String) {
        if (text.isBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("AI 回复", text))
    }

    private companion object {
        const val CHANNEL_ID = "suggestion_bubble"
        const val NOTIFICATION_ID = 1001
    }
}

