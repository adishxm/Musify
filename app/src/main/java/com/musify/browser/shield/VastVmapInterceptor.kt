package com.musify.browser.shield

/**
 * Result of evaluating a request against VAST, VMAP, and in-stream video ad patterns.
 */
sealed class VastVmapAction {
    data class VastXml(val version: String = "3.0") : VastVmapAction() {
        val xml: String = """<VAST version="$version"></VAST>""" + "\n"
        val mimeType: String = "text/xml; charset=utf-8"
    }

    object VmapXml : VastVmapAction() {
        const val xml: String = """<vmap:VMAP xmlns:vmap="http://www.iab.net/videosuite/vmap" version="1.0"></vmap:VMAP>""" + "\n"
        const val mimeType: String = "text/xml; charset=utf-8"
    }

    object DoubleClickAdStatus : VastVmapAction() {
        const val script: String = "window.google_ad_status = 1;\n"
        const val mimeType: String = "application/javascript; charset=utf-8"
    }

    data class SilentAudio(val mimeType: String = "audio/mp3") : VastVmapAction() {
        // Minimal valid MP3 header (MPEG-1 Layer 3, 128 kbps, 44.1 kHz, silent frame)
        val data: ByteArray = byteArrayOf(
            0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0x64.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )
    }
}

/**
 * VastVmapInterceptor detects video advertising tags (VAST 2.0/3.0/4.0 and VMAP 1.0)
 * and in-stream ad status probes, returning synthetic empty payloads.
 *
 * Reverse-engineered from Brave Browser's `resources.json` (`noop-vast3.xml`,
 * `noop-vmap1.xml`, `doubleclick_instream_ad_status.js`).
 *
 * When video players (e.g. streaming news, Twitch, web video players) receive an
 * empty VAST/VMAP structure, they detect zero ad pods and immediately proceed
 * with main content playback in 0ms without error spinners or timeouts.
 */
object VastVmapInterceptor {

    private val VAST_URL_SUBSTRINGS = listOf(
        "/vast",
        ".vast",
        "/vmap",
        ".vmap",
        "output=vast",
        "output=vmap",
        "output=xml_vast",
        "format=vast",
        "format=vmap",
        "env=vp",
        "vpaid",
        "imasdk.googleapis.com/js/sdkloader/ima3.js",
        "pubads.g.doubleclick.net/gampad/ads",
        "securepubads.g.doubleclick.net/gampad/ads",
        "innovid.com/v1/endpoints/vast",
        "springserve.com/vast",
        "spotxchange.com/vast",
        "tremorhub.com/live/vast",
        "aniview.com/api/adserver/vast",
        "targetspot.com/vast"
    )

    private val DOUBLECLICK_AD_STATUS_PATTERNS = listOf(
        "doubleclick.net/instream/ad_status.js",
        "google_ad_status.js"
    )

    /**
     * Determines whether a request URL represents a video ad tag or in-stream ad status call.
     */
    fun isVideoAdRequest(requestUrl: String): Boolean {
        val lower = requestUrl.lowercase()
        return DOUBLECLICK_AD_STATUS_PATTERNS.any { lower.contains(it) } ||
                VAST_URL_SUBSTRINGS.any { lower.contains(it) }
    }

    /**
     * Evaluates a request URL and returns the appropriate synthetic noop response.
     */
    fun intercept(requestUrl: String): VastVmapAction? {
        val lower = requestUrl.lowercase()

        // 1. DoubleClick instream ad status probe
        if (DOUBLECLICK_AD_STATUS_PATTERNS.any { lower.contains(it) }) {
            return VastVmapAction.DoubleClickAdStatus
        }

        // 2. VMAP playlists
        if (lower.contains("/vmap") || lower.contains(".vmap") || lower.contains("output=vmap") || lower.contains("format=vmap")) {
            return VastVmapAction.VmapXml
        }

        // 3. VAST templates (2.0, 3.0, 4.0)
        if (VAST_URL_SUBSTRINGS.any { lower.contains(it) }) {
            val version = when {
                lower.contains("vast4") || lower.contains("version=4") -> "4.0"
                lower.contains("vast2") || lower.contains("version=2") -> "2.0"
                else -> "3.0"
            }
            return VastVmapAction.VastXml(version)
        }

        return null
    }
}
