package com.musify.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 12 Tests: Sanitizing Media Protection & Disabling Invasive Content-Script Hacks
 */
class Phase12MediaProtectionSanitizationTest {

    private val scriptFile = File("src/main/assets/musify_adblocker/spotify_adblock.js")
    private val webViewAdapterFile = File("src/main/java/com/musify/browser/engine/WebViewAdapter.kt")

    @Test
    fun testSpotifyAdBlockScriptHasNoMediaTamperingKeywords() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val scriptContent = scriptFile.readText()

        // Verify aggressive media manipulation is completely eradicated
        assertFalse("Must not force currentTime to 9999", scriptContent.contains("currentTime = 9999"))
        assertFalse("Must not set playbackRate to 16.0", scriptContent.contains("playbackRate = 16.0"))
        assertFalse("Must not force media to muted", scriptContent.contains("media.muted = true"))
        assertFalse("Must not force media volume to 0", scriptContent.contains("media.volume = 0"))
        assertFalse("Must not artificially skip playback", scriptContent.contains("triggerSkip()"))
        assertFalse("Must not perform arbitrary parent traversal", scriptContent.contains("cur = cur.parentElement"))
    }

    @Test
    fun testSpotifyAdBlockScriptExplicitlyDisablesMediaTampering() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val scriptContent = scriptFile.readText()

        assertTrue(
            "Must define safety flag __musifyMediaTamperingEnabled = false",
            scriptContent.contains("window.__musifyMediaTamperingEnabled = false")
        )
    }

    @Test
    fun testSpotifyAdBlockScriptPreservesAdAndAppNagSelectors() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val scriptContent = scriptFile.readText()

        // Verify ad hiding CSS is intact
        assertTrue(scriptContent.contains("[data-testid=\"ad-companion-banner\"]"))
        assertTrue(scriptContent.contains("[aria-label=\"Advertisement\"]"))
        assertTrue(scriptContent.contains(".Root__ads-container"))

        // Verify app install nag suppression is intact
        assertTrue(scriptContent.contains("[data-testid=\"open-app-button\"]"))
        assertTrue(scriptContent.contains("[data-testid=\"install-app-button\"]"))
        assertTrue(scriptContent.contains("[aria-label=\"Open App\"]"))
        assertTrue(scriptContent.contains("a[href^=\"spotify:\"]"))
        assertTrue(scriptContent.contains("a[href*=\"spotify.link\"]"))
    }

    @Test
    fun testPlaybackControlsExemptFromInterception() {
        assertTrue("spotify_adblock.js must exist", scriptFile.exists())
        val scriptContent = scriptFile.readText()

        assertTrue(
            "Must protect controls/play/pause/skip from click interception",
            scriptContent.contains("testId.includes('control')") || scriptContent.contains("testId.indexOf('control')")
        )
    }

    @Test
    fun testWebViewAdapterFallbackScriptHasNoMediaTampering() {
        assertTrue("WebViewAdapter.kt must exist", webViewAdapterFile.exists())
        val content = webViewAdapterFile.readText()

        assertFalse("WebView fallback must not set playbackRate = 16.0", content.contains("playbackRate = 16.0"))
        assertFalse("WebView fallback must not force muted", content.contains("media.muted = true"))
        assertFalse("WebView fallback must not alter currentTime", content.contains("media.currentTime ="))
        assertFalse("WebView fallback must not auto-skip Spotify tracks", content.contains("control-button-skip-forward"))
    }
}
