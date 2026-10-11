package com.musify.browser.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.GeckoEngineAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SingleTabViewModel(
    val adapter: EngineAdapter = GeckoEngineAdapter(),
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: runCatching { viewModelScope }.getOrElse {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    private val _tabState = MutableStateFlow(BrowserTabState())
    val tabState: StateFlow<BrowserTabState> = _tabState.asStateFlow()

    init {
        // Observe navigation changes
        adapter.navigationState.onEach { nav ->
            val parsed = UrlParser.parse(nav.url)
            val origin = if (parsed is ParsedNavigation.AllowedUrl) parsed.origin else ""
            val host = if (parsed is ParsedNavigation.AllowedUrl) parsed.host else ""
            val isHttps = if (parsed is ParsedNavigation.AllowedUrl) parsed.isHttps else false
            val isPunycode = if (parsed is ParsedNavigation.AllowedUrl) parsed.isPunycode else false

            _tabState.value = _tabState.value.copy(
                currentUrl = nav.url,
                originDisplay = origin,
                host = host,
                isHttps = isHttps,
                isPunycodeAlert = isPunycode,
                title = nav.title,
                isLoading = nav.isLoading,
                progress = nav.progress,
                canGoBack = nav.canGoBack,
                canGoForward = nav.canGoForward
            )
        }.launchIn(scope)

        // Observe security changes
        adapter.securityState.onEach { sec ->
            _tabState.value = _tabState.value.copy(
                securityState = sec,
                errorState = if (!sec.isSecure && sec.tlsError != null) {
                    BrowserErrorState(
                        title = "Security Alert: Invalid Certificate",
                        message = sec.tlsError ?: "Untrusted connection",
                        failingUrl = _tabState.value.currentUrl,
                        isTlsError = true,
                        canRetry = false
                    )
                } else {
                    _tabState.value.errorState?.takeIf { !it.isTlsError }
                }
            )
        }.launchIn(scope)

        // Observe shield counters
        adapter.shieldState.onEach { shield ->
            _tabState.value = _tabState.value.copy(shieldState = shield)
        }.launchIn(scope)

        // Observe process health
        adapter.processState.onEach { proc ->
            _tabState.value = _tabState.value.copy(
                processState = proc,
                errorState = if (proc.hasCrashed) {
                    BrowserErrorState(
                        title = "Render Process Lost",
                        message = proc.crashReason ?: "Process terminated abnormally",
                        failingUrl = _tabState.value.currentUrl,
                        canRetry = true
                    )
                } else {
                    _tabState.value.errorState?.takeIf { it.title != "Render Process Lost" }
                }
            )
        }.launchIn(scope)
    }

    fun navigate(rawInput: String) {
        when (val result = UrlParser.parse(rawInput)) {
            is ParsedNavigation.AllowedUrl -> {
                _tabState.value = _tabState.value.copy(errorState = null)
                adapter.loadUrl(result.url)
            }
            is ParsedNavigation.Search -> {
                _tabState.value = _tabState.value.copy(errorState = null)
                adapter.loadUrl(result.searchUrl)
            }
            is ParsedNavigation.Rejected -> {
                _tabState.value = _tabState.value.copy(
                    errorState = BrowserErrorState(
                        title = "Navigation Blocked",
                        message = result.reason,
                        failingUrl = result.rawInput,
                        isRejectedScheme = true,
                        canRetry = false
                    )
                )
            }
        }
    }

    fun goBack(): Boolean = adapter.goBack()

    fun goForward(): Boolean = adapter.goForward()

    fun reload() {
        _tabState.value = _tabState.value.copy(errorState = null)
        adapter.reload()
    }

    fun stop() = adapter.stop()

    fun recoverSession() {
        _tabState.value = _tabState.value.copy(errorState = null)
        adapter.recoverRenderer()
    }

    fun toggleShields(enabled: Boolean) {
        adapter.setProtectionEnabled(enabled)
    }

    fun dismissError() {
        _tabState.value = _tabState.value.copy(errorState = null)
    }

    fun loadTestFixture() {
        _tabState.value = _tabState.value.copy(errorState = null)
        adapter.loadUrl("file:///android_asset/test_fixture.html")
    }

    fun handleNetworkError(
        failingUrl: String = _tabState.value.currentUrl,
        message: String = "Unable to connect to the host. Please check your network connection."
    ) {
        _tabState.value = _tabState.value.copy(
            errorState = BrowserErrorState(
                title = "Network Connection Error",
                message = message,
                failingUrl = failingUrl,
                canRetry = true
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        adapter.destroy()
    }
}
