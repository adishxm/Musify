package com.musify.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 21 Unit Tests: Brave-Aligned Spotify Ad-Shield & Non-Destructive Protection
 */
class Phase21BraveSpotifyAdBlockTest {

    private val scriptFile = File("src/main/assets/musify_adblocker/spotify_adblock.js")
    private val manifestFile = File("src/main/assets/musify_adblocker/manifest.json")

    @Test
    fun testSpotifyAdBlockScriptExistsAndIsValid() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val content = scriptFile.readText()
        assertTrue("Script must not be blank", content.isNotBlank())
        assertTrue("Must include shield active check", content.contains("window.__musifySpotifyShieldActive"))
    }

    @Test
    fun testBraveCosmeticAdSelectorsPresent() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val content = scriptFile.readText()

        val requiredSelectors = listOf(
            "[data-testid=\"ad-companion-banner\"]",
            "[data-testid=\"ad-iframe\"]",
            "[data-testid=\"interstitial-ad\"]",
            "[aria-label=\"Advertisement\"]",
            ".Root__ads-container",
            "div[data-testid=\"advertisement-row\"]",
            "aside[aria-label=\"Upgrade to Premium\"]",
            "[class*=\"AdsContainer\"]",
            "[aria-label=\"Sponsored\"]",
            "div[data-test=\"upsell-container\"]",
            "#ad-leaderboard",
            "#leaderboard",
            ".main-leaderboard-container",
            ".desktop-interstitial",
            ".upsell-banner",
            "[data-testid=\"track-info-advertiser\"]",
            ".advertisement",
            ".sponsored-playlist",
            "div[data-testid=\"top-banner-ad\"]",
            "div[data-testid=\"in-feed-ad\"]",
            "div[data-testid=\"tracklist-row-ad\"]",
            "div[data-testid=\"billboard-ad\"]",
            "div[data-testid=\"leaderboard-ad\"]",
            "div[data-testid=\"mpu-ad\"]",
            "div[data-testid=\"sponsored-header\"]",
            "div[data-testid=\"sponsored-card\"]"
        )

        for (selector in requiredSelectors) {
            assertTrue("Must contain Brave cosmetic selector: $selector", content.contains(selector))
        }
    }

    @Test
    fun testAppNagAndIntentSuppressionSelectors() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val content = scriptFile.readText()

        val requiredAppNagSelectors = listOf(
            "[data-testid=\"open-app-button\"]",
            "[data-testid=\"install-app-button\"]",
            "[data-testid=\"smart-banner-pitch\"]",
            "[data-testid=\"banner-open-button\"]",
            "[data-testid=\"mobile-app-banner\"]",
            "[data-testid=\"smart-banner\"]",
            "[aria-label=\"Open App\"]",
            "[aria-label=\"Open in app\"]",
            "[aria-label=\"Open in Spotify\"]",
            "[aria-label=\"Install App\"]",
            "a[href^=\"spotify:\"]",
            "a[href*=\"spotify.link\"]",
            "a[href*=\"spotify.com/download\"]"
        )

        for (selector in requiredAppNagSelectors) {
            assertTrue("Must contain app-nag suppression selector: $selector", content.contains(selector))
        }
    }

    @Test
    fun testMediaSafetyPreservationNoTampering() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val content = scriptFile.readText()

        // Strict media safety compliance (Phase 12 compatibility)
        assertFalse("Must not force currentTime to 9999", content.contains("currentTime = 9999"))
        assertFalse("Must not set playbackRate to 16.0", content.contains("playbackRate = 16.0"))
        assertFalse("Must not force media to muted", content.contains("media.muted = true"))
        assertFalse("Must not force media volume to 0", content.contains("media.volume = 0"))
        assertFalse("Must not artificially skip playback", content.contains("triggerSkip()"))
        assertFalse("Must not perform arbitrary parent traversal", content.contains("cur = cur.parentElement"))
        assertTrue("Must define safety flag __musifyMediaTamperingEnabled = false", content.contains("window.__musifyMediaTamperingEnabled = false"))
    }

    @Test
    fun testPlaybackControlsExemptFromClickInterception() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val content = scriptFile.readText()

        assertTrue(
            "Must protect controls/play/pause/skip from click interception",
            content.contains("testId.includes('control')") || content.contains("testId.indexOf('control')")
        )
    }

    @Test
    fun testManifestJsonConfiguredForDocumentStart() {
        assertTrue("manifest.json must exist", manifestFile.exists())
        val content = manifestFile.readText()

        assertTrue("Manifest must configure run_at as document_start", content.contains("\"run_at\": \"document_start\""))
        assertTrue("Manifest must match spotify.com", content.contains("\"*://*.spotify.com/*\""))
        assertTrue("Manifest must reference spotify_adblock.js", content.contains("\"spotify_adblock.js\""))
    }
}
