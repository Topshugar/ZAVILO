package com.zavilo.app

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * LicenseManager - locked-by-default license helper.
 *
 * - Defaults to locked (isLicensed() == false) unless a valid license token is stored.
 * - Stores license tokens in EncryptedSharedPreferences (AndroidX Security) backed by
 *   the platform keystore.
 * - Stores an optional expiry timestamp (milliseconds since epoch) and clears expired tokens.
 * - getDeviceId() returns a SHA-256 hash of ANDROID_ID to avoid storing the raw hardware id.
 *
 * Note: this class is a client-side helper only — server-side license verification is required
 * for a robust protection model. Do not treat presence of a local token as the single source of truth.
 */
class LicenseManager(private val context: Context) {

    companion object {
        private const val PREF_FILE = "zavilo_secure_prefs"
        private const val KEY_LICENSE_TOKEN = "license_token"
        private const val KEY_LICENSE_EXPIRY = "license_expiry"
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"
        return sha256(androidId)
    }

    fun isLicensed(): Boolean {
        val token = prefs.getString(KEY_LICENSE_TOKEN, null) ?: return false
        val expiry = prefs.getLong(KEY_LICENSE_EXPIRY, 0L)
        if (expiry != 0L && System.currentTimeMillis() > expiry) {
            // expired -> clear and return false
            clearLicense()
            return false
        }
        // Note: presence of a token indicates a locally-stored license; for full security,
        // verify with server-side endpoint (e.g., at app start or periodically).
        return true
    }

    fun saveLicense(token: String, expiresAtMillis: Long = 0L) {
        prefs.edit().putString(KEY_LICENSE_TOKEN, token)
            .putLong(KEY_LICENSE_EXPIRY, expiresAtMillis)
            .apply()
    }

    fun clearLicense() {
        prefs.edit().remove(KEY_LICENSE_TOKEN).remove(KEY_LICENSE_EXPIRY).apply()
    }

    fun getLicenseToken(): String? = prefs.getString(KEY_LICENSE_TOKEN, null)

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
