package com.musify.browser

import com.musify.browser.engine.BuildVariantInfo
import com.musify.browser.engine.CapabilityStatus
import com.musify.browser.engine.EngineType
import com.musify.browser.engine.MediaCapabilityState
import com.musify.browser.engine.MusifyVariant
import com.musify.browser.ui.theme.Danger
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.Success
import com.musify.browser.ui.theme.Warning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Phase17PrivacyDiagnosticsTest {

    private fun resolveSourceFile(pathWithinApp: String): File {
        return if (File("src/main/java").exists()) {
            File(pathWithinApp.removePrefix("app/"))
        } else {
            File(pathWithinApp)
        }
    }

    @Test
    fun testPlaybackCompatibilityDialogPreservesPhase11to13Features() {
        val dialogFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/PlaybackCompatibilityDialog.kt")
        assertTrue("PlaybackCompatibilityDialog.kt must exist", dialogFile.exists())
        val source = dialogFile.readText()

        // 1. BuildVariantInfo detection
        assertTrue("Must detect BuildVariantInfo", source.contains("BuildVariantInfo.detect"))
        assertTrue("Must render buildInfo.variant.displayName", source.contains("buildInfo.variant.displayName"))

        // 2. Widevine limitation warning banner for Lite / WebView
        assertTrue("Must check protectedPlaybackLimitationNotice", source.contains("buildInfo.protectedPlaybackLimitationNotice != null"))
        assertTrue("Must display System WebView Limitation", source.contains("System WebView Limitation"))

        // 3. Media capability states from Phases 10-13
        assertTrue("Must display widevineStatus", source.contains("mediaCap.widevineStatus"))
        assertTrue("Must display encryptedMediaStatus", source.contains("mediaCap.encryptedMediaStatus"))
        assertTrue("Must display deviceDrmProvider", source.contains("mediaCap.deviceDrmProvider"))
        assertTrue("Must display isAutoplayAllowed", source.contains("mediaCap.isAutoplayAllowed"))
        assertTrue("Must display cookiePolicy", source.contains("mediaCap.cookiePolicy"))
        assertTrue("Must display liveBlockCategory", source.contains("mediaCap.liveBlockCategory"))
        assertTrue("Must display lastMediaFailureReason", source.contains("mediaCap.lastMediaFailureReason"))

        // 4. Must use MusifyIcons and Obsidian tokens
        assertTrue("Must use MusifyIcons.Music", source.contains("MusifyIcons.Music"))
        assertTrue("Must use MusifyIcons.Warning", source.contains("MusifyIcons.Warning"))
        assertTrue("Must use MusifyIcons.Reload", source.contains("MusifyIcons.Reload"))
        assertTrue("Must use MusifyIcons.Bolt", source.contains("MusifyIcons.Bolt"))
    }

    @Test
    fun testPrivacySheetsEmojiRetirement() {
        val filesToCheck = listOf(
            "app/src/main/java/com/musify/browser/ui/screens/PrivacyShieldSheet.kt",
            "app/src/main/java/com/musify/browser/ui/screens/DnsSettingsDialog.kt",
            "app/src/main/java/com/musify/browser/ui/screens/SitePermissionsScreen.kt",
            "app/src/main/java/com/musify/browser/ui/screens/DataControlScreen.kt",
            "app/src/main/java/com/musify/browser/ui/screens/PlaybackCompatibilityDialog.kt"
        )

        val retiredEmojis = listOf("🛡", "🕶", "🔒", "🔓", "⚡", "🗑", "📷", "🎤", "📍", "💾", "⚠", "🎵")

        filesToCheck.forEach { relPath ->
            val file = resolveSourceFile(relPath)
            assertTrue("File $relPath must exist", file.exists())
            val content = file.readText()
            retiredEmojis.forEach { emoji ->
                assertFalse(
                    "File $relPath should not contain raw emoji '$emoji'",
                    content.contains("\"$emoji\"") || content.contains("'$emoji'")
                )
            }
        }
    }

    @Test
    fun testCapabilityStatusColorMapping() {
        fun resolveStatusColor(status: CapabilityStatus): androidx.compose.ui.graphics.Color {
            return when (status) {
                CapabilityStatus.SUPPORTED -> Success
                CapabilityStatus.UNSUPPORTED -> Danger
                CapabilityStatus.FAILED -> Danger
                CapabilityStatus.UNKNOWN -> Warning
            }
        }

        assertEquals(Success, resolveStatusColor(CapabilityStatus.SUPPORTED))
        assertEquals(Danger, resolveStatusColor(CapabilityStatus.UNSUPPORTED))
        assertEquals(Danger, resolveStatusColor(CapabilityStatus.FAILED))
        assertEquals(Warning, resolveStatusColor(CapabilityStatus.UNKNOWN))
    }
}
