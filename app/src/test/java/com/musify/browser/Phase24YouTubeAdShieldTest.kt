package com.musify.browser

import com.musify.browser.engine.MediaSafeResourcePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 24 Unit Tests: Brave-Aligned YouTube & YouTube Music Ad Shield & Background Playback Engine
 */
class Phase24YouTubeAdShieldTest {

    private val manifestFile = File("src/main/assets/musify_adblocker/manifest.json")
    private val backgroundScriptFile = File("src/main/assets/musify_adblocker/background.js")
    private val ytContentScriptFile = File("src/main/assets/musify_adblocker/youtube_adblock.js")

    @Test
    fun testWebExtensionManifestRegistersYouTubeContentScript() {
        assertTrue("manifest.json must exist", manifestFile.exists())
        val content = manifestFile.readText()

        assertTrue("Manifest must match youtube.com", content.contains("*://*.youtube.com/*"))
        assertTrue("Manifest must match youtube-nocookie.com", content.contains("*://*.youtube-nocookie.com/*"))
        assertTrue("Manifest must register youtube_adblock.js", content.contains("youtube_adblock.js"))
        assertTrue("Manifest must execute at document_start", content.contains("\"run_at\": \"document_start\""))
    }

    @Test
    fun testYouTubeContentScriptExistsAndHasCoreHooks() {
        assertTrue("youtube_adblock.js must exist", ytContentScriptFile.exists())
        val content = ytContentScriptFile.readText()

        assertTrue("Must inject main-world hooks script", content.contains("musify-yt-main-world-hooks"))
        assertTrue("Must trap ytInitialPlayerResponse", content.contains("ytInitialPlayerResponse"))
        assertTrue("Must strip playerAds", content.contains("delete obj.playerAds") || content.contains("playerAds"))
        assertTrue("Must strip adPlacements", content.contains("delete obj.adPlacements") || content.contains("adPlacements"))
        assertTrue("Must strip adSlots", content.contains("delete obj.adSlots") || content.contains("adSlots"))
        assertTrue("Must hook JSON.parse for player responses", content.contains("JSON.parse = function"))
        assertTrue("Must hook window.fetch for player queries", content.contains("window.fetch = function") || content.contains("window.fetch = async function"))
        assertTrue("Must hook XMLHttpRequest for player queries", content.contains("XMLHttpRequest.prototype.open"))
    }

    @Test
    fun testYouTubeBackgroundPlaybackPreservationAndActivityKeepAlive() {
        assertTrue("youtube_adblock.js must exist", ytContentScriptFile.exists())
        val content = ytContentScriptFile.readText()

        assertTrue("Must spoof document.hidden to false", content.contains("'hidden': { value: false"))
        assertTrue("Must spoof visibilityState to visible", content.contains("'visibilityState': { value: 'visible'"))
        assertTrue("Must trap visibilitychange events", content.contains("visibilitychange"))
        assertTrue("Must refresh window._lact user activity timestamp", content.contains("window._lact = Date.now()"))
    }

    @Test
    fun testYouTubeCosmeticSelectorsHideAdsAndDialogs() {
        assertTrue("youtube_adblock.js must exist", ytContentScriptFile.exists())
        val content = ytContentScriptFile.readText()

        val selectors = listOf(
            ".video-ads",
            ".ytp-ad-module",
            ".ytp-ad-overlay-container",
            "ytd-ad-slot-renderer",
            "ytd-in-feed-ad-layout-renderer",
            "ytd-banner-promo-renderer",
            "tp-yt-paper-dialog.ytmusic-popup-container"
        )

        for (selector in selectors) {
            assertTrue("youtube_adblock.js must contain selector: $selector", content.contains(selector))
        }
    }

    @Test
    fun testBackgroundScriptBlocksYouTubeAdEndpointsAndPreservesVideoPlayback() {
        assertTrue("background.js must exist", backgroundScriptFile.exists())
        val content = backgroundScriptFile.readText()

        assertTrue("Must block youtube.com/api/stats/ads", content.contains("youtube.com/api/stats/ads"))
        assertTrue("Must block youtube.com/pagead", content.contains("youtube.com/pagead"))
        assertTrue("Must block youtube.com/ptracking", content.contains("youtube.com/ptracking"))
        assertTrue("Must block doubleclick ad server", content.contains("googleads.g.doubleclick.net"))
        assertTrue("Must safeguard videoplayback media streams", content.contains("videoplayback"))
    }

    @Test
    fun testMediaSafeResourcePolicyRecognizesYouTubePlayback() {
        assertTrue(
            "videoplayback stream must be classified as protected media",
            MediaSafeResourcePolicy.isMediaOrLicenseRequest("https://rr3---sn-4g5ednks.googlevideo.com/videoplayback?expire=1730000000")
        )
        assertTrue(
            "initplayback stream must be classified as protected media",
            MediaSafeResourcePolicy.isMediaOrLicenseRequest("https://rr3---sn-4g5ednks.googlevideo.com/initplayback?source=youtube")
        )
    }
}
