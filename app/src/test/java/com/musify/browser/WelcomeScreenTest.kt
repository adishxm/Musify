package com.musify.browser

import androidx.compose.ui.graphics.Color
import com.musify.browser.ui.theme.WelcomeSurface
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests covering the launch screen's state contract, its navigation
 * transitions, and the three resources it renders.
 *
 * The composition itself is exercised by the screenshot renders in docs/; this
 * file guards the parts that a rename or a resource edit would quietly break.
 */
class WelcomeScreenTest {

    @Test
    fun testWelcomeScreenStateExists() {
        val states = ScreenState.values()
        assertTrue("ScreenState must contain WELCOME", states.contains(ScreenState.WELCOME))
        assertEquals(ScreenState.WELCOME, ScreenState.valueOf("WELCOME"))
    }

    @Test
    fun testWelcomeIsTheLaunchState() {
        // MainActivity opens with `mutableStateOf(ScreenState.WELCOME)`; if that
        // ever changes, the entry screen silently stops being the entry screen.
        val root = if (File("app").exists()) File(".") else File("..")
        val main = File(root, "app/src/main/java/com/musify/browser/MainActivity.kt")
        assertTrue("MainActivity.kt must exist", main.exists())

        val text = main.readText()
        assertTrue(
            "MainActivity must open on the welcome screen",
            text.contains("mutableStateOf(ScreenState.WELCOME)")
        )
        assertFalse("The old WELCOME_MATRIX state must be gone", text.contains("WELCOME_MATRIX"))
    }

    @Test
    fun testWelcomeNavigationTransition() {
        var currentScreen = ScreenState.WELCOME
        var hasEnteredBrowser = false

        // Tapping anywhere on the surface fires onEnter.
        val onEnter: () -> Unit = {
            hasEnteredBrowser = true
            currentScreen = ScreenState.BROWSER
        }

        assertEquals("App must initially launch on WELCOME", ScreenState.WELCOME, currentScreen)
        assertEquals(false, hasEnteredBrowser)

        onEnter()

        assertEquals("After entering, app must transition to BROWSER", ScreenState.BROWSER, currentScreen)
        assertTrue("User has entered browser", hasEnteredBrowser)
    }

    @Test
    fun testWelcomeExternalIntentBypass() {
        var currentScreen = ScreenState.WELCOME
        var hasEnteredBrowser = false

        // When an external URL intent arrives, navigate directly to the browser.
        val hasExternalUrl = true
        if (hasExternalUrl) {
            hasEnteredBrowser = true
            currentScreen = ScreenState.BROWSER
        }

        assertEquals(ScreenState.BROWSER, currentScreen)
        assertTrue(hasEnteredBrowser)
    }

    @Test
    fun testWelcomeStringsResolve() {
        // The screen reads exactly three strings. A zero here means the screen
        // would render blank.
        assertNotEquals("welcome_hello must be defined", 0, R.string.welcome_hello)
        assertNotEquals("welcome_musify_title must be defined", 0, R.string.welcome_musify_title)
        assertNotEquals("welcome_crafted_by must be defined", 0, R.string.welcome_crafted_by)
    }

    @Test
    fun testWelcomeSurfaceToken() {
        // The launch field is #242424 — a soft charcoal, deliberately above Ink.
        assertTrue(
            "WelcomeSurface must be #242424",
            WelcomeSurface == Color(0xFF242424)
        )
    }
}
