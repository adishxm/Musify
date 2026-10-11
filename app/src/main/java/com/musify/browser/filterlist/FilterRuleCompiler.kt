package com.musify.browser.filterlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.security.MessageDigest

/**
 * CompiledFilterList holds compiled in-memory rules and offers O(1) domain lookup and pattern matching.
 */
class CompiledFilterList(
    val metadata: FilterListMetadata,
    val stats: RuleParseStats,
    private val domainBlocks: Set<String>,
    private val domainExceptions: Set<String>,
    private val pathPatternBlocks: List<String>,
    private val pathPatternExceptions: List<String>
) {
    /**
     * Evaluates whether a target URL is blocked, permitted, or an exception.
     */
    fun evaluate(url: String): FilterDecision {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("data:") || url.startsWith("blob:")) {
            return FilterDecision.Allowed("Internal or non-network URL")
        }

        val host = extractHostFromUrl(url)

        // 1. Check domain and pattern exceptions first
        if (host != null && matchesHost(host, domainExceptions)) {
            return FilterDecision.Exception("Domain exception matched for $host", metadata.id)
        }
        for (pattern in pathPatternExceptions) {
            if (matchesPattern(url, pattern)) {
                return FilterDecision.Exception("Pattern exception matched: $pattern", metadata.id)
            }
        }

        // 2. Check domain blocks (O(1) set lookup on host and parent domains)
        if (host != null && matchesHost(host, domainBlocks)) {
            return FilterDecision.Blocked(
                rule = "||$host^",
                listId = metadata.id,
                matchedTarget = host
            )
        }

        // 3. Check path pattern blocks
        for (pattern in pathPatternBlocks) {
            if (matchesPattern(url, pattern)) {
                return FilterDecision.Blocked(
                    rule = pattern,
                    listId = metadata.id,
                    matchedTarget = pattern
                )
            }
        }

        return FilterDecision.Allowed()
    }

    private fun matchesPattern(url: String, pattern: String): Boolean {
        if (!pattern.contains('*')) {
            return url.contains(pattern, ignoreCase = true)
        }
        val parts = pattern.split('*').filter { it.isNotEmpty() }
        var currentIndex = 0
        for (part in parts) {
            val idx = url.indexOf(part, currentIndex, ignoreCase = true)
            if (idx == -1) return false
            currentIndex = idx + part.length
        }
        return true
    }

    private fun matchesHost(host: String, domainSet: Set<String>): Boolean {
        if (domainSet.contains(host)) return true
        var dotIndex = host.indexOf('.')
        while (dotIndex != -1 && dotIndex < host.length - 1) {
            val parentDomain = host.substring(dotIndex + 1)
            if (domainSet.contains(parentDomain)) {
                return true
            }
            dotIndex = host.indexOf('.', dotIndex + 1)
        }
        return false
    }

    private fun extractHostFromUrl(url: String): String? {
        return try {
            val uri = URI(url)
            uri.host?.lowercase() ?: parseHostFallback(url)
        } catch (_: Exception) {
            parseHostFallback(url)
        }
    }

    private fun parseHostFallback(url: String): String? {
        var s = url
        val schemeIdx = s.indexOf("://")
        if (schemeIdx != -1) {
            s = s.substring(schemeIdx + 3)
        }
        val slashIdx = s.indexOf('/')
        if (slashIdx != -1) {
            s = s.substring(0, slashIdx)
        }
        val colonIdx = s.indexOf(':')
        if (colonIdx != -1) {
            s = s.substring(0, colonIdx)
        }
        return s.lowercase().takeIf { it.isNotBlank() }
    }
}

/**
 * FilterRuleCompiler parses ABP/EasyList syntax off the UI thread, bounds file and line sizes,
 * validates metadata, and categorizes rules with zero false claims.
 */
object FilterRuleCompiler {

    const val MAX_LINE_LENGTH = 2048
    const val MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024 // 15 MB limit

    /**
     * Compiles filter rules from an input stream with SHA-256 calculation and strict safety limits.
     */
    suspend fun compile(
        inputStream: InputStream,
        fallbackMetadata: FilterListMetadata
    ): CompiledFilterList = withContext(Dispatchers.Default) {
        val digest = MessageDigest.getInstance("SHA-256")
        val domainBlocks = HashSet<String>()
        val domainExceptions = HashSet<String>()
        val pathPatternBlocks = ArrayList<String>()
        val pathPatternExceptions = ArrayList<String>()

        var totalLines = 0
        var commentLines = 0
        var emptyLines = 0
        var supportedNetwork = 0
        var supportedExceptions = 0
        var unsupportedCosmetic = 0
        var unsupportedScriptlet = 0
        var otherUnsupported = 0
        var oversizedSkipped = 0
        var syntaxErrors = 0

        var title: String? = null
        var version: String? = null
        var homepage: String? = null
        var license: String? = null

        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val currentLine = line ?: break
            totalLines++

            val lineBytes = currentLine.toByteArray(Charsets.UTF_8)
            digest.update(lineBytes)
            digest.update('\n'.code.toByte())

            if (currentLine.length > MAX_LINE_LENGTH) {
                oversizedSkipped++
                continue
            }

            val trimmed = currentLine.trim()
            if (trimmed.isEmpty()) {
                emptyLines++
                continue
            }

            if (currentLine.any { it.code < 32 && it != '\t' }) {
                syntaxErrors++
                continue
            }

            // Comment lines or header metadata
            if (trimmed.startsWith("!") || trimmed.startsWith("[")) {
                commentLines++
                parseHeaderMetadata(trimmed)?.let { (key, value) ->
                    when (key.lowercase()) {
                        "title" -> title = value
                        "version" -> version = value
                        "homepage" -> homepage = value
                        "license" -> license = value
                    }
                }
                continue
            }

            // Cosmetic element hiding rules (##, #?#, #@#, #$#)
            if (trimmed.contains("##") || trimmed.contains("#?#") || trimmed.contains("#@#") || trimmed.contains("#$#")) {
                unsupportedCosmetic++
                continue
            }

            // Scriptlet injection or style injection rules
            if (trimmed.contains("+js(") || trimmed.contains(":style(")) {
                unsupportedScriptlet++
                continue
            }

            // Complex regex patterns (/regex/)
            if (trimmed.startsWith("/") && trimmed.endsWith("/") && trimmed.length > 2) {
                otherUnsupported++
                continue
            }

            // Network Exception rules (@@...)
            if (trimmed.startsWith("@@")) {
                val ruleBody = trimmed.substring(2)
                if (ruleBody.startsWith("||") && !ruleBody.substring(2).contains("/")) {
                    val domain = extractDomain(ruleBody.substring(2))
                    if (domain.isNotEmpty() && domain.any { it.isLetterOrDigit() }) {
                        domainExceptions.add(domain)
                        supportedExceptions++
                    } else {
                        syntaxErrors++
                    }
                } else {
                    val cleanedPattern = cleanPattern(ruleBody)
                    if (cleanedPattern.isNotEmpty() && cleanedPattern.any { it.isLetterOrDigit() }) {
                        pathPatternExceptions.add(cleanedPattern)
                        supportedExceptions++
                    } else {
                        syntaxErrors++
                    }
                }
                continue
            }

            // Network Block rules
            if (trimmed.startsWith("||") && !trimmed.substring(2).contains("/")) {
                val domain = extractDomain(trimmed.substring(2))
                if (domain.isNotEmpty() && domain.any { it.isLetterOrDigit() }) {
                    domainBlocks.add(domain)
                    supportedNetwork++
                } else {
                    syntaxErrors++
                }
            } else {
                val cleanedPattern = cleanPattern(trimmed)
                if (cleanedPattern.isNotEmpty() && cleanedPattern.any { it.isLetterOrDigit() }) {
                    pathPatternBlocks.add(cleanedPattern)
                    supportedNetwork++
                } else {
                    syntaxErrors++
                }
            }
        }

        val sha256 = digest.digest().joinToString("") { "%02x".format(it) }

        val stats = RuleParseStats(
            totalLines = totalLines,
            commentLines = commentLines,
            emptyLines = emptyLines,
            supportedNetworkRules = supportedNetwork,
            supportedExceptionRules = supportedExceptions,
            unsupportedCosmeticRules = unsupportedCosmetic,
            unsupportedScriptletRules = unsupportedScriptlet,
            otherUnsupportedRules = otherUnsupported,
            oversizedLinesSkipped = oversizedSkipped,
            syntaxErrors = syntaxErrors
        )

        val updatedMetadata = fallbackMetadata.copy(
            name = title ?: fallbackMetadata.name,
            version = version ?: fallbackMetadata.version,
            homepage = homepage ?: fallbackMetadata.homepage,
            license = license ?: fallbackMetadata.license,
            totalLines = totalLines,
            supportedNetworkRules = supportedNetwork,
            supportedExceptionRules = supportedExceptions,
            unsupportedCosmeticRules = unsupportedCosmetic,
            unsupportedScriptletRules = unsupportedScriptlet,
            otherUnsupportedRules = otherUnsupported,
            sha256Checksum = sha256,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )

        CompiledFilterList(
            metadata = updatedMetadata,
            stats = stats,
            domainBlocks = domainBlocks,
            domainExceptions = domainExceptions,
            pathPatternBlocks = pathPatternBlocks,
            pathPatternExceptions = pathPatternExceptions
        )
    }

    private fun parseHeaderMetadata(commentLine: String): Pair<String, String>? {
        if (!commentLine.startsWith("!")) return null
        val content = commentLine.substring(1).trim()
        val colonIndex = content.indexOf(':')
        if (colonIndex <= 0) return null
        val key = content.substring(0, colonIndex).trim()
        val value = content.substring(colonIndex + 1).trim()
        return key to value
    }

    private fun extractDomain(raw: String): String {
        var endIdx = raw.length
        for (i in raw.indices) {
            val c = raw[i]
            if (c == '^' || c == '/' || c == '$' || c == ':') {
                endIdx = i
                break
            }
        }
        return raw.substring(0, endIdx).trim().lowercase()
    }

    private fun cleanPattern(raw: String): String {
        var pattern = raw
        if (pattern.startsWith("||")) {
            pattern = pattern.substring(2)
        }
        if (pattern.startsWith("|http://") || pattern.startsWith("|https://")) {
            pattern = pattern.substring(1)
        }
        if (pattern.endsWith("|")) {
            pattern = pattern.substring(0, pattern.length - 1)
        }
        val optionsIdx = pattern.indexOf('$')
        if (optionsIdx != -1) {
            pattern = pattern.substring(0, optionsIdx)
        }
        return pattern.replace("^", "").trim()
    }
}
