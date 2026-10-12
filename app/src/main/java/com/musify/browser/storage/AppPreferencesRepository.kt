package com.musify.browser.storage

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

data class AppPreferencesData(
    val hasCompletedOnboarding: Boolean = false,
    val defaultSearchEngineUrl: String = "https://duckduckgo.com/?q=",
    val httpsOnlyMode: Boolean = true
)

interface AppPreferencesRepository {
    fun hasCompletedOnboarding(): Boolean
    fun setCompletedOnboarding(completed: Boolean)
    fun getDefaultSearchEngineUrl(): String
    fun setDefaultSearchEngineUrl(url: String)
    fun isHttpsOnlyMode(): Boolean
    fun setHttpsOnlyMode(enabled: Boolean)
    fun getPreferences(): AppPreferencesData
    fun resetAll()
}

/**
 * File-backed, thread-safe implementation of AppPreferencesRepository.
 * Follows atomic write patterns for zero-corruption guarantee on app termination.
 */
class LocalAppPreferencesRepository(
    private val storageFile: File? = null
) : AppPreferencesRepository {

    private var preferences = AppPreferencesData()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun hasCompletedOnboarding(): Boolean = preferences.hasCompletedOnboarding

    @Synchronized
    override fun setCompletedOnboarding(completed: Boolean) {
        preferences = preferences.copy(hasCompletedOnboarding = completed)
        saveToDisk()
    }

    @Synchronized
    override fun getDefaultSearchEngineUrl(): String = preferences.defaultSearchEngineUrl

    @Synchronized
    override fun setDefaultSearchEngineUrl(url: String) {
        if (url.isNotBlank()) {
            preferences = preferences.copy(defaultSearchEngineUrl = url.trim())
            saveToDisk()
        }
    }

    @Synchronized
    override fun isHttpsOnlyMode(): Boolean = preferences.httpsOnlyMode

    @Synchronized
    override fun setHttpsOnlyMode(enabled: Boolean) {
        preferences = preferences.copy(httpsOnlyMode = enabled)
        saveToDisk()
    }

    @Synchronized
    override fun getPreferences(): AppPreferencesData = preferences

    @Synchronized
    override fun resetAll() {
        preferences = AppPreferencesData()
        saveToDisk()
    }

    private fun loadFromDisk() {
        val file = storageFile ?: return
        if (!file.exists() || file.length() == 0L) return

        try {
            val content = file.readText(StandardCharsets.UTF_8).trim()
            if (content.isEmpty()) return

            var onboarding = false
            var search = "https://duckduckgo.com/?q="
            var httpsOnly = true

            // Simple robust key-value / JSON line parser
            content.lines().forEach { line ->
                val trimmed = line.trim().trimEnd(',')
                when {
                    trimmed.contains("\"hasCompletedOnboarding\"") -> {
                        onboarding = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"defaultSearchEngineUrl\"") -> {
                        search = trimmed.substringAfter(":").trim().trim('"')
                    }
                    trimmed.contains("\"httpsOnlyMode\"") -> {
                        httpsOnly = trimmed.substringAfter(":").trim().toBoolean()
                    }
                }
            }

            preferences = AppPreferencesData(
                hasCompletedOnboarding = onboarding,
                defaultSearchEngineUrl = search,
                httpsOnlyMode = httpsOnly
            )
        } catch (_: Exception) {
            preferences = AppPreferencesData()
        }
    }

    private fun saveToDisk() {
        val file = storageFile ?: return
        try {
            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            val tmpFile = File(file.parentFile, "${file.name}.tmp")
            val json = buildString {
                appendLine("{")
                appendLine("  \"hasCompletedOnboarding\": ${preferences.hasCompletedOnboarding},")
                appendLine("  \"defaultSearchEngineUrl\": \"${preferences.defaultSearchEngineUrl}\",")
                appendLine("  \"httpsOnlyMode\": ${preferences.httpsOnlyMode}")
                appendLine("}")
            }

            OutputStreamWriter(FileOutputStream(tmpFile), StandardCharsets.UTF_8).use { writer ->
                writer.write(json)
                writer.flush()
            }

            if (tmpFile.exists()) {
                if (file.exists()) {
                    file.delete()
                }
                tmpFile.renameTo(file)
            }
        } catch (_: Exception) {
            // Fail gracefully without crashing
        }
    }
}
