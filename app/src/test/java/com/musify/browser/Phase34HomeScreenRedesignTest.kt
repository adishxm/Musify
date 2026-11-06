package com.musify.browser

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.EditorialTypography_Headline
import com.musify.browser.ui.theme.EditorialTypography_Metadata
import com.musify.browser.ui.theme.EditorialTypography_SectionHeader
import com.musify.browser.ui.theme.EditorialTypography_ShortcutDomain
import com.musify.browser.ui.theme.EditorialTypography_ShortcutTitle
import com.musify.browser.ui.theme.EditorialTypography_Subtitle
import com.musify.browser.ui.theme.EditorialTypography_Wordmark
import com.musify.browser.ui.theme.HomeCanvas
import com.musify.browser.ui.theme.HomeControlOutline
import com.musify.browser.ui.theme.HomeDivider
import com.musify.browser.ui.theme.HomeDockBorder
import com.musify.browser.ui.theme.HomeDockSurface
import com.musify.browser.ui.theme.HomeSurface
import com.musify.browser.ui.theme.HomeSurfaceRaised
import com.musify.browser.ui.theme.HomeTextPrimary
import com.musify.browser.ui.theme.HomeTextSecondary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 34 Unit Test Suite:
 * Editorial New-Tab Home Screen Redesign & DM Serif Display Typography
 *
 * Verifies design tokens, typography specifications, semantic test tags,
 * shortcut routing invariants, and shell integration.
 */
class Phase34HomeScreenRedesignTest {

    private fun resolveSourceFile(pathWithinApp: String): File {
        return if (File("src/main/java").exists()) {
            File(pathWithinApp.removePrefix("app/"))
        } else {
            File(pathWithinApp)
        }
    }

    @Test
    fun testEditorialDarkPaletteTokens() {
        assertEquals("HomeCanvas must match #09090A", Color(0xFF09090A), HomeCanvas)
        assertEquals("HomeSurface must match #18181A", Color(0xFF18181A), HomeSurface)
        assertEquals("HomeSurfaceRaised must match #242326", Color(0xFF242326), HomeSurfaceRaised)
        assertEquals("HomeTextPrimary must match #F3EEEA", Color(0xFFF3EEEA), HomeTextPrimary)
        assertEquals("HomeTextSecondary must match #B4AFB2", Color(0xFFB4AFB2), HomeTextSecondary)
        assertEquals("HomeDivider must match #3C3A3E", Color(0xFF3C3A3E), HomeDivider)
        assertEquals("HomeControlOutline must match #A9A5AA", Color(0xFFA9A5AA), HomeControlOutline)
        assertEquals("HomeDockSurface must match 94% #1B1A1C", Color(0xF01B1A1C), HomeDockSurface)
        assertEquals("HomeDockBorder must match 20% #F3EEEA", Color(0x33F3EEEA), HomeDockBorder)
    }

    @Test
    fun testEditorialTypographyScale() {
        assertEquals("Wordmark must be 30sp", 30.sp, EditorialTypography_Wordmark.fontSize)
        assertEquals("Headline must be 46sp", 46.sp, EditorialTypography_Headline.fontSize)
        assertEquals("Subtitle must be 20sp", 20.sp, EditorialTypography_Subtitle.fontSize)
        assertEquals("SectionHeader must be 24sp", 24.sp, EditorialTypography_SectionHeader.fontSize)
        assertEquals("ShortcutTitle must be 16sp", 16.sp, EditorialTypography_ShortcutTitle.fontSize)
        assertEquals("ShortcutDomain must be 12.5sp", 12.5.sp, EditorialTypography_ShortcutDomain.fontSize)
        assertEquals("Metadata tag must be 11sp", 11.sp, EditorialTypography_Metadata.fontSize)
        assertTrue("Metadata tag must have positive letter spacing", EditorialTypography_Metadata.letterSpacing >= 1.2.sp)
    }

    @Test
    fun testFontAndLicenseFilesExist() {
        val fontFile = resolveSourceFile("app/src/main/res/font/dm_serif_display_regular.ttf")
        assertTrue("dm_serif_display_regular.ttf must exist in res/font", fontFile.exists())
        assertTrue("Font file size must be > 50KB", fontFile.length() > 50_000)

        val licenseFile = resolveSourceFile("app/src/main/assets/licenses/DMSerifDisplay-OFL.txt")
        assertTrue("DMSerifDisplay-OFL.txt must exist in assets/licenses", licenseFile.exists())
    }

    @Test
    fun testHomeScreenSemanticTagsAndAccessibility() {
        val homeFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/MusifyHomeScreen.kt")
        assertTrue("MusifyHomeScreen.kt must exist", homeFile.exists())
        val source = homeFile.readText()

        // Required test tags per Section 7 of implementation guide
        val requiredTags = listOf(
            "home.root",
            "home.search",
            "home.shortcut.spotify",
            "home.shortcut.youtube_music",
            "home.shortcut.youtube",
            "home.shortcut.soundcloud",
            "home.back",
            "home.shields",
            "home.tabs",
            "home.more"
        )
        for (tag in requiredTags) {
            assertTrue("MusifyHomeScreen must declare testTag '$tag'", source.contains("\"$tag\""))
        }

        // Accessibility content descriptions
        assertTrue("Must declare TalkBack description for shortcuts", source.contains("Open \$title, \$domain"))
        assertTrue("Must declare TalkBack description for Shields", source.contains("Privacy shields"))
        assertTrue("Must declare TalkBack description for More options", source.contains("More browser options"))
    }

    @Test
    fun testShortcutUrlsAreAccurateAndUnmodified() {
        val homeFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/MusifyHomeScreen.kt")
        val source = homeFile.readText()

        assertTrue("Must route to real Spotify Web URL", source.contains("https://open.spotify.com/"))
        assertTrue("Must route to real YouTube Music URL", source.contains("https://music.youtube.com/"))
        assertTrue("Must route to real mobile YouTube URL", source.contains("https://m.youtube.com/"))
        assertTrue("Must route to real SoundCloud URL", source.contains("https://soundcloud.com/"))
    }

    @Test
    fun testProceduralHeroArtworkAndCopy() {
        val homeFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/MusifyHomeScreen.kt")
        val source = homeFile.readText()

        assertTrue("Must include MUSIC + THE WEB tag", source.contains("MUSIC + THE WEB"))
        assertTrue("Must render procedural lathe grooves via Canvas", source.contains("DecorativeVinylHero"))
        assertTrue("Must render vertical editorial listening caption", source.contains("DEEPER") && source.contains("LISTENING"))
    }

    @Test
    fun testBrowserShellScreenHomeScreenIntegration() {
        val shellFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/BrowserShellScreen.kt")
        val source = shellFile.readText()

        assertTrue("BrowserShellScreen must compute isHome", source.contains("isHome"))
        assertTrue("BrowserShellScreen must pass canGoBack to MusifyHomeScreen", source.contains("canGoBack = activeTab?.canGoBack == true"))
        assertTrue("BrowserShellScreen must pass tabCount to MusifyHomeScreen", source.contains("tabCount = tabsState.tabCount"))
        assertTrue("MusifyActionBar must only display on web pages (!isHome)", source.contains("if (!isHome)"))
    }
}

