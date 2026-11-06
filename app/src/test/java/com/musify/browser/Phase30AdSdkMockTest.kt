package com.musify.browser

import com.musify.browser.shield.AdSdkMockRepository
import com.musify.browser.shield.LocalShieldExceptionRepository
import com.musify.browser.shield.MockScriptType
import com.musify.browser.shield.UnifiedShieldAction
import com.musify.browser.shield.UnifiedShieldController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Phase 30 Unit Tests: Validates Brave-aligned Ad SDK mocks (Google Publisher Tag, Google AdSense, Amazon A9)
 * and Anti-Adblock defusers (FuckAdBlock, BlockAdBlock v2, Prebid bait, Google Analytics noop).
 */
class Phase30AdSdkMockTest {

    private lateinit var controller: UnifiedShieldController
    private lateinit var exceptionRepo: LocalShieldExceptionRepository

    @Before
    fun setUp() {
        exceptionRepo = LocalShieldExceptionRepository()
        controller = UnifiedShieldController(exceptionRepository = exceptionRepo)
    }

    @Test
    fun testGptScriptStubStructure() {
        val script = AdSdkMockRepository.GPT_SCRIPT
        assertTrue("GPT stub must define googletag", script.contains("window.googletag"))
        assertTrue("GPT stub must define cmd.push callback runner", script.contains("gpt.cmd.push = function(fn)"))
        assertTrue("GPT stub must set pubadsReady flag", script.contains("gpt.pubadsReady = true"))
        assertTrue("GPT stub must provide defineSlot stub", script.contains("gpt.defineSlot = function()"))
        assertTrue("GPT stub must provide enableServices stub", script.contains("gpt.enableServices = noopfn"))
        assertTrue("GPT stub must provide display stub", script.contains("gpt.display = noopfn"))
        assertTrue("GPT stub must drain queued commands", script.contains("gpt.cmd.push(cmd.shift())"))
    }

    @Test
    fun testAdSenseScriptStubStructure() {
        val script = AdSdkMockRepository.ADSBYGOOGLE_SCRIPT
        assertTrue("AdSense stub must define adsbygoogle", script.contains("self.adsbygoogle"))
        assertTrue("AdSense stub must mark loaded true", script.contains("loaded: true"))
        assertTrue("AdSense stub must query unhandled placeholders", script.contains(".adsbygoogle:not"))
        assertTrue("AdSense stub must set filled ad-status to bypass anti-adblock probe", script.contains("dataset.adStatus = 'filled'"))
        assertTrue("AdSense stub must mark adsbygoogleStatus done", script.contains("dataset.adsbygoogleStatus = 'done'"))
    }

    @Test
    fun testAmazonApstagAndAdsScriptStructure() {
        val apstag = AdSdkMockRepository.AMAZON_APSTAG_SCRIPT
        assertTrue("Amazon APSTag stub must define apstag", apstag.contains("w.apstag = apstag"))
        assertTrue("Amazon APSTag must return empty bids without error", apstag.contains("callback([])"))
        assertTrue("Amazon APSTag must handle queue draining", apstag.contains("_Q.push"))

        val amznAds = AdSdkMockRepository.AMAZON_ADS_SCRIPT
        assertTrue("Amazon Ads stub must define amznads", amznAds.contains("window.amznads"))
        assertTrue("Amazon Ads must provide getAds stub", amznAds.contains("getAds: noopfn"))
    }

    @Test
    fun testFuckAdBlockDefuserStructure() {
        val script = AdSdkMockRepository.FUCKADBLOCK_SCRIPT
        assertTrue("FuckAdBlock stub must define FuckAdBlock constructor", script.contains("window.FuckAdBlock = Fab"))
        assertTrue("FuckAdBlock stub must define BlockAdBlock constructor", script.contains("window.BlockAdBlock = Fab"))
        assertTrue("FuckAdBlock stub must define global instance fuckAdBlock", script.contains("window.fuckAdBlock = fab"))
        assertTrue("FuckAdBlock stub must immediately execute onNotDetected callback", script.contains("Fab.prototype.onNotDetected = function(callback)"))
        assertTrue("FuckAdBlock stub must ignore onDetected callback", script.contains("Fab.prototype.onDetected = function()"))
    }

    @Test
    fun testBlockAdBlock2AndPrebidDefuserStructure() {
        val bab = AdSdkMockRepository.BLOCKADBLOCK2_SCRIPT
        assertTrue("BlockAdBlock v2 stub must set honeypot global variable", bab.contains("window.nH7eXzOsG = 858;"))

        val prebid = AdSdkMockRepository.PREBID_ADS_SCRIPT
        assertTrue("Prebid bait stub must assert canRunAds true", prebid.contains("window.canRunAds = true;"))
        assertTrue("Prebid bait stub must assert isAdBlockActive false", prebid.contains("window.isAdBlockActive = false;"))
    }

    @Test
    fun testGoogleAnalyticsDefuserStructure() {
        val ga = AdSdkMockRepository.GOOGLE_ANALYTICS_SCRIPT
        assertTrue("GA stub must define ga and loaded flag", ga.contains("ga.loaded = true"))
        assertTrue("GA stub must immediately fire hitCallback", ga.contains("fn = a.hitCallback"))
        assertTrue("GA stub must drain GoogleAnalyticsObject queue", ga.contains("ga.apply(null, q[i])"))
        assertTrue("GA stub must drain dataLayer eventCallback", ga.contains("item.eventCallback"))
    }

    @Test
    fun testMockScriptUrlMatching() {
        // 1. Google Publisher Tag
        val gptMock = AdSdkMockRepository.matchMockScript("https://www.googletagservices.com/tag/js/gpt.js")
        assertNotNull(gptMock)
        assertEquals(MockScriptType.GOOGLE_PUBLISHER_TAG, gptMock!!.type)
        assertFalse(gptMock.isAntiAdblockDefuser)

        val gptMock2 = AdSdkMockRepository.matchMockScript("https://securepubads.g.doubleclick.net/tag/js/gpt.js?v=2026")
        assertNotNull(gptMock2)
        assertEquals(MockScriptType.GOOGLE_PUBLISHER_TAG, gptMock2!!.type)

        // 2. Google AdSense
        val adsenseMock = AdSdkMockRepository.matchMockScript("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-1234")
        assertNotNull(adsenseMock)
        assertEquals(MockScriptType.GOOGLE_ADSENSE, adsenseMock!!.type)
        assertFalse(adsenseMock.isAntiAdblockDefuser)

        // 3. Amazon
        val apstagMock = AdSdkMockRepository.matchMockScript("https://c.amazon-adsystem.com/aax2/apstag.js")
        assertNotNull(apstagMock)
        assertEquals(MockScriptType.AMAZON_APSTAG, apstagMock!!.type)

        val amznMock = AdSdkMockRepository.matchMockScript("https://amazon-adsystem.com/aax2/amzn_ads.js")
        assertNotNull(amznMock)
        assertEquals(MockScriptType.AMAZON_ADS, amznMock!!.type)

        // 4. FuckAdBlock & BlockAdBlock
        val fabMock = AdSdkMockRepository.matchMockScript("https://example.com/assets/fuckadblock.js")
        assertNotNull(fabMock)
        assertEquals(MockScriptType.FUCK_ADBLOCK, fabMock!!.type)
        assertTrue(fabMock.isAntiAdblockDefuser)

        val nobabMock = AdSdkMockRepository.matchMockScript("https://example.com/js/nobab2.js")
        assertNotNull(nobabMock)
        assertEquals(MockScriptType.BLOCK_ADBLOCK, nobabMock!!.type)
        assertTrue(nobabMock.isAntiAdblockDefuser)

        val adclixxMock = AdSdkMockRepository.matchMockScript("https://adclixx.net/banner.js")
        assertNotNull(adclixxMock)
        assertEquals(MockScriptType.BLOCK_ADBLOCK, adclixxMock!!.type)

        // 5. Prebid / Bait
        val prebidMock = AdSdkMockRepository.matchMockScript("https://example.com/scripts/prebid-ads.js")
        assertNotNull(prebidMock)
        assertEquals(MockScriptType.PREBID_ADS, prebidMock!!.type)
        assertTrue(prebidMock.isAntiAdblockDefuser)

        val canRunAdsMock = AdSdkMockRepository.matchMockScript("https://example.com/canrunads.js")
        assertNotNull(canRunAdsMock)
        assertEquals(MockScriptType.PREBID_ADS, canRunAdsMock!!.type)

        // 6. Google Analytics
        val gaMock = AdSdkMockRepository.matchMockScript("https://www.google-analytics.com/analytics.js")
        assertNotNull(gaMock)
        assertEquals(MockScriptType.GOOGLE_ANALYTICS, gaMock!!.type)

        val gtmMock = AdSdkMockRepository.matchMockScript("https://www.googletagmanager.com/gtm.js?id=GTM-XXXX")
        assertNotNull(gtmMock)
        assertEquals(MockScriptType.GOOGLE_ANALYTICS, gtmMock!!.type)

        // Negative: normal URLs must not match
        assertNull(AdSdkMockRepository.matchMockScript("https://open.spotify.com/track/123"))
        assertNull(AdSdkMockRepository.matchMockScript("https://m.youtube.com/watch?v=123"))
        assertNull(AdSdkMockRepository.matchMockScript("https://example.com/bundle.js"))
    }

    @Test
    fun testUnifiedShieldControllerInterceptionAndTelemetry() {
        val gptUrl = "https://securepubads.g.doubleclick.net/tag/js/gpt.js"
        val gptAction = controller.evaluateResource(gptUrl, "https://news.example.com")
        assertTrue("Must return MockResponse for GPT", gptAction is UnifiedShieldAction.MockResponse)
        val gptMock = gptAction as UnifiedShieldAction.MockResponse
        assertEquals("application/javascript", gptMock.mimeType)
        assertTrue(gptMock.body.contains("window.googletag"))

        val fabUrl = "https://news.example.com/fuckadblock.js"
        val fabAction = controller.evaluateResource(fabUrl, "https://news.example.com")
        assertTrue("Must return MockResponse for FuckAdBlock", fabAction is UnifiedShieldAction.MockResponse)
        val fabMock = fabAction as UnifiedShieldAction.MockResponse
        assertEquals("application/javascript", fabMock.mimeType)
        assertTrue(fabMock.body.contains("window.FuckAdBlock"))

        val metrics = controller.getMetrics()
        assertEquals(1L, metrics.sdkMocksServed)
        assertEquals(1L, metrics.antiAdblockDefused)
        assertEquals(2L, metrics.trackersBlocked)
        assertEquals(2L, metrics.requestsEvaluated)
    }

    @Test
    fun testMediaRequestsNeverBlockedBySdkMock() {
        val spotifyAudio = "https://audio-fa.scdn.co/stream/12345"
        val ytVideo = "https://rr1---sn-xxx.googlevideo.com/videoplayback?id=123"
        val widevineDrm = "https://spclient.wg.spotify.com/widevine-license/v1/video/license"

        val actionAudio = controller.evaluateResource(spotifyAudio, "https://open.spotify.com")
        assertTrue(actionAudio is UnifiedShieldAction.Allow)

        val actionVideo = controller.evaluateResource(ytVideo, "https://m.youtube.com")
        assertTrue(actionVideo is UnifiedShieldAction.Allow)

        val actionDrm = controller.evaluateResource(widevineDrm, "https://open.spotify.com")
        assertTrue(actionDrm is UnifiedShieldAction.Allow)

        val metrics = controller.getMetrics()
        assertEquals(3L, metrics.protectedMediaAllowed)
        assertEquals(0L, metrics.trackersBlocked)
        assertEquals(0L, metrics.sdkMocksServed)
    }

    @Test
    fun testUserOriginExceptionBypassesSdkMock() {
        val origin = "https://partner-portal.com"
        exceptionRepo.setShieldDisabled(origin, true)

        val gptUrl = "https://www.googletagservices.com/tag/js/gpt.js"
        val action = controller.evaluateResource(gptUrl, origin)

        assertTrue("When shields disabled for origin, must return Allow", action is UnifiedShieldAction.Allow)
        val allow = action as UnifiedShieldAction.Allow
        assertTrue(allow.reason.contains("User disabled shields for origin"))

        val metrics = controller.getMetrics()
        assertEquals(0L, metrics.sdkMocksServed)
    }
}
