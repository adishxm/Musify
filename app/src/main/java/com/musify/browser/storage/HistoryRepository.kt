package com.musify.browser.storage

import java.io.File
import java.util.UUID

interface HistoryRepository {
    fun addVisit(url: String, title: String): HistoryItem?
    fun getHistory(query: String? = null): List<HistoryItem>
    fun deleteItem(id: String): Boolean
    fun deleteByTimeRange(range: TimeRange, now: Long = System.currentTimeMillis()): Int
    fun clearAll()
}

/**
 * File-backed, thread-safe implementation of HistoryRepository.
 * Functions seamlessly on both Android runtime and host JVM unit tests.
 */
class LocalHistoryRepository(private val storageFile: File? = null) : HistoryRepository {

    private val items = mutableListOf<HistoryItem>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun addVisit(url: String, title: String): HistoryItem? {
        if (url.isBlank() || url == "about:blank") return null

        val cleanTitle = if (title.isBlank()) url else title
        val now = System.currentTimeMillis()

        // Check if top item is identical (same URL visited recently)
        val existingIndex = items.indexOfFirst { it.url == url }
        val item = if (existingIndex != -1) {
            val existing = items.removeAt(existingIndex)
            existing.copy(
                title = cleanTitle,
                visitTime = now,
                visitCount = existing.visitCount + 1
            )
        } else {
            HistoryItem(
                id = UUID.randomUUID().toString(),
                url = url,
                title = cleanTitle,
                visitTime = now,
                visitCount = 1
            )
        }

        items.add(0, item) // Insert newest at the top
        saveToDisk()
        return item
    }

    @Synchronized
    override fun getHistory(query: String?): List<HistoryItem> {
        val trimmed = query?.trim()?.lowercase() ?: ""
        return if (trimmed.isEmpty()) {
            items.toList()
        } else {
            items.filter {
                it.url.lowercase().contains(trimmed) || it.title.lowercase().contains(trimmed)
            }
        }
    }

    @Synchronized
    override fun deleteItem(id: String): Boolean {
        val removed = items.removeAll { it.id == id }
        if (removed) {
            saveToDisk()
        }
        return removed
    }

    @Synchronized
    override fun deleteByTimeRange(range: TimeRange, now: Long): Int {
        val cutoff = if (range == TimeRange.ALL_TIME) 0L else (now - range.durationMs)
        val initialSize = items.size
        items.removeAll { it.visitTime >= cutoff }
        val deletedCount = initialSize - items.size
        if (deletedCount > 0) {
            saveToDisk()
        }
        return deletedCount
    }

    @Synchronized
    override fun clearAll() {
        items.clear()
        saveToDisk()
    }

    @Synchronized
    fun seedHistoryForTesting(list: List<HistoryItem>) {
        items.clear()
        items.addAll(list)
        saveToDisk()
    }

    private fun loadFromDisk() {
        if (storageFile == null || !storageFile.exists()) return
        try {
            val text = storageFile.readText()
            val parsed = deserializeJson(text)
            items.clear()
            items.addAll(parsed)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveToDisk() {
        if (storageFile == null) return
        try {
            val parent = storageFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            val json = serializeJson(items)
            val tempFile = File(storageFile.absolutePath + ".tmp")
            tempFile.writeText(json)
            if (!tempFile.renameTo(storageFile)) {
                storageFile.writeText(json)
                tempFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun serializeJson(list: List<HistoryItem>): String {
        val sb = StringBuilder("[\n")
        list.forEachIndexed { i, it ->
            sb.append("  {\n")
            sb.append("    \"id\": \"").append(escape(it.id)).append("\",\n")
            sb.append("    \"url\": \"").append(escape(it.url)).append("\",\n")
            sb.append("    \"title\": \"").append(escape(it.title)).append("\",\n")
            sb.append("    \"visitTime\": ").append(it.visitTime).append(",\n")
            sb.append("    \"visitCount\": ").append(it.visitCount).append("\n")
            sb.append("  }").append(if (i < list.size - 1) "," else "").append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun deserializeJson(json: String): List<HistoryItem> {
        val regex = Regex("""\{[^{}]*"id"\s*:\s*"([^"]+)"[^{}]*"url"\s*:\s*"([^"]+)"[^{}]*"title"\s*:\s*"([^"]+)"[^{}]*"visitTime"\s*:\s*(\d+)[^{}]*"visitCount"\s*:\s*(\d+)[^{}]*\}""")
        return regex.findAll(json).map { match ->
            HistoryItem(
                id = unescape(match.groupValues[1]),
                url = unescape(match.groupValues[2]),
                title = unescape(match.groupValues[3]),
                visitTime = match.groupValues[4].toLongOrNull() ?: 0L,
                visitCount = match.groupValues[5].toIntOrNull() ?: 1
            )
        }.toList()
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    private fun unescape(s: String) = s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
}
