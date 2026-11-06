package com.musify.browser

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
import com.musify.browser.ui.theme.SafariGlass
import com.musify.browser.ui.theme.SafariGlassBorder
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariOrange
import com.musify.browser.ui.theme.SafariPurple
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_LargeTitle
import com.musify.browser.ui.theme.SafariTypography_Subhead
import com.musify.browser.ui.theme.SafariTypography_Title
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 35 Unit Test Suite:
 * Apple Safari-Inspired Universal UI/UX Overhaul
 *
 * Verifies HIG design tokens, SF Pro / Inter typography, custom vector iconography,
 * Safari Start Page squircle favorites, bottom floating tab bar semantics,
 * 2-column tab grid overview, Intelligent Tracking Prevention privacy report,
 * and seamless browser shell integration.
 */
class Phase35SafariRedesignTest {

    private fun resolveSourceFile(pathWithinApp: String): File {
        return if (File("src/main/java").exists()) {
            File(pathWithinApp.removePrefix("app/"))
        } else {
            File(pathWithinApp)
        }
    }

    @Test
    fun testSafariColorTokens() {
        assertEquals("SafariBackgroundDark must be #000000", Color(0xFF000000), SafariBackgroundDark)
        assertEquals("SafariCardDark must be #1C1C1E", Color(0xFF1C1C1E), SafariCardDark)
        assertEquals("SafariCardElevated must be #2C2C2E", Color(0xFF2C2C2E), SafariCardElevated)
        assertEquals("SafariBorderDark must be #38383A", Color(0xFF38383A), SafariBorderDark)
        assertEquals("SafariAccentBlue must be #007AFF", Color(0xFF007AFF), SafariAccentBlue)
        assertEquals("SafariGreen must be #34C759", Color(0xFF34C759), SafariGreen)
        assertEquals("SafariOrange must be #FF9500", Color(0xFFFF9500), SafariOrange)
        assertEquals("SafariDangerRed must be #FF3B30", Color(0xFFFF3B30), SafariDangerRed)
        assertEquals("SafariPurple must be #AF52DE", Color(0xFFAF52DE), SafariPurple)
        assertEquals("SafariGlass must match frosted glass #1C1C1E", Color(0xEB1C1C1E), SafariGlass)
        assertEquals("SafariGlassBorder must match subtle rim", Color(0x33FFFFFF), SafariGlassBorder)
    }

    @Test
    fun testSafariTypographyTokens() {
        assertEquals("Safari Large Title must be 32sp", 32.sp, SafariTypography_LargeTitle.fontSize)
        assertEquals("Safari Title must be 22sp", 22.sp, SafariTypography_Title.fontSize)
        assertEquals("Safari Headline must be 17sp", 17.sp, SafariTypography_Headline.fontSize)
        assertEquals("Safari Body must be 17sp", 17.sp, SafariTypography_Body.fontSize)
        assertEquals("Safari Callout must be 16sp", 16.sp, SafariTypography_Callout.fontSize)
        assertEquals("Safari Subhead must be 15sp", 15.sp, SafariTypography_Subhead.fontSize)
        assertEquals("Safari Footnote must be 13sp", 13.sp, SafariTypography_Footnote.fontSize)
        assertEquals("Safari Caption must be 11sp", 11.sp, SafariTypography_Caption.fontSize)
    }

    @Test
    fun testSafariIconsPresent() {
        assertNotNull("MusifyIcons.FormatAA must be defined", MusifyIcons.FormatAA)
        assertNotNull("MusifyIcons.ReadingList must be defined", MusifyIcons.ReadingList)
        assertNotNull("MusifyIcons.ShareIOS must be defined", MusifyIcons.ShareIOS)
        assertNotNull("MusifyIcons.TabsGrid must be defined", MusifyIcons.TabsGrid)
        assertNotNull("MusifyIcons.Compass must be defined", MusifyIcons.Compass)
        assertNotNull("MusifyIcons.BookOpen must be defined", MusifyIcons.BookOpen)
    }

    @Test
    fun testSafariStartPageStructureAndSemantics() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/SafariStartPage.kt")
        assertTrue("SafariStartPage.kt must exist", file.exists())
        val source = file.readText()

        val requiredTags = listOf(
            "safari.start_page.root",
            "safari.privacy_report.card"
        )
        for (tag in requiredTags) {
            assertTrue("SafariStartPage must declare tag '$tag'", source.contains("\"$tag\""))
        }
        assertTrue(
            "SafariStartPage must declare dynamic favorite test tag",
            source.contains("safari.favorite.\${item.id}")
        )

        // Must route to genuine web URLs
        assertTrue(source.contains("https://open.spotify.com/"))
        assertTrue(source.contains("https://music.youtube.com/"))
        assertTrue(source.contains("https://m.youtube.com/"))
        assertTrue(source.contains("https://soundcloud.com/"))
        assertTrue(source.contains("https://music.apple.com/"))
        assertTrue(source.contains("https://www.reddit.com/"))
        assertTrue(source.contains("https://www.wikipedia.org/"))
        assertTrue(source.contains("https://github.com/"))

        // ITP Privacy report copy
        assertTrue(source.contains("Privacy Report"))
        assertTrue(source.contains("trackers from profiling you"))
    }

    @Test
    fun testSafariBottomTabBarSemantics() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/SafariBottomTabBar.kt")
        assertTrue("SafariBottomTabBar.kt must exist", file.exists())
        val source = file.readText()

        val requiredTags = listOf(
            "safari.bottom_bar.root",
            "safari.button.format_aa",
            "safari.capsule.domain",
            "safari.button.reload",
            "safari.button.share",
            "safari.button.tabs",
            "safari.address_input"
        )
        for (tag in requiredTags) {
            assertTrue("SafariBottomTabBar must declare tag '$tag'", source.contains("\"$tag\""))
        }
    }

    @Test
    fun testSafariTabSwitcherScreenStructure() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/SafariTabSwitcherScreen.kt")
        assertTrue("SafariTabSwitcherScreen.kt must exist", file.exists())
        val source = file.readText()

        val requiredTags = listOf(
            "safari.tab_switcher.root",
            "safari.tab_mode.normal",
            "safari.tab_mode.private",
            "safari.button.done",
            "safari.button.new_tab"
        )
        for (tag in requiredTags) {
            assertTrue("SafariTabSwitcherScreen must declare tag '$tag'", source.contains("\"$tag\""))
        }
        assertTrue("Must support private browsing mode explanation", source.contains("Private Browsing Mode"))
    }

    @Test
    fun testSafariPrivacyReportSheetStructure() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/SafariPrivacyReportSheet.kt")
        assertTrue("SafariPrivacyReportSheet.kt must exist", file.exists())
        val source = file.readText()

        assertTrue(source.contains("safari.privacy_report.sheet"))
        assertTrue(source.contains("stats.g.doubleclick.net"))
        assertTrue(source.contains("connect.facebook.net"))
        assertTrue(source.contains("google-analytics.com"))
        assertTrue(source.contains("criteo.com"))
        assertTrue(source.contains("aax.amazon-adsystem.com"))
    }

    @Test
    fun testSafariShareSheetStructure() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/SafariShareSheet.kt")
        assertTrue("SafariShareSheet.kt must exist", file.exists())
        val source = file.readText()

        assertTrue(source.contains("safari.share.sheet"))
        assertTrue(source.contains("Copy Link"))
        assertTrue(source.contains("Share..."))
        assertTrue(source.contains("Add Bookmark"))
        assertTrue(source.contains("Add to Reading"))
        assertTrue(source.contains("Find on Page"))
    }

    @Test
    fun testBrowserShellScreenSafariIntegration() {
        val file = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/BrowserShellScreen.kt")
        assertTrue("BrowserShellScreen.kt must exist", file.exists())
        val source = file.readText()

        assertTrue("Must integrate SafariStartPage", source.contains("SafariStartPage"))
        assertTrue("Must integrate SafariBottomTabBar", source.contains("SafariBottomTabBar"))
        assertTrue("Must integrate SafariTabSwitcherScreen", source.contains("SafariTabSwitcherScreen"))
        assertTrue("Must integrate SafariPageSettingsSheet", source.contains("SafariPageSettingsSheet"))
        assertTrue("Must integrate SafariPrivacyReportSheet", source.contains("SafariPrivacyReportSheet"))
        assertTrue("Must integrate SafariShareSheet", source.contains("SafariShareSheet"))

        // Zero cartoon emojis check
        val retiredEmojis = listOf("🛡", "🕶", "🔍", "🔒", "🔓", "➕", "✕", "▲", "▼", "↻", "🦆", "⚙", "⭐", "📂", "🎵")
        retiredEmojis.forEach { emoji ->
            assertFalse(
                "BrowserShellScreen must not contain cartoon emoji '$emoji'",
                source.contains("\"$emoji\"") || source.contains("'$emoji'")
            )
        }
    }
}

