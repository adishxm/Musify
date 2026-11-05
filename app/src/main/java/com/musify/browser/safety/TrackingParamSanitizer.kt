package com.musify.browser.safety

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * TrackingParamSanitizer: Browser-level tracking parameter stripper.
 *
 * Reverse-engineered from Brave Browser's Shields query filter and clean URLs components:
 * - query-filter.json (global tracking parameter removal)
 * - clean-urls.json (domain-specific tracking parameter removal)
 */
object TrackingParamSanitizer {

    // Global tracking parameters stripped across all domains
    private val GLOBAL_TRACKING_PARAMS = setOf(
        "fbclid",
        "gclid",
        "msclkid",
        "dclid",
        "irclickid",
        "mc_eid",
        "_gl",
        "_hsenc",
        "_hsfp",
        "_hssc",
        "_hstc",
        "_openstat",
        "s_cid",
        "pk_cid",
        "hsctatracking",
        "rb_clickid",
        "at_recipient_id",
        "at_recipient_list",
        "bsft_clkid",
        "bsft_uid",
        "et_rid",
        "ml_subscriber",
        "ml_subscriber_hash",
        "mtm_cid",
        "oft_id",
        "oft_c",
        "oft_ck",
        "oft_d",
        "oft_k",
        "oft_lk",
        "oft_sk",
        "oly_anon_id",
        "oly_enc_id",
        "__s",
        "_bhlid",
        "_branch_match_id",
        "_branch_referrer",
        "guce_referrer",
        "guce_referrer_sig"
    )

    // Platform-specific tracking parameters (Facebook, Amazon, Google, etc.)
    private val PLATFORM_TRACKING_PARAMS = setOf(
        "__cft__",
        "__cft__[0]",
        "__cft__%5b0%5d",
        "__tn__",
        "acontext",
        "external_ref",
        "gd_impression_id",
        "idorvanity",
        "store_visit_source",
        "with_pv",
        "ascsubtag",
        "pf_rd_p",
        "pf_rd_r",
        "pf_rd_i",
        "pf_rd_m",
        "pf_rd_s",
        "pf_rd_t",
        "pd_rd_r",
        "pd_rd_w",
        "pd_rd_wg",
        "pd_rd_i",
        "crid",
        "sprefix",
        "social_share",
        "ved",
        "ei",
        "sca_esv",
        "sclient",
        "gs_lcrp",
        "gs_lp",
        "s_kwcid"
    )

    /**
     * Checks if a query parameter key is a known tracking token.
     */
    fun isTrackingParam(key: String): Boolean {
        if (key.isBlank()) return false
        val lower = key.lowercase()
        return GLOBAL_TRACKING_PARAMS.contains(lower) ||
                PLATFORM_TRACKING_PARAMS.contains(lower) ||
                GLOBAL_TRACKING_PARAMS.any { lower.startsWith(it) }
    }

    /**
     * Sanitizes a URL by stripping all tracking query parameters while preserving
     * functional queries (e.g., video ID `v`, search query `q`, page number, timestamps).
     */
    fun sanitize(rawUrl: String): String {
        if (rawUrl.isBlank() || !rawUrl.contains('?')) return rawUrl

        return try {
            val queryStart = rawUrl.indexOf('?')
            val hashStart = rawUrl.indexOf('#')

            val basePart = rawUrl.substring(0, queryStart)
            val queryString = if (hashStart != -1 && hashStart > queryStart) {
                rawUrl.substring(queryStart + 1, hashStart)
            } else {
                rawUrl.substring(queryStart + 1)
            }
            val hashPart = if (hashStart != -1) rawUrl.substring(hashStart) else ""

            if (queryString.isBlank()) return "$basePart$hashPart"

            val pairs = queryString.split('&')
            val cleanPairs = mutableListOf<String>()

            for (pair in pairs) {
                if (pair.isBlank()) continue
                val eqIdx = pair.indexOf('=')
                val key = if (eqIdx != -1) pair.substring(0, eqIdx) else pair
                val decodedKey = try {
                    URLDecoder.decode(key, "UTF-8")
                } catch (_: Throwable) {
                    key
                }

                if (!isTrackingParam(decodedKey)) {
                    cleanPairs.add(pair)
                }
            }

            if (cleanPairs.isEmpty()) {
                "$basePart$hashPart"
            } else {
                "$basePart?${cleanPairs.joinToString("&")}$hashPart"
            }
        } catch (_: Throwable) {
            rawUrl
        }
    }
}
