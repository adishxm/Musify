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
            encryptedMediaStatus = CapabilityStatus.UNKNOWN,
            widevineStatus = CapabilityStatus.UNSUPPORTED,
            isDesktopMode = false,
            isPrivateMode = isPrivate,
            isAutoplayAllowed = true,
            cookiePolicy = "Accept Non-Trackers & Isolate",
            lastMediaFailureReason = "Standard Android WebView does not expose full Widevine CDM to arbitrary web content without custom system integration",
            deviceDrmProvider = "System WebView (Widevine restricted)",
            engineDetails = "System WebView ${Build.VERSION.RELEASE}"
        )
    )
    override val mediaCapabilityState: StateFlow<MediaCapabilityState> = _mediaCapabilityState.asStateFlow()

    private var webView: WebView? = null
    private var isInitialized = false
    private var pendingUrl: String? = null

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

    private val spotifyAdKeywords = listOf(
        "adeventtracker.spotify.com",
        "adstudio.spotify.com",
        "ads.spotify.com",
        "pixel.spotify.com",
        "analytics.spotify.com",
        "/ad-logic/",
        "/ads/",
        "/v1/ads",
        "/desktop-interstitial/",
        "spclient.wg.spotify.com/ad-logic",
        "spclient.wg.spotify.com/ads",
        "spclient.wg.spotify.com/v1/ads"
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
                    if (url?.contains("spotify.com", ignoreCase = true) == true && _shieldState.value.isProtectionEnabled) {
                        injectSpotifyAdBlockerScript(view)
                    }
                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    if (url?.contains("spotify.com", ignoreCase = true) == true && _shieldState.value.isProtectionEnabled) {
                        injectSpotifyAdBlockerScript(view)
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString()?.lowercase() ?: return false
                    // Block deep-linking to external Spotify app or app store prompts
                    if (url.startsWith("spotify:") ||
                        url.startsWith("intent:") ||
                        url.contains("spotify.link") ||
                        url.contains("play.google.com/store/apps/details?id=com.spotify.music")
                    ) {
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, request)
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

                    // Safeguard: Never block protected media, Widevine DRM licenses, or manifests
                    if (MediaSafeResourcePolicy.isMediaOrLicenseRequest(requestUrl)) {
                        return super.shouldInterceptRequest(view, request)
                    }

                    // Intercept Spotify Audio & Visual Ad networks (checked BEFORE isFirstParty bypass)
                    val isSpotifyAd = spotifyAdKeywords.any { kw ->
                        requestUrl.contains(kw, ignoreCase = true) || host.contains(kw, ignoreCase = true)
                    }
                    if (isSpotifyAd) {
                        _shieldState.value = _shieldState.value.copy(
                            blockedCount = _shieldState.value.blockedCount + 1,
                            lastBlockedUrl = MediaSafeResourcePolicy.sanitizeUrlForDiagnostics(requestUrl),
                            lastBlockCategory = "Spotify Advertisement"
                        )
                        // Anti-Freeze Response: For spclient ad endpoints, synthesize a 200 OK empty JSON `{}`
                        // to satisfy Spotify's state machine and prevent playback freeze loop.
                        val isSpclientAdLogic = requestUrl.contains("spclient.wg.spotify.com/ad-logic", ignoreCase = true) ||
                                requestUrl.contains("spclient.wg.spotify.com/ads", ignoreCase = true) ||
                                requestUrl.contains("spclient.wg.spotify.com/v1/ads", ignoreCase = true)

                        return if (isSpclientAdLogic) {
                            WebResourceResponse(
                                "application/json",
                                "UTF-8",
                                200,
                                "OK",
                                mapOf("Access-Control-Allow-Origin" to "*", "Content-Type" to "application/json"),
                                ByteArrayInputStream("{}".toByteArray(Charsets.UTF_8))
                            )
                        } else {
                            WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                204,
                                "No Content",
                                mapOf("Content-Type" to "text/plain"),
                                ByteArrayInputStream(ByteArray(0))
                            )
                        }
                    }

                    if (!_shieldState.value.isProtectionEnabled || _shieldState.value.isOriginException) {
                        return super.shouldInterceptRequest(view, request)
                    }

                    // Safeguard: Never block protected media, Widevine DRM licenses, or manifests
                    if (MediaSafeResourcePolicy.isMediaOrLicenseRequest(requestUrl)) {
                        return super.shouldInterceptRequest(view, request)
                    }

                    // Preserve first-party, top-level main frame, and essential application resources
                    val currentUrl = _navigationState.value.url
                    if (request?.isForMainFrame == true || MediaSafeResourcePolicy.isFirstPartyOrAuth(requestUrl, currentUrl)) {
                        return super.shouldInterceptRequest(view, request)
                    }

                    val matchesTracker = blockedTrackerKeywords.any { host.contains(it, ignoreCase = true) }
                    if (matchesTracker) {
                        val sanitizedUrl = MediaSafeResourcePolicy.sanitizeUrlForDiagnostics(requestUrl)
                        _shieldState.value = _shieldState.value.copy(
                            blockedCount = _shieldState.value.blockedCount + 1,
                            lastBlockedUrl = sanitizedUrl,
                            lastBlockCategory = "Known Third-Party Tracker"
                        )
                        _mediaCapabilityState.value = _mediaCapabilityState.value.copy(
                            liveBlockCategory = "Known Third-Party Tracker"
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

        val pending = pendingUrl
        if (!pending.isNullOrBlank() && pending != "about:blank") {
            pendingUrl = null
            view.loadUrl(pending)
        }

        return view
    }

    override fun loadUrl(url: String) {
        val targetUrl = when {
            url.startsWith("about:") || url.startsWith("data:") || url.startsWith("file:") -> url
            !url.startsWith("http://") && !url.startsWith("https://") -> "https://$url"
            else -> url
        }
        val wv = webView
        if (wv != null) {
            wv.loadUrl(targetUrl)
        } else {
            pendingUrl = targetUrl
        }
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

    private fun injectSpotifyAdBlockerScript(view: WebView?) {
        val assetScript = try {
            view?.context?.assets?.open("musify_adblocker/spotify_adblock.js")?.bufferedReader()?.use { it.readText() }
        } catch (_: Throwable) { null }

        if (!assetScript.isNullOrBlank()) {
            view?.evaluateJavascript(assetScript, null)
            return
        }

        val js = """
            (function() {
                if (window.__musifySpotifyBlockerInjected) return;
                window.__musifySpotifyBlockerInjected = true;

                function isSpotifyAdActive() {
                    if (document.querySelector('[aria-label="Advertisement"], [data-testid="ad-companion-banner"], [data-testid="ad-iframe"]')) return true;
                    var nowPlaying = document.querySelector('[data-testid="now-playing-widget"]');
                    if (nowPlaying) {
                        var text = nowPlaying.innerText || "";
                        if (text.indexOf("Advertisement") !== -1 || text.indexOf("Spotify Premium") !== -1) return true;
                    }
                    var adHeading = Array.from(document.querySelectorAll('h1, h2, h3, header, p, span, div')).some(function(el) {
                        return el.childNodes.length === 1 && el.textContent.trim() === "Advertisement";
                    });
                    if (adHeading) return true;
                    return false;
                }

                function cleanSpotifyAds() {
                    // 1. Hide visual ad banners, companion iframes, interstitials, and app install nags
                    var selectors = [
                        '[data-testid="ad-companion-banner"]',
                        '[data-testid="ad-iframe"]',
                        '.Root__ads-container',
                        '[aria-label="Advertisement"]',
                        '[data-testid="interstitial-ad"]',
                        'div[data-testid="advertisement-row"]',
                        'aside[aria-label="Upgrade to Premium"]',
                        '[class*="AdsContainer"]',
                        '[aria-label="Sponsored"]',
                        'div[data-test="upsell-container"]',
                        '#ad-leaderboard',
                        '#leaderboard',
                        '.main-leaderboard-container',
                        '.desktop-interstitial',
                        '.upsell-banner',
                        '[data-testid="track-info-advertiser"]',
                        '.advertisement',
                        '.sponsored-playlist',
                        'div[data-testid="top-banner-ad"]',
                        'div[data-testid="in-feed-ad"]',
                        'div[data-testid="tracklist-row-ad"]',
                        'div[data-testid="billboard-ad"]',
                        'div[data-testid="leaderboard-ad"]',
                        'div[data-testid="mpu-ad"]',
                        'div[data-testid="sponsored-header"]',
                        'div[data-testid="sponsored-card"]',
                        '[data-testid="open-app-button"]',
                        '[data-testid="install-app-button"]',
                        '[data-testid="smart-banner-pitch"]',
                        '[data-testid="banner-open-button"]',
                        '[data-testid="mobile-app-banner"]',
                        '[data-testid="smart-banner"]',
                        '[data-testid*="open-app"]',
                        '[data-testid*="install-app"]',
                        '[aria-label="Open App"]',
                        '[aria-label="Open in app"]',
                        '[aria-label="Open in Spotify"]',
                        '[aria-label="Install App"]',
                        '[aria-label*="Install the app"]',
                        '[aria-label*="Install Spotify"]',
                        'a[href^="spotify:"]',
                        'a[href*="spotify.link"]',
                        'a[href*="spotify.com/download"]'
                    ];
                    selectors.forEach(function(sel) {
                        var els = document.querySelectorAll(sel);
                        els.forEach(function(el) { el.style.setProperty('display', 'none', 'important'); });
                    });

                    // Hide any button or link whose text is 'Open App', 'Install App', 'Get the app'
                    var clickables = document.querySelectorAll('button, a, div[role="button"], span');
                    clickables.forEach(function(el) {
                        var testId = el.getAttribute('data-testid') || '';
                        if (testId.indexOf('control') !== -1 || testId.indexOf('play') !== -1 || testId.indexOf('pause') !== -1 || testId.indexOf('skip') !== -1) {
                            return;
                        }
                        var txt = (el.textContent || '').trim().toLowerCase();
                        if (
                            txt === 'open app' ||
                            txt === 'open in app' ||
                            txt === 'open in spotify' ||
                            txt === 'install app' ||
                            txt === 'install spotify' ||
                            txt === 'get the app' ||
                            txt === 'get app' ||
                            txt === 'switch to the app'
                        ) {
                            var target = el.closest('button, a, div[role="button"]') || el;
                            target.style.setProperty('display', 'none', 'important');
                        }
                    });

                    // 2. Dismiss or close modal dialogues promoting ads / premium / app install
                    var modals = document.querySelectorAll('[data-testid="modal-container"], div[role="dialog"]');
                    modals.forEach(function(modal) {
                        var text = (modal.innerText || "").toLowerCase();
                        if (text.indexOf("spotify premium") !== -1 || text.indexOf("advertisement") !== -1 || text.indexOf("get premium") !== -1 || text.indexOf("open app") !== -1 || text.indexOf("install app") !== -1) {
                            var closeBtn = modal.querySelector('button[aria-label="Close"], button[aria-label="Dismiss"], button[title="Close"]');
                            if (closeBtn) {
                                try { closeBtn.click(); } catch(e) {}
                            } else {
                                modal.style.setProperty('display', 'none', 'important');
                            }
                        }
                    });
                }

                setInterval(cleanSpotifyAds, 500);
                cleanSpotifyAds();
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
