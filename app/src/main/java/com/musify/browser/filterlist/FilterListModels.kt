package com.musify.browser.filterlist

/**
 * FilterListMetadata represents metadata, provenance, licensing, and rule counters for a filter list.
 */
data class FilterListMetadata(
    val id: String,
    val name: String,
    val version: String,
    val sourceUrl: String,
    val homepage: String,
    val license: String,
    val attribution: String,
    val totalLines: Int,
    val supportedNetworkRules: Int,
    val supportedExceptionRules: Int,
    val unsupportedCosmeticRules: Int,
    val unsupportedScriptletRules: Int,
    val otherUnsupportedRules: Int,
    val sha256Checksum: String,
    val lastUpdatedEpochMs: Long,
    val isEnabled: Boolean = true
) {
    val totalSupportedRules: Int
        get() = supportedNetworkRules + supportedExceptionRules

    val totalUnsupportedRules: Int
        get() = unsupportedCosmeticRules + unsupportedScriptletRules + otherUnsupportedRules
}

/**
 * FilterDecision describes the outcome of evaluating a URL or request against active filter lists.
 */
sealed class FilterDecision {
    data class Allowed(val reason: String = "No blocking rule matched") : FilterDecision()
    data class Blocked(
        val rule: String,
        val listId: String,
        val matchedTarget: String
    ) : FilterDecision()
    data class Exception(
        val rule: String,
        val listId: String
    ) : FilterDecision()
}

/**
 * RuleParseStats aggregates parsing statistics, rigorously distinguishing supported network rules
 * from unsupported cosmetic and scriptlet rules.
 */
data class RuleParseStats(
    val totalLines: Int = 0,
    val commentLines: Int = 0,
    val emptyLines: Int = 0,
    val supportedNetworkRules: Int = 0,
    val supportedExceptionRules: Int = 0,
    val unsupportedCosmeticRules: Int = 0,
    val unsupportedScriptletRules: Int = 0,
    val otherUnsupportedRules: Int = 0,
    val oversizedLinesSkipped: Int = 0,
    val syntaxErrors: Int = 0
)

/**
 * UpdateResult represents the status of an HTTPS update attempt.
 */
sealed class UpdateResult {
    data class Success(val metadata: FilterListMetadata) : UpdateResult()
    data class Rollback(val reason: String, val activeMetadata: FilterListMetadata?) : UpdateResult()
    data class Failed(val error: String) : UpdateResult()
}
