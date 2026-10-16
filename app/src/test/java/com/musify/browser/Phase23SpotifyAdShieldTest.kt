package com.musify.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 23 Unit Tests: Complete Multi-Layered Spotify Ad Shield & Non-Invasive Ad Bypass Engine
 */
class Phase23SpotifyAdShieldTest {

    private val manifestFile = File("src/main/assets/musify_adblocker/manifest.json")
    private val backgroundScriptFile = File("src/main/assets/musify_adblocker/background.js")
    private val contentScriptFile = File("src/main/assets/musify_adblocker/spotify_adblock.js")

    @Test
    fun testWebExtensionManifestPermissionsAndBackgroundRegistration() {
        assertTrue("manifest.json must exist", manifestFile.exists())
        val content = manifestFile.readText()

        assertTrue("Manifest must declare webRequest permission", content.contains("\"webRequest\""))
        assertTrue("Manifest must declare webRequestBlocking permission", content.contains("\"webRequestBlocking\""))
        assertTrue("Manifest must declare all_urls permission for network blocking", content.contains("\"<all_urls>\""))
        assertTrue("Manifest must register background.js", content.contains("\"background.js\""))
        assertTrue("Manifest must preserve gecko ID", content.contains("\"shields@musify.browser\""))
    }

    @Test
    fun testBackgroundScriptAdLogicInterceptionAndAntiFreezeRedirect() {
        assertTrue("background.js must exist", backgroundScriptFile.exists())
        val content = backgroundScriptFile.readText()

        assertTrue("Must intercept ad-logic endpoint", content.contains("spclient.wg.spotify.com/ad-logic"))
        assertTrue("Must intercept desktop-ad-service endpoint", content.contains("spclient.wg.spotify.com/desktop-ad-service"))
        assertTrue("Must intercept commercial-break endpoint", content.contains("spclient.wg.spotify.com/commercial-break"))
        assertTrue("Must intercept ads CDN stream", content.contains("audio-ak-spotify-com.akamaized.net"))
        assertTrue(
            "Must redirect to synthetic empty JSON {} to avoid client freeze loop",
            content.contains("data:application/json,%7B%7D") || content.contains("data:application/json,{}")
        )
    }

    @Test
    fun testBackgroundScriptPreservesWidevineDrmAndMediaStreams() {
        assertTrue("background.js must exist", backgroundScriptFile.exists())
        val content = backgroundScriptFile.readText()

        assertTrue("Must safeguard widevine license", content.contains("widevine-license") || content.contains("widevine"))
        assertTrue("Must safeguard general license endpoint", content.contains("/license"))
        assertTrue("Must safeguard audio-fa.scdn.co streaming CDN", content.contains("audio-fa.scdn.co"))
        assertTrue("Must safeguard streaming manifests", content.contains(".mpd") && content.contains(".m3u8"))
    }

    @Test
    fun testContentScriptMainWorldFetchAndXhrInterception() {
        assertTrue("spotify_adblock.js must exist", contentScriptFile.exists())
        val content = contentScriptFile.readText()

        assertTrue("Must inject main-world hooks script", content.contains("musify-main-world-hooks"))
        assertTrue("Must hook window.fetch", content.contains("window.fetch = async function"))
        assertTrue("Must hook XMLHttpRequest.prototype.send", content.contains("XMLHttpRequest.prototype.send"))
        assertTrue("Must synthesize JSON response for ad queries", content.contains("new Response('{}'") || content.contains("responseText', { value: '{}'"))
    }

    @Test
    fun testContentScriptNonDestructiveAdAdvancementAndMediaIntegrity() {
        assertTrue("spotify_adblock.js must exist", contentScriptFile.exists())
        val content = contentScriptFile.readText()

        assertTrue("Must define commercial break advancer", content.contains("function advanceCommercialBreaks()"))
        assertTrue("Must check title for advertisement", content.contains("title.includes('advertisement')"))
        assertTrue("Must query track-info-advertiser badge", content.contains("[data-testid=\"track-info-advertiser\"]"))
        
        // Ensure Phase 12 & 21 Media Protection Invariants are strictly honored
        assertFalse("Must not force currentTime to 9999", content.contains("currentTime = 9999"))
        assertFalse("Must not set playbackRate to 16.0", content.contains("playbackRate = 16.0"))
        assertFalse("Must not force media to muted", content.contains("media.muted = true"))
        assertFalse("Must not force media volume to 0", content.contains("media.volume = 0"))
        assertTrue("Must define safety flag __musifyMediaTamperingEnabled = false", content.contains("window.__musifyMediaTamperingEnabled = false"))
    }

    @Test
    fun testContentScriptCosmeticSelectors() {
        assertTrue("spotify_adblock.js must exist", contentScriptFile.exists())
        val content = contentScriptFile.readText()

        val selectors = listOf(
            "[data-testid=\"ad-companion-banner\"]",
            "[data-testid=\"ad-iframe\"]",
            "[data-testid=\"interstitial-ad\"]",
            "[aria-label=\"Advertisement\"]",
            "div[data-testid=\"advertisement-row\"]",
            "aside[aria-label=\"Upgrade to Premium\"]",
            "[data-testid=\"track-info-advertiser\"]",
            ".advertisement",
            "div[data-testid=\"top-banner-ad\"]",
            "div[data-testid=\"in-feed-ad\"]",
            "div[data-testid=\"billboard-ad\"]"
        )

        for (selector in selectors) {
            assertTrue("Must contain selector: $selector", content.contains(selector))
        }
    }
}
