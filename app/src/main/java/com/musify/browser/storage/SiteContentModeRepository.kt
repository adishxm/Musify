package com.musify.browser.storage

import com.musify.browser.engine.ContentMode
import java.io.File

/**
 * SiteContentModeRepository persists and provides per-origin desktop/mobile content mode preferences.
 *
 * Guarantees:
 * - Scoped strictly to normalized origin (e.g., https://open.spotify.com).
 * - Defaults to ContentMode.MOBILE for standard mobile presentation.
 * - Reversible at any time by the user via toolbar or privacy panels.
 * - Thread-safe in-memory cache backed by lightweight file storage.
 */
interface SiteContentModeRepository {
    fun getContentMode(origin: String): ContentMode
    fun setContentMode(origin: String, mode: ContentMode)
    fun isDesktopMode(origin: String): Boolean
    fun toggleContentMode(origin: String): ContentMode
    fun getCustomOrigins(): Map<String, ContentMode>
    fun removeOrigin(origin: String)
    fun clearAll()
}

class LocalSiteContentModeRepository(private val storageFile: File? = null) : SiteContentModeRepository {

    private val originModes = mutableMapOf<String, ContentMode>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun getContentMode(origin: String): ContentMode {
        if (origin.isBlank()) return ContentMode.MOBILE
        val normalized = normalizeOrigin(origin)
        return originModes[normalized] ?: ContentMode.MOBILE
    }

    @Synchronized
    override fun setContentMode(origin: String, mode: ContentMode) {
        if (origin.isBlank()) return
        val normalized = normalizeOrigin(origin)
        if (mode == ContentMode.MOBILE) {
            originModes.remove(normalized)
        } else {
            originModes[normalized] = mode
        }
        saveToDisk()
    }

    @Synchronized
    override fun isDesktopMode(origin: String): Boolean {
        return getContentMode(origin) == ContentMode.DESKTOP
    }

    @Synchronized
    override fun toggleContentMode(origin: String): ContentMode {
        val nextMode = if (isDesktopMode(origin)) ContentMode.MOBILE else ContentMode.DESKTOP
        setContentMode(origin, nextMode)
        return nextMode
    }

    @Synchronized
    override fun getCustomOrigins(): Map<String, ContentMode> {
        return originModes.toMap()
    }

    @Synchronized
    override fun removeOrigin(origin: String) {
        val normalized = normalizeOrigin(origin)
        if (originModes.remove(normalized) != null) {
            saveToDisk()
        }
    }

    @Synchronized
    override fun clearAll() {
        originModes.clear()
        saveToDisk()
    }

    private fun normalizeOrigin(raw: String): String {
        val trimmed = raw.trim().lowercase()
        return try {
            val uri = java.net.URI(trimmed)
            val scheme = uri.scheme ?: "https"
            val host = uri.host ?: trimmed
            "$scheme://$host"
        } catch (_: Exception) {
            trimmed
        }
    }

    private fun loadFromDisk() {
        if (storageFile == null || !storageFile.exists()) return
        try {
            val lines = storageFile.readLines()
            originModes.clear()
            for (line in lines) {
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    val origin = normalizeOrigin(parts[0])
                    val mode = if (parts[1].trim().equals("DESKTOP", ignoreCase = true)) {
                        ContentMode.DESKTOP
                    } else {
                        ContentMode.MOBILE
                    }
                    if (mode == ContentMode.DESKTOP) {
                        originModes[origin] = mode
                    }
                }
            }
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
            val content = originModes.entries.joinToString("\n") { "${it.key}=${it.value.name}" }
            storageFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
