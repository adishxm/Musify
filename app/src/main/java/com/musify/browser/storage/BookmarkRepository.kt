package com.musify.browser.storage

import java.io.File
import java.util.UUID

interface BookmarkRepository {
    fun addBookmark(url: String, title: String): BookmarkItem?
    fun isBookmarked(url: String): Boolean
    fun removeBookmark(url: String): Boolean
    fun getBookmarks(query: String? = null): List<BookmarkItem>
    fun clearAll()
}

/**
 * File-backed, thread-safe implementation of BookmarkRepository.
 */
class LocalBookmarkRepository(private val storageFile: File? = null) : BookmarkRepository {

    private val bookmarks = mutableListOf<BookmarkItem>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun addBookmark(url: String, title: String): BookmarkItem? {
        if (url.isBlank() || url == "about:blank") return null
        if (isBookmarked(url)) return bookmarks.find { it.url == url }

        val cleanTitle = if (title.isBlank()) url else title
        val item = BookmarkItem(
            id = UUID.randomUUID().toString(),
            url = url,
            title = cleanTitle,
            createdAt = System.currentTimeMillis()
        )
        bookmarks.add(0, item)
        saveToDisk()
        return item
    }

    @Synchronized
    override fun isBookmarked(url: String): Boolean {
        if (url.isBlank() || url == "about:blank") return false
        return bookmarks.any { it.url == url }
    }

    @Synchronized
    override fun removeBookmark(url: String): Boolean {
        val removed = bookmarks.removeAll { it.url == url }
        if (removed) {
            saveToDisk()
        }
        return removed
    }

    @Synchronized
    override fun getBookmarks(query: String?): List<BookmarkItem> {
        val trimmed = query?.trim()?.lowercase() ?: ""
        return if (trimmed.isEmpty()) {
            bookmarks.toList()
        } else {
            bookmarks.filter {
                it.url.lowercase().contains(trimmed) || it.title.lowercase().contains(trimmed)
            }
        }
    }

    @Synchronized
    override fun clearAll() {
        bookmarks.clear()
        saveToDisk()
    }

    private fun loadFromDisk() {
        if (storageFile == null || !storageFile.exists()) return
        try {
            val text = storageFile.readText()
            val parsed = deserializeJson(text)
            bookmarks.clear()
            bookmarks.addAll(parsed)
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
            val json = serializeJson(bookmarks)
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

    private fun serializeJson(list: List<BookmarkItem>): String {
        val sb = StringBuilder("[\n")
        list.forEachIndexed { i, it ->
            sb.append("  {\n")
            sb.append("    \"id\": \"").append(escape(it.id)).append("\",\n")
            sb.append("    \"url\": \"").append(escape(it.url)).append("\",\n")
            sb.append("    \"title\": \"").append(escape(it.title)).append("\",\n")
            sb.append("    \"createdAt\": ").append(it.createdAt).append("\n")
            sb.append("  }").append(if (i < list.size - 1) "," else "").append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun deserializeJson(json: String): List<BookmarkItem> {
        val regex = Regex("""\{[^{}]*"id"\s*:\s*"([^"]+)"[^{}]*"url"\s*:\s*"([^"]+)"[^{}]*"title"\s*:\s*"([^"]+)"[^{}]*"createdAt"\s*:\s*(\d+)[^{}]*\}""")
        return regex.findAll(json).map { match ->
            BookmarkItem(
                id = unescape(match.groupValues[1]),
                url = unescape(match.groupValues[2]),
                title = unescape(match.groupValues[3]),
                createdAt = match.groupValues[4].toLongOrNull() ?: 0L
            )
        }.toList()
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    private fun unescape(s: String) = s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
}
