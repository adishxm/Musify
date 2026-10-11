package com.musify.browser.downloads

import java.util.Locale

/**
 * DownloadSecurityUtils enforces strict filename sanitization,
 * directory traversal protection, and executable threat detection.
 */
object DownloadSecurityUtils {

    private val RESERVED_NAMES = setOf(
        "con", "prn", "aux", "nul",
        "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
        "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    )

    private val EXECUTABLE_EXTENSIONS = setOf(
        "apk", "dex", "sh", "exe", "bat", "cmd", "jar", "vbs", "ps1", "elf", "bin", "msi"
    )

    /**
     * Sanitizes a suggested filename by removing path traversal characters,
     * control codes, forbidden symbols, and reserved system names.
     */
    fun sanitizeFileName(suggestedName: String?, fallbackMime: String? = null): String {
        if (suggestedName.isNullOrBlank()) {
            return generateFallbackName(fallbackMime)
        }

        // 1. Strip path components (slashes & backslashes)
        var name = suggestedName
            .replace('\\', '/')
            .substringAfterLast('/')
            .trim()

        // 2. Strip control characters (< 32) and null bytes
        name = name.filter { it.code >= 32 }

        // 3. Remove forbidden characters in Android/FAT32 filesystems: < > : " / \ | ? *
        name = name.replace(Regex("""[<>:"/\\|?*]"""), "")

        // 4. Strip leading dots to prevent creating hidden system files (.bashrc, .env)
        while (name.startsWith('.')) {
            name = name.substring(1)
        }

        // 5. Check for Windows / DOS reserved device names
        val baseName = name.substringBeforeLast('.').lowercase(Locale.ROOT)
        if (RESERVED_NAMES.contains(baseName)) {
            name = "file_$name"
        }

        // 6. Bound length to 120 characters while preserving extension
        if (name.length > 120) {
            val ext = name.substringAfterLast('.', "")
            val maxBaseLen = if (ext.isNotEmpty()) 120 - (ext.length + 1) else 120
            val truncatedBase = name.substringBeforeLast('.').take(maxBaseLen)
            name = if (ext.isNotEmpty()) "$truncatedBase.$ext" else truncatedBase
        }

        return if (name.isBlank() || name == ".") {
            generateFallbackName(fallbackMime)
        } else {
            name
        }
    }

    /**
     * Returns true if the file extension is considered potentially executable or high-risk.
     */
    fun isExecutable(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return EXECUTABLE_EXTENSIONS.contains(ext)
    }

    private fun generateFallbackName(mimeType: String?): String {
        val ext = when (mimeType?.lowercase(Locale.ROOT)) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "application/pdf" -> "pdf"
            "text/html" -> "html"
            "text/plain" -> "txt"
            "application/zip" -> "zip"
            else -> "bin"
        }
        return "download.$ext"
    }
}
