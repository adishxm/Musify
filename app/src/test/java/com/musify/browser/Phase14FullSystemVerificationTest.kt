package com.musify.browser

import com.musify.browser.engine.BuildVariantInfo
import com.musify.browser.engine.CapabilityStatus
import com.musify.browser.engine.ContentMode
import com.musify.browser.engine.EngineType
import com.musify.browser.engine.MediaSafeResourcePolicy
import com.musify.browser.engine.MusifyVariant
import com.musify.browser.engine.ResourceClassification
import com.musify.browser.engine.WebViewAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 14 Tests: Real-Device Spotify Verification & Diagnostic Test Hardening
 *
 * Full system integration and regression verification across Phases 11–14.
 */
class Phase14FullSystemVerificationTest {

    private val spotifyAdBlockFile = File("src/main/assets/musify_adblocker/spotify_adblock.js")
    private val webViewAdapterFile = File("src/main/java/com/musify/browser/engine/WebViewAdapter.kt")

    @Test
    fun testAllVariantConfigurationsValid() {
        for (variant in MusifyVariant.values()) {
            assertTrue("Variant name must not be blank", variant.displayName.isNotBlank())
            assertTrue("Variant description must not be blank", variant.description.isNotBlank())
        }

        val liteInfo = BuildVariantInfo.detect(EngineType.WEB_VIEW)
        assertEquals(MusifyVariant.LITE, liteInfo.variant)
        assertFalse(liteInfo.isWidevineDrmAvailable)
        assertNotNull(liteInfo.protectedPlaybackLimitationNotice)

        val geckoInfo = BuildVariantInfo.detect(EngineType.GECKO_VIEW)
        assertTrue(geckoInfo.variant == MusifyVariant.ARM64 || geckoInfo.variant == MusifyVariant.UNIVERSAL)
        assertTrue(geckoInfo.isWidevineDrmAvailable)
    }

    @Test
    fun testSanitizedContentBlockerGuaranteesNoMediaTampering() {
        assertTrue("spotify_adblock.js must exist", spotifyAdBlockFile.exists())
        val script = spotifyAdBlockFile.readText()

        assertFalse("No currentTime = 9999", script.contains("currentTime = 9999"))
        assertFalse("No playbackRate = 16.0", script.contains("playbackRate = 16.0"))
        assertFalse("No forced muting", script.contains("media.muted = true"))
        assertFalse("No volume zeroing", script.contains("media.volume = 0"))
        assertFalse("No destructive parent traversal", script.contains("cur = cur.parentElement"))
        assertTrue("Media tampering explicitly disabled", script.contains("window.__musifyMediaTamperingEnabled = false"))

        assertTrue("WebViewAdapter.kt must exist", webViewAdapterFile.exists())
        val webViewSrc = webViewAdapterFile.readText()
        assertFalse("WebView fallback has no playbackRate alteration", webViewSrc.contains("playbackRate = 16.0"))
        assertFalse("WebView fallback has no forced muting", webViewSrc.contains("media.muted = true"))
    }

    @Test
    fun testMediaSafeResourcePolicyComprehensiveEvaluation() {
        val testBattery = listOf(
            Triple("https://spclient.wg.spotify.com/widevine-license/v1/license", "https://open.spotify.com", ResourceClassification.MEDIA_OR_LICENSE),
            Triple("https://audio-fa.scdn.co/audio/track_id_123", "https://open.spotify.com", ResourceClassification.MEDIA_OR_LICENSE),
            Triple("https://video-fa.scdn.co/manifest.mpd", "https://open.spotify.com", ResourceClassification.MEDIA_OR_LICENSE),
            Triple("https://accounts.spotify.com/login", "https://open.spotify.com", ResourceClassification.FIRST_PARTY_OR_AUTH),
            Triple("https://api.spotify.com/v1/me", "https://open.spotify.com", ResourceClassification.FIRST_PARTY_OR_AUTH),
            Triple("https://googleads.g.doubleclick.net/pagead/ads", "https://open.spotify.com", ResourceClassification.TRACKER_CANDIDATE)
        )

        for ((url, origin, expected) in testBattery) {
            val result = MediaSafeResourcePolicy.classifyResource(url, origin, isOriginException = false)
            assertEquals("Classification mismatch for $url", expected, result)
        }
    }

    @Test
    fun testEnginePolicyIsolationAcrossMultipleTabs() {
        val tab1 = WebViewAdapter(isPrivate = false)
        val tab2 = WebViewAdapter(isPrivate = true)
        val tab3 = WebViewAdapter(isPrivate = false)

        // Configure tab 1 for desktop mode with origin exception
        tab1.setContentMode(ContentMode.DESKTOP)
        tab1.setOriginException(true)

        // Configure tab 2 for mobile mode without origin exception
        tab2.setContentMode(ContentMode.MOBILE)
        tab2.setOriginException(false)

        // Configure tab 3 default
        assertEquals(ContentMode.DESKTOP, tab1.contentMode.value)
        assertEquals(ContentMode.MOBILE, tab2.contentMode.value)
        assertEquals(ContentMode.MOBILE, tab3.contentMode.value)

        assertTrue(tab1.getMediaCapabilityState().isOriginExceptionActive)
        assertFalse(tab2.getMediaCapabilityState().isOriginExceptionActive)
        assertFalse(tab3.getMediaCapabilityState().isOriginExceptionActive)

        assertEquals("Permissive Session (Origin Exception)", tab1.getMediaCapabilityState().cookiePolicy)
        assertEquals("Accept Non-Trackers & Isolate", tab2.getMediaCapabilityState().cookiePolicy)
        assertEquals("Accept Non-Trackers & Isolate", tab3.getMediaCapabilityState().cookiePolicy)
    }

    @Test
    fun testDiagnosticsUrlSanitizationPreservesPathWhileStrippingTokens() {
        val rawUrl = "https://spclient.wg.spotify.com/widevine-license/v1/license?access_token=SENSITIVE_BEARER_TOKEN&deviceId=ABC#frag"
        val clean = MediaSafeResourcePolicy.sanitizeUrlForDiagnostics(rawUrl)

        assertEquals("https://spclient.wg.spotify.com/widevine-license/v1/license", clean)
        assertFalse(clean.contains("SENSITIVE_BEARER_TOKEN"))
        assertFalse(clean.contains("deviceId"))
        assertFalse(clean.contains("#frag"))
    }
}
