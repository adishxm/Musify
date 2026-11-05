package com.musify.browser.shield

import com.musify.browser.engine.MediaSafeResourcePolicy
import com.musify.browser.engine.ResourceClassification
import com.musify.browser.engine.SabrProtobufDecoder
import com.musify.browser.filterlist.FilterDecision
import com.musify.browser.filterlist.FilterEngine
import com.musify.browser.safety.DebounceInterceptor
import com.musify.browser.safety.TrackingParamSanitizer
import java.util.concurrent.atomic.AtomicLong

/**
 * UnifiedShieldAction represents the shielding action determined for a resource request or navigation.
 */
sealed class UnifiedShieldAction {
    data class Allow(val reason: String) : UnifiedShieldAction()
    data class Block(val reason: String, val category: String) : UnifiedShieldAction()
    data class MockResponse(val mimeType: String, val body: String) : UnifiedShieldAction()
    data class CleanNavigation(val sanitizedUrl: String, val wasDebounced: Boolean) : UnifiedShieldAction()
}

/**
 * Statistics tracked by UnifiedShieldController across all active tiers.
 */
data class ShieldMetrics(
    val requestsEvaluated: Long = 0L,
    val trackersBlocked: Long = 0L,
    val protectedMediaAllowed: Long = 0L,
    val trackingParamsCleaned: Long = 0L,
    val bounceHopsDefused: Long = 0L,
    val sabrStreamsProtected: Long = 0L
)

/**
 * UnifiedShieldController is the central orchestrator harmonizing all privacy and adblocking tiers:
 * 1. Network Filtering & Media-Safe Protection (MediaSafeResourcePolicy + FilterEngine)
 * 2. Spotify & YouTube Network Ad Mocking (Anti-Freeze 200 OK responses)
 * 3. YouTube SABR Stream Protocol Defense (Protobuf Field 4 Varint Rewriting)
 * 4. URL Navigation Cleaning (TrackingParamSanitizer + DebounceInterceptor)
 * 5. Reversible Per-Origin Exemption Management (ShieldExceptionRepository)
 */
class UnifiedShieldController(
    private val exceptionRepository: ShieldExceptionRepository? = null,
    private val filterEngine: FilterEngine? = null
) {
    private val requestsEvaluatedCounter = AtomicLong(0)
    private val trackersBlockedCounter = AtomicLong(0)
    private val protectedMediaAllowedCounter = AtomicLong(0)
    private val trackingParamsCleanedCounter = AtomicLong(0)
    private val bounceHopsDefusedCounter = AtomicLong(0)
    private val sabrStreamsProtectedCounter = AtomicLong(0)

    companion object {
        private const val EMPTY_JSON = "{}"
        private const val APPLICATION_JSON = "application/json"

        private val MOCK_EMPTY_JSON_ENDPOINTS = listOf(
            "spclient.wg.spotify.com/ad-logic/",
            "spclient.wg.spotify.com/ads/",
            "youtube.com/api/stats/ads",
            "youtube.com/youtubei/v1/player/ad_break"
        )

        private val KNOWN_AD_DOMAINS = listOf(
            "googleads.g.doubleclick.net",
            "pagead2.googlesyndication.com",
            "adservice.google.com",
            "audio-ak-spotify-com.akamaized.net",
            "ads.spotify.com"
        )
    }

    /**
     * Evaluates a resource request URL within the context of a tab's main origin.
     */
    fun evaluateResource(requestUrl: String, mainFrameOrigin: String): UnifiedShieldAction {
        requestsEvaluatedCounter.incrementAndGet()

        val isOriginDisabled = exceptionRepository?.isShieldDisabled(mainFrameOrigin) ?: false
        val classification = MediaSafeResourcePolicy.classifyResource(
            requestUrl = requestUrl,
            currentOrigin = mainFrameOrigin,
            isOriginException = isOriginDisabled
        )

        // 1. Never block media playback, Widevine DRM licenses, or manifests
        if (classification == ResourceClassification.MEDIA_OR_LICENSE) {
            protectedMediaAllowedCounter.incrementAndGet()
            if (SabrProtobufDecoder.isSabrRequest(requestUrl)) {
                sabrStreamsProtectedCounter.incrementAndGet()
            }
            return UnifiedShieldAction.Allow("Media stream or DRM license preserved by MediaSafeResourcePolicy")
        }

        // 2. User-granted per-origin exception: honor immediately
        if (classification == ResourceClassification.EXCEPTION_ALLOWED || isOriginDisabled) {
            return UnifiedShieldAction.Allow("User disabled shields for origin: $mainFrameOrigin")
        }

        val lowerUrl = requestUrl.lowercase()

        // 3. Anti-freeze JSON mock responses for ad endpoints that would crash playback if 403'd
        if (MOCK_EMPTY_JSON_ENDPOINTS.any { lowerUrl.contains(it) }) {
            trackersBlockedCounter.incrementAndGet()
            return UnifiedShieldAction.MockResponse(APPLICATION_JSON, EMPTY_JSON)
        }

        // 4. Known ad network trackers
        if (KNOWN_AD_DOMAINS.any { lowerUrl.contains(it) }) {
            trackersBlockedCounter.incrementAndGet()
            return UnifiedShieldAction.Block("Blocked known advertising domain", ShieldCategories.ADS_AND_TRACKERS)
        }

        // 5. Allow essential first-party endpoints and auth
        if (classification == ResourceClassification.FIRST_PARTY_OR_AUTH) {
            return UnifiedShieldAction.Allow("First-party authentication or essential service")
        }

        // 6. Consult FilterEngine if registered
        if (filterEngine != null) {
            when (val decision = filterEngine.evaluate(requestUrl)) {
                is FilterDecision.Blocked -> {
                    trackersBlockedCounter.incrementAndGet()
                    return UnifiedShieldAction.Block(
                        reason = "FilterEngine rule: ${decision.rule}",
                        category = ShieldCategories.ADS_AND_TRACKERS
                    )
                }
                is FilterDecision.Exception -> {
                    return UnifiedShieldAction.Allow("FilterEngine exception rule: ${decision.rule}")
                }
                is FilterDecision.Allowed -> {}
            }
        }

        return UnifiedShieldAction.Allow("Default allow")
    }

    /**
     * Sanitizes an outgoing URL or navigation target:
     * - Bypasses intermediate bounce trackers (e.g. dpbolvw.net, howl.me, tkqlhce.com).
     * - Strips invasive query tracking parameters (fbclid, gclid, _gl, etc.).
     */
    fun sanitizeNavigation(rawUrl: String): UnifiedShieldAction.CleanNavigation {
        val debounced = DebounceInterceptor.resolveBounceUrl(rawUrl)
        val wasDebounced = debounced != rawUrl
        if (wasDebounced) {
            bounceHopsDefusedCounter.incrementAndGet()
        }

        val cleaned = TrackingParamSanitizer.sanitize(debounced)
        val trackingCleaned = cleaned != debounced || (wasDebounced && rawUrl != cleaned)
        if (trackingCleaned) {
            trackingParamsCleanedCounter.incrementAndGet()
        }

        return UnifiedShieldAction.CleanNavigation(
            sanitizedUrl = cleaned,
            wasDebounced = wasDebounced
        )
    }

    /**
     * Returns current snapshot of metrics.
     */
    fun getMetrics(): ShieldMetrics {
        return ShieldMetrics(
            requestsEvaluated = requestsEvaluatedCounter.get(),
            trackersBlocked = trackersBlockedCounter.get(),
            protectedMediaAllowed = protectedMediaAllowedCounter.get(),
            trackingParamsCleaned = trackingParamsCleanedCounter.get(),
            bounceHopsDefused = bounceHopsDefusedCounter.get(),
            sabrStreamsProtected = sabrStreamsProtectedCounter.get()
        )
    }

    /**
     * Resets telemetry metrics.
     */
    fun resetMetrics() {
        requestsEvaluatedCounter.set(0)
        trackersBlockedCounter.set(0)
        protectedMediaAllowedCounter.set(0)
        trackingParamsCleanedCounter.set(0)
        bounceHopsDefusedCounter.set(0)
        sabrStreamsProtectedCounter.set(0)
    }
}

