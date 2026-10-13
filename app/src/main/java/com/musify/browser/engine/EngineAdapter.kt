package com.musify.browser.engine

import android.content.Context
import android.view.View
import kotlinx.coroutines.flow.StateFlow

/**
 * EngineAdapter abstracts the web engine away from the presentation layer.
 * This guarantees UI code is decoupled from concrete engine implementations (GeckoView vs WebView).
 */
interface EngineAdapter {
    val engineType: EngineType

    val navigationState: StateFlow<EngineNavigationState>
    val securityState: StateFlow<EngineSecurityState>
    val shieldState: StateFlow<EngineShieldState>
    val processState: StateFlow<EngineProcessState>
    val contentMode: StateFlow<ContentMode>
        get() = kotlinx.coroutines.flow.MutableStateFlow(ContentMode.MOBILE)
    val mediaCapabilityState: StateFlow<MediaCapabilityState>
        get() = kotlinx.coroutines.flow.MutableStateFlow(MediaCapabilityState())

    fun createView(context: Context): View
    fun loadUrl(url: String)
    fun goBack(): Boolean
    fun goForward(): Boolean
    fun reload()
    fun stop()
    fun recoverRenderer()
    fun setProtectionEnabled(enabled: Boolean)
    fun setOriginException(isException: Boolean) {}
    fun resetPageShieldCount() {}
    fun setContentMode(mode: ContentMode) {}
    fun getMediaCapabilityState(): MediaCapabilityState = mediaCapabilityState.value
    fun simulateRendererLoss()
    fun destroy()
}
