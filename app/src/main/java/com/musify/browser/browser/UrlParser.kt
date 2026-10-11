package com.musify.browser.browser

import android.net.Uri
import java.net.IDN
import java.net.URLEncoder
import java.util.Locale
import java.util.regex.Pattern

sealed class ParsedNavigation {
    data class AllowedUrl(
        val url: String,
        val origin: String,
        val host: String,
        val isHttps: Boolean,
        val isPunycode: Boolean
    ) : ParsedNavigation()

    data class Search(
        val query: String,
        val searchUrl: String
    ) : ParsedNavigation()

    data class Rejected(
        val rawInput: String,
        val reason: String
    ) : ParsedNavigation()
}

object UrlParser {
    private const val DEFAULT_SEARCH_ENGINE_URL = "https://duckduckgo.com/?q="

    private val BLOCKED_SCHEMES = setOf(
        "javascript",
        "file",
        "content",
        "intent",
        "data",
        "blob",
        "about"
    )

    private val IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:\\d{1,5})?$"
    )

    private val DOMAIN_PATTERN = Pattern.compile(
        "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}(?::\\d{1,5})?(?:/.*)?$"
    )

    fun parse(rawInput: String): ParsedNavigation {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return ParsedNavigation.Rejected(trimmed, "Empty navigation input")
        }

        // Check for blocked top-level schemes
        val colonIndex = trimmed.indexOf(':')
        if (colonIndex > 0) {
            val potentialScheme = trimmed.substring(0, colonIndex).lowercase(Locale.ROOT)
            if (BLOCKED_SCHEMES.contains(potentialScheme)) {
                return ParsedNavigation.Rejected(
                    trimmed,
                    "Top-level navigation with '$potentialScheme:' scheme is rejected for safety."
                )
            }
        }

        // Check if explicitly http or https
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return try {
                val uri = Uri.parse(trimmed)
                val host = uri.host ?: ""
                if (host.isBlank()) {
                    ParsedNavigation.Rejected(trimmed, "Malformed URL missing valid host")
                } else {
                    buildAllowedUrl(uri)
                }
            } catch (e: Exception) {
                ParsedNavigation.Rejected(trimmed, "Malformed URL: ${e.message}")
            }
        }

        // Does it look like a search query (contains spaces or starts with search operator)?
        if (trimmed.contains(" ") || !trimmed.contains(".")) {
            if (trimmed.equals("localhost", ignoreCase = true) || trimmed.startsWith("localhost:", ignoreCase = true)) {
                return buildAllowedUrl(Uri.parse("http://$trimmed"))
            }
            return buildSearch(trimmed)
        }

        // Check IPv4 or Domain pattern
        if (IPV4_PATTERN.matcher(trimmed).matches() || DOMAIN_PATTERN.matcher(trimmed).matches()) {
            return try {
                // HTTPS-First upgrade by default
                val targetUrl = "https://$trimmed"
                val uri = Uri.parse(targetUrl)
                buildAllowedUrl(uri)
            } catch (_: Exception) {
                buildSearch(trimmed)
            }
        }

        // Fallback to privacy search query
        return buildSearch(trimmed)
    }

    private fun buildAllowedUrl(uri: Uri): ParsedNavigation {
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: "https"
        val rawHost = uri.host ?: ""
        val port = if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""

        // Anti-spoofing: IDN ASCII/Unicode conversion
        val asciiHost = try {
            IDN.toASCII(rawHost)
        } catch (_: Exception) {
            rawHost
        }
        val isPunycode = asciiHost.startsWith("xn--") || asciiHost.contains(".xn--")

        val origin = "$scheme://$asciiHost$port"
        val fullUrl = uri.toString()

        return ParsedNavigation.AllowedUrl(
            url = fullUrl,
            origin = origin,
            host = asciiHost,
            isHttps = scheme == "https",
            isPunycode = isPunycode
        )
    }

    private fun buildSearch(query: String): ParsedNavigation {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val searchUrl = "$DEFAULT_SEARCH_ENGINE_URL$encoded"
        return ParsedNavigation.Search(query, searchUrl)
    }
}
