package com.musify.browser

import androidx.compose.ui.graphics.Color
import com.musify.browser.ui.theme.Iridescent
import com.musify.browser.ui.theme.MusifyAction
import com.musify.browser.ui.theme.MusifyButtonVariant
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.WelcomeSurface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 19 verification test suite:
 * Asserts the floating action bar, action grid architecture, iridescent glow button
 * palette tokens, launcher background, and string resources.
 */
class Phase19ActionBarGlowTest {

    private fun findSourceFile(relativePath: String): File {
        val rootCandidate = File(relativePath)
        if (rootCandidate.exists()) return rootCandidate
        val appCandidate = File("app", relativePath)
        if (appCandidate.exists()) return appCandidate
        val parentCandidate = File("..", relativePath)
        if (parentCandidate.exists()) return parentCandidate
        return File("../app", relativePath)
    }

    @Test
    fun testWelcomeSurfaceAndIridescentPalette() {
        assertEquals(
            "WelcomeSurface must match canonical #242424 soft charcoal",
            Color(0xFF242424),
            WelcomeSurface
        )

        // Assert 6 sweep gradient stops for the iridescent rotating light catch
        assertEquals("Iridescent palette must define exactly 6 stops", 6, Iridescent.size)
        assertEquals("Stop 0 must be light blue", Color(0xFF60A5FA), Iridescent[0])
        assertEquals("Stop 1 must be lavender purple", Color(0xFFC084FC), Iridescent[1])
        assertEquals("Stop 2 must be pink", Color(0xFFF472B6), Iridescent[2])
        assertEquals("Stop 3 must be amber gold", Color(0xFFFBBF24), Iridescent[3])
        assertEquals("Stop 4 must be emerald green", Color(0xFF34D399), Iridescent[4])
        assertEquals("Stop 5 must close the loop with light blue", Color(0xFF60A5FA), Iridescent[5])
    }

    @Test
    fun testMusifyButtonVariantsAndActionModels() {
        // Assert button variants exist
        val variants = MusifyButtonVariant.values()
        assertTrue("Must include Hero variant", variants.contains(MusifyButtonVariant.Hero))
        assertTrue("Must include Plate variant", variants.contains(MusifyButtonVariant.Plate))
        assertTrue("Must include Ghost variant", variants.contains(MusifyButtonVariant.Ghost))

        // Assert MusifyAction model
        val action = MusifyAction(
            id = "test_action",
            label = "Test Action",
            icon = MusifyIcons.Music,
            highlighted = true
        )
        assertEquals("test_action", action.id)
        assertEquals("Test Action", action.label)
        assertTrue(action.highlighted)
        assertNotNull(action.icon)
    }

    @Test
    fun testLauncherBackgroundIsPureBlack() {
        val bgXml = findSourceFile("src/main/res/drawable/ic_launcher_background.xml")
        assertTrue("ic_launcher_background.xml must exist", bgXml.exists())
        val content = bgXml.readText()
        assertTrue(
            "Launcher background must be pure black #000000 without green Spotify cast",
            content.contains("#000000")
        )
        assertFalse(
            "Legacy greenish #010201 must not be present",
            content.contains("#010201")
        )
    }

    @Test
    fun testStringResourcesIncludeWelcomeHello() {
        val stringsXml = findSourceFile("src/main/res/values/strings.xml")
        assertTrue("strings.xml must exist", stringsXml.exists())
        val content = stringsXml.readText()
        assertTrue(
            "strings.xml must declare welcome_hello",
            content.contains("name=\"welcome_hello\"")
        )
    }

    @Test
    fun testBrowserShellWiresActionBarAndGlowButton() {
        val shellSource = findSourceFile("src/main/java/com/musify/browser/ui/screens/BrowserShellScreen.kt")
        assertTrue("BrowserShellScreen.kt must exist", shellSource.exists())
        val content = shellSource.readText()
        assertTrue(
            "BrowserShellScreen must import MusifyActionBar",
            content.contains("MusifyActionBar")
        )
        assertTrue(
            "BrowserShellScreen must import MusifyGlowButton",
            content.contains("MusifyGlowButton")
        )
        assertTrue(
            "BrowserShellScreen must mount MusifyActionGrid",
            content.contains("MusifyActionGrid")
        )
        assertTrue(
            "BrowserShellScreen must use MusifyButtonVariant.Plate",
            content.contains("MusifyButtonVariant.Plate")
        )
    }
}
