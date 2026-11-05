package com.musify.browser

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.M6Canvas
import com.musify.browser.ui.theme.M6Card
import com.musify.browser.ui.theme.M6Elevated
import com.musify.browser.ui.theme.M6Emerald
import com.musify.browser.ui.theme.M6Hairline
import com.musify.browser.ui.theme.M6HairlineStrong
import com.musify.browser.ui.theme.M6SignalOrange
import com.musify.browser.ui.theme.M6SignalRed
import com.musify.browser.ui.theme.M6SoundViolet
import com.musify.browser.ui.theme.M6Surface
import com.musify.browser.ui.theme.M6TextMuted
import com.musify.browser.ui.theme.M6TextSecondary
import com.musify.browser.ui.theme.M6Typography_CatalogTag
import com.musify.browser.ui.theme.M6Typography_ExhibitionSubtitle
import com.musify.browser.ui.theme.M6Typography_ExhibitionTitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 28 Unit Tests: M6 Exhibition UI/UX Redesign
 * Museum-Grade Digital Sound Archive & Kinetic Shield Interface
 */
class Phase28M6ExhibitionDesignTest {

    private fun resolveSourceFile(pathWithinApp: String): File {
        return if (File("src/main/java").exists()) {
            File(pathWithinApp.removePrefix("app/"))
        } else {
            File(pathWithinApp)
        }
    }

    @Test
    fun testM6ExhibitionPaletteTokens() {
        assertEquals("M6Canvas must match deep gallery void #08080A", Color(0xFF08080A), M6Canvas)
        assertEquals("M6Surface must match persistent chrome #101014", Color(0xFF101014), M6Surface)
        assertEquals("M6Card must match curated container #16161C", Color(0xFF16161C), M6Card)
        assertEquals("M6Elevated must match interactive fill #1E1E26", Color(0xFF1E1E26), M6Elevated)
        assertEquals("M6Hairline must match resting grid boundary #282832", Color(0xFF282832), M6Hairline)
        assertEquals("M6HairlineStrong must match active border #3E3E4C", Color(0xFF3E3E4C), M6HairlineStrong)

        assertEquals("M6TextSecondary must match metadata copy #9898A6", Color(0xFF9898A6), M6TextSecondary)
        assertEquals("M6TextMuted must match caption copy #5C5C6E", Color(0xFF5C5C6E), M6TextMuted)
    }

    @Test
    fun testM6ExhibitionJewelSemantics() {
        assertEquals("M6Emerald must match seismic safe green #00F59B", Color(0xFF00F59B), M6Emerald)
        assertEquals("M6SignalOrange must match shield telemetry orange #FF5C00", Color(0xFFFF5C00), M6SignalOrange)
        assertEquals("M6SignalRed must match critical alert red #FF3B30", Color(0xFFFF3B30), M6SignalRed)
        assertEquals("M6SoundViolet must match Widevine DRM violet #A855F7", Color(0xFFA855F7), M6SoundViolet)
    }

    @Test
    fun testM6ExhibitionTypographyStyles() {
        assertEquals("Catalog tag must be 11sp with positive tracking", 11.sp, M6Typography_CatalogTag.fontSize)
        assertEquals(FontWeight.W600, M6Typography_CatalogTag.fontWeight)
        assertTrue("Catalog tag tracking must be >= 1.0sp", M6Typography_CatalogTag.letterSpacing >= 1.sp)

        assertEquals("Exhibition title must be 28sp with tight tracking", 28.sp, M6Typography_ExhibitionTitle.fontSize)
        assertEquals(FontWeight.W800, M6Typography_ExhibitionTitle.fontWeight)
        assertTrue("Exhibition title tracking must be tight (< 0sp)", M6Typography_ExhibitionTitle.letterSpacing < 0.sp)

        assertEquals("Exhibition subtitle must be 14sp", 14.sp, M6Typography_ExhibitionSubtitle.fontSize)
        assertEquals(FontWeight.W500, M6Typography_ExhibitionSubtitle.fontWeight)
    }

    @Test
    fun testM6LandingScreenPavilionIntegration() {
        val landingFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/LandingScreen.kt")
        assertTrue("LandingScreen.kt must exist", landingFile.exists())
        val source = landingFile.readText()

        assertTrue("Must contain M6 Sound Archive catalog tag", source.contains("[M6 // SOUND ARCHIVE]"))
        assertTrue("Must contain MUSIFY EXHIBITION title", source.contains("MUSIFY EXHIBITION"))
        assertTrue("Must feature Spotify Web exhibit", source.contains("Spotify Web Player"))
        assertTrue("Must feature YouTube Music exhibit", source.contains("YouTube Music Web"))
        assertTrue("Must feature YouTube Video exhibit", source.contains("YouTube Video Stream"))
        assertTrue("Must feature SoundCloud exhibit", source.contains("SoundCloud Streaming"))
        assertTrue("Must incorporate M6SeismographBar", source.contains("M6SeismographBar"))
    }

    @Test
    fun testM6PrivacyShieldSheetSeismographIntegration() {
        val sheetFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/PrivacyShieldSheet.kt")
        assertTrue("PrivacyShieldSheet.kt must exist", sheetFile.exists())
        val source = sheetFile.readText()

        assertTrue("Must feature M6 PRIVACY SEISMOGRAPH header", source.contains("M6 PRIVACY SEISMOGRAPH"))
        assertTrue("Must use M6Card container color", source.contains("M6Card"))
        assertTrue("Must use M6Emerald indicator", source.contains("M6Emerald"))
        assertTrue("Must use M6SignalOrange indicator", source.contains("M6SignalOrange"))
        assertTrue("Must contain M6CatalogTag", source.contains("M6CatalogTag"))
    }

    @Test
    fun testM6TabSwitcherScreenGalleryIntegration() {
        val switcherFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/TabSwitcherScreen.kt")
        assertTrue("TabSwitcherScreen.kt must exist", switcherFile.exists())
        val source = switcherFile.readText()

        assertTrue("Must feature M6 EXHIBITION TABS title", source.contains("M6 EXHIBITION TABS"))
        assertTrue("Must use M6Canvas scaffold container", source.contains("M6Canvas"))
        assertTrue("Must use M6Card for tab items", source.contains("M6Card"))
        assertTrue("Must use M6SignalOrange active border", source.contains("M6SignalOrange"))
    }

    @Test
    fun testM6BrowserShellNewTabPavilionIntegration() {
        val shellFile = resolveSourceFile("app/src/main/java/com/musify/browser/ui/screens/BrowserShellScreen.kt")
        assertTrue("BrowserShellScreen.kt must exist", shellFile.exists())
        val source = shellFile.readText()

        assertTrue("Must contain M6 SOUND ARCHIVE home screen title", source.contains("M6 SOUND ARCHIVE"))
        assertTrue("Must feature M6HomeTile bento layout", source.contains("M6HomeTile"))
        assertTrue("Must use M6Canvas background", source.contains("M6Canvas"))
        assertTrue("Must feature M6CatalogTag in home screen", source.contains("M6CatalogTag"))
    }
}
