package com.musify.browser

import com.musify.browser.engine.BuildVariantInfo
import com.musify.browser.engine.EngineType
import com.musify.browser.engine.MusifyVariant
import com.musify.browser.shield.LocalShieldExceptionRepository
import com.musify.browser.shield.ShieldCategories
import com.musify.browser.shield.UnifiedShieldAction
import com.musify.browser.shield.UnifiedShieldController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Phase 27 Unit Tests: Universal Privacy Shields Harmonization & Multi-Variant Release Assembly
 */
class Phase27UniversalShieldsAndPackagingTest {

    private lateinit var exceptionRepo: LocalShieldExceptionRepository
    private lateinit var shieldController: UnifiedShieldController

    @Before
    fun setUp() {
        exceptionRepo = LocalShieldExceptionRepository()
        exceptionRepo.clearAllExceptions()
        shieldController = UnifiedShieldController(exceptionRepo)
    }

    @Test
    fun testUnifiedShieldControllerAllowsMediaAndWidevineDrm() {
        val spotifyDrmUrl = "https://spclient.wg.spotify.com/widevine-license/v1/video/license"
        val drmAction = shieldController.evaluateResource(spotifyDrmUrl, "https://open.spotify.com")
        assertTrue(drmAction is UnifiedShieldAction.Allow)

        val ytStreamUrl = "https://rr1---sn-4g5ednkk.googlevideo.com/videoplayback?expire=12345"
        val streamAction = shieldController.evaluateResource(ytStreamUrl, "https://www.youtube.com")
        assertTrue(streamAction is UnifiedShieldAction.Allow)

        val sabrStreamUrl = "https://rr1---sn-4g5ednkk.googlevideo.com/videoplayback?sabr=1&expire=12345"
        val sabrAction = shieldController.evaluateResource(sabrStreamUrl, "https://music.youtube.com")
        assertTrue(sabrAction is UnifiedShieldAction.Allow)

        val metrics = shieldController.getMetrics()
        assertEquals(3, metrics.protectedMediaAllowed)
        assertEquals(1, metrics.sabrStreamsProtected)
    }

    @Test
    fun testUnifiedShieldControllerMocksAdEndpointsWithEmptyJson() {
        val spotifyAdUrl = "https://spclient.wg.spotify.com/ad-logic/v1/bootstrap"
        val spotifyAction = shieldController.evaluateResource(spotifyAdUrl, "https://open.spotify.com")
        assertTrue(spotifyAction is UnifiedShieldAction.MockResponse)
        val mock1 = spotifyAction as UnifiedShieldAction.MockResponse
        assertEquals("application/json", mock1.mimeType)
        assertEquals("{}", mock1.body)

        val ytAdBreakUrl = "https://www.youtube.com/youtubei/v1/player/ad_break?key=xyz"
        val ytAction = shieldController.evaluateResource(ytAdBreakUrl, "https://www.youtube.com")
        assertTrue(ytAction is UnifiedShieldAction.MockResponse)
        val mock2 = ytAction as UnifiedShieldAction.MockResponse
        assertEquals("{}", mock2.body)

        val metrics = shieldController.getMetrics()
        assertEquals(2, metrics.trackersBlocked)
    }

    @Test
    fun testUnifiedShieldControllerBlocksKnownAdTrackers() {
        val doubleclickUrl = "https://googleads.g.doubleclick.net/pagead/id?fmt=json"
        val action = shieldController.evaluateResource(doubleclickUrl, "https://example.com")
        assertTrue(action is UnifiedShieldAction.Block)
        val block = action as UnifiedShieldAction.Block
        assertEquals(ShieldCategories.ADS_AND_TRACKERS, block.category)
    }

    @Test
    fun testUnifiedShieldControllerPerOriginExemptionReversibility() {
        val origin = "https://example.com"
        val trackerUrl = "https://googleads.g.doubleclick.net/pagead/ads"

        // Default state: blocked
        val defaultAction = shieldController.evaluateResource(trackerUrl, origin)
        assertTrue(defaultAction is UnifiedShieldAction.Block)

        // Disable shields for origin
        exceptionRepo.setShieldDisabled(origin, true)
        val exemptedAction = shieldController.evaluateResource(trackerUrl, origin)
        assertTrue(exemptedAction is UnifiedShieldAction.Allow)

        // Re-enable shields
        exceptionRepo.setShieldDisabled(origin, false)
        val restoredAction = shieldController.evaluateResource(trackerUrl, origin)
        assertTrue(restoredAction is UnifiedShieldAction.Block)
    }

    @Test
    fun testUnifiedShieldNavigationDebounceAndSanitize() {
        val rawRedirect = "https://dpbolvw.net/click-54321?url=https%3A%2F%2Fmusify.app%2Fmusic%3Ffbclid%3Dtrack_token%26msclkid%3Dbing_token"
        val navAction = shieldController.sanitizeNavigation(rawRedirect)

        assertEquals("https://musify.app/music", navAction.sanitizedUrl)
        assertTrue(navAction.wasDebounced)

        val metrics = shieldController.getMetrics()
        assertEquals(1, metrics.bounceHopsDefused)
        assertEquals(1, metrics.trackingParamsCleaned)
    }

    @Test
    fun testUnifiedShieldMetricsTrackingAndReset() {
        shieldController.resetMetrics()
        var metrics = shieldController.getMetrics()
        assertEquals(0, metrics.requestsEvaluated)
        assertEquals(0, metrics.trackersBlocked)

        shieldController.evaluateResource("https://googleads.g.doubleclick.net/ad", "https://news.com")
        shieldController.evaluateResource("https://spclient.wg.spotify.com/widevine-license", "https://open.spotify.com")

        metrics = shieldController.getMetrics()
        assertEquals(2, metrics.requestsEvaluated)
        assertEquals(1, metrics.trackersBlocked)
        assertEquals(1, metrics.protectedMediaAllowed)

        shieldController.resetMetrics()
        assertEquals(0, shieldController.getMetrics().requestsEvaluated)
    }

    @Test
    fun testReleasePackagingBuildConfigurationParity() {
        // Universal / ARM64 GeckoView engine configuration
        val geckoInfo = BuildVariantInfo.detect(EngineType.GECKO_VIEW)
        assertEquals(EngineType.GECKO_VIEW, geckoInfo.engineType)
        assertTrue(geckoInfo.isWidevineDrmAvailable)

        // Lite WebView engine configuration
        val liteInfo = BuildVariantInfo.detect(EngineType.WEB_VIEW)
        assertEquals(EngineType.WEB_VIEW, liteInfo.engineType)
        assertEquals(MusifyVariant.LITE, liteInfo.variant)
        assertFalse(liteInfo.isWidevineDrmAvailable)
    }
}

