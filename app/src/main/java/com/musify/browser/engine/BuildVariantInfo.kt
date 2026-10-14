package com.musify.browser.engine

import android.os.Build

enum class MusifyVariant(val displayName: String, val description: String) {
    LITE("Musify-Lite", "Ultra-lightweight WebView build (~8.4 MB) — no Widevine DRM"),
    ARM64("Musify-ARM64", "64-bit GeckoView build (~84 MB) — Widevine L1/L3 hardware candidate"),
    UNIVERSAL("Musify-Universal", "Universal GeckoView build (~153 MB) — all ABIs")
}

data class BuildVariantInfo(
    val variant: MusifyVariant,
    val engineType: EngineType,
    val engineVersion: String,
    val androidApiLevel: Int = try { Build.VERSION.SDK_INT } catch (_: Throwable) { 0 },
    val androidRelease: String = try { Build.VERSION.RELEASE ?: "unknown" } catch (_: Throwable) { "unknown" },
    val deviceManufacturer: String = try { Build.MANUFACTURER ?: "unknown" } catch (_: Throwable) { "unknown" },
    val deviceModel: String = try { Build.MODEL ?: "unknown" } catch (_: Throwable) { "unknown" },
    val primaryAbi: String = try { Build.SUPPORTED_ABIS?.firstOrNull() ?: "unknown" } catch (_: Throwable) { "unknown" },
    val isWidevineDrmAvailable: Boolean,
    val protectedPlaybackLimitationNotice: String? = null
) {
    companion object {
        fun detect(currentEngine: EngineType): BuildVariantInfo {
            val isWebView = currentEngine == EngineType.WEB_VIEW
            val supportedAbis = try { Build.SUPPORTED_ABIS ?: emptyArray() } catch (_: Throwable) { emptyArray() }

            val variant = when {
                isWebView -> MusifyVariant.LITE
                supportedAbis.contains("arm64-v8a") && !supportedAbis.contains("x86_64") -> MusifyVariant.ARM64
                else -> MusifyVariant.UNIVERSAL
            }

            val limitation = if (isWebView) {
                "Android System WebView does not expose Widevine CDM. Protected media (such as Spotify Web Player) requires the full GeckoView build (Musify.apk or Musify-ARM64.apk)."
            } else {
                null
            }

            return BuildVariantInfo(
                variant = variant,
                engineType = currentEngine,
                engineVersion = if (isWebView) "AndroidX WebKit (System WebView)" else "Mozilla GeckoView 135.0",
                isWidevineDrmAvailable = !isWebView,
                protectedPlaybackLimitationNotice = limitation
            )
        }
    }
}
