package com.zavilo.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class ZaviloNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Handle notification posted
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Handle notification removed
    }
}