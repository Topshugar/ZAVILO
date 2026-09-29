package com.zavilo

import android.content.Context
import android.provider.Settings

class LicenseManager(private val context: Context) {

    fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    fun isLicensed(): Boolean {
        val prefs = context.getSharedPreferences("zavilo_lic", Context.MODE_PRIVATE)
        // For now allow dev mode. Later we check online.
        return prefs.getBoolean("is_licensed", true)
    }

    fun saveLicense(key: String) {
        val prefs = context.getSharedPreferences("zavilo_lic", Context.MODE_PRIVATE)
        prefs.edit().putString("license_key", key).putBoolean("is_licensed", true).apply()
    }
}
