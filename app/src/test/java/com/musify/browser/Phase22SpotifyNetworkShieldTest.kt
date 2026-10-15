package com.musify.browser

import com.musify.browser.engine.MediaSafeResourcePolicy
import com.musify.browser.engine.ResourceClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 22 Unit Tests: Spotify Network Shield Harmonization, Anti-Freeze Mocking & Policy Integration
 */
class Phase22SpotifyNetworkShieldTest {

    private val webViewAdapterFile = File("src/main/java/com/musify/browser/engine/WebViewAdapter.kt")
    private val mediaPolicyFile = File("src/main/java/com/musify/browser/engine/MediaSafeResourcePolicy.kt")

    @Test
    fun testSpotifyWidevineDrmLicenseEndpointsPreserved() {
        val licenseUrls = listOf(
            "https://spclient.wg.spotify.com/widevine-license/v1/license",
            "https://spclient.wg.spotify.com/license",
            "https://audio-fa.scdn.co/audio/track_id_999",
            "https://audio-ak.spotify.com/audio/segment_001",
            "https://audio-ak-p-b.spotifycdn.net/media/track_id_555"
        )

        for (url in licenseUrls) {
            assertTrue("Must classify as media or license: $url", MediaSafeResourcePolicy.isMediaOrLicenseRequest(url))
            assertEquals(
                ResourceClassification.MEDIA_OR_LICENSE,
                MediaSafeResourcePolicy.classifyResource(url, "https://open.spotify.com", false)
            )
        }
    }

    @Test
    fun testSpotifyAdAndTelemetrySubdomainsClassifiedAsTrackers() {
        val telemetryUrls = listOf(
            "https://adeventtracker.spotify.com/event",
            "https://adstudio.spotify.com/api",
            "https://pixel.spotify.com/event.gif",
            "https://analytics.spotify.com/v1/log",
            "https://google-analytics.com/analytics.js",
            "https://doubleclick.net/ad"
        )

        for (url in telemetryUrls) {
            assertFalse("Telemetry must NOT be classified as media/license: $url", MediaSafeResourcePolicy.isMediaOrLicenseRequest(url))
            val classification = MediaSafeResourcePolicy.classifyResource(url, "https://open.spotify.com", false)
            assertEquals("Must be classified as tracker candidate: $url", ResourceClassification.TRACKER_CANDIDATE, classification)
        }
    }

    @Test
    fun testAntiFreezeResponseSynthesizedForSpclientAdEndpoints() {
        assertTrue("WebViewAdapter.kt must exist", webViewAdapterFile.exists())
        val content = webViewAdapterFile.readText()

        // Anti-freeze logic check
        assertTrue("Must detect spclient ad-logic or ads endpoints", content.contains("isSpclientAdLogic"))
        assertTrue("Must return application/json 200 OK for anti-freeze", content.contains("\"application/json\"") && content.contains("200"))
        assertTrue("Must return empty JSON object to avoid playback freeze loop", content.contains("\"{}\".toByteArray(Charsets.UTF_8)"))
    }

    @Test
    fun testMediaSafeCheckPrecedesAdKeywordsInWebViewAdapter() {
        assertTrue("WebViewAdapter.kt must exist", webViewAdapterFile.exists())
        val content = webViewAdapterFile.readText()

        val mediaSafeCheckIndex = content.indexOf("MediaSafeResourcePolicy.isMediaOrLicenseRequest(requestUrl)")
        val isSpotifyAdIndex = content.indexOf("val isSpotifyAd = spotifyAdKeywords.any")

        assertTrue("MediaSafe check must exist", mediaSafeCheckIndex != -1)
        assertTrue("isSpotifyAd check must exist", isSpotifyAdIndex != -1)
        assertTrue(
            "MediaSafe check must precede ad keyword blocking to protect Widevine DRM & CDN audio streams",
            mediaSafeCheckIndex < isSpotifyAdIndex
        )
    }

    @Test
    fun testBraveCosmeticSelectorsPresentInFallbackScript() {
        assertTrue("WebViewAdapter.kt must exist", webViewAdapterFile.exists())
        val content = webViewAdapterFile.readText()

        val selectors = listOf(
            "div[data-testid=\"advertisement-row\"]",
            "aside[aria-label=\"Upgrade to Premium\"]",
            "[class*=\"AdsContainer\"]",
            "[aria-label=\"Sponsored\"]",
            "[data-testid=\"track-info-advertiser\"]",
            ".advertisement",
            ".sponsored-playlist",
            "div[data-testid=\"top-banner-ad\"]",
            "div[data-testid=\"in-feed-ad\"]",
            "div[data-testid=\"billboard-ad\"]"
        )

        for (sel in selectors) {
            assertTrue("WebViewAdapter fallback must contain Brave cosmetic selector: $sel", content.contains(sel))
        }
    }

    @Test
    fun testOriginExceptionPreservesSpotifyStreaming() {
        val thirdPartyAdUrl = "https://adeventtracker.spotify.com/event"
        val exceptionDecision = MediaSafeResourcePolicy.classifyResource(
            thirdPartyAdUrl,
            "https://open.spotify.com",
            isOriginException = true
        )
        assertEquals(ResourceClassification.EXCEPTION_ALLOWED, exceptionDecision)
    }
}
