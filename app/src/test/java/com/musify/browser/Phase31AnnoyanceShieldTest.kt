package com.musify.browser

import com.musify.browser.shield.AnnoyanceFilterRepository
import com.musify.browser.shield.LocalShieldExceptionRepository
import com.musify.browser.shield.UnifiedShieldAction
import com.musify.browser.shield.UnifiedShieldController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Phase 31 Unit Tests: Validates Brave-aligned Cookie Notice Blocker and Mobile App Promo Blocker,
 * CSS hiding rules, scroll unfreezing, network request interception, and controller integration.
 */
class Phase31AnnoyanceShieldTest {

    private lateinit var controller: UnifiedShieldController
    private lateinit var exceptionRepo: LocalShieldExceptionRepository

    @Before
    fun setUp() {
        exceptionRepo = LocalShieldExceptionRepository()
        controller = UnifiedShieldController(exceptionRepository = exceptionRepo)
    }

    @Test
    fun testCookieNoticeSelectorsIntegrity() {
        val selectors = AnnoyanceFilterRepository.COOKIE_NOTICE_SELECTORS
        assertTrue("Must include OneTrust", selectors.contains("#onetrust-consent-sdk"))
        assertTrue("Must include Cookiebot", selectors.contains("#CookiebotWidget"))
        assertTrue("Must include Didomi", selectors.contains("#didomi-host"))
        assertTrue("Must include TrustArc", selectors.contains("#truste-consent-track"))
        assertTrue("Must include Quantcast", selectors.contains(".qc-cmp2-container"))
        assertTrue("Must include Usercentrics", selectors.contains("#usercentrics-root"))
        assertTrue("Must include Complianz", selectors.contains(".cmplz-cookiebanner"))
        assertTrue("Must include generic cookie consent wildcard", selectors.contains("[id*='cookie-consent' i]"))
    }

    @Test
    fun testMobilePromoSelectorsIntegrity() {
        val selectors = AnnoyanceFilterRepository.MOBILE_PROMO_SELECTORS
        assertTrue("Must include smartbanner class", selectors.contains(".smartbanner"))
        assertTrue("Must include open-in-app class", selectors.contains(".open-in-app"))
        assertTrue("Must include reddit app button", selectors.contains(".reddit-app-button"))
        assertTrue("Must include reddit pwa bottom sheet", selectors.contains("reddit-pwa-bottom-sheet"))
        assertTrue("Must include twitter bottom bar", selectors.contains("[data-testid='BottomBar']"))
    }

    @Test
    fun testCombinedCssGeneration() {
        val css = AnnoyanceFilterRepository.getCombinedCss(blockCookies = true, blockPromos = true)
        assertTrue("CSS must hide target elements with display: none !important", css.contains("display: none !important"))
        assertTrue("CSS must reset body overflow to auto to unfreeze scroll", css.contains("overflow: auto !important"))
        assertTrue("CSS must reset body position to static", css.contains("position: static !important"))
        assertTrue("CSS must hide modal backdrop blurs", css.contains(".modal-backdrop"))

        val cookiesOnlyCss = AnnoyanceFilterRepository.getCombinedCss(blockCookies = true, blockPromos = false)
        assertTrue(cookiesOnlyCss.contains("#onetrust-consent-sdk"))
        assertFalse(cookiesOnlyCss.contains("reddit-pwa-bottom-sheet"))

        val promosOnlyCss = AnnoyanceFilterRepository.getCombinedCss(blockCookies = false, blockPromos = true)
        assertFalse(promosOnlyCss.contains("#onetrust-consent-sdk"))
        assertTrue(promosOnlyCss.contains("reddit-pwa-bottom-sheet"))
    }

    @Test
    fun testInjectionScriptStructure() {
        val script = AnnoyanceFilterRepository.getInjectionScript(blockCookies = true, blockPromos = true)
        assertTrue("Script must enforce idempotence check", script.contains("window.__musifyAnnoyanceShieldInjected"))
        assertTrue("Script must define style tag creation", script.contains("musify-annoyance-style"))
        assertTrue("Script must provide unfreezeScroll routine", script.contains("unfreezeScroll"))
        assertTrue("Script must observe DOM mutations dynamically", script.contains("new MutationObserver"))
    }

    @Test
    fun testNetworkAnnoyanceMatching() {
        assertTrue(AnnoyanceFilterRepository.isAnnoyanceRequest("https://cdn.example.com/assets/-cookie-banner.js"))
        assertTrue(AnnoyanceFilterRepository.isAnnoyanceRequest("https://cdn.example.com/scripts/-cookie-consent.js"))
        assertTrue(AnnoyanceFilterRepository.isAnnoyanceRequest("https://example.com/js/smart-app-banner.js"))
        assertTrue(AnnoyanceFilterRepository.isAnnoyanceRequest("https://cdn.cookielaw.org/consent/onetrust.com/consent/otSDKStub.js"))
        assertTrue(AnnoyanceFilterRepository.isAnnoyanceRequest("https://consent.cookiebot.com/uc.js"))

        // Legitimate requests must NOT match
        assertFalse(AnnoyanceFilterRepository.isAnnoyanceRequest("https://open.spotify.com/track/123"))
        assertFalse(AnnoyanceFilterRepository.isAnnoyanceRequest("https://m.youtube.com/watch?v=123"))
        assertFalse(AnnoyanceFilterRepository.isAnnoyanceRequest("https://example.com/bundle.min.js"))
    }

    @Test
    fun testUnifiedShieldControllerAnnoyanceInterception() {
        val cookieUrl = "https://cdn.example.com/js/-cookie-banner.js"
        val cookieAction = controller.evaluateResource(cookieUrl, "https://news.example.com")
        assertTrue("Cookie notice request must be blocked", cookieAction is UnifiedShieldAction.Block)

        val promoUrl = "https://example.com/scripts/smart-app-banner.js"
        val promoAction = controller.evaluateResource(promoUrl, "https://portal.example.com")
        assertTrue("Mobile app promo request must be blocked", promoAction is UnifiedShieldAction.Block)

        val metrics = controller.getMetrics()
        assertEquals(1L, metrics.cookieBannersBlocked)
        assertEquals(1L, metrics.appPromosBlocked)
        assertEquals(2L, metrics.trackersBlocked)
    }

    @Test
    fun testMediaRequestsNeverBlockedByAnnoyanceShield() {
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
        assertEquals(0L, metrics.cookieBannersBlocked)
        assertEquals(0L, metrics.appPromosBlocked)
    }

    @Test
    fun testOriginExemptionBypassesAnnoyanceShield() {
        val origin = "https://partner-portal.com"
        exceptionRepo.setShieldDisabled(origin, true)

        val cookieUrl = "https://cdn.example.com/js/-cookie-banner.js"
        val action = controller.evaluateResource(cookieUrl, origin)

        assertTrue("When shields disabled for origin, must return Allow", action is UnifiedShieldAction.Allow)
        val allow = action as UnifiedShieldAction.Allow
        assertTrue(allow.reason.contains("User disabled shields for origin"))

        val metrics = controller.getMetrics()
        assertEquals(0L, metrics.cookieBannersBlocked)
    }
}

