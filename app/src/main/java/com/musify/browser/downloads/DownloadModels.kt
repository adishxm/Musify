package com.musify.browser.downloads

/**
 * Status of a download operation.
 */
enum class DownloadStatus {
    PENDING_CONFIRMATION,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    CANCELLED,
    FAILED
}

/**
 * DownloadItem represents a user-managed download and its state.
 */
data class DownloadItem(
    val id: String,
    val url: String,
    val origin: String,
    val fileName: String,
    val mimeType: String,
    val contentLength: Long,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.PENDING_CONFIRMATION,
    val filePath: String? = null,
    val errorMessage: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (contentLength > 0L) (downloadedBytes.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f) else 0f

    val isFinished: Boolean
        get() = status == DownloadStatus.COMPLETED || status == DownloadStatus.CANCELLED || status == DownloadStatus.FAILED
}

/**
 * DownloadPromptRequest represents an incoming download request waiting for explicit user confirmation.
 */
data class DownloadPromptRequest(
    val id: String,
    val url: String,
    val origin: String,
    val suggestedFileName: String,
    val mimeType: String,
    val contentLength: Long,
    val isExecutableWarning: Boolean = false
)
