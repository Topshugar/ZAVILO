package com.zavilo.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * GPTFallback - simple wrapper that posts the message to your server-side AI endpoint.
 *
 * IMPORTANT:
 * - Do not store API keys in the APK. Use a server endpoint that holds your key and performs rate-limiting.
 * - The endpoint should validate the request (device id, license token, play integrity attestation).
 *
 * Example server endpoint: POST https://your-server.example/api/ai/reply
 * Body: { "message": "...", "context": { ... } }
 * Response: { "reply": "..." }
 */
class GPTFallback(private val serverUrl: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun generateReply(message: String, maxTokens: Int = 200): String? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("message", message)
                put("max_tokens", maxTokens)
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = RequestBody.create(mediaType, json.toString())
            val req = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val txt = resp.body?.string() ?: return@withContext null
                val j = JSONObject(txt)
                return@withContext j.optString("reply", null)
            }
        } catch (e: Exception) {
            // swallow and return null so caller falls back to clarification message
            return@withContext null
        }
    }
}
