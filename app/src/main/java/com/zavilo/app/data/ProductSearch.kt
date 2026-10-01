package com.zavilo.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProductSearch {
    private fun tokensFromMessage(text: String): List<String> {
        return text.lowercase()
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
    }

    suspend fun findBestMatch(repo: ProductRepository, message: String): Product? {
        val tokens = tokensFromMessage(message)
        return withContext(Dispatchers.IO) {
            // try SKU-ish tokens first
            for (t in tokens) {
                if (t.length >= 3) {
                    val bySku = repo.getBySku(t)
                    if (bySku != null) return@withContext bySku
                }
            }
            // then keywords
            for (t in tokens) {
                if (t.length < 2) continue
                val p = repo.findByKeyword(t)
                if (p != null) return@withContext p
            }
            null
        }
    }
}
