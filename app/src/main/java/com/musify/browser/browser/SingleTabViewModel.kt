package com.musify.browser.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.GeckoEngineAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SingleTabViewModel(
    val adapter: EngineAdapter = GeckoEngineAdapter()
) : ViewModel() {

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
        }.launchIn(viewModelScope)

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
        }.launchIn(viewModelScope)

        // Observe shield counters
        adapter.shieldState.onEach { shield ->
            _tabState.value = _tabState.value.copy(shieldState = shield)
        }.launchIn(viewModelScope)

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
        }.launchIn(viewModelScope)
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

    override fun onCleared() {
        super.onCleared()
        adapter.destroy()
    }
}
