package com.musify.browser.downloads

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * MusifyDownloadManager orchestrates user-confirmed downloads, off-thread streaming,
 * cancellation, and mediated file opening via FileProvider.
 */
class MusifyDownloadManager(
    private val context: Context,
    val repository: DownloadRepository,
    private val scope: CoroutineScope,
    private val downloadDir: File = File(context.filesDir, "downloads")
) {
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val _pendingPrompt = MutableStateFlow<DownloadPromptRequest?>(null)
    val pendingPrompt: StateFlow<DownloadPromptRequest?> = _pendingPrompt.asStateFlow()

    init {
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
    }

    /**
     * Called when the browser engine detects a downloadable resource.
     * Enforces user confirmation before taking any network action.
     */
    fun requestDownload(
        url: String,
        origin: String,
        suggestedFileName: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        val sanitized = DownloadSecurityUtils.sanitizeFileName(suggestedFileName, mimeType)
        val isExecutable = DownloadSecurityUtils.isExecutable(sanitized)
        val request = DownloadPromptRequest(
            id = UUID.randomUUID().toString(),
            url = url,
            origin = origin,
            suggestedFileName = sanitized,
            mimeType = mimeType ?: "application/octet-stream",
            contentLength = contentLength,
            isExecutableWarning = isExecutable
        )
        _pendingPrompt.value = request
    }

    /**
     * User explicitly confirms download.
     */
    fun confirmDownload(request: DownloadPromptRequest, customFileName: String? = null) {
        _pendingPrompt.value = null
        val targetName = DownloadSecurityUtils.sanitizeFileName(customFileName ?: request.suggestedFileName, request.mimeType)
        val targetFile = File(downloadDir, targetName)

        val item = DownloadItem(
            id = request.id,
            url = request.url,
            origin = request.origin,
            fileName = targetName,
            mimeType = request.mimeType,
            contentLength = request.contentLength,
            status = DownloadStatus.DOWNLOADING,
            filePath = targetFile.absolutePath,
            createdAtEpochMs = System.currentTimeMillis()
        )
        repository.addDownload(item)

        val job = scope.launch(Dispatchers.IO) {
            streamDownload(item, targetFile)
        }
        activeJobs[item.id] = job
    }

    /**
     * User declines/cancels download prompt.
     */
    fun dismissPrompt() {
        _pendingPrompt.value = null
    }

    /**
     * User cancels an ongoing download.
     */
    fun cancelDownload(downloadId: String) {
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)
        repository.updateStatus(downloadId, DownloadStatus.CANCELLED)
    }

    /**
     * Builds a safe intent to open a downloaded file via FileProvider.
     */
    fun buildOpenIntent(item: DownloadItem): Intent? {
        val path = item.filePath ?: return null
        val file = File(path)
        if (!file.exists()) return null

        return try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, item.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Builds a safe share intent for a downloaded file.
     */
    fun buildShareIntent(item: DownloadItem): Intent? {
        val path = item.filePath ?: return null
        val file = File(path)
        if (!file.exists()) return null

        return try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)
            Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun streamDownload(item: DownloadItem, destination: File) = withContext(Dispatchers.IO) {
        try {
            val connection = URL(item.url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "MusifyBrowser/1.0 (Android; Privacy)")

            if (connection.responseCode !in 200..299) {
                repository.updateStatus(
                    id = item.id,
                    status = DownloadStatus.FAILED,
                    errorMessage = "Server returned HTTP ${connection.responseCode}"
                )
                return@withContext
            }

            val total = if (item.contentLength > 0) item.contentLength else connection.contentLengthLong
            var downloaded = 0L

            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 300) {
                            repository.updateProgress(item.id, downloaded, total)
                            lastUpdate = now
                        }
                    }
                    output.flush()
                }
            }

            repository.updateProgress(item.id, downloaded, total)
            repository.updateStatus(item.id, DownloadStatus.COMPLETED, destination.absolutePath)
        } catch (e: Exception) {
            if (activeJobs[item.id]?.isCancelled == true) {
                repository.updateStatus(item.id, DownloadStatus.CANCELLED)
            } else {
                repository.updateStatus(item.id, DownloadStatus.FAILED, errorMessage = e.message ?: "Download failed")
            }
        } finally {
            activeJobs.remove(item.id)
        }
    }
}
