package com.musify.browser.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream

/**
 * WebViewAdapter: Bounded prototype, comparison and fallback adapter.
 *
 * Implements strict security policies:
 * - Cancels invalid TLS certificates unconditionally (zero SSL proceed).
 * - Disables dangerous file and content access.
 * - Forbids native JavaScript bridges.
 * - Handles renderer process death gracefully without crash loops.
 */
class WebViewAdapter(val isPrivate: Boolean = false) : EngineAdapter {
    override val engineType: EngineType = EngineType.WEB_VIEW

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

    private val _mediaCapabilityState = MutableStateFlow(
        MediaCapabilityState(
            isEncryptedMediaSupported = true,
            isWidevineSupported = false,
            isDesktopMode = false,
            isPrivateMode = isPrivate,
            isAutoplayAllowed = true,
            cookiePolicy = "Per-site sandbox",
            lastMediaFailureReason = null
        )
    )
    override val mediaCapabilityState: StateFlow<MediaCapabilityState> = _mediaCapabilityState.asStateFlow()

    private var webView: WebView? = null
    private var isInitialized = false

    // Known tracker indicators for the WebView fallback spike demonstration
    private val blockedTrackerKeywords = listOf(
        "google-analytics.com",
        "doubleclick.net",
        "googlesyndication.com",
        "analytics.twitter.com",
        "connect.facebook.net",
        "scorecardresearch.com",
        "adnxs.com",
        "criteo.com",
        "hotjar.com",
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "youtube.com/pagead",
        "youtube.com/api/stats/ads",
        "youtube.com/get_midroll_info",
        "youtube.com/ptracking",
        "s0.2mdn.net",
        "s.youtube.com"
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun createView(context: Context): View {
        val view = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                // Strict sandbox settings
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                if (_contentMode.value == ContentMode.DESKTOP) {
                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    val current = url ?: "about:blank"
                    val uri = try { Uri.parse(current) } catch (_: Exception) { null }
                    val isHttps = uri?.scheme?.equals("https", ignoreCase = true) == true

                    _navigationState.value = _navigationState.value.copy(
                        url = current,
                        isLoading = true,
                        progress = 15,
                        canGoBack = view?.canGoBack() ?: false,
                        canGoForward = view?.canGoForward() ?: false
                    )

                    _securityState.value = _securityState.value.copy(
                        isSecure = isHttps,
                        protocol = uri?.scheme?.uppercase() ?: "UNKNOWN",
                        host = uri?.host,
                        tlsError = null,
                        isBypassAllowed = false // Strict security gate: never allow bypass
                    )
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    _navigationState.value = _navigationState.value.copy(
                        isLoading = false,
                        progress = 100,
                        canGoBack = view?.canGoBack() ?: false,
                        canGoForward = view?.canGoForward() ?: false
                    )

                    if (url?.contains("youtube.com", ignoreCase = true) == true && _shieldState.value.isProtectionEnabled) {
                        injectYouTubeAdBlockerScript(view)
                    }
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    // Security Gate: NEVER call handler?.proceed()
                    handler?.cancel()

                    val errorDescription = when (error?.primaryError) {
                        SslError.SSL_UNTRUSTED -> "Untrusted certificate authority"
                        SslError.SSL_EXPIRED -> "Certificate has expired"
                        SslError.SSL_IDMISMATCH -> "Hostname mismatch"
                        SslError.SSL_NOTYETVALID -> "Certificate is not yet valid"
                        else -> "SSL verification failure"
                    }

                    _securityState.value = _securityState.value.copy(
                        isSecure = false,
                        tlsError = errorDescription,
                        isBypassAllowed = false
                    )
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    if (!_shieldState.value.isProtectionEnabled) {
                        return super.shouldInterceptRequest(view, request)
                    }

                    val requestUrl = request?.url?.toString().orEmpty()
                    val host = request?.url?.host.orEmpty()

                    // Preserve required media, authentication, and licensing flows
                    val isPreservedMediaHost = host.contains("spotify.com") || host.contains("scdn.co") || host.contains("spotifycdn.com")
                    if (isPreservedMediaHost) {
                        return super.shouldInterceptRequest(view, request)
                    }

                    val matchesTracker = blockedTrackerKeywords.any { host.contains(it, ignoreCase = true) }
                    if (matchesTracker) {
                        _shieldState.value = _shieldState.value.copy(
                            blockedCount = _shieldState.value.blockedCount + 1,
                            lastBlockedUrl = requestUrl,
                            lastBlockCategory = "Known Third-Party Tracker"
                        )
                        // Return empty response (204 No Content equivalent)
                        return WebResourceResponse(
                            "text/plain",
                            "UTF-8",
                            200,
                            "OK",
                            mapOf("Content-Type" to "text/plain"),
                            ByteArrayInputStream(ByteArray(0))
                        )
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    val didCrash = detail?.didCrash() ?: false
                    val reason = if (didCrash) {
                        "WebView render process crashed"
                    } else {
                        "WebView render process was killed by the system"
                    }

                    _processState.value = _processState.value.copy(
                        isHealthy = false,
                        hasCrashed = true,
                        crashReason = reason
                    )

                    // Return true to indicate host app handled the condition
                    return true
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    _navigationState.value = _navigationState.value.copy(progress = newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    _navigationState.value = _navigationState.value.copy(title = title ?: "")
                }
            }
        }

        webView = view
        isInitialized = true
        return view
    }

    override fun loadUrl(url: String) {
        val targetUrl = when {
            url.startsWith("about:") || url.startsWith("data:") || url.startsWith("file:") -> url
            !url.startsWith("http://") && !url.startsWith("https://") -> "https://$url"
            else -> url
        }
        webView?.loadUrl(targetUrl)
    }

    override fun goBack(): Boolean {
        return if (webView?.canGoBack() == true) {
            webView?.goBack()
            true
        } else {
            false
        }
    }

    override fun goForward(): Boolean {
        return if (webView?.canGoForward() == true) {
            webView?.goForward()
            true
        } else {
            false
        }
    }

    override fun reload() {
        webView?.reload()
    }

    override fun stop() {
        webView?.stopLoading()
    }

    override fun recoverRenderer() {
        _processState.value = EngineProcessState(
            isHealthy = true,
            hasCrashed = false,
            crashReason = null,
            recoveryCount = _processState.value.recoveryCount + 1
        )
        val currentUrl = _navigationState.value.url.takeIf { it.isNotBlank() && it != "about:blank" } ?: "https://example.com"
        webView?.loadUrl(currentUrl)
    }

    override fun setProtectionEnabled(enabled: Boolean) {
        _shieldState.value = _shieldState.value.copy(isProtectionEnabled = enabled)
    }

    override fun setContentMode(mode: ContentMode) {
        _contentMode.value = mode
        val isDesktop = mode == ContentMode.DESKTOP
        webView?.settings?.let { s ->
            s.userAgentString = if (isDesktop) {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
            } else {
                null
            }
            s.useWideViewPort = isDesktop
            s.loadWithOverviewMode = isDesktop
        }
        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
            isDesktopMode = isDesktop
        )
    }

    override fun getMediaCapabilityState(): MediaCapabilityState {
        return _mediaCapabilityState.value
    }

    override fun simulateRendererLoss() {
        _processState.value = _processState.value.copy(
            isHealthy = false,
            hasCrashed = true,
            crashReason = "Simulated WebView renderer loss for recovery verification"
        )
    }

    private fun injectYouTubeAdBlockerScript(view: WebView?) {
        val js = """
            (function() {
                if (window.__musifyAdBlockerInjected) return;
                window.__musifyAdBlockerInjected = true;
                
                function cleanYouTubeAds() {
                    var selectors = [
                        '.video-ads', '.ytp-ad-module', '.ytp-ad-overlay-container',
                        'ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer',
                        '.ad-container', '.ad-div', 'ytm-statement-banner-renderer',
                        'ytm-banner-promo-renderer', '.ytp-ad-action-interstitial',
                        'ytm-promoted-video-renderer'
                    ];
                    selectors.forEach(function(sel) {
                        var els = document.querySelectorAll(sel);
                        els.forEach(function(el) { el.style.setProperty('display', 'none', 'important'); });
                    });
                    
                    var skipBtns = document.querySelectorAll('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button, .ytp-ad-skip-button-slot');
                    skipBtns.forEach(function(btn) { btn.click(); });

                    var video = document.querySelector('video');
                    var adShowing = document.querySelector('.ad-interrupting, .html5-ad-product');
                    if (video && adShowing && video.duration > 0) {
                        video.currentTime = video.duration;
                    }
                }
                
                setInterval(cleanYouTubeAds, 500);
            })();
        """.trimIndent()
        view?.evaluateJavascript(js, null)
    }

    override fun destroy() {
        webView?.apply {
            stopLoading()
            clearHistory()
            destroy()
        }
        webView = null
        isInitialized = false
    }
}
