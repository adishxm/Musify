package com.musify.browser.filterlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * FetchResponse holds the HTTP status code, input stream, and optional headers.
 */
data class FetchResponse(
    val statusCode: Int,
    val inputStream: InputStream,
    val expectedSha256: String? = null
)

/**
 * HttpFetcher abstraction allows deterministic unit testing of timeouts, errors, and corrupt responses.
 */
fun interface HttpFetcher {
    suspend fun fetch(url: String, timeoutMs: Int): FetchResponse
}

/**
 * DefaultHttpsFetcher performs standard HTTPS downloads with TLS and bounded timeouts.
 */
class DefaultHttpsFetcher : HttpFetcher {
    override suspend fun fetch(url: String, timeoutMs: Int): FetchResponse = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = timeoutMs
        connection.readTimeout = timeoutMs
        connection.instanceFollowRedirects = true
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "MusifyBrowser/1.0 (Android; Privacy)")

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream ?: ByteArrayInputStream(ByteArray(0))
        val shaHeader = connection.getHeaderField("X-Checksum-Sha256")
        FetchResponse(code, stream, shaHeader)
    }
}

/**
 * FilterListUpdater downloads remote filter lists, stages them, validates them against golden rules,
 * and atomically commits them or triggers a safe rollback on failure.
 */
class FilterListUpdater(
    private val storage: FilterListStorage,
    private val fetcher: HttpFetcher = DefaultHttpsFetcher(),
    private val timeoutMs: Int = 10000
) {
    /**
     * Executes an update for the specified filter list.
     * Enforces HTTPS, stages the download, compiles and validates, and preserves active on failure.
     */
    suspend fun updateList(
        target: FilterListMetadata,
        expectedChecksum: String? = null
    ): UpdateResult = withContext(Dispatchers.IO) {
        if (!target.sourceUrl.startsWith("https://", ignoreCase = true)) {
            return@withContext UpdateResult.Failed("Insecure update URL rejected: Updates must use HTTPS")
        }

        val activeMeta = storage.getActiveMetadata(target.id)

        try {
            val response = fetcher.fetch(target.sourceUrl, timeoutMs)

            if (response.statusCode !in 200..299) {
                return@withContext UpdateResult.Rollback(
                    "HTTP update failed with status code ${response.statusCode}",
                    activeMeta
                )
            }

            // Stage the downloaded stream
            storage.stageList(target.id, response.inputStream)

            // Validate and atomically activate
            val result = storage.validateAndActivate(
                listId = target.id,
                fallbackMetadata = target,
                expectedSha256 = expectedChecksum ?: response.expectedSha256
            )

            result
        } catch (e: Exception) {
            UpdateResult.Rollback(
                "Update failed with network/IO error: ${e.message}",
                activeMeta
            )
        }
    }
}
