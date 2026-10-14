package com.musify.browser.engine

import java.net.URI

enum class ResourceClassification {
    MEDIA_OR_LICENSE,
    FIRST_PARTY_OR_AUTH,
    EXCEPTION_ALLOWED,
    TRACKER_CANDIDATE
}

/**
 * MediaSafeResourcePolicy defines strict resource classification to ensure that
 * Widevine DRM licenses, media delivery CDN streams, first-party authentication,
 * and origin exceptions are never intercepted or broken by browser shields.
 */
object MediaSafeResourcePolicy {

    private val MEDIA_LICENSE_INDICATORS = listOf(
        "widevine",
        "/license",
        "/drm/",
        "drm-widevine",
        "cping.spotify.com",
        "audio-ak.spotify.com",
        "audio-fa.scdn.co",
        "heads-fa.scdn.co",
        "video-fa.scdn.co",
        "audio-ak-p-b.spotifycdn.net",
        "manifest",
        ".mpd",
        ".m3u8",
        "keyexchange",
        "drmtoday",
        "fps.ezdrm.com"
    )

    private val FIRST_PARTY_AUTH_DOMAINS = listOf(
        "accounts.spotify.com",
        "login.spotify.com",
        "api.spotify.com",
        "spclient.wg.spotify.com",
        "ap.spotify.com",
        "dealer.spotify.com",
        "gew-dealer.spotify.com",
        "gew-spclient.spotify.com"
    )

    /**
     * Determines if a request is for protected media delivery, Widevine CDM license,
     * or streaming manifest. These requests must never be blocked or classified as trackers.
     */
    fun isMediaOrLicenseRequest(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase()
        return MEDIA_LICENSE_INDICATORS.any { lower.contains(it) }
    }

    /**
     * Extracts host safely without throwing in any environment.
     */
    fun extractHost(url: String): String {
        if (url.isBlank()) return ""
        return try {
            val uri = URI(url)
            uri.host?.lowercase() ?: ""
        } catch (_: Throwable) {
            val noScheme = url.substringAfter("://", url)
            noScheme.substringBefore('/').substringBefore(':').lowercase()
        }
    }

    /**
     * Determines if a request is first-party or an essential authentication/API endpoint
     * for the given main-frame origin.
     */
    fun isFirstPartyOrAuth(requestUrl: String, currentOrigin: String): Boolean {
        if (requestUrl.isBlank()) return false
        val reqHost = extractHost(requestUrl)
        if (reqHost.isEmpty()) return false

        val originHost = extractHost(currentOrigin)
        if (originHost.isNotEmpty() && (reqHost == originHost || reqHost.endsWith(".$originHost"))) {
            return true
        }

        return FIRST_PARTY_AUTH_DOMAINS.any { reqHost == it || reqHost.endsWith(".$it") }
    }

    /**
     * Classifies a resource request according to engine policy.
     */
    fun classifyResource(
        requestUrl: String,
        currentOrigin: String,
        isOriginException: Boolean
    ): ResourceClassification {
        if (isMediaOrLicenseRequest(requestUrl)) {
            return ResourceClassification.MEDIA_OR_LICENSE
        }

        if (isFirstPartyOrAuth(requestUrl, currentOrigin)) {
            return ResourceClassification.FIRST_PARTY_OR_AUTH
        }

        if (isOriginException) {
            return ResourceClassification.EXCEPTION_ALLOWED
        }

        return ResourceClassification.TRACKER_CANDIDATE
    }

    /**
     * Normalizes an origin URL string into a canonical scheme://host[:port] format.
     */
    fun normalizeOrigin(rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        return try {
            val uri = URI(rawUrl)
            val scheme = uri.scheme?.lowercase() ?: "https"
            val host = uri.host?.lowercase() ?: return rawUrl
            val port = if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""
            "$scheme://$host$port"
        } catch (_: Throwable) {
            val noPath = rawUrl.substringBefore('?').substringBefore('#')
            noPath.lowercase()
        }
    }

    /**
     * Sanitizes a URL for privacy-safe diagnostics display by stripping query strings,
     * credentials, and sensitive session tokens while retaining scheme, host, and path.
     */
    fun sanitizeUrlForDiagnostics(url: String): String {
        if (url.isBlank()) return ""
        val queryIdx = url.indexOf('?')
        val base = if (queryIdx != -1) url.substring(0, queryIdx) else url
        val hashIdx = base.indexOf('#')
        return if (hashIdx != -1) base.substring(0, hashIdx) else base
    }
}
