package com.musify.browser.downloads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

interface DownloadRepository {
    fun addDownload(item: DownloadItem)
    fun updateProgress(id: String, downloadedBytes: Long, totalBytes: Long)
    fun updateStatus(id: String, status: DownloadStatus, filePath: String? = null, errorMessage: String? = null)
    fun getDownload(id: String): DownloadItem?
    fun getAllDownloads(): List<DownloadItem>
    fun removeDownload(id: String): Boolean
    fun clearAll()
    val downloadsFlow: StateFlow<List<DownloadItem>>
}

class LocalDownloadRepository(private val storageFile: File? = null) : DownloadRepository {

    private val downloads = mutableListOf<DownloadItem>()
    private val _downloadsFlow = MutableStateFlow<List<DownloadItem>>(emptyList())
    override val downloadsFlow: StateFlow<List<DownloadItem>> = _downloadsFlow.asStateFlow()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun addDownload(item: DownloadItem) {
        downloads.removeAll { it.id == item.id }
        downloads.add(0, item)
        saveToDisk()
        notifyState()
    }

    @Synchronized
    override fun updateProgress(id: String, downloadedBytes: Long, totalBytes: Long) {
        val index = downloads.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = downloads[index]
            val updatedLength = if (totalBytes > 0) totalBytes else current.contentLength
            downloads[index] = current.copy(
                downloadedBytes = downloadedBytes,
                contentLength = updatedLength,
                status = DownloadStatus.DOWNLOADING
            )
            notifyState()
        }
    }

    @Synchronized
    override fun updateStatus(id: String, status: DownloadStatus, filePath: String?, errorMessage: String?) {
        val index = downloads.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = downloads[index]
            downloads[index] = current.copy(
                status = status,
                filePath = filePath ?: current.filePath,
                errorMessage = errorMessage ?: current.errorMessage
            )
            saveToDisk()
            notifyState()
        }
    }

    @Synchronized
    override fun getDownload(id: String): DownloadItem? {
        return downloads.find { it.id == id }
    }

    @Synchronized
    override fun getAllDownloads(): List<DownloadItem> {
        return downloads.toList()
    }

    @Synchronized
    override fun removeDownload(id: String): Boolean {
        val removed = downloads.removeAll { it.id == id }
        if (removed) {
            saveToDisk()
            notifyState()
        }
        return removed
    }

    @Synchronized
    override fun clearAll() {
        downloads.clear()
        saveToDisk()
        notifyState()
    }

    private fun notifyState() {
        _downloadsFlow.value = downloads.toList()
    }

    private fun loadFromDisk() {
        if (storageFile == null || !storageFile.exists()) return
        try {
            val content = storageFile.readText(Charsets.UTF_8)
            val parsed = deserializeJson(content)
            downloads.clear()
            downloads.addAll(parsed)
            notifyState()
        } catch (_: Exception) {}
    }

    private fun saveToDisk() {
        if (storageFile == null) return
        try {
            val parent = storageFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            val json = serializeJson(downloads)
            val tempFile = File(storageFile.absolutePath + ".tmp")
            tempFile.writeText(json, Charsets.UTF_8)
            if (!tempFile.renameTo(storageFile)) {
                storageFile.writeText(json, Charsets.UTF_8)
                tempFile.delete()
            }
        } catch (_: Exception) {}
    }

    private fun serializeJson(list: List<DownloadItem>): String {
        val sb = StringBuilder("[\n")
        list.forEachIndexed { i, it ->
            sb.append("  {\n")
            sb.append("    \"id\": \"").append(escape(it.id)).append("\",\n")
            sb.append("    \"url\": \"").append(escape(it.url)).append("\",\n")
            sb.append("    \"origin\": \"").append(escape(it.origin)).append("\",\n")
            sb.append("    \"fileName\": \"").append(escape(it.fileName)).append("\",\n")
            sb.append("    \"mimeType\": \"").append(escape(it.mimeType)).append("\",\n")
            sb.append("    \"contentLength\": ").append(it.contentLength).append(",\n")
            sb.append("    \"downloadedBytes\": ").append(it.downloadedBytes).append(",\n")
            sb.append("    \"status\": \"").append(it.status.name).append("\",\n")
            sb.append("    \"filePath\": \"").append(escape(it.filePath ?: "")).append("\",\n")
            sb.append("    \"errorMessage\": \"").append(escape(it.errorMessage ?: "")).append("\",\n")
            sb.append("    \"createdAtEpochMs\": ").append(it.createdAtEpochMs).append("\n")
            sb.append("  }").append(if (i < list.size - 1) "," else "").append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun deserializeJson(json: String): List<DownloadItem> {
        val regex = Regex("""\{[^{}]*"id"\s*:\s*"([^"]+)"[^{}]*"url"\s*:\s*"([^"]+)"[^{}]*"origin"\s*:\s*"([^"]+)"[^{}]*"fileName"\s*:\s*"([^"]+)"[^{}]*"mimeType"\s*:\s*"([^"]+)"[^{}]*"contentLength"\s*:\s*(-?\d+)[^{}]*"downloadedBytes"\s*:\s*(\d+)[^{}]*"status"\s*:\s*"([^"]+)"[^{}]*"filePath"\s*:\s*"([^"]*)"[^{}]*"errorMessage"\s*:\s*"([^"]*)"[^{}]*"createdAtEpochMs"\s*:\s*(\d+)[^{}]*\}""")
        return regex.findAll(json).map { match ->
            val statusStr = match.groupValues[8]
            val status = try {
                DownloadStatus.valueOf(statusStr)
            } catch (_: Exception) {
                DownloadStatus.COMPLETED
            }
            DownloadItem(
                id = unescape(match.groupValues[1]),
                url = unescape(match.groupValues[2]),
                origin = unescape(match.groupValues[3]),
                fileName = unescape(match.groupValues[4]),
                mimeType = unescape(match.groupValues[5]),
                contentLength = match.groupValues[6].toLongOrNull() ?: 0L,
                downloadedBytes = match.groupValues[7].toLongOrNull() ?: 0L,
                status = status,
                filePath = unescape(match.groupValues[9]).ifEmpty { null },
                errorMessage = unescape(match.groupValues[10]).ifEmpty { null },
                createdAtEpochMs = match.groupValues[11].toLongOrNull() ?: 0L
            )
        }.toList()
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    private fun unescape(s: String) = s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
}
