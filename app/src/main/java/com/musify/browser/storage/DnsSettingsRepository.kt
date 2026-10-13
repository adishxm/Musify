package com.musify.browser.storage

import com.musify.browser.engine.DnsProtectionMode
import com.musify.browser.engine.DnsProtectionState
import java.io.File

/**
 * DnsSettingsRepository manages AdGuard DNS guidance and configuration status.
 *
 * Architecture Guarantee:
 * - AdGuard DNS operates at the operating system network / DNS-over-TLS (DoT) layer.
 * - Browser Shields operates at the in-engine HTTP / DOM request layer.
 * - Status and counters are strictly segregated to avoid false ad-blocking attribution.
 */
interface DnsSettingsRepository {
    fun getDnsProtectionState(): DnsProtectionState
    fun setDnsMode(mode: DnsProtectionMode, host: String = ADGUARD_STANDARD_HOST)
    fun resetToDefault()

    companion object {
        const val ADGUARD_STANDARD_HOST = "dns.adguard-dns.com"
        const val ADGUARD_UNFILTERED_HOST = "unfiltered.adguard-dns.com"
    }
}

class LocalDnsSettingsRepository(private val storageFile: File? = null) : DnsSettingsRepository {

    private var currentMode: DnsProtectionMode = DnsProtectionMode.SYSTEM_DEFAULT
    private var currentHost: String = DnsSettingsRepository.ADGUARD_STANDARD_HOST

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun getDnsProtectionState(): DnsProtectionState {
        val isActive = currentMode != DnsProtectionMode.SYSTEM_DEFAULT
        val description = when (currentMode) {
            DnsProtectionMode.SYSTEM_DEFAULT -> "System Default DNS (Unfiltered or network assigned)"
            DnsProtectionMode.ADGUARD_PRIVATE_DNS -> "AdGuard Private DNS (DoT active: $currentHost)"
            DnsProtectionMode.CUSTOM_DNS -> "Custom DNS-over-TLS Provider ($currentHost)"
        }
        return DnsProtectionState(
            mode = currentMode,
            configuredHost = currentHost,
            isActive = isActive,
            description = description
        )
    }

    @Synchronized
    override fun setDnsMode(mode: DnsProtectionMode, host: String) {
        currentMode = mode
        currentHost = if (host.isNotBlank()) host.trim() else DnsSettingsRepository.ADGUARD_STANDARD_HOST
        saveToDisk()
    }

    @Synchronized
    override fun resetToDefault() {
        currentMode = DnsProtectionMode.SYSTEM_DEFAULT
        currentHost = DnsSettingsRepository.ADGUARD_STANDARD_HOST
        saveToDisk()
    }

    private fun loadFromDisk() {
        val file = storageFile ?: return
        if (!file.exists()) return
        try {
            val lines = file.readLines()
            if (lines.isNotEmpty()) {
                val modeStr = lines[0].trim()
                currentMode = try {
                    DnsProtectionMode.valueOf(modeStr)
                } catch (_: Exception) {
                    DnsProtectionMode.SYSTEM_DEFAULT
                }
            }
            if (lines.size > 1 && lines[1].isNotBlank()) {
                currentHost = lines[1].trim()
            }
        } catch (_: Exception) {
            // Fallback to defaults on read error
        }
    }

    private fun saveToDisk() {
        val file = storageFile ?: return
        try {
            file.writeText("${currentMode.name}\n$currentHost\n")
        } catch (_: Exception) {
            // Best effort persistence
        }
    }
}
