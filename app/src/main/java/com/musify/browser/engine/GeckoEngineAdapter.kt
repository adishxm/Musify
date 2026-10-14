package com.musify.browser.engine

import android.content.Context
import android.media.MediaDrm
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

class GeckoEngineAdapter(val isPrivate: Boolean = false) : EngineAdapter {
    override val engineType: EngineType = EngineType.GECKO_VIEW

    private val _navigationState = MutableStateFlow(EngineNavigationState())
    override val navigationState: StateFlow<EngineNavigationState> = _navigationState.asStateFlow()

    private val _securityState = MutableStateFlow(EngineSecurityState())
    override val securityState: StateFlow<EngineSecurityState> = _securityState.asStateFlow()

    private val _shieldState = MutableStateFlow(EngineShieldState())
    override val shieldState: StateFlow<EngineShieldState> = _shieldState.asStateFlow()

    private val _processState = MutableStateFlow(EngineProcessState())
    override val processState: StateFlow<EngineProcessState> = _processState.asStateFlow()

    private val _contentMode = MutableStateFlow(ContentMode.MOBILE)
    override val contentMode: StateFlow<ContentMode> = _contentMode.asStateFlow()

    private fun probeWidevineDrm(): CapabilityStatus {
        return try {
            val widevineUuid = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")
            if (MediaDrm.isCryptoSchemeSupported(widevineUuid)) {
                CapabilityStatus.SUPPORTED
            } else {
                CapabilityStatus.UNSUPPORTED
            }
        } catch (_: Throwable) {
            CapabilityStatus.UNKNOWN
        }
    }

    private val _mediaCapabilityState = MutableStateFlow(
        MediaCapabilityState(
            encryptedMediaStatus = CapabilityStatus.UNKNOWN,
            widevineStatus = CapabilityStatus.UNKNOWN,
            isDesktopMode = false,
            isPrivateMode = isPrivate,
            isAutoplayAllowed = true,
            cookiePolicy = "Accept Non-Trackers & Total Cookie Isolation",
            lastMediaFailureReason = null,
            deviceDrmProvider = "Pending runtime probe",
            engineDetails = "GeckoView 135"
        )
    )
    override val mediaCapabilityState: StateFlow<MediaCapabilityState> = _mediaCapabilityState.asStateFlow()

    private var geckoSession: GeckoSession? = null
    private var geckoView: GeckoView? = null
    private var isInitialized = false
    private var pendingUrl: String? = null

    companion object {
        @Volatile
        private var sharedRuntime: GeckoRuntime? = null

        @Synchronized
        fun getSharedRuntime(context: Context): GeckoRuntime {
            if (sharedRuntime == null) {
                val settings = GeckoRuntimeSettings.Builder()
                    .contentBlocking(
                        ContentBlocking.Settings.Builder()
                            .antiTracking(ContentBlocking.AntiTracking.STRICT)
                            .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
                            .safeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
                            .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
                            .build()
                    )
                    .aboutConfigEnabled(true)
                    .build()
                val runtime = GeckoRuntime.create(context.applicationContext, settings)
                try {
                    runtime.webExtensionController
                        .ensureBuiltIn("resource://android/assets/musify_adblocker/", "shields@musify.browser")
                        .accept(
                            { extension ->
                                extension?.let {
                                    runtime.webExtensionController.setAllowedInPrivateBrowsing(it, true)
                                }
                            },
                            { error ->
                                android.util.Log.e("MusifyGecko", "Shields extension registration error: ${error?.message}")
                            }
                        )
                } catch (t: Throwable) {
                    android.util.Log.e("MusifyGecko", "Error registering shields extension", t)
                }
                sharedRuntime = runtime
            }
            return sharedRuntime!!
        }
    }

    private fun ensureRuntime(context: Context): GeckoRuntime {
        return getSharedRuntime(context)
    }

    private fun initSession(context: Context) {
        val runtime = ensureRuntime(context)
        val isDesktop = _contentMode.value == ContentMode.DESKTOP
        val sessionSettings = GeckoSessionSettings.Builder()
            .usePrivateMode(isPrivate)
            .userAgentMode(if (isDesktop) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP else GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .viewportMode(if (isDesktop) GeckoSessionSettings.VIEWPORT_MODE_DESKTOP else GeckoSessionSettings.VIEWPORT_MODE_MOBILE)
            .build()
        val session = GeckoSession(sessionSettings)
        if (isDesktop) {
            session.settings.userAgentOverride = "Mozilla/5.0 (X11; Linux x86_64; rv:135.0) Gecko/20100101 Firefox/135.0"
        }

        val widevineStatus = probeWidevineDrm()
        val drmProvider = when (widevineStatus) {
            CapabilityStatus.SUPPORTED -> "Google Widevine Modular DRM (Hardware/OS Verified)"
            CapabilityStatus.UNSUPPORTED -> "Widevine DRM Scheme not present on this device"
            else -> "Widevine DRM status unmeasured"
        }
        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
            widevineStatus = widevineStatus,
            encryptedMediaStatus = if (widevineStatus == CapabilityStatus.UNSUPPORTED) CapabilityStatus.UNSUPPORTED else CapabilityStatus.UNKNOWN,
            deviceDrmProvider = drmProvider,
            engineDetails = "GeckoView 135"
        )

        session.permissionDelegate = object : GeckoSession.PermissionDelegate {
            override fun onContentPermissionRequest(
                session: GeckoSession,
                perm: GeckoSession.PermissionDelegate.ContentPermission
            ): org.mozilla.geckoview.GeckoResult<Int> {
                if (perm.permission == GeckoSession.PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS) {
                    val currentWidevine = _mediaCapabilityState.value.widevineStatus
                    return if (currentWidevine == CapabilityStatus.UNSUPPORTED) {
                        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
                            encryptedMediaStatus = CapabilityStatus.FAILED,
                            lastMediaFailureReason = "Device lacks Widevine Modular CDM hardware/OS support"
                        )
                        org.mozilla.geckoview.GeckoResult.fromValue(
                            GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY
                        )
                    } else {
                        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
                            encryptedMediaStatus = CapabilityStatus.SUPPORTED,
                            lastMediaFailureReason = null
                        )
                        org.mozilla.geckoview.GeckoResult.fromValue(
                            GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
                        )
                    }
                }

                val isAllowedMediaOrStorage = when (perm.permission) {
                    GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE,
                    GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE,
                    GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE,
                    GeckoSession.PermissionDelegate.PERMISSION_STORAGE_ACCESS -> true
                    else -> false
                }
                return if (isAllowedMediaOrStorage) {
                    org.mozilla.geckoview.GeckoResult.fromValue(
                        GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
                    )
                } else {
                    org.mozilla.geckoview.GeckoResult.fromValue(
                        GeckoSession.PermissionDelegate.ContentPermission.VALUE_PROMPT
                    )
                }
            }
        }

        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                val current = url ?: "about:blank"
                val uri = try { Uri.parse(current) } catch (_: Exception) { null }
                val isHttps = uri?.scheme?.equals("https", ignoreCase = true) == true
                val host = uri?.host

                _navigationState.value = _navigationState.value.copy(url = current)
                _securityState.value = _securityState.value.copy(
                    isSecure = isHttps,
                    protocol = uri?.scheme?.uppercase() ?: "UNKNOWN",
                    host = host,
                    tlsError = null,
                    isBypassAllowed = false // Security Gate: never allow blanket bypass
                )
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                _navigationState.value = _navigationState.value.copy(canGoBack = canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                _navigationState.value = _navigationState.value.copy(canGoForward = canGoForward)
            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny>? {
                val uri = request.uri.lowercase()
                // Block deep-linking to external Spotify app or app store prompts
                if (uri.startsWith("spotify:") ||
                    uri.startsWith("intent:") ||
                    uri.contains("spotify.link") ||
                    uri.contains("play.google.com/store/apps/details?id=com.spotify.music")
                ) {
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                return null // Allow default GeckoView navigation
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                _navigationState.value = _navigationState.value.copy(
                    isLoading = true,
                    progress = 10,
                    url = url
                )
                resetPageShieldCount()
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                _navigationState.value = _navigationState.value.copy(
                    isLoading = false,
                    progress = 100
                )
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                _navigationState.value = _navigationState.value.copy(progress = progress)
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation
            ) {
                val isSecure = try { securityInfo.isSecure } catch (_: Throwable) { false }
                val host = try { securityInfo.host } catch (_: Throwable) { null }
                _securityState.value = _securityState.value.copy(
                    isSecure = isSecure,
                    host = host,
                    protocol = if (isSecure) "HTTPS" else "HTTP",
                    tlsError = if (!isSecure) "Insecure connection or certificate error" else null,
                    isBypassAllowed = false
                )
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _navigationState.value = _navigationState.value.copy(title = title ?: "")
            }

            override fun onCrash(session: GeckoSession) {
                handleProcessCrash("Gecko renderer process terminated abnormally (crash)")
            }

            override fun onKill(session: GeckoSession) {
                handleProcessCrash("Gecko process terminated by OS (low memory/kill)")
            }
        }

        session.contentBlockingDelegate = object : ContentBlocking.Delegate {
            override fun onContentBlocked(session: GeckoSession, event: ContentBlocking.BlockEvent) {
                if (_shieldState.value.isProtectionEnabled && !_shieldState.value.isOriginException) {
                    val uri = event.uri
                    // Safeguard: Never classify media streaming CDN or Widevine DRM license flows as trackers
                    if (MediaSafeResourcePolicy.isMediaOrLicenseRequest(uri)) {
                        return
                    }

                    val count = _shieldState.value.blockedCount + 1
                    var ads = _shieldState.value.blockedAds
                    var social = _shieldState.value.blockedSocial
                    var crypto = _shieldState.value.blockedCryptominers
                    var finger = _shieldState.value.blockedFingerprinters

                    val category = when (event.antiTrackingCategory) {
                        ContentBlocking.AntiTracking.AD,
                        ContentBlocking.AntiTracking.ANALYTIC -> {
                            ads++
                            "Advertising & Analytics Tracker"
                        }
                        ContentBlocking.AntiTracking.SOCIAL -> {
                            social++
                            "Cross-Site Social Tracker"
                        }
                        ContentBlocking.AntiTracking.CRYPTOMINING -> {
                            crypto++
                            "Cryptomining Script"
                        }
                        ContentBlocking.AntiTracking.FINGERPRINTING -> {
                            finger++
                            "Browser Fingerprinter"
                        }
                        else -> {
                            ads++
                            "Blocked Tracking Content"
                        }
                    }
                    val sanitizedUrl = MediaSafeResourcePolicy.sanitizeUrlForDiagnostics(uri)
                    _shieldState.value = _shieldState.value.copy(
                        blockedCount = count,
                        lastBlockedUrl = sanitizedUrl,
                        lastBlockCategory = category,
                        blockedAds = ads,
                        blockedSocial = social,
                        blockedCryptominers = crypto,
                        blockedFingerprinters = finger
                    )
                    _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
                        liveBlockCategory = category
                    )
                }
            }
        }

        session.open(runtime)
        geckoSession = session
        isInitialized = true

        val pending = pendingUrl
        if (!pending.isNullOrBlank() && pending != "about:blank") {
            pendingUrl = null
            session.loadUri(pending)
        }
    }

    private fun handleProcessCrash(reason: String) {
        _processState.value = _processState.value.copy(
            isHealthy = false,
            hasCrashed = true,
            crashReason = reason
        )
    }

    override fun createView(context: Context): View {
        if (!isInitialized) {
            initSession(context)
        }
        val view = GeckoView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setSession(geckoSession!!)
        }
        geckoView = view
        return view
    }

    override fun loadUrl(url: String) {
        val targetUrl = when {
            url.startsWith("about:") || url.startsWith("data:") || url.startsWith("file:") -> url
            !url.startsWith("http://") && !url.startsWith("https://") -> "https://$url"
            else -> url
        }
        val session = geckoSession
        if (session != null) {
            session.loadUri(targetUrl)
        } else {
            pendingUrl = targetUrl
        }
    }

    override fun goBack(): Boolean {
        if (_navigationState.value.canGoBack) {
            geckoSession?.goBack()
            return true
        }
        return false
    }

    override fun goForward(): Boolean {
        if (_navigationState.value.canGoForward) {
            geckoSession?.goForward()
            return true
        }
        return false
    }

    override fun reload() {
        geckoSession?.reload()
    }

    override fun stop() {
        geckoSession?.stop()
    }

    override fun recoverRenderer() {
        val count = _processState.value.recoveryCount + 1
        _processState.value = EngineProcessState(
            isHealthy = true,
            hasCrashed = false,
            crashReason = null,
            recoveryCount = count
        )
        geckoSession?.reload()
    }

    override fun setProtectionEnabled(enabled: Boolean) {
        _shieldState.value = _shieldState.value.copy(isProtectionEnabled = enabled)
    }

    override fun setOriginException(isException: Boolean) {
        _shieldState.value = _shieldState.value.copy(isOriginException = isException)
        val policyDesc = if (isException) "Permissive Session (Origin Exception)" else "Accept Non-Trackers & Isolate"
        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
            isOriginExceptionActive = isException,
            cookiePolicy = policyDesc
        )
    }

    override fun resetPageShieldCount() {
        _shieldState.value = _shieldState.value.copy(
            blockedCount = 0,
            lastBlockedUrl = null,
            lastBlockCategory = null,
            blockedAds = 0,
            blockedSocial = 0,
            blockedCryptominers = 0,
            blockedFingerprinters = 0
        )
        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
            liveBlockCategory = null
        )
    }

    override fun setContentMode(mode: ContentMode) {
        _contentMode.value = mode
        val isDesktop = mode == ContentMode.DESKTOP
        geckoSession?.let { session ->
            session.settings.userAgentMode = if (isDesktop) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP else GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            session.settings.viewportMode = if (isDesktop) GeckoSessionSettings.VIEWPORT_MODE_DESKTOP else GeckoSessionSettings.VIEWPORT_MODE_MOBILE
            session.settings.userAgentOverride = if (isDesktop) "Mozilla/5.0 (X11; Linux x86_64; rv:135.0) Gecko/20100101 Firefox/135.0" else null
        }
        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
            isDesktopMode = isDesktop
        )
    }

    override fun getMediaCapabilityState(): MediaCapabilityState {
        return _mediaCapabilityState.value
    }

    override fun simulateRendererLoss() {
        handleProcessCrash("Simulated renderer crash for lifecycle verification")
    }

    override fun destroy() {
        geckoView?.releaseSession()
        geckoSession?.close()
        geckoSession = null
        geckoView = null
        isInitialized = false
    }
}
