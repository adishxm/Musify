package com.musify.browser.shield

import java.io.File

/**
 * Event representing a single blocked third-party tracking or mining request.
 */
data class BlockedRequestEvent(
    val url: String,
    val category: String,
    val isThirdParty: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Categories of tracking and malicious requests recognized by Musify's engine-native content blocking.
 */
object ShieldCategories {
    const val ADS_AND_TRACKERS = "Advertising & Analytics Tracker"
    const val SOCIAL_TRACKERS = "Cross-Site Social Tracker"
    const val CRYPTOMINING = "Cryptomining Script"
    const val FINGERPRINTING = "Browser Fingerprinter"
    const val OTHER_TRACKING = "Other Tracking Content"
}

/**
 * Repository interface for managing per-origin shield exceptions.
 * Guarantees:
 * - Scoped to individual web origins (scheme + host + port).
 * - 100% reversible (user can re-enable protections at any time).
 * - Zero hardcoded exceptions for any production domain.
 */
interface ShieldExceptionRepository {
    fun isShieldDisabled(origin: String): Boolean
    fun setShieldDisabled(origin: String, disabled: Boolean)
    fun getExceptions(): Set<String>
    fun clearException(origin: String)
    fun clearAllExceptions()
}

/**
 * In-memory / file-backed repository for reversible per-origin shield exceptions.
 */
class LocalShieldExceptionRepository(private val storageFile: File? = null) : ShieldExceptionRepository {

    private val disabledOrigins = mutableSetOf<String>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun isShieldDisabled(origin: String): Boolean {
        if (origin.isBlank()) return false
        val normalized = normalizeOrigin(origin)
        return disabledOrigins.contains(normalized)
    }

    @Synchronized
    override fun setShieldDisabled(origin: String, disabled: Boolean) {
        if (origin.isBlank()) return
        val normalized = normalizeOrigin(origin)
        if (disabled) {
            disabledOrigins.add(normalized)
        } else {
            disabledOrigins.remove(normalized)
        }
        saveToDisk()
    }

    @Synchronized
    override fun getExceptions(): Set<String> {
        return disabledOrigins.toSet()
    }

    @Synchronized
    override fun clearException(origin: String) {
        val normalized = normalizeOrigin(origin)
        if (disabledOrigins.remove(normalized)) {
            saveToDisk()
        }
    }

    @Synchronized
    override fun clearAllExceptions() {
        disabledOrigins.clear()
        saveToDisk()
    }

    private fun normalizeOrigin(raw: String): String {
        return raw.trim().lowercase()
    }

    private fun loadFromDisk() {
        if (storageFile == null || !storageFile.exists()) return
        try {
            val lines = storageFile.readLines()
            disabledOrigins.clear()
            disabledOrigins.addAll(lines.filter { it.isNotBlank() }.map { normalizeOrigin(it) })
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
            storageFile.writeText(disabledOrigins.joinToString("\n"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
