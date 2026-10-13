package com.musify.browser.engine

enum class EngineType {
    GECKO_VIEW,
    WEB_VIEW
}

enum class ContentMode {
    MOBILE,
    DESKTOP
}

data class MediaCapabilityState(
    val isEncryptedMediaSupported: Boolean = true,
    val isWidevineSupported: Boolean = true,
    val isDesktopMode: Boolean = false,
    val isPrivateMode: Boolean = false,
    val isAutoplayAllowed: Boolean = true,
    val cookiePolicy: String = "Accept Non-Trackers & Isolate",
    val lastMediaFailureReason: String? = null
)

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
