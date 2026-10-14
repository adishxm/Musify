package com.musify.browser

import com.musify.browser.engine.MediaSafeResourcePolicy
import com.musify.browser.engine.ResourceClassification
import com.musify.browser.engine.WebViewAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 13 Tests: Real Engine Policy Isolation & Media-Safe Protection Architecture
 */
class Phase13EnginePolicyIsolationTest {

    @Test
    fun testMediaSafeResourcePolicyClassifiesMediaAndLicenseRequests() {
        val licenseUrl = "https://spclient.wg.spotify.com/widevine-license/v1/license"
        val audioCdnUrl = "https://audio-fa.scdn.co/audio/abcdef1234567890"
        val dashManifestUrl = "https://video-fa.scdn.co/manifest.mpd"

        assertTrue(MediaSafeResourcePolicy.isMediaOrLicenseRequest(licenseUrl))
        assertTrue(MediaSafeResourcePolicy.isMediaOrLicenseRequest(audioCdnUrl))
        assertTrue(MediaSafeResourcePolicy.isMediaOrLicenseRequest(dashManifestUrl))

        assertEquals(
            ResourceClassification.MEDIA_OR_LICENSE,
            MediaSafeResourcePolicy.classifyResource(licenseUrl, "https://open.spotify.com", false)
        )
        assertEquals(
            ResourceClassification.MEDIA_OR_LICENSE,
            MediaSafeResourcePolicy.classifyResource(audioCdnUrl, "https://open.spotify.com", false)
        )
    }

    @Test
    fun testMediaSafeResourcePolicyProtectsFirstPartyAndAuthHosts() {
        val origin = "https://open.spotify.com"
        val authUrl = "https://accounts.spotify.com/login"
        val clientApiUrl = "https://api.spotify.com/v1/me"
        val thirdPartyTrackerUrl = "https://google-analytics.com/analytics.js"

        assertTrue(MediaSafeResourcePolicy.isFirstPartyOrAuth(authUrl, origin))
        assertTrue(MediaSafeResourcePolicy.isFirstPartyOrAuth(clientApiUrl, origin))
        assertFalse(MediaSafeResourcePolicy.isFirstPartyOrAuth(thirdPartyTrackerUrl, origin))

        assertEquals(
            ResourceClassification.FIRST_PARTY_OR_AUTH,
            MediaSafeResourcePolicy.classifyResource(authUrl, origin, false)
        )
        assertEquals(
            ResourceClassification.TRACKER_CANDIDATE,
            MediaSafeResourcePolicy.classifyResource(thirdPartyTrackerUrl, origin, false)
        )
    }

    @Test
    fun testMediaSafeResourcePolicyAllowsWhenOriginExceptionActive() {
        val origin = "https://open.spotify.com"
        val thirdPartyUrl = "https://some-tracker.com/script.js"

        // When origin exception is true, third party request is classified as EXCEPTION_ALLOWED
        val decision = MediaSafeResourcePolicy.classifyResource(thirdPartyUrl, origin, isOriginException = true)
        assertEquals(ResourceClassification.EXCEPTION_ALLOWED, decision)
    }

    @Test
    fun testMediaSafeResourcePolicyNormalizesOriginsConsistently() {
        val complexUrl = "https://Open.Spotify.Com:443/album/37i9dQZF1DXcBWIGoYBM5M?si=abcd123#player"
        val customPortUrl = "http://localhost:8080/app/index.html"

        assertEquals("https://open.spotify.com", MediaSafeResourcePolicy.normalizeOrigin(complexUrl))
        assertEquals("http://localhost:8080", MediaSafeResourcePolicy.normalizeOrigin(customPortUrl))
    }

    @Test
    fun testMediaSafeResourcePolicySanitizesUrlsForDiagnostics() {
        val sensitiveUrl = "https://spclient.wg.spotify.com/widevine-license/v1/license?token=SECRET_ACCESS_TOKEN_123&session=XYZ456#signature"
        val sanitized = MediaSafeResourcePolicy.sanitizeUrlForDiagnostics(sensitiveUrl)

        assertEquals("https://spclient.wg.spotify.com/widevine-license/v1/license", sanitized)
        assertFalse("Sanitized URL must not leak token", sanitized.contains("SECRET_ACCESS_TOKEN_123"))
        assertFalse("Sanitized URL must not leak session", sanitized.contains("XYZ456"))
    }

    @Test
    fun testWebViewAdapterOriginExceptionUpdatesCapabilityState() {
        val adapter = WebViewAdapter(isPrivate = false)

        // Default state
        val initialCap = adapter.getMediaCapabilityState()
        assertFalse("Default origin exception should be false", initialCap.isOriginExceptionActive)
        assertEquals("Accept Non-Trackers & Isolate", initialCap.cookiePolicy)

        // Relax shields for origin
        adapter.setOriginException(true)
        val relaxedCap = adapter.getMediaCapabilityState()
        assertTrue("Origin exception should be active", relaxedCap.isOriginExceptionActive)
        assertEquals("Permissive Session (Origin Exception)", relaxedCap.cookiePolicy)

        // Restore shields
        adapter.setOriginException(false)
        val restoredCap = adapter.getMediaCapabilityState()
        assertFalse("Origin exception should be reverted to false", restoredCap.isOriginExceptionActive)
        assertEquals("Accept Non-Trackers & Isolate", restoredCap.cookiePolicy)
    }

    @Test
    fun testTabIsolationOfOriginExceptions() {
        val adapterTabA = WebViewAdapter(isPrivate = false)
        val adapterTabB = WebViewAdapter(isPrivate = false)

        // Set exception on Tab A only
        adapterTabA.setOriginException(true)

        assertTrue("Tab A should have origin exception active", adapterTabA.getMediaCapabilityState().isOriginExceptionActive)
        assertFalse("Tab B must remain isolated with origin exception inactive", adapterTabB.getMediaCapabilityState().isOriginExceptionActive)
        assertEquals("Permissive Session (Origin Exception)", adapterTabA.getMediaCapabilityState().cookiePolicy)
        assertEquals("Accept Non-Trackers & Isolate", adapterTabB.getMediaCapabilityState().cookiePolicy)
    }
}
