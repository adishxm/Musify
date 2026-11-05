package com.musify.browser

import com.musify.browser.browser.ParsedNavigation
import com.musify.browser.browser.UrlParser
import com.musify.browser.safety.DebounceInterceptor
import com.musify.browser.safety.TrackingParamSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 26 Unit Tests: Universal Tracking Parameter Sanitizer & Bounce Tracker Defuser (Clean URLs & Debouncing)
 */
class Phase26CleanUrlsAndDebounceTest {

    @Test
    fun testTrackingParameterStrippingFromStandardUrls() {
        val dirtyUrl = "https://example.com/product?fbclid=12345&gclid=67890&_gl=abc&msclkid=xyz"
        val cleanUrl = TrackingParamSanitizer.sanitize(dirtyUrl)
        assertEquals("https://example.com/product", cleanUrl)
    }

    @Test
    fun testLegitimateParametersPreserved() {
        val mixedUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&fbclid=ad_track_id&t=42s"
        val cleanUrl = TrackingParamSanitizer.sanitize(mixedUrl)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s", cleanUrl)
    }

    @Test
    fun testPlatformSpecificTrackingTokens() {
        // Amazon tracking tokens stripped
        val amazonUrl = "https://www.amazon.com/dp/B000?ascsubtag=affiliate_123&pf_rd_p=promo&crid=abc"
        val cleanAmazon = TrackingParamSanitizer.sanitize(amazonUrl)
        assertEquals("https://www.amazon.com/dp/B000", cleanAmazon)

        // Google search tracking tokens stripped
        val googleUrl = "https://www.google.com/search?q=musify&ved=2ahUKE&sca_esv=123&ei=xyz"
        val cleanGoogle = TrackingParamSanitizer.sanitize(googleUrl)
        assertEquals("https://www.google.com/search?q=musify", cleanGoogle)
    }

    @Test
    fun testBounceTrackingInterceptorExtraction() {
        // Intercept CJ Affiliate / dpbolvw.net redirect
        val cjUrl = "https://dpbolvw.net/click-12345?url=https%3A%2F%2Fmusify.app%2Fdownload%3Ffbclid%3Dtrack"
        val resolvedCj = DebounceInterceptor.resolveBounceUrl(cjUrl)
        assertEquals("https://musify.app/download", resolvedCj)

        // Intercept howl.me / btn_url redirect
        val howlUrl = "https://howl.me/link/?btn_url=https%3A%2F%2Fstore.com%2Fitem%3Fgclid%3Dtracker"
        val resolvedHowl = DebounceInterceptor.resolveBounceUrl(howlUrl)
        assertEquals("https://store.com/item", resolvedHowl)
    }

    @Test
    fun testNonBounceTrackerUnmodified() {
        val wikiUrl = "https://en.wikipedia.org/wiki/Brave_(web_browser)"
        val resolved = DebounceInterceptor.resolveBounceUrl(wikiUrl)
        assertEquals(wikiUrl, resolved)
    }

    @Test
    fun testUrlParserIntegrationWithCleanUrlsAndDebounce() {
        // Test tracking token stripping via UrlParser
        val parsed1 = UrlParser.parse("https://open.spotify.com/track/123?si=user_token&fbclid=fake_fb")
        assertTrue(parsed1 is ParsedNavigation.AllowedUrl)
        val allowed1 = parsed1 as ParsedNavigation.AllowedUrl
        assertEquals("https://open.spotify.com/track/123?si=user_token", allowed1.url)

        // Test debounce hop bypass via UrlParser
        val parsed2 = UrlParser.parse("https://www.tkqlhce.com/click-999?url=https%3A%2F%2Fexample.org%2Fpage%3Fmsclkid%3Dbing123")
        assertTrue(parsed2 is ParsedNavigation.AllowedUrl)
        val allowed2 = parsed2 as ParsedNavigation.AllowedUrl
        assertEquals("https://example.org/page", allowed2.url)
        assertEquals("example.org", allowed2.host)
    }
}
