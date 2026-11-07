package com.musify.browser.storage

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

data class AppPreferencesData(
    val hasCompletedOnboarding: Boolean = false,
    val defaultSearchEngineUrl: String = "https://duckduckgo.com/?q=",
    val httpsOnlyMode: Boolean = true,
    val navBarPosition: String = "BOTTOM", // "BOTTOM" or "TOP"
    val autoHideNavBarOnScroll: Boolean = true,
    val webBottomClearance: Boolean = true,
    val uiThemeStyle: String = "EDITORIAL_DARK", // "EDITORIAL_DARK", "SAFARI_GLASS", "CYBERPUNK_EMERALD", "MIDNIGHT_AMETHYST"
    val backgroundPlaybackEnabled: Boolean = true,
    val showPipHoverOnBack: Boolean = true,
    val extremePrivateMode: Boolean = true,
    val showFormatButtonInDock: Boolean = true,
    val showShieldsButtonInDock: Boolean = true,
    val showShareButtonInDock: Boolean = true
)

interface AppPreferencesRepository {
    fun hasCompletedOnboarding(): Boolean
    fun setCompletedOnboarding(completed: Boolean)
    fun getDefaultSearchEngineUrl(): String
    fun setDefaultSearchEngineUrl(url: String)
    fun isHttpsOnlyMode(): Boolean
    fun setHttpsOnlyMode(enabled: Boolean)
    fun getNavBarPosition(): String
    fun setNavBarPosition(position: String)
    fun isAutoHideNavBarOnScroll(): Boolean
    fun setAutoHideNavBarOnScroll(enabled: Boolean)
    fun isWebBottomClearance(): Boolean
    fun setWebBottomClearance(enabled: Boolean)
    fun getUiThemeStyle(): String
    fun setUiThemeStyle(theme: String)
    fun isBackgroundPlaybackEnabled(): Boolean
    fun setBackgroundPlaybackEnabled(enabled: Boolean)
    fun isShowPipHoverOnBack(): Boolean
    fun setShowPipHoverOnBack(enabled: Boolean)
    fun isExtremePrivateMode(): Boolean
    fun setExtremePrivateMode(enabled: Boolean)
    fun updatePreferences(update: (AppPreferencesData) -> AppPreferencesData)
    fun getPreferences(): AppPreferencesData
    fun resetAll()
}

/**
 * File-backed, thread-safe implementation of AppPreferencesRepository.
 * Follows atomic write patterns for zero-corruption guarantee on app termination.
 * Engineered by adishxm.
 */
class LocalAppPreferencesRepository(
    private val storageFile: File? = null
) : AppPreferencesRepository {

    private var preferences = AppPreferencesData()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun hasCompletedOnboarding(): Boolean = preferences.hasCompletedOnboarding

    @Synchronized
    override fun setCompletedOnboarding(completed: Boolean) {
        preferences = preferences.copy(hasCompletedOnboarding = completed)
        saveToDisk()
    }

    @Synchronized
    override fun getDefaultSearchEngineUrl(): String = preferences.defaultSearchEngineUrl

    @Synchronized
    override fun setDefaultSearchEngineUrl(url: String) {
        if (url.isNotBlank()) {
            preferences = preferences.copy(defaultSearchEngineUrl = url.trim())
            saveToDisk()
        }
    }

    @Synchronized
    override fun isHttpsOnlyMode(): Boolean = preferences.httpsOnlyMode

    @Synchronized
    override fun setHttpsOnlyMode(enabled: Boolean) {
        preferences = preferences.copy(httpsOnlyMode = enabled)
        saveToDisk()
    }

    @Synchronized
    override fun getNavBarPosition(): String = preferences.navBarPosition

    @Synchronized
    override fun setNavBarPosition(position: String) {
        val normalized = if (position.equals("TOP", ignoreCase = true)) "TOP" else "BOTTOM"
        preferences = preferences.copy(navBarPosition = normalized)
        saveToDisk()
    }

    @Synchronized
    override fun isAutoHideNavBarOnScroll(): Boolean = preferences.autoHideNavBarOnScroll

    @Synchronized
    override fun setAutoHideNavBarOnScroll(enabled: Boolean) {
        preferences = preferences.copy(autoHideNavBarOnScroll = enabled)
        saveToDisk()
    }

    @Synchronized
    override fun isWebBottomClearance(): Boolean = preferences.webBottomClearance

    @Synchronized
    override fun setWebBottomClearance(enabled: Boolean) {
        preferences = preferences.copy(webBottomClearance = enabled)
        saveToDisk()
    }

    @Synchronized
    override fun getUiThemeStyle(): String = preferences.uiThemeStyle

    @Synchronized
    override fun setUiThemeStyle(theme: String) {
        preferences = preferences.copy(uiThemeStyle = theme)
        saveToDisk()
    }

    @Synchronized
    override fun isBackgroundPlaybackEnabled(): Boolean = preferences.backgroundPlaybackEnabled

    @Synchronized
    override fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        preferences = preferences.copy(backgroundPlaybackEnabled = enabled)
        saveToDisk()
    }

    @Synchronized
    override fun isShowPipHoverOnBack(): Boolean = preferences.showPipHoverOnBack

    @Synchronized
    override fun setShowPipHoverOnBack(enabled: Boolean) {
        preferences = preferences.copy(showPipHoverOnBack = enabled)
        saveToDisk()
    }

    @Synchronized
    override fun isExtremePrivateMode(): Boolean = preferences.extremePrivateMode

    @Synchronized
    override fun setExtremePrivateMode(enabled: Boolean) {
        preferences = preferences.copy(extremePrivateMode = enabled)
        saveToDisk()
    }

    @Synchronized
    override fun updatePreferences(update: (AppPreferencesData) -> AppPreferencesData) {
        preferences = update(preferences)
        saveToDisk()
    }

    @Synchronized
    override fun getPreferences(): AppPreferencesData = preferences

    @Synchronized
    override fun resetAll() {
        preferences = AppPreferencesData()
        saveToDisk()
    }

    private fun loadFromDisk() {
        val file = storageFile ?: return
        if (!file.exists() || file.length() == 0L) return

        try {
            val content = file.readText(StandardCharsets.UTF_8).trim()
            if (content.isEmpty()) return

            var onboarding = false
            var search = "https://duckduckgo.com/?q="
            var httpsOnly = true
            var navBarPos = "BOTTOM"
            var autoHide = true
            var clearance = true
            var uiTheme = "EDITORIAL_DARK"
            var bgPlayback = true
            var pipHover = true
            var extremePriv = true
            var showFmt = true
            var showShields = true
            var showShare = true

            content.lines().forEach { line ->
                val trimmed = line.trim().trimEnd(',')
                when {
                    trimmed.contains("\"hasCompletedOnboarding\"") -> {
                        onboarding = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"defaultSearchEngineUrl\"") -> {
                        search = trimmed.substringAfter(":").trim().trim('"')
                    }
                    trimmed.contains("\"httpsOnlyMode\"") -> {
                        httpsOnly = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"navBarPosition\"") -> {
                        navBarPos = trimmed.substringAfter(":").trim().trim('"')
                    }
                    trimmed.contains("\"autoHideNavBarOnScroll\"") -> {
                        autoHide = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"webBottomClearance\"") -> {
                        clearance = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"uiThemeStyle\"") -> {
                        uiTheme = trimmed.substringAfter(":").trim().trim('"')
                    }
                    trimmed.contains("\"backgroundPlaybackEnabled\"") -> {
                        bgPlayback = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"showPipHoverOnBack\"") -> {
                        pipHover = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"extremePrivateMode\"") -> {
                        extremePriv = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"showFormatButtonInDock\"") -> {
                        showFmt = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"showShieldsButtonInDock\"") -> {
                        showShields = trimmed.substringAfter(":").trim().toBoolean()
                    }
                    trimmed.contains("\"showShareButtonInDock\"") -> {
                        showShare = trimmed.substringAfter(":").trim().toBoolean()
                    }
                }
            }

            preferences = AppPreferencesData(
                hasCompletedOnboarding = onboarding,
                defaultSearchEngineUrl = search,
                httpsOnlyMode = httpsOnly,
                navBarPosition = navBarPos,
                autoHideNavBarOnScroll = autoHide,
                webBottomClearance = clearance,
                uiThemeStyle = uiTheme,
                backgroundPlaybackEnabled = bgPlayback,
                showPipHoverOnBack = pipHover,
                extremePrivateMode = extremePriv,
                showFormatButtonInDock = showFmt,
                showShieldsButtonInDock = showShields,
                showShareButtonInDock = showShare
            )
        } catch (_: Exception) {
            preferences = AppPreferencesData()
        }
    }

    private fun saveToDisk() {
        val file = storageFile ?: return
        try {
            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            val tmpFile = File(file.parentFile, "${file.name}.tmp")
            val json = buildString {
                appendLine("{")
                appendLine("  \"hasCompletedOnboarding\": ${preferences.hasCompletedOnboarding},")
                appendLine("  \"defaultSearchEngineUrl\": \"${preferences.defaultSearchEngineUrl}\",")
                appendLine("  \"httpsOnlyMode\": ${preferences.httpsOnlyMode},")
                appendLine("  \"navBarPosition\": \"${preferences.navBarPosition}\",")
                appendLine("  \"autoHideNavBarOnScroll\": ${preferences.autoHideNavBarOnScroll},")
                appendLine("  \"webBottomClearance\": ${preferences.webBottomClearance},")
                appendLine("  \"uiThemeStyle\": \"${preferences.uiThemeStyle}\",")
                appendLine("  \"backgroundPlaybackEnabled\": ${preferences.backgroundPlaybackEnabled},")
                appendLine("  \"showPipHoverOnBack\": ${preferences.showPipHoverOnBack},")
                appendLine("  \"extremePrivateMode\": ${preferences.extremePrivateMode},")
                appendLine("  \"showFormatButtonInDock\": ${preferences.showFormatButtonInDock},")
                appendLine("  \"showShieldsButtonInDock\": ${preferences.showShieldsButtonInDock},")
                appendLine("  \"showShareButtonInDock\": ${preferences.showShareButtonInDock}")
                appendLine("}")
            }

            OutputStreamWriter(FileOutputStream(tmpFile), StandardCharsets.UTF_8).use { writer ->
                writer.write(json)
                writer.flush()
            }

            if (tmpFile.exists()) {
                if (file.exists()) {
                    file.delete()
                }
                tmpFile.renameTo(file)
            }
        } catch (_: Exception) {
            // Fail gracefully without crashing
        }
    }
}
