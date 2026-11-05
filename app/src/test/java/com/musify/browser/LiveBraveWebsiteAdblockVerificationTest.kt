package com.musify.browser

import com.musify.browser.engine.SabrProtobufDecoder
import com.musify.browser.filterlist.FilterDecision
import com.musify.browser.filterlist.FilterEngine
import com.musify.browser.filterlist.FilterListRegistry
import com.musify.browser.filterlist.FilterListStorage
import com.musify.browser.filterlist.FilterRuleCompiler
import com.musify.browser.shield.LocalShieldExceptionRepository
import com.musify.browser.shield.UnifiedShieldAction
import com.musify.browser.shield.UnifiedShieldController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

/**
 * End-to-end verification test suite for all websites protected by Brave-aligned shields in Musify:
 * - YouTube / YouTube Music (SABR Protobuf Backoff & Ad Break Mocking)
 * - Spotify Web Player (Widevine DRM License Safe & Ad-Logic Empty JSON Mocking)
 * - SoundCloud (Audio Delivery Safe & Tracker Blocking)
 * - Industry AdBlock Benchmarks (d3ward / CanYouBlockIt ad delivery networks)
 * - Affiliate Bounce Tracking & Query Tracking Token Sanitization
 */
class LiveBraveWebsiteAdblockVerificationTest {

    private lateinit var shieldController: UnifiedShieldController
    private lateinit var exceptionRepo: LocalShieldExceptionRepository

    @Before
    fun setUp() = runBlocking {
        exceptionRepo = LocalShieldExceptionRepository()
        exceptionRepo.clearAllExceptions()

        // Create in-memory mock filter engine with standard rules
        val rulesContent = """
            ! Title: Brave Test List
            ||pagead2.googlesyndication.com^
            ||c.amazon-adsystem.com^
            ||static.criteo.net^
            ||cdn.taboola.com^
            ||outbrain.com^
            ||api-v2.soundcloud.com/analytics/tracking/*
        """.trimIndent()

        val inputStream = ByteArrayInputStream(rulesContent.toByteArray(Charsets.UTF_8))
        val compiledList = FilterRuleCompiler.compile(inputStream, FilterListRegistry.PETER_LOWE_SEED)

        val tempDir = File(System.getProperty("java.io.tmpdir"), "musify_test_${System.nanoTime()}")
        val storage = FilterListStorage(tempDir)
        val filterEngine = FilterEngine(storage)
        filterEngine.registerList(compiledList)

        shieldController = UnifiedShieldController(
            exceptionRepository = exceptionRepo,
            filterEngine = filterEngine
        )
    }

    @Test
    fun testYouTubeAdblockingAndSabrDefense() {
        val ytOrigin = "https://www.youtube.com"

        // 1. YouTube player ad stats & ad break requests intercepted with empty JSON mock
        val adBreakUrl = "https://www.youtube.com/youtubei/v1/player/ad_break?key=AIzaSyA..."
        val adBreakAction = shieldController.evaluateResource(adBreakUrl, ytOrigin)
        assertTrue("Ad break must return mock response", adBreakAction is UnifiedShieldAction.MockResponse)
        assertEquals("{}", (adBreakAction as UnifiedShieldAction.MockResponse).body)

        val adStatsUrl = "https://www.youtube.com/api/stats/ads?v=dQw4w9WgXcQ"
        val adStatsAction = shieldController.evaluateResource(adStatsUrl, ytOrigin)
        assertTrue("Ad stats must return mock response", adStatsAction is UnifiedShieldAction.MockResponse)

        // 2. DoubleClick ad network blocked
        val doubleclickUrl = "https://googleads.g.doubleclick.net/pagead/id?fmt=json"
        val dcAction = shieldController.evaluateResource(doubleclickUrl, ytOrigin)
        assertTrue("DoubleClick must be blocked", dcAction is UnifiedShieldAction.Block)

        // 3. Media streams and SABR requests preserved
        val mediaUrl = "https://rr1---sn-4g5ednkk.googlevideo.com/videoplayback?expire=1700000000&itag=251"
        val mediaAction = shieldController.evaluateResource(mediaUrl, ytOrigin)
        assertTrue("Media stream must be allowed", mediaAction is UnifiedShieldAction.Allow)

        val sabrUrl = "https://rr1---sn-4g5ednkk.googlevideo.com/videoplayback?sabr=1&expire=1700000000"
        val sabrAction = shieldController.evaluateResource(sabrUrl, ytOrigin)
        assertTrue("SABR media stream must be allowed", sabrAction is UnifiedShieldAction.Allow)

        // 4. SABR Protobuf Field 4 backoff rewriting
        val artificialBackoffPayload = byteArrayOf(
            0x08, 0x01,                   // Field 1: varint 1
            0x20, 0xE8.toByte(), 0x3E     // Field 4 (tag 0x20): varint 8000 (8000ms backoff penalty)
        )
        val wasPatched = SabrProtobufDecoder.patchBackoffField(artificialBackoffPayload)
        assertTrue("Backoff field must be patched", wasPatched)
        val (decodedBackoff, _) = SabrProtobufDecoder.decodeVarint(artificialBackoffPayload, 3)
        assertTrue("Patched backoff must be randomized between 50 and 150ms", decodedBackoff in 50L..150L)
    }

    @Test
    fun testYouTubeMusicShields() {
        val ytmOrigin = "https://music.youtube.com"

        // Audio stream delivery preserved
        val audioChunkUrl = "https://rr5---sn-4g5ednkk.googlevideo.com/videoplayback?sabr=1&itag=140&source=youtube"
        val action = shieldController.evaluateResource(audioChunkUrl, ytmOrigin)
        assertTrue("YTM audio chunk must be allowed", action is UnifiedShieldAction.Allow)

        // Ad tracking blocked
        val trackerUrl = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"
        val trackerAction = shieldController.evaluateResource(trackerUrl, ytmOrigin)
        assertTrue("PageAd must be blocked", trackerAction is UnifiedShieldAction.Block)
    }

    @Test
    fun testSpotifyWebPlayerProtection() {
        val spotifyOrigin = "https://open.spotify.com"

        // 1. Widevine DRM CDM license must NEVER be blocked or altered
        val licenseUrl = "https://spclient.wg.spotify.com/widevine-license/v1/video/license"
        val licenseAction = shieldController.evaluateResource(licenseUrl, spotifyOrigin)
        assertTrue("Widevine DRM license must be allowed", licenseAction is UnifiedShieldAction.Allow)

        // 2. Spotify ad-logic endpoint returns anti-freeze empty JSON
        val adLogicUrl = "https://spclient.wg.spotify.com/ad-logic/v1/bootstrap"
        val adLogicAction = shieldController.evaluateResource(adLogicUrl, spotifyOrigin)
        assertTrue("Ad logic must return empty JSON mock", adLogicAction is UnifiedShieldAction.MockResponse)
        assertEquals("{}", (adLogicAction as UnifiedShieldAction.MockResponse).body)

        // 3. Audio ad CDN file blocked
        val audioAdUrl = "https://audio-ak-spotify-com.akamaized.net/audio/commercial_ad.mp3"
        val audioAdAction = shieldController.evaluateResource(audioAdUrl, spotifyOrigin)
        assertTrue("Audio ad CDN must be blocked", audioAdAction is UnifiedShieldAction.Block)

        // 4. Essential first-party API routes allowed
        val apiMetadataUrl = "https://api.spotify.com/v1/me/player"
        val apiAction = shieldController.evaluateResource(apiMetadataUrl, spotifyOrigin)
        assertTrue("Spotify user metadata API must be allowed", apiAction is UnifiedShieldAction.Allow)
    }

    @Test
    fun testAdblockBenchmarkDomains() {
        val testOrigin = "https://d3ward.github.io"

        // Google AdSense
        val adsenseUrl = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"
        assertTrue(shieldController.evaluateResource(adsenseUrl, testOrigin) is UnifiedShieldAction.Block)

        // Amazon AdSystem
        val amazonAdUrl = "https://c.amazon-adsystem.com/aax2/apstag.js"
        assertTrue(shieldController.evaluateResource(amazonAdUrl, testOrigin) is UnifiedShieldAction.Block)

        // Criteo
        val criteoUrl = "https://static.criteo.net/js/ld/ld.js"
        assertTrue(shieldController.evaluateResource(criteoUrl, testOrigin) is UnifiedShieldAction.Block)

        // Taboola
        val taboolaUrl = "https://cdn.taboola.com/libtrc/uncompressed_ns.js"
        assertTrue(shieldController.evaluateResource(taboolaUrl, testOrigin) is UnifiedShieldAction.Block)

        // Outbrain
        val outbrainUrl = "https://outbrain.com/widget/detect"
        assertTrue(shieldController.evaluateResource(outbrainUrl, testOrigin) is UnifiedShieldAction.Block)
    }

    @Test
    fun testBounceTrackingAndQuerySanitization() {
        // Intermediary tracking hop bypassed straight to clean destination
        val cjHop = "https://dpbolvw.net/click-1002?url=https%3A%2F%2Fmusify.app%2Ffeatured%3Ffbclid%3Dtoken123%26gclid%3Dgoogle456"
        val navAction = shieldController.sanitizeNavigation(cjHop)
        assertEquals("https://musify.app/featured", navAction.sanitizedUrl)
        assertTrue("Hop must be debounced", navAction.wasDebounced)

        // Howl tracking hop bypassed
        val howlHop = "https://howl.me/link/?btn_url=https%3A%2F%2Fstore.example.com%2Fitem%3Fmsclkid%3Dbing789"
        val howlAction = shieldController.sanitizeNavigation(howlHop)
        assertEquals("https://store.example.com/item", howlAction.sanitizedUrl)
        assertTrue("Howl hop must be debounced", howlAction.wasDebounced)

        // Standard link with multiple tracking parameters stripped
        val directTrackingUrl = "https://news.ycombinator.com/item?id=12345&fbclid=fb_click&_gl=analytics_token"
        val directAction = shieldController.sanitizeNavigation(directTrackingUrl)
        assertEquals("https://news.ycombinator.com/item?id=12345", directAction.sanitizedUrl)
        assertFalse("Direct link was not a debounce hop", directAction.wasDebounced)
    }
}

