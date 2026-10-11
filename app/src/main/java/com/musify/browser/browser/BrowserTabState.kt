package com.musify.browser.browser

import com.musify.browser.engine.EngineProcessState
import com.musify.browser.engine.EngineSecurityState
import com.musify.browser.engine.EngineShieldState

data class BrowserErrorState(
    val title: String,
    val message: String,
    val failingUrl: String,
    val isTlsError: Boolean = false,
    val isRejectedScheme: Boolean = false,
    val canRetry: Boolean = true
)

data class BrowserTabState(
    val currentUrl: String = "about:blank",
    val originDisplay: String = "",
    val host: String = "",
    val isHttps: Boolean = false,
    val isPunycodeAlert: Boolean = false,
    val title: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val securityState: EngineSecurityState = EngineSecurityState(),
    val shieldState: EngineShieldState = EngineShieldState(),
    val processState: EngineProcessState = EngineProcessState(),
    val errorState: BrowserErrorState? = null
)
