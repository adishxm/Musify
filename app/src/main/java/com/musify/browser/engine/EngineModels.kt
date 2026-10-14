package com.musify.browser.engine

enum class EngineType {
    GECKO_VIEW,
    WEB_VIEW
}

enum class ContentMode {
    MOBILE,
    DESKTOP
}

enum class CapabilityStatus {
    UNKNOWN,
    SUPPORTED,
    UNSUPPORTED,
    FAILED
}

enum class DnsProtectionMode {
    SYSTEM_DEFAULT,
    ADGUARD_PRIVATE_DNS,
    CUSTOM_DNS
}

data class DnsProtectionState(
    val mode: DnsProtectionMode = DnsProtectionMode.SYSTEM_DEFAULT,
    val configuredHost: String = "dns.adguard-dns.com",
    val isActive: Boolean = false,
    val description: String = "Operating via Android system DNS resolution"
)

data class MediaCapabilityState(
    val encryptedMediaStatus: CapabilityStatus = CapabilityStatus.UNKNOWN,
    val widevineStatus: CapabilityStatus = CapabilityStatus.UNKNOWN,
    val isDesktopMode: Boolean = false,
    val isPrivateMode: Boolean = false,
    val isAutoplayAllowed: Boolean = true,
    val cookiePolicy: String = "Accept Non-Trackers & Isolate",
    val lastMediaFailureReason: String? = null,
    val deviceDrmProvider: String? = null,
    val engineDetails: String? = null,
    val isOriginExceptionActive: Boolean = false,
    val liveBlockCategory: String? = null
) {
    val isEncryptedMediaSupported: Boolean
        get() = encryptedMediaStatus == CapabilityStatus.SUPPORTED

    val isWidevineSupported: Boolean
        get() = widevineStatus == CapabilityStatus.SUPPORTED
}

data class EngineSecurityState(
    val isSecure: Boolean = false,
    val protocol: String = "HTTP",
    val host: String? = null,
    val tlsError: String? = null,
    val isBypassAllowed: Boolean = false
)

data class EngineNavigationState(
    val url: String = "about:blank",
    val title: String = "",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false
)

data class EngineShieldState(
    val blockedCount: Int = 0,
    val lastBlockedUrl: String? = null,
    val lastBlockCategory: String? = null,
    val isProtectionEnabled: Boolean = true,
    val isOriginException: Boolean = false,
    val blockedAds: Int = 0,
    val blockedSocial: Int = 0,
    val blockedCryptominers: Int = 0,
    val blockedFingerprinters: Int = 0
)

data class EngineProcessState(
    val isHealthy: Boolean = true,
    val hasCrashed: Boolean = false,
    val crashReason: String? = null,
    val recoveryCount: Int = 0
)

data class DownloadEvent(
    val url: String,
    val userAgent: String?,
    val contentDisposition: String?,
    val mimeType: String?,
    val contentLength: Long
)
