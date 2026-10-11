package com.musify.browser.storage

import java.util.UUID

/**
 * Time ranges for selective browsing data deletion.
 */
enum class TimeRange(val displayName: String, val durationMs: Long) {
    LAST_HOUR("Last Hour", 60 * 60 * 1000L),
    LAST_24_HOURS("Last 24 Hours", 24 * 60 * 60 * 1000L),
    LAST_7_DAYS("Last 7 Days", 7 * 24 * 60 * 60 * 1000L),
    ALL_TIME("All Time", Long.MAX_VALUE)
}

/**
 * Record representing a single visited page in local browsing history.
 */
data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val visitTime: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
)

/**
 * Record representing a user-saved bookmark.
 */
data class BookmarkItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
