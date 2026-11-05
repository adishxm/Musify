package com.musify.browser

import com.musify.browser.engine.SabrProtobufDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Phase 25 Unit Tests: SABR Protobuf Streaming Interceptor & Video Backoff Defense
 */
class Phase25SabrInterceptorTest {

    private val ytContentScriptFile = File("src/main/assets/musify_adblocker/youtube_adblock.js")

    @Test
    fun testSabrUrlDetection() {
        assertTrue(
            "Must detect googlevideo.com with sabr=1 parameter",
            SabrProtobufDecoder.isSabrRequest("https://rr1---sn-4g5ednks.googlevideo.com/videoplayback?expire=173000&sabr=1&rn=12")
        )
        assertFalse(
            "Must not flag non-sabr video streams",
            SabrProtobufDecoder.isSabrRequest("https://rr1---sn-4g5ednks.googlevideo.com/videoplayback?expire=173000&rn=12")
        )
        assertFalse(
            "Must handle empty URL cleanly",
            SabrProtobufDecoder.isSabrRequest("")
        )
    }

    @Test
    fun testControlChunkThresholdDetection() {
        assertTrue("Chunk of 320 bytes is a control chunk", SabrProtobufDecoder.isControlChunk(320))
        assertTrue("Chunk of 999 bytes is a control chunk", SabrProtobufDecoder.isControlChunk(999))
        assertFalse("Chunk of 1000 bytes is not a control chunk", SabrProtobufDecoder.isControlChunk(1000))
        assertFalse("Chunk of 128000 bytes is media audio/video payload", SabrProtobufDecoder.isControlChunk(128000))
        assertFalse("Chunk of 0 bytes is invalid", SabrProtobufDecoder.isControlChunk(0))
    }

    @Test
    fun testProtobufTagComputation() {
        // Wire format: field 4, wire_type 0 = (4 shl 3) or 0 = 32 = 0x20
        assertEquals(0x20.toByte(), SabrProtobufDecoder.TAG_FIELD_4_VARINT)
    }

    @Test
    fun testVarintDecoding() {
        // Varint for 300: 300 = 0b00000010_0101100 -> [0xAC, 0x02]
        val bytes = byteArrayOf(0xAC.toByte(), 0x02.toByte())
        val (decoded, nextPos) = SabrProtobufDecoder.decodeVarint(bytes, 0)
        assertEquals(300L, decoded)
        assertEquals(2, nextPos)

        // Varint for 8000: 8000 = [0xC0, 0x3E]
        val bytes8000 = byteArrayOf(0xC0.toByte(), 0x3E.toByte())
        val (decoded8000, nextPos8000) = SabrProtobufDecoder.decodeVarint(bytes8000, 0)
        assertEquals(8000L, decoded8000)
        assertEquals(2, nextPos8000)
    }

    @Test
    fun testPatchBackoffFieldRewritesInPlace() {
        // Construct protobuf chunk: header bytes, then tag 0x20 followed by backoff 8000ms (0xC0, 0x3E)
        val chunk = byteArrayOf(
            0x08, 0x01,                   // Field 1: varint 1
            0x20, 0xC0.toByte(), 0x3E,    // Field 4: varint 8000 ms (ad backoff delay!)
            0x12, 0x04, 0x61, 0x62, 0x63, 0x64 // Field 2: string "abcd"
        )
        val originalLength = chunk.size

        val seededRandom = Random(42)
        val patched = SabrProtobufDecoder.patchBackoffField(chunk, seededRandom)

        assertTrue("Backoff > 500ms must be patched", patched)
        assertEquals("Buffer length must not change during in-place patch", originalLength, chunk.size)

        // Verify the patched value at offset 3
        val (newBackoff, nextPos) = SabrProtobufDecoder.decodeVarint(chunk, 3)
        assertTrue("New backoff must be in noise range [50..150] ms, but was $newBackoff", newBackoff in 50..150)
        assertEquals(5, nextPos)
    }

    @Test
    fun testLowBackoffIsNotPatched() {
        // Normal pacing backoff of 200ms (0xC8, 0x01)
        val chunk = byteArrayOf(
            0x08, 0x01,
            0x20, 0xC8.toByte(), 0x01,
            0x12, 0x02, 0x61, 0x62
        )
        val patched = SabrProtobufDecoder.patchBackoffField(chunk)
        assertFalse("Normal pacing backoffs <= 500ms must not be patched", patched)
    }

    @Test
    fun testYouTubeAdBlockContentScriptContainsSabrLogic() {
        assertTrue("youtube_adblock.js must exist", ytContentScriptFile.exists())
        val content = ytContentScriptFile.readText()

        assertTrue("Must detect sabr=1 parameter", content.contains("url.includes('sabr=1')"))
        assertTrue("Must define forceFreshSabrSession function", content.contains("forceFreshSabrSession"))
        assertTrue("Must flag isInlinePlaybackNoAd on video data", content.contains("vd.isInlinePlaybackNoAd = true"))
        assertTrue("Must define readStreamBytes with threshold", content.contains("readStreamBytes"))
        assertTrue("Must define patchSabrBackoffField looking for 0x20 tag", content.contains("patchSabrBackoffField") && content.contains("0x20"))
        assertTrue("Must rewrite backoff in-place to noise target", content.contains("target = 50 + Math.floor(Math.random() * 100)"))
    }
}
