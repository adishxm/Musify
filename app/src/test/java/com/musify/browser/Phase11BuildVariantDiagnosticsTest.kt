package com.musify.browser

import com.musify.browser.engine.BuildVariantInfo
import com.musify.browser.engine.CapabilityStatus
import com.musify.browser.engine.EngineType
import com.musify.browser.engine.MediaCapabilityState
import com.musify.browser.engine.MusifyVariant
import com.musify.browser.engine.WebViewAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 11 Tests: Build Variant & Protected Media Capability Transparency
 */
class Phase11BuildVariantDiagnosticsTest {

    @Test
    fun testBuildVariantDetectionForWebView() {
        val buildInfo = BuildVariantInfo.detect(EngineType.WEB_VIEW)
        assertEquals(MusifyVariant.LITE, buildInfo.variant)
        assertEquals(EngineType.WEB_VIEW, buildInfo.engineType)
        assertFalse("WebView should report Widevine unavailable", buildInfo.isWidevineDrmAvailable)
        assertNotNull("WebView must have protected playback limitation notice", buildInfo.protectedPlaybackLimitationNotice)
        assertTrue(buildInfo.protectedPlaybackLimitationNotice!!.contains("Android System WebView does not expose Widevine CDM"))
    }

    @Test
    fun testBuildVariantDetectionForGeckoView() {
        val buildInfo = BuildVariantInfo.detect(EngineType.GECKO_VIEW)
        assertTrue(
            "GeckoView variant should be ARM64 or UNIVERSAL",
            buildInfo.variant == MusifyVariant.ARM64 || buildInfo.variant == MusifyVariant.UNIVERSAL
        )
        assertEquals(EngineType.GECKO_VIEW, buildInfo.engineType)
        assertTrue("GeckoView has Widevine support candidate", buildInfo.isWidevineDrmAvailable)
        assertNull("GeckoView should not have WebView limitation notice", buildInfo.protectedPlaybackLimitationNotice)
    }

    @Test
    fun testWebViewAdapterExplicitlyReportsUnsupportedWidevine() {
        val adapter = WebViewAdapter(isPrivate = false)
        val state = adapter.getMediaCapabilityState()

        assertEquals(
            "WebViewAdapter must explicitly set widevineStatus to UNSUPPORTED",
            CapabilityStatus.UNSUPPORTED,
            state.widevineStatus
        )
        assertFalse(state.isWidevineSupported)
        assertNotNull(state.lastMediaFailureReason)
        assertTrue(state.lastMediaFailureReason!!.contains("Widevine CDM"))
    }

    @Test
    fun testMediaCapabilityStateDefaultsToUnknown() {
        val defaultState = MediaCapabilityState()
        assertEquals(CapabilityStatus.UNKNOWN, defaultState.encryptedMediaStatus)
        assertEquals(CapabilityStatus.UNKNOWN, defaultState.widevineStatus)
        assertFalse("Default widevine status must not be assumed true", defaultState.isWidevineSupported)
        assertFalse("Default encrypted media status must not be assumed true", defaultState.isEncryptedMediaSupported)
    }
}
