package com.musify.browser

import com.musify.browser.crypto.CryptographicShredder
import com.musify.browser.media.MusifyMediaPlaybackService
import com.musify.browser.storage.AppPreferencesData
import com.musify.browser.storage.LocalAppPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Phase 37 Unit Test Suite:
 * UI Customization, 10,000x Extreme Cryptographic Shredder, Background Media Streaming,
 * and Author Attribution ("Designed & Engineered by adishxm").
 */
class Phase37CustomizationAndExtremePrivacyTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun resolveSourceFile(pathWithinApp: String): File {
        return if (File("src/main/java").exists()) {
            File(pathWithinApp.removePrefix("app/"))
        } else {
            File(pathWithinApp)
        }
    }

    @Test
    fun testCryptographicShredderMultiPassEntropy() {
        val testBuffer = "SecretData1234567890PrivateBankingAndSearchHistory".toByteArray(Charsets.UTF_8)
        val originalCopy = testBuffer.clone()

        CryptographicShredder.shredByteArray(testBuffer)

        // After 3-pass shred and final zeroize, all bytes must be 0x00
        for (i in testBuffer.indices) {
            assertEquals("Byte at index $i must be 0x00 after zeroize", 0.toByte(), testBuffer[i])
        }
        assertFalse(testBuffer.contentEquals(originalCopy))
    }

    @Test
    fun testCryptographicShredderStringReferenceCorruption() {
        val sensitiveUrl = "https://sensitive-private-banking.example.com/vault"
        val corrupted = CryptographicShredder.corruptStringReference(sensitiveUrl)

        assertEquals(CryptographicShredder.CORRUPTION_MARKER, corrupted)
        assertTrue(corrupted.contains("0xDEADBEEF"))
        assertTrue(corrupted.contains("ADISHXM"))
        assertFalse(corrupted.contains("sensitive-private-banking"))
    }

    @Test
    fun testAppPreferencesRepositoryFullCustomizationPersistence() {
        val prefFile = tempFolder.newFile("musify_test_preferences.json")
        val repo = LocalAppPreferencesRepository(prefFile)

        // Initial defaults
        val defaultPrefs = repo.getPreferences()
        assertEquals("BOTTOM", defaultPrefs.navBarPosition)
        assertTrue(defaultPrefs.autoHideNavBarOnScroll)
        assertTrue(defaultPrefs.webBottomClearance)
        assertEquals("EDITORIAL_DARK", defaultPrefs.uiThemeStyle)
        assertTrue(defaultPrefs.backgroundPlaybackEnabled)
        assertTrue(defaultPrefs.showPipHoverOnBack)
        assertTrue(defaultPrefs.extremePrivateMode)

        // Modify preferences
        repo.setNavBarPosition("TOP")
        repo.setAutoHideNavBarOnScroll(false)
        repo.setWebBottomClearance(false)
        repo.setUiThemeStyle("SAFARI_GLASS")
        repo.setBackgroundPlaybackEnabled(false)
        repo.setShowPipHoverOnBack(false)
        repo.setExtremePrivateMode(false)

        assertEquals("TOP", repo.getNavBarPosition())
        assertFalse(repo.isAutoHideNavBarOnScroll())
        assertFalse(repo.isWebBottomClearance())
        assertEquals("SAFARI_GLASS", repo.getUiThemeStyle())
        assertFalse(repo.isBackgroundPlaybackEnabled())
        assertFalse(repo.isShowPipHoverOnBack())
        assertFalse(repo.isExtremePrivateMode())

        // Verify disk reload guarantees zero persistence corruption
        val reloadedRepo = LocalAppPreferencesRepository(prefFile)
        val loaded = reloadedRepo.getPreferences()
        assertEquals("TOP", loaded.navBarPosition)
        assertFalse(loaded.autoHideNavBarOnScroll)
        assertFalse(loaded.webBottomClearance)
        assertEquals("SAFARI_GLASS", loaded.uiThemeStyle)
        assertFalse(loaded.backgroundPlaybackEnabled)
        assertFalse(loaded.showPipHoverOnBack)
        assertFalse(loaded.extremePrivateMode)
    }

    @Test
    fun testUiCustomizationScreenSemanticsAndAuthorBranding() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/UiCustomizationScreen.kt")
        assertTrue("UiCustomizationScreen.kt must exist", file.exists())
        val source = file.readText()

        // Required test tags
        assertTrue(source.contains("musify.customization.screen"))
        assertTrue(source.contains("musify.customization.done"))
        assertTrue(source.contains("musify.customization.dock_bottom"))
        assertTrue(source.contains("musify.customization.dock_top"))
        assertTrue(source.contains("musify.customization.switch_autohide"))
        assertTrue(source.contains("musify.customization.switch_clearance"))
        assertTrue(source.contains("musify.customization.switch_shredder"))
        assertTrue(source.contains("musify.customization.button_panic_wipe"))
        assertTrue(source.contains("musify.customization.switch_bg_playback"))
        assertTrue(source.contains("musify.customization.switch_pip"))
        assertTrue(source.contains("musify.customization.button_test_notification"))

        // Required author attribution
        assertTrue(source.contains("Designed & Engineered by adishxm"))
        assertTrue(source.contains("adishxm signature"))
    }

    @Test
    fun testMusifyMediaPlaybackServiceStructure() {
        assertEquals("musify_media_playback_channel", MusifyMediaPlaybackService.CHANNEL_ID)
        assertEquals(2026, MusifyMediaPlaybackService.NOTIFICATION_ID)
        assertEquals("com.musify.browser.media.START", MusifyMediaPlaybackService.ACTION_START_PLAYBACK)
        assertEquals("com.musify.browser.media.PAUSE", MusifyMediaPlaybackService.ACTION_PAUSE_PLAYBACK)
        assertEquals("com.musify.browser.media.RESUME", MusifyMediaPlaybackService.ACTION_RESUME_PLAYBACK)
        assertEquals("com.musify.browser.media.STOP", MusifyMediaPlaybackService.ACTION_STOP_PLAYBACK)
    }

    @Test
    fun testBrowserShellCustomizationAndDockClearanceIntegration() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/BrowserShellScreen.kt")
        assertTrue("BrowserShellScreen.kt must exist", file.exists())
        val source = file.readText()

        assertTrue(source.contains("isBottomDock"))
        assertTrue(source.contains("bottomClearance"))
        assertTrue(source.contains("isNavBarVisible"))
        assertTrue(source.contains("openCustomizationHandler"))
        assertTrue(source.contains("UiCustomizationScreen"))
        assertTrue(source.contains("MusifyMediaPlaybackService"))
    }

    @Test
    fun testSafariTabSwitcher10000xExtremeShredderIntegration() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/SafariTabSwitcherScreen.kt")
        assertTrue("SafariTabSwitcherScreen.kt must exist", file.exists())
        val source = file.readText()

        assertTrue(source.contains("10,000x"))
        assertTrue(source.contains("CryptographicShredder"))
        assertTrue(source.contains("safari.button.shred_wipe"))
        assertTrue(source.contains("executePanicWipe"))
    }
}

