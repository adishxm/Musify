package com.musify.browser.engine

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.ContentBlocking
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

    private var geckoSession: GeckoSession? = null
    private var geckoView: GeckoView? = null
    private var isInitialized = false

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
                            .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY)
                            .build()
                    )
                    .build()
                sharedRuntime = GeckoRuntime.create(context.applicationContext, settings)
            }
            return sharedRuntime!!
        }
    }

    private fun ensureRuntime(context: Context): GeckoRuntime {
        return getSharedRuntime(context)
    }

    private fun initSession(context: Context) {
        val runtime = ensureRuntime(context)
        val sessionSettings = GeckoSessionSettings.Builder()
            .usePrivateMode(isPrivate)
            .build()
        val session = GeckoSession(sessionSettings)

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
                val isSecure = securityInfo.isSecure
                val host = securityInfo.host
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
                    _shieldState.value = _shieldState.value.copy(
                        blockedCount = count,
                        lastBlockedUrl = event.uri,
                        lastBlockCategory = category,
                        blockedAds = ads,
                        blockedSocial = social,
                        blockedCryptominers = crypto,
                        blockedFingerprinters = finger
                    )
                }
            }
        }

        session.open(runtime)
        geckoSession = session
        isInitialized = true
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
        geckoSession?.loadUri(targetUrl)
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
        val antiTracking = if (enabled) ContentBlocking.AntiTracking.STRICT else ContentBlocking.AntiTracking.DEFAULT
        sharedRuntime?.settings?.contentBlocking?.setAntiTracking(antiTracking)
    }

    override fun setOriginException(isException: Boolean) {
        _shieldState.value = _shieldState.value.copy(isOriginException = isException)
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
