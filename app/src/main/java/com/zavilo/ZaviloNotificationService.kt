package com.zavilo

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class ZaviloNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let {
            val packageName = it.packageName
            val title = it.notification.extras.getString("android.title") ?: ""
            val text = it.notification.extras.getCharSequence("android.text")?.toString() ?: ""

            // TODO: This is where your AI logic will go
            // For now we just log
            println("Zavilo caught: $packageName - $title - $text")

            // Later you will add:
            // 1. Check if user is talking to someone
            // 2. Send to AI to generate reply
            // 3. Auto-reply logic
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
