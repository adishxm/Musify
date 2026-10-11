package com.musify.browser.permissions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

interface SitePermissionRepository {
    fun getDecision(origin: String, type: SitePermissionType): SitePermissionDecision
    fun setDecision(origin: String, type: SitePermissionType, decision: SitePermissionDecision)
    fun revoke(origin: String, type: SitePermissionType): Boolean
    fun revokeAllForOrigin(origin: String): Boolean
    fun getAllPermissions(): List<SitePermission>
    fun clearAll()
    val permissionsFlow: StateFlow<List<SitePermission>>
}

class LocalSitePermissionRepository(private val storageFile: File? = null) : SitePermissionRepository {

    private val permissions = mutableListOf<SitePermission>()
    private val _permissionsFlow = MutableStateFlow<List<SitePermission>>(emptyList())
    override val permissionsFlow: StateFlow<List<SitePermission>> = _permissionsFlow.asStateFlow()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun getDecision(origin: String, type: SitePermissionType): SitePermissionDecision {
        val cleanOrigin = origin.trim().lowercase()
        return permissions.find { it.origin == cleanOrigin && it.type == type }?.decision
            ?: SitePermissionDecision.PROMPT
    }

    @Synchronized
    override fun setDecision(origin: String, type: SitePermissionType, decision: SitePermissionDecision) {
        val cleanOrigin = origin.trim().lowercase()
        permissions.removeAll { it.origin == cleanOrigin && it.type == type }
        if (decision != SitePermissionDecision.PROMPT) {
            permissions.add(
                SitePermission(
                    origin = cleanOrigin,
                    type = type,
                    decision = decision,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
        saveToDisk()
        notifyState()
    }

    @Synchronized
    override fun revoke(origin: String, type: SitePermissionType): Boolean {
        val cleanOrigin = origin.trim().lowercase()
        val removed = permissions.removeAll { it.origin == cleanOrigin && it.type == type }
        if (removed) {
            saveToDisk()
            notifyState()
        }
        return removed
    }

    @Synchronized
    override fun revokeAllForOrigin(origin: String): Boolean {
        val cleanOrigin = origin.trim().lowercase()
        val removed = permissions.removeAll { it.origin == cleanOrigin }
        if (removed) {
            saveToDisk()
            notifyState()
        }
        return removed
    }

    @Synchronized
    override fun getAllPermissions(): List<SitePermission> {
        return permissions.toList()
    }

    @Synchronized
    override fun clearAll() {
        permissions.clear()
        saveToDisk()
        notifyState()
    }

    private fun notifyState() {
        _permissionsFlow.value = permissions.toList()
    }

    private fun loadFromDisk() {
        if (storageFile == null || !storageFile.exists()) return
        try {
            val content = storageFile.readText(Charsets.UTF_8)
            val parsed = deserializeJson(content)
            permissions.clear()
            permissions.addAll(parsed)
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
            val json = serializeJson(permissions)
            val tempFile = File(storageFile.absolutePath + ".tmp")
            tempFile.writeText(json, Charsets.UTF_8)
            if (!tempFile.renameTo(storageFile)) {
                storageFile.writeText(json, Charsets.UTF_8)
                tempFile.delete()
            }
        } catch (_: Exception) {}
    }

    private fun serializeJson(list: List<SitePermission>): String {
        val sb = StringBuilder("[\n")
        list.forEachIndexed { i, it ->
            sb.append("  {\n")
            sb.append("    \"origin\": \"").append(escape(it.origin)).append("\",\n")
            sb.append("    \"type\": \"").append(it.type.name).append("\",\n")
            sb.append("    \"decision\": \"").append(it.decision.name).append("\",\n")
            sb.append("    \"updatedAtEpochMs\": ").append(it.updatedAtEpochMs).append("\n")
            sb.append("  }").append(if (i < list.size - 1) "," else "").append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun deserializeJson(json: String): List<SitePermission> {
        val regex = Regex("""\{[^{}]*"origin"\s*:\s*"([^"]+)"[^{}]*"type"\s*:\s*"([^"]+)"[^{}]*"decision"\s*:\s*"([^"]+)"[^{}]*"updatedAtEpochMs"\s*:\s*(\d+)[^{}]*\}""")
        return regex.findAll(json).mapNotNull { match ->
            try {
                val origin = unescape(match.groupValues[1])
                val type = SitePermissionType.valueOf(match.groupValues[2])
                val decision = SitePermissionDecision.valueOf(match.groupValues[3])
                val updatedAt = match.groupValues[4].toLongOrNull() ?: 0L
                SitePermission(origin, type, decision, updatedAt)
            } catch (_: Exception) {
                null
            }
        }.toList()
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    private fun unescape(s: String) = s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
}
