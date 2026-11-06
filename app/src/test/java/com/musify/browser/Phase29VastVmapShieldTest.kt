package com.musify.browser

import com.musify.browser.shield.UnifiedShieldAction
import com.musify.browser.shield.UnifiedShieldController
import com.musify.browser.shield.VastVmapAction
import com.musify.browser.shield.VastVmapInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Phase 29 Unit Tests: Validates Brave-aligned VAST and VMAP synthetic XML generation,
 * DoubleClick in-stream status mocking, silent media containers, and UnifiedShieldController integration.
 */
class Phase29VastVmapShieldTest {

    private lateinit var controller: UnifiedShieldController

    @Before
    fun setUp() {
        controller = UnifiedShieldController()
    }

    @Test
    fun testVastXmlGenerationValidSyntax() {
        val action3 = VastVmapAction.VastXml("3.0")
        assertEquals("text/xml; charset=utf-8", action3.mimeType)
        assertTrue(action3.xml.contains("<VAST version=\"3.0\"></VAST>"))

        val action4 = VastVmapAction.VastXml("4.0")
        assertTrue(action4.xml.contains("<VAST version=\"4.0\"></VAST>"))

        val action2 = VastVmapAction.VastXml("2.0")
        assertTrue(action2.xml.contains("<VAST version=\"2.0\"></VAST>"))
    }

    @Test
    fun testVmapXmlGenerationValidSyntax() {
        val action = VastVmapAction.VmapXml
        assertEquals("text/xml; charset=utf-8", action.mimeType)
        assertTrue(action.xml.contains("<vmap:VMAP"))
        assertTrue(action.xml.contains("xmlns:vmap=\"http://www.iab.net/videosuite/vmap\""))
        assertTrue(action.xml.contains("version=\"1.0\""))
        assertTrue(action.xml.contains("</vmap:VMAP>"))
    }

    @Test
    fun testDoubleClickAdStatusScript() {
        val action = VastVmapAction.DoubleClickAdStatus
        assertEquals("application/javascript; charset=utf-8", action.mimeType)
        assertEquals("window.google_ad_status = 1;\n", action.script)
    }

    @Test
    fun testVideoAdUrlPatternMatching() {
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://pubads.g.doubleclick.net/gampad/ads?env=vp&gdfp_req=1&output=vast"))
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://securepubads.g.doubleclick.net/gampad/ads?output=xml_vast"))
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://imasdk.googleapis.com/js/sdkloader/ima3.js"))
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://video.example.com/api/v1/vast/video_preroll"))
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://video.example.com/stream/playlist.vmap"))
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://s.innovid.com/v1/endpoints/vast?tag=12345"))
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://engine.spotxchange.com/vast/2.00/12345"))
        assertTrue(VastVmapInterceptor.isVideoAdRequest("https://static.doubleclick.net/instream/ad_status.js"))

        // Functional requests should NOT be detected as video ads
        assertFalse(VastVmapInterceptor.isVideoAdRequest("https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT"))
        assertFalse(VastVmapInterceptor.isVideoAdRequest("https://m.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertFalse(VastVmapInterceptor.isVideoAdRequest("https://music.youtube.com/watch?v=9bZkp7q19f0"))
    }

    @Test
    fun testUnifiedShieldControllerInterceptsVastXml() {
        val vastUrl = "https://pubads.g.doubleclick.net/gampad/ads?env=vp&output=vast"
        val action = controller.evaluateResource(vastUrl, "https://example-news.com")

        assertTrue("Must return MockResponse for VAST tag", action is UnifiedShieldAction.MockResponse)
        val mock = action as UnifiedShieldAction.MockResponse
        assertEquals("text/xml; charset=utf-8", mock.mimeType)
        assertTrue(mock.body.contains("<VAST version=\"3.0\"></VAST>"))

        val metrics = controller.getMetrics()
        assertEquals(1L, metrics.vastVmapMocksServed)
        assertEquals(1L, metrics.trackersBlocked)
    }

    @Test
    fun testUnifiedShieldControllerDoubleClickStatusMock() {
        val statusUrl = "https://ad.doubleclick.net/instream/ad_status.js"
        val action = controller.evaluateResource(statusUrl, "https://example-video.com")

        assertTrue("Must return MockResponse for DoubleClick status probe", action is UnifiedShieldAction.MockResponse)
        val mock = action as UnifiedShieldAction.MockResponse
        assertEquals("application/javascript; charset=utf-8", mock.mimeType)
        assertEquals("window.google_ad_status = 1;\n", mock.body)

        val metrics = controller.getMetrics()
        assertEquals(1L, metrics.vastVmapMocksServed)
        assertEquals(1L, metrics.trackersBlocked)
    }

    @Test
    fun testWidevineDrmAndMediaStreamsNotInterceptedByVast() {
        val drmUrl = "https://spclient.wg.spotify.com/widevine-license/v1/video/license"
        val drmAction = controller.evaluateResource(drmUrl, "https://open.spotify.com")
        assertTrue("Widevine DRM must be unconditionally allowed", drmAction is UnifiedShieldAction.Allow)

        val ytVideoUrl = "https://rr3---sn-4g5edn6r.googlevideo.com/videoplayback?expire=1730000000"
        val ytAction = controller.evaluateResource(ytVideoUrl, "https://m.youtube.com")
        assertTrue("Genuine videoplayback must be unconditionally allowed", ytAction is UnifiedShieldAction.Allow)

        val metrics = controller.getMetrics()
        assertEquals("No VAST mocks must be served for genuine DRM or video streams", 0L, metrics.vastVmapMocksServed)
        assertEquals(2L, metrics.protectedMediaAllowed)
    }

    @Test
    fun testSilentAudioNoopBufferIntegrity() {
        val silent = VastVmapAction.SilentAudio()
        assertEquals("audio/mp3", silent.mimeType)
        assertTrue(silent.data.isNotEmpty())
        // Validate MPEG audio sync header
        assertEquals(0xFF.toByte(), silent.data[0])
        assertEquals(0xFB.toByte(), silent.data[1])
    }
}

