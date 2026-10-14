package com.musify.browser

import com.musify.browser.engine.EngineSecurityState
import com.musify.browser.engine.EngineType
import com.musify.browser.engine.GeckoEngineAdapter
import com.musify.browser.engine.WebViewAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 01 Browser Engine Feasibility Spike Tests
 *
 * Verifies EngineAdapter boundary, security invariants, process recovery, and shield state.
 */
class Phase01EngineSpikeTest {

    @Test
    fun testEngineAdapterTypes() {
        val geckoAdapter = GeckoEngineAdapter()
        val webViewAdapter = WebViewAdapter()

        assertEquals(EngineType.GECKO_VIEW, geckoAdapter.engineType)
        assertEquals(EngineType.WEB_VIEW, webViewAdapter.engineType)
    }

    @Test
    fun testStrictSecurityGateNoBypassAllowed() {
        val geckoAdapter = GeckoEngineAdapter()
        val webViewAdapter = WebViewAdapter()

        // Security Gate Requirement: No blanket SSL proceed or bypass allowed
        assertFalse("Gecko adapter must never allow TLS bypass", geckoAdapter.securityState.value.isBypassAllowed)
        assertFalse("WebView adapter must never allow TLS bypass", webViewAdapter.securityState.value.isBypassAllowed)

        val insecureState = EngineSecurityState(
            isSecure = false,
            protocol = "HTTP",
            tlsError = "Untrusted certificate authority",
            isBypassAllowed = false
        )
        assertFalse(insecureState.isSecure)
        assertFalse(insecureState.isBypassAllowed)
        assertNotNull(insecureState.tlsError)
    }

    @Test
    fun testRendererCrashAndRecoveryCycle() {
        val geckoAdapter = GeckoEngineAdapter()

        // 1. Initially healthy
        assertTrue(geckoAdapter.processState.value.isHealthy)
        assertFalse(geckoAdapter.processState.value.hasCrashed)
        assertEquals(0, geckoAdapter.processState.value.recoveryCount)

        // 2. Simulate renderer termination
        geckoAdapter.simulateRendererLoss()
        assertFalse(geckoAdapter.processState.value.isHealthy)
        assertTrue(geckoAdapter.processState.value.hasCrashed)
        assertNotNull(geckoAdapter.processState.value.crashReason)

        // 3. Recover renderer
        geckoAdapter.recoverRenderer()
        assertTrue(geckoAdapter.processState.value.isHealthy)
        assertFalse(geckoAdapter.processState.value.hasCrashed)
        assertEquals(1, geckoAdapter.processState.value.recoveryCount)
    }

    @Test
    fun testWebViewRendererCrashAndRecoveryCycle() {
        val webViewAdapter = WebViewAdapter()

        assertTrue(webViewAdapter.processState.value.isHealthy)
        assertFalse(webViewAdapter.processState.value.hasCrashed)

        webViewAdapter.simulateRendererLoss()
        assertTrue(webViewAdapter.processState.value.hasCrashed)
        assertFalse(webViewAdapter.processState.value.isHealthy)

        webViewAdapter.recoverRenderer()
        assertTrue(webViewAdapter.processState.value.isHealthy)
        assertEquals(1, webViewAdapter.processState.value.recoveryCount)
    }

    @Test
    fun testShieldProtectionToggle() {
        val geckoAdapter = GeckoEngineAdapter()

        assertTrue(geckoAdapter.shieldState.value.isProtectionEnabled)
        geckoAdapter.setProtectionEnabled(false)
        assertFalse(geckoAdapter.shieldState.value.isProtectionEnabled)
        geckoAdapter.setProtectionEnabled(true)
        assertTrue(geckoAdapter.shieldState.value.isProtectionEnabled)
    }

    @Test
    fun testWebExtensionControllerApi() {
        val clazz = Class.forName("org.mozilla.geckoview.WebExtensionController")
        assertNotNull(clazz)
        val hasEnsureBuiltIn = clazz.methods.any { it.name == "ensureBuiltIn" }
        assertTrue("GeckoView WebExtensionController must support ensureBuiltIn for native shields", hasEnsureBuiltIn)
    }
}
