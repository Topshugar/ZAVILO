package com.zavilo.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Hardened notification listener. This version is the canonical implementation used by the app.
 *
 * Security/Ethics:
 * - Only handles WhatsApp and WhatsApp Business notifications.
 * - Requires explicit automation consent before processing.
 * - Uses a reply marker to prevent loops.
 * - Enforces per-sender cooldown + device-level rate limiting.
 * - Broadcasts a suggested reply to the UI instead of auto-sending immediately.
 */
class ZaviloNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "ZaviloNotifService"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

        private const val PREFS_NAME = "zavilo_prefs"
        private const val KEY_AUTOMATION_ENABLED = "automation_enabled"
        private const val KEY_LAST_DEVICE_REPLY_TIME = "last_device_reply_time"
        private const val REPLY_MARKER = "[ZAVILO]"

        private const val DEFAULT_COOLDOWN_MS = 5 * 60 * 1000L
        private const val DEVICE_RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000L
        private const val DEVICE_RATE_LIMIT_MAX = 60
    }

    private lateinit var prefs: SharedPreferences
    private val lastReplyPerSender = mutableMapOf<String, Long>()
    private var deviceWindowStart = 0L
    private var deviceReplyCount = 0

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        deviceWindowStart = prefs.getLong(KEY_LAST_DEVICE_REPLY_TIME, 0L)
        if (deviceWindowStart == 0L) {
            deviceWindowStart = System.currentTimeMillis()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val pkg = sbn.packageName ?: return
        if (pkg != WHATSAPP_PACKAGE && pkg != WHATSAPP_BUSINESS_PACKAGE) return

        val automationEnabled = prefs.getBoolean(KEY_AUTOMATION_ENABLED, false)
        if (!automationEnabled) {
            if (BuildConfig.DEBUG) Log.d(TAG, "automation disabled; skipping")
            return
        }

        val licenseManager = LicenseManager(this)
        if (!licenseManager.isLicensed() && !BuildConfig.DEBUG) {
            Log.w(TAG, "device not licensed; skipping")
            return
        }

        val extras = sbn.notification.extras ?: return
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val senderId = title.trim()
        if (senderId.isEmpty()) return

        if (text.contains(REPLY_MARKER)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "skip due to reply marker")
            return
        }

        val now = System.currentTimeMillis()
        val lastReply = lastReplyPerSender[senderId] ?: 0L
        if (now - lastReply < DEFAULT_COOLDOWN_MS) {
            if (BuildConfig.DEBUG) Log.d(TAG, "cooldown active for sender=$senderId")
            return
        }

        if (now - deviceWindowStart > DEVICE_RATE_LIMIT_WINDOW_MS) {
            deviceWindowStart = now
            deviceReplyCount = 0
        }

        if (deviceReplyCount >= DEVICE_RATE_LIMIT_MAX) {
            Log.w(TAG, "device rate limit reached; skipping")
            return
        }

        lastReplyPerSender[senderId] = now
        deviceReplyCount += 1
        prefs.edit().putLong(KEY_LAST_DEVICE_REPLY_TIME, deviceWindowStart).apply()

        val suggestedReply = generateSuggestedReply(text)
        notifyUiForApproval(senderId, suggestedReply)

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "eligible for approval: sender=$senderId reply=${suggestedReply.take(120)}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    private fun generateSuggestedReply(messageText: String): String {
        val lower = messageText.lowercase()
        return when {
            lower.contains("price") || lower.contains("how much") || lower.contains("cost") -> {
                "Hi! Thanks for your message. The price for that item is ₦X. Delivery is ₦Y. Reply 'buy' to proceed. $REPLY_MARKER"
            }
            lower.contains("deliver") || lower.contains("delivery") -> {
                "We deliver within Z days. Delivery fee depends on location — send your address. $REPLY_MARKER"
            }
            else -> {
                "Thanks for reaching out! Can you tell me which product you mean? $REPLY_MARKER"
            }
        }
    }

    private fun notifyUiForApproval(senderId: String, suggestedReply: String) {
        val intent = Intent("com.zavilo.ACTION_SUGGESTED_REPLY")
        intent.putExtra("sender", senderId)
        intent.putExtra("suggested_reply", suggestedReply)
        sendBroadcast(intent)
    }
}
