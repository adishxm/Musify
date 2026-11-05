package com.musify.browser.safety

import java.net.URI
import java.net.URLDecoder

/**
 * DebounceInterceptor: Implements bounce-tracking bypass.
 *
 * Reverse-engineered from Brave Browser's debounce.json component.
 * Intercepts tracking intermediary redirect domains (e.g. dpbolvw.net, tkqlhce.com,
 * avantlink.com, howl.me) and extracts the embedded target URL directly,
 * bypassing the tracking hop completely.
 */
object DebounceInterceptor {

    // Known bounce tracking intermediary domains from Brave's debounce.json
    private val BOUNCE_DOMAINS = setOf(
        "dpbolvw.net",
        "tkqlhce.com",
        "avantlink.com",
        "howl.me",
        "pntrac.com",
        "jdoqocy.com",
        "kqzyfj.com",
        "pjtra.com",
        "pjatr.com",
        "pntrs.com",
        "gopjn.com",
        "anrdoezrs.net",
        "clickmate",
        "redirectingat.com",
        "skimresources.com",
        "tradedoubler.com",
        "flexlinks.com",
        "cfjump.com",
        "bttn.io",
        "narrativ.com",
        "pepperjamnetwork.com",
        "wordseed.com",
        "laylo.co",
        "effiliation.com"
    )

    // Destination parameter keys commonly used by bounce trackers
    private val DESTINATION_PARAM_KEYS = listOf(
        "url",
        "Url",
        "URL",
        "btn_url",
        "link",
        "dest",
        "destination",
        "target",
        "redirect",
        "r"
    )

    /**
     * Checks if a host is an identified bounce-tracking domain.
     */
    fun isBounceTracker(host: String): Boolean {
        if (host.isBlank()) return false
        val lower = host.lowercase()
        return BOUNCE_DOMAINS.any { lower == it || lower.endsWith(".$it") }
    }

    /**
     * Inspects [rawUrl]. If it matches a bounce tracking intermediary, extracts the
     * embedded destination URL, sanitizes its tracking tokens, and returns it.
     * If not a bounce tracker, returns [rawUrl].
     */
    fun resolveBounceUrl(rawUrl: String): String {
        if (rawUrl.isBlank() || !rawUrl.contains('?')) return rawUrl

        return try {
            val uri = URI(rawUrl)
            val host = uri.host?.lowercase() ?: return rawUrl

            if (!isBounceTracker(host)) return rawUrl

            val query = uri.rawQuery ?: return rawUrl
            val pairs = query.split('&')

            for (key in DESTINATION_PARAM_KEYS) {
                for (pair in pairs) {
                    val eqIdx = pair.indexOf('=')
                    if (eqIdx != -1) {
                        val k = pair.substring(0, eqIdx)
                        if (k.equals(key, ignoreCase = true)) {
                            val rawVal = pair.substring(eqIdx + 1)
                            val decoded = URLDecoder.decode(rawVal, "UTF-8")
                            if (decoded.startsWith("http://", ignoreCase = true) ||
                                decoded.startsWith("https://", ignoreCase = true)
                            ) {
                                // Extract destination and strip any tracking parameters from the target URL
                                return TrackingParamSanitizer.sanitize(decoded)
                            }
                        }
                    }
                }
            }
            rawUrl
        } catch (_: Throwable) {
            rawUrl
        }
    }
}
