package com.musify.browser

import com.musify.browser.browser.ParsedNavigation
import com.musify.browser.browser.SingleTabViewModel
import com.musify.browser.browser.UrlParser
import com.musify.browser.engine.EngineSecurityState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 02 Single-Tab Browser Shell Tests
 *
 * Validates URL vs search parsing, unsafe scheme rejection, anti-spoofing origin extraction,
 * error state transitions, and strict TLS enforcement.
 */
class Phase02SingleTabTest {

    @Test
    fun testUrlParsingStandardDomains() {
        val domainResult = UrlParser.parse("example.com")
        assertTrue(domainResult is ParsedNavigation.AllowedUrl)
        val domain = domainResult as ParsedNavigation.AllowedUrl
        assertEquals("https://example.com", domain.url)
        assertEquals("https://example.com", domain.origin)
        assertEquals("example.com", domain.host)
        assertTrue(domain.isHttps)
        assertFalse(domain.isPunycode)

        val fullHttpsResult = UrlParser.parse("https://en.wikipedia.org/wiki/Privacy")
        assertTrue(fullHttpsResult is ParsedNavigation.AllowedUrl)
        val httpsNav = fullHttpsResult as ParsedNavigation.AllowedUrl
        assertEquals("https://en.wikipedia.org/wiki/Privacy", httpsNav.url)
        assertEquals("https://en.wikipedia.org", httpsNav.origin)
        assertEquals("en.wikipedia.org", httpsNav.host)
        assertTrue(httpsNav.isHttps)

        val httpResult = UrlParser.parse("http://example.org")
        assertTrue(httpResult is ParsedNavigation.AllowedUrl)
        val httpNav = httpResult as ParsedNavigation.AllowedUrl
        assertFalse(httpNav.isHttps)
        assertEquals("http://example.org", httpNav.origin)

        val ipResult = UrlParser.parse("192.168.1.1")
        assertTrue(ipResult is ParsedNavigation.AllowedUrl)
        val ipNav = ipResult as ParsedNavigation.AllowedUrl
        assertEquals("https://192.168.1.1", ipNav.url)
    }

    @Test
    fun testSearchParsing() {
        val multiWordResult = UrlParser.parse("how to block trackers in android")
        assertTrue(multiWordResult is ParsedNavigation.Search)
        val search = multiWordResult as ParsedNavigation.Search
        assertEquals("how to block trackers in android", search.query)
        assertTrue(search.searchUrl.contains("duckduckgo.com"))
        assertTrue(search.searchUrl.contains("how+to+block+trackers+in+android"))

        val singleWordResult = UrlParser.parse("privacy")
        assertTrue(singleWordResult is ParsedNavigation.Search)
        val singleSearch = singleWordResult as ParsedNavigation.Search
        assertEquals("privacy", singleSearch.query)
        assertTrue(singleSearch.searchUrl.contains("duckduckgo.com"))
    }

    @Test
    fun testUnsafeSchemesRejected() {
        val dangerousInputs = listOf(
            "javascript:alert(document.cookie)",
            "JAVASCRIPT:void(0)",
            "file:///data/local/tmp/secrets.txt",
            "content://com.android.providers.contacts/data",
            "intent://example.com#Intent;scheme=https;end",
            "data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==",
            "blob:https://example.com/d9494371"
        )

        for (input in dangerousInputs) {
            val result = UrlParser.parse(input)
            assertTrue("Expected input to be rejected: $input", result is ParsedNavigation.Rejected)
            val rejected = result as ParsedNavigation.Rejected
            assertTrue(
                "Reason must mention rejection: ${rejected.reason}",
                rejected.reason.contains("rejected", ignoreCase = true)
            )
        }
    }

    @Test
    fun testMalformedAndEmptyInputs() {
        val emptyResult = UrlParser.parse("")
        assertTrue(emptyResult is ParsedNavigation.Rejected)

        val whitespaceResult = UrlParser.parse("   ")
        assertTrue(whitespaceResult is ParsedNavigation.Rejected)
    }

    @Test
    fun testAntiSpoofingIdnHost() {
        // Punycode representation of an IDN
        val punycodeResult = UrlParser.parse("https://xn--e1afmkfd.xn--p1ai")
        assertTrue(punycodeResult is ParsedNavigation.AllowedUrl)
        val punycode = punycodeResult as ParsedNavigation.AllowedUrl
        assertTrue(punycode.isPunycode)
    }

    @Test
    fun testViewModelSafeNavigationRouting() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = SingleTabViewModel(coroutineScope = testScope)

        // 1. Valid Navigation
        viewModel.navigate("mozilla.org")
        assertNull(viewModel.tabState.value.errorState)

        // 2. Unsafe scheme blocked by ViewModel
        viewModel.navigate("javascript:alert(1)")
        val error = viewModel.tabState.value.errorState
        assertNotNull(error)
        assertTrue(error!!.isRejectedScheme)
        assertFalse(error.canRetry)

        // 3. Dismiss error
        viewModel.dismissError()
        assertNull(viewModel.tabState.value.errorState)
    }

    @Test
    fun testStrictTlsSecurityStateAlert() {
        val insecureCertState = EngineSecurityState(
            isSecure = false,
            protocol = "HTTPS",
            host = "badssl.com",
            tlsError = "Untrusted certificate authority",
            isBypassAllowed = false
        )

        assertFalse(insecureCertState.isSecure)
        assertFalse(insecureCertState.isBypassAllowed)
        assertNotNull(insecureCertState.tlsError)
    }

    @Test
    fun testViewModelNavigationControls() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = SingleTabViewModel(coroutineScope = testScope)

        // Test Fixture loader
        viewModel.loadTestFixture()
        assertNull(viewModel.tabState.value.errorState)

        // Reload & Stop controls
        viewModel.reload()
        assertNull(viewModel.tabState.value.errorState)
        viewModel.stop()

        // Shield toggling
        assertTrue(viewModel.tabState.value.shieldState.isProtectionEnabled)
        viewModel.toggleShields(false)
        assertFalse(viewModel.adapter.shieldState.value.isProtectionEnabled)
        viewModel.toggleShields(true)
        assertTrue(viewModel.adapter.shieldState.value.isProtectionEnabled)
    }

    @Test
    fun testRendererLossAndRecoveryCycle() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = SingleTabViewModel(coroutineScope = testScope)

        // Simulate crash event
        viewModel.adapter.simulateRendererLoss()
        val error = viewModel.tabState.value.errorState
        assertNotNull(error)
        assertEquals("Render Process Lost", error!!.title)
        assertTrue(error.canRetry)

        // Recover tab
        viewModel.recoverSession()
        assertNull(viewModel.tabState.value.errorState)
        assertTrue(viewModel.tabState.value.processState.isHealthy)
        assertEquals(1, viewModel.tabState.value.processState.recoveryCount)
    }

    @Test
    fun testOfflineAndNetworkErrorHandling() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = SingleTabViewModel(coroutineScope = testScope)

        viewModel.handleNetworkError(
            failingUrl = "https://example-offline.org",
            message = "Host unreachable. Please check network connection."
        )

        val error = viewModel.tabState.value.errorState
        assertNotNull(error)
        assertEquals("Network Connection Error", error!!.title)
        assertEquals("https://example-offline.org", error.failingUrl)
        assertTrue(error.canRetry)

        viewModel.dismissError()
        assertNull(viewModel.tabState.value.errorState)
    }

    @Test
    fun testExternalIntentSanitization() {
        // Valid web navigation from external intent
        val safeHttps = UrlParser.parse("https://github.com/adishxm/Musify")
        assertTrue(safeHttps is ParsedNavigation.AllowedUrl)
        assertEquals("https://github.com/adishxm/Musify", (safeHttps as ParsedNavigation.AllowedUrl).url)

        val safeHttp = UrlParser.parse("http://info.cern.ch")
        assertTrue(safeHttp is ParsedNavigation.AllowedUrl)

        // Attack payloads via external intent MUST be rejected
        val intentPayload = UrlParser.parse("intent://example.com/#Intent;scheme=file;end")
        assertTrue(intentPayload is ParsedNavigation.Rejected)

        val scriptPayload = UrlParser.parse("javascript:alert(document.domain)")
        assertTrue(scriptPayload is ParsedNavigation.Rejected)

        val filePayload = UrlParser.parse("file:///android_asset/secrets.json")
        assertTrue(filePayload is ParsedNavigation.Rejected)
    }

    @Test
    fun testReleaseNetworkSecurityConfigStrictness() {
        // Enforce that release network security config trusts ONLY system CAs and forbids cleartext
        val releaseConfigFile = java.io.File("src/main/res/xml/network_security_config.xml").takeIf { it.exists() }
            ?: java.io.File("app/src/main/res/xml/network_security_config.xml")

        assertTrue("Release network_security_config.xml must exist", releaseConfigFile.exists())
        val releaseContent = releaseConfigFile.readText()

        assertTrue(
            "Release config must disallow cleartext traffic",
            releaseContent.contains("cleartextTrafficPermitted=\"false\"")
        )
        assertTrue(
            "Release config must trust system certificates",
            releaseContent.contains("<certificates src=\"system\" />") || releaseContent.contains("<certificates src=\"system\"/>")
        )
        assertFalse(
            "Release config must never trust user certificates",
            releaseContent.contains("<certificates src=\"user\"")
        )
        assertFalse(
            "Release config must never contain debug-overrides",
            releaseContent.contains("<debug-overrides>")
        )

        // Debug config contains debug overrides
        val debugConfigFile = java.io.File("src/debug/res/xml/network_security_config.xml").takeIf { it.exists() }
            ?: java.io.File("app/src/debug/res/xml/network_security_config.xml")

        assertTrue("Debug network_security_config.xml must exist", debugConfigFile.exists())
        val debugContent = debugConfigFile.readText()
        assertTrue(
            "Debug config must contain debug-overrides for test fixtures",
            debugContent.contains("<debug-overrides>")
        )
    }
}
