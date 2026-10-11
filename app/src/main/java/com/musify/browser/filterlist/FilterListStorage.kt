package com.musify.browser.filterlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * FilterListStorage manages staged atomic updates, active filter list persistence,
 * and automatic rollback to the last-known-good list upon corruption or failed updates.
 */
class FilterListStorage(private val rootDir: File) {

    init {
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
    }

    private fun getActiveFile(listId: String) = File(rootDir, "active_${listId}.txt")
    private fun getActiveMetaFile(listId: String) = File(rootDir, "active_${listId}_meta.json")
    private fun getStagingFile(listId: String) = File(rootDir, "stage_${listId}.tmp")
    private fun getLkgFile(listId: String) = File(rootDir, "lkg_${listId}.txt")
    private fun getLkgMetaFile(listId: String) = File(rootDir, "lkg_${listId}_meta.json")

    /**
     * Writes raw stream to a staging file without touching active list.
     */
    suspend fun stageList(listId: String, inputStream: InputStream): File = withContext(Dispatchers.IO) {
        val stageFile = getStagingFile(listId)
        if (stageFile.exists()) {
            stageFile.delete()
        }
        FileOutputStream(stageFile).use { out ->
            inputStream.copyTo(out)
            out.flush()
        }
        stageFile
    }

    /**
     * Validates staged file and atomically activates it if golden rules and compilation succeed.
     * Preserves last-known-good list if update fails or is malformed.
     */
    suspend fun validateAndActivate(
        listId: String,
        fallbackMetadata: FilterListMetadata,
        expectedSha256: String? = null
    ): UpdateResult = withContext(Dispatchers.IO) {
        val stageFile = getStagingFile(listId)
        if (!stageFile.exists() || stageFile.length() == 0L) {
            return@withContext UpdateResult.Rollback("Staging file missing or empty", getActiveMetadata(listId))
        }

        // Check file size bound
        if (stageFile.length() > FilterRuleCompiler.MAX_FILE_SIZE_BYTES) {
            stageFile.delete()
            return@withContext UpdateResult.Rollback("File exceeded maximum size bound of 15MB", getActiveMetadata(listId))
        }

        val compiled = try {
            FileInputStream(stageFile).use { input ->
                FilterRuleCompiler.compile(input, fallbackMetadata)
            }
        } catch (e: Exception) {
            stageFile.delete()
            return@withContext UpdateResult.Rollback("Parser crashed during compilation: ${e.message}", getActiveMetadata(listId))
        }

        // Check optional expected checksum
        if (expectedSha256 != null && !compiled.metadata.sha256Checksum.equals(expectedSha256, ignoreCase = true)) {
            stageFile.delete()
            return@withContext UpdateResult.Rollback(
                "Integrity check failed: Expected $expectedSha256 but got ${compiled.metadata.sha256Checksum}",
                getActiveMetadata(listId)
            )
        }

        // Golden sanity test: Must have parsed at least 1 supported rule or valid line
        if (compiled.metadata.totalLines == 0) {
            stageFile.delete()
            return@withContext UpdateResult.Rollback("Golden validation failed: 0 lines parsed", getActiveMetadata(listId))
        }

        // Backup current active file as Last-Known-Good before overwriting
        val activeFile = getActiveFile(listId)
        val lkgFile = getLkgFile(listId)
        if (activeFile.exists() && activeFile.length() > 0L) {
            activeFile.copyTo(lkgFile, overwrite = true)
            val activeMeta = getActiveMetaFile(listId)
            if (activeMeta.exists()) {
                activeMeta.copyTo(getLkgMetaFile(listId), overwrite = true)
            }
        }

        // Atomic activation: move staging to active
        if (activeFile.exists()) {
            activeFile.delete()
        }
        val activated = stageFile.renameTo(activeFile)
        if (!activated) {
            // Fallback copy if rename fails across filesystems
            stageFile.copyTo(activeFile, overwrite = true)
            stageFile.delete()
        }

        // Persist metadata
        saveMetadata(compiled.metadata)

        UpdateResult.Success(compiled.metadata)
    }

    /**
     * Rolls back to the last-known-good backup list.
     */
    suspend fun rollback(listId: String): Boolean = withContext(Dispatchers.IO) {
        val lkgFile = getLkgFile(listId)
        val activeFile = getActiveFile(listId)
        if (lkgFile.exists() && lkgFile.length() > 0L) {
            lkgFile.copyTo(activeFile, overwrite = true)
            val lkgMeta = getLkgMetaFile(listId)
            if (lkgMeta.exists()) {
                lkgMeta.copyTo(getActiveMetaFile(listId), overwrite = true)
            }
            true
        } else {
            false
        }
    }

    /**
     * Loads the active compiled filter list, falling back to LKG if active is corrupted.
     */
    suspend fun loadActiveOrRollback(
        listId: String,
        fallbackMetadata: FilterListMetadata
    ): CompiledFilterList? = withContext(Dispatchers.IO) {
        val activeFile = getActiveFile(listId)
        if (activeFile.exists() && activeFile.length() > 0L) {
            try {
                val compiled = FileInputStream(activeFile).use { input ->
                    FilterRuleCompiler.compile(input, getActiveMetadata(listId) ?: fallbackMetadata)
                }
                if ((compiled.metadata.totalSupportedRules == 0 || compiled.stats.syntaxErrors > 0) && hasLkgList(listId)) {
                    throw IllegalStateException("Active list corrupted with 0 rules or syntax errors")
                }
                return@withContext compiled
            } catch (_: Exception) {
                // Active file corrupted -> attempt rollback to LKG
                if (rollback(listId)) {
                    val lkgActive = getActiveFile(listId)
                    if (lkgActive.exists()) {
                        return@withContext FileInputStream(lkgActive).use { input ->
                            FilterRuleCompiler.compile(input, getActiveMetadata(listId) ?: fallbackMetadata)
                        }
                    }
                }
            }
        }
        null
    }

    suspend fun saveMetadata(metadata: FilterListMetadata) = withContext(Dispatchers.IO) {
        val metaFile = getActiveMetaFile(metadata.id)
        val sb = StringBuilder("{\n")
        sb.append("  \"id\": \"").append(escape(metadata.id)).append("\",\n")
        sb.append("  \"name\": \"").append(escape(metadata.name)).append("\",\n")
        sb.append("  \"version\": \"").append(escape(metadata.version)).append("\",\n")
        sb.append("  \"sourceUrl\": \"").append(escape(metadata.sourceUrl)).append("\",\n")
        sb.append("  \"homepage\": \"").append(escape(metadata.homepage)).append("\",\n")
        sb.append("  \"license\": \"").append(escape(metadata.license)).append("\",\n")
        sb.append("  \"attribution\": \"").append(escape(metadata.attribution)).append("\",\n")
        sb.append("  \"totalLines\": ").append(metadata.totalLines).append(",\n")
        sb.append("  \"supportedNetworkRules\": ").append(metadata.supportedNetworkRules).append(",\n")
        sb.append("  \"supportedExceptionRules\": ").append(metadata.supportedExceptionRules).append(",\n")
        sb.append("  \"unsupportedCosmeticRules\": ").append(metadata.unsupportedCosmeticRules).append(",\n")
        sb.append("  \"unsupportedScriptletRules\": ").append(metadata.unsupportedScriptletRules).append(",\n")
        sb.append("  \"otherUnsupportedRules\": ").append(metadata.otherUnsupportedRules).append(",\n")
        sb.append("  \"sha256Checksum\": \"").append(escape(metadata.sha256Checksum)).append("\",\n")
        sb.append("  \"lastUpdatedEpochMs\": ").append(metadata.lastUpdatedEpochMs).append(",\n")
        sb.append("  \"isEnabled\": ").append(metadata.isEnabled).append("\n")
        sb.append("}")
        metaFile.writeText(sb.toString(), Charsets.UTF_8)
    }

    suspend fun getActiveMetadata(listId: String): FilterListMetadata? = withContext(Dispatchers.IO) {
        val metaFile = getActiveMetaFile(listId)
        if (!metaFile.exists()) return@withContext null
        try {
            val content = metaFile.readText(Charsets.UTF_8)
            FilterListMetadata(
                id = extractString(content, "id"),
                name = extractString(content, "name"),
                version = extractString(content, "version").ifEmpty { "1.0" },
                sourceUrl = extractString(content, "sourceUrl"),
                homepage = extractString(content, "homepage"),
                license = extractString(content, "license"),
                attribution = extractString(content, "attribution"),
                totalLines = extractInt(content, "totalLines"),
                supportedNetworkRules = extractInt(content, "supportedNetworkRules"),
                supportedExceptionRules = extractInt(content, "supportedExceptionRules"),
                unsupportedCosmeticRules = extractInt(content, "unsupportedCosmeticRules"),
                unsupportedScriptletRules = extractInt(content, "unsupportedScriptletRules"),
                otherUnsupportedRules = extractInt(content, "otherUnsupportedRules"),
                sha256Checksum = extractString(content, "sha256Checksum"),
                lastUpdatedEpochMs = extractLong(content, "lastUpdatedEpochMs"),
                isEnabled = extractBoolean(content, "isEnabled")
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    private fun unescape(s: String) = s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")

    private fun extractString(json: String, key: String): String {
        val match = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"").find(json)
        return match?.groupValues?.get(1)?.let { unescape(it) } ?: ""
    }

    private fun extractInt(json: String, key: String): Int {
        val match = Regex("\"$key\"\\s*:\\s*(\\d+)").find(json)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun extractLong(json: String, key: String): Long {
        val match = Regex("\"$key\"\\s*:\\s*(\\d+)").find(json)
        return match?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }

    private fun extractBoolean(json: String, key: String): Boolean {
        val match = Regex("\"$key\"\\s*:\\s*(true|false)").find(json)
        return match?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: true
    }

    fun hasActiveList(listId: String): Boolean {
        val file = getActiveFile(listId)
        return file.exists() && file.length() > 0L
    }

    fun hasLkgList(listId: String): Boolean {
        val file = getLkgFile(listId)
        return file.exists() && file.length() > 0L
    }
}
