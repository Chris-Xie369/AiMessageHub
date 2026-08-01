package com.aimessagehub.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.aimessagehub.app.ServiceLocator
import com.aimessagehub.app.domain.NotificationEnvelope

class MessageHubNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        ServiceLocator.repository.onNotification(
            NotificationEnvelope(
                packageName = sbn.packageName,
                notificationKey = sbn.key,
                title = title,
                text = text,
                postTime = sbn.postTime,
            ),
        )
    }
}
