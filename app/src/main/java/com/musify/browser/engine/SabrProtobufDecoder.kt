package com.musify.browser.engine

import kotlin.random.Random

/**
 * SabrProtobufDecoder: Implements binary Protobuf wire-format inspection
 * and in-place backoff patching for YouTube's Server Adaptive Bitrate (SABR) protocol.
 *
 * Reverse-engineered from Brave Shields (brave-yt-sabr-fix.js).
 */
object SabrProtobufDecoder {

    const val TAG_FIELD_4_VARINT: Byte = 0x20 // (4 shl 3) or 0 = tag 0x20
    const val DEFAULT_THRESHOLD_BYTES = 1000
    const val BACKOFF_THRESHOLD_MS = 500
    const val MAX_PLAUSIBLE_BACKOFF_MS = 100_000

    /**
     * Determines whether a request URL is a SABR media control request.
     */
    fun isSabrRequest(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase()
        return lower.contains("googlevideo.com") && lower.contains("sabr=1")
    }

    /**
     * Determines whether a streaming chunk is a control packet (< 1000 bytes)
     * rather than bulk media audio/video payload.
     */
    fun isControlChunk(sizeInBytes: Int, threshold: Int = DEFAULT_THRESHOLD_BYTES): Boolean {
        return sizeInBytes in 1 until threshold
    }

    /**
     * Decodes the unsigned 35-bit varint starting at [offset].
     * Returns a Pair of (decodedValue, nextOffset).
     */
    fun decodeVarint(bytes: ByteArray, offset: Int): Pair<Long, Int> {
        var value = 0L
        var shift = 0
        var pos = offset
        while (pos < bytes.size && shift < 35) {
            val b = bytes[pos].toInt() and 0xFF
            value = value or ((b and 0x7F).toLong() shl shift)
            pos++
            if ((b and 0x80) == 0) break
            shift += 7
        }
        return Pair(value, pos)
    }

    /**
     * Scans [bytes] for Protobuf Field 4 (`tag 0x20`), reads `backoffTimeMs`.
     * If `backoffTimeMs > 500ms`, rewrites the varint in-place with a small randomized
     * target (50-150ms) preserving the exact byte length with continuation bits.
     * Returns true if any field was patched.
     */
    fun patchBackoffField(bytes: ByteArray, randomSeed: Random = Random.Default): Boolean {
        var patched = false
        var i = 0
        while (i < bytes.size - 2) {
            if (bytes[i] == TAG_FIELD_4_VARINT) {
                val (backoffMs, endPos) = decodeVarint(bytes, i + 1)
                if (backoffMs in (BACKOFF_THRESHOLD_MS + 1)..MAX_PLAUSIBLE_BACKOFF_MS) {
                    val targetMs = 50 + randomSeed.nextInt(101) // 50 to 150 ms
                    var pos = i + 1
                    var remaining = targetMs.toLong()
                    // Pad with continuation bits up to endPos - 1
                    while (pos < endPos - 1) {
                        bytes[pos] = ((remaining and 0x7F) or 0x80).toByte()
                        remaining = remaining ushr 7
                        pos++
                    }
                    bytes[pos] = (remaining and 0x7F).toByte()
                    patched = true
                    i = endPos - 1
                }
            }
            i++
        }
        return patched
    }
}
