package com.musify.browser.tabs

import java.io.File

/**
 * TabMetadata holds strictly the minimal non-sensitive data needed to recover a tab.
 *
 * Privacy Invariant:
 * Under no circumstances may DOM contents, form inputs, cookies,
 * private session tokens, or credentials be included here.
 */
data class TabMetadata(
    val id: String,
    val url: String,
    val title: String,
    val createdAt: Long,
    val lastActiveAt: Long
)

/**
 * TabSessionSerializer provides zero-dependency JSON serialization and deserialization
 * that functions identically on Android runtime and host JVM unit tests.
 */
object TabSessionSerializer {

    fun serialize(state: TabsState): String {
        // Strict Privacy Invariant: Private browsing tabs are never written to disk
        val tabsToPersist = state.tabs.filterNot { it.isPrivate }
        val targetSelectedId = if (tabsToPersist.any { it.id == state.selectedTabId }) {
            state.selectedTabId
        } else {
            tabsToPersist.firstOrNull()?.id
        }

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"version\": 1,\n")
        sb.append("  \"selectedTabId\": ").append(targetSelectedId?.let { "\"$it\"" } ?: "null").append(",\n")
        sb.append("  \"tabs\": [\n")
        tabsToPersist.forEachIndexed { index, tab ->
            sb.append("    {\n")
            sb.append("      \"id\": \"").append(escapeJson(tab.id)).append("\",\n")
            sb.append("      \"url\": \"").append(escapeJson(tab.url)).append("\",\n")
            sb.append("      \"title\": \"").append(escapeJson(tab.title)).append("\",\n")
            sb.append("      \"createdAt\": ").append(tab.createdAt).append(",\n")
            sb.append("      \"lastActiveAt\": ").append(tab.lastActiveAt).append("\n")
            sb.append("    }").append(if (index < tabsToPersist.size - 1) "," else "").append("\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    fun deserialize(json: String, maxTabs: Int = 20): TabsState? {
        if (json.isBlank()) return null
        return try {
            val selectedTabId = extractStringField(json, "selectedTabId")

            val tabsRegex = Regex("""\{[^{}]*"id"\s*:\s*"([^"]+)"[^{}]*"url"\s*:\s*"([^"]+)"[^{}]*"title"\s*:\s*"([^"]+)"[^{}]*"createdAt"\s*:\s*(\d+)[^{}]*"lastActiveAt"\s*:\s*(\d+)[^{}]*\}""")
            val matches = tabsRegex.findAll(json)

            val parsedTabs = matches.map { match ->
                val id = unescapeJson(match.groupValues[1])
                val url = unescapeJson(match.groupValues[2])
                val title = unescapeJson(match.groupValues[3])
                val createdAt = match.groupValues[4].toLongOrNull() ?: System.currentTimeMillis()
                val lastActiveAt = match.groupValues[5].toLongOrNull() ?: System.currentTimeMillis()

                TabSession(
                    id = id,
                    url = url,
                    title = title,
                    origin = deriveOrigin(url),
                    createdAt = createdAt,
                    lastActiveAt = lastActiveAt
                )
            }.take(maxTabs).toList()

            if (parsedTabs.isEmpty()) {
                null
            } else {
                val validSelectedId = if (parsedTabs.any { it.id == selectedTabId }) {
                    selectedTabId
                } else {
                    parsedTabs.first().id
                }
                TabsState(
                    tabs = parsedTabs,
                    selectedTabId = validSelectedId,
                    maxTabs = maxTabs
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun deriveOrigin(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val scheme = uri.scheme ?: "https"
            val host = uri.host ?: ""
            if (host.isNotBlank()) "$scheme://$host" else url
        } catch (_: Exception) {
            url
        }
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun unescapeJson(value: String): String {
        return value.replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    private fun extractStringField(json: String, field: String): String? {
        val pattern = Regex(""""$field"\s*:\s*"([^"]+)"""")
        return pattern.find(json)?.groupValues?.get(1)
    }
}

/**
 * TabSessionStore interface for managing persisted session recovery across app restarts.
 */
interface TabSessionStore {
    fun save(state: TabsState)
    fun load(): TabsState?
    fun clear()
}

/**
 * File-backed implementation of TabSessionStore storing metadata in the private app data directory.
 */
class FileTabSessionStore(private val file: File) : TabSessionStore {

    @Synchronized
    override fun save(state: TabsState) {
        try {
            val serialized = TabSessionSerializer.serialize(state)
            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            val tempFile = File(file.absolutePath + ".tmp")
            tempFile.writeText(serialized)
            if (tempFile.renameTo(file).not()) {
                file.writeText(serialized)
                tempFile.delete()
            }
        } catch (e: Exception) {
            // Fail safely without crashing
            e.printStackTrace()
        }
    }

    @Synchronized
    override fun load(): TabsState? {
        if (!file.exists()) return null
        return try {
            val content = file.readText()
            TabSessionSerializer.deserialize(content)
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    override fun clear() {
        if (file.exists()) {
            file.delete()
        }
    }
}
