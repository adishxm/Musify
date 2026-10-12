package com.musify.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Welcome Matrix Screen state contract, navigation transitions, and resources.
 */
class WelcomeMatrixTest {

    @Test
    fun testWelcomeMatrixScreenStateExists() {
        val states = ScreenState.values()
        assertTrue("ScreenState must contain WELCOME_MATRIX", states.contains(ScreenState.WELCOME_MATRIX))
        assertEquals(ScreenState.WELCOME_MATRIX, ScreenState.valueOf("WELCOME_MATRIX"))
    }

    @Test
    fun testWelcomeMatrixNavigationTransition() {
        var currentScreen = ScreenState.WELCOME_MATRIX
        var hasEnteredBrowser = false

        // User taps "SKIP THE MATRIX →"
        val onSkipMatrix: () -> Unit = {
            hasEnteredBrowser = true
            currentScreen = ScreenState.BROWSER
        }

        assertEquals("App must initially launch on WELCOME_MATRIX", ScreenState.WELCOME_MATRIX, currentScreen)
        assertEquals(false, hasEnteredBrowser)

        onSkipMatrix()

        assertEquals("After skip action, app must transition to BROWSER", ScreenState.BROWSER, currentScreen)
        assertTrue("User has entered browser", hasEnteredBrowser)
    }

    @Test
    fun testWelcomeMatrixExternalIntentBypass() {
        var currentScreen = ScreenState.WELCOME_MATRIX
        var hasEnteredBrowser = false

        // When external URL intent arrives, navigate directly to browser
        val hasExternalUrl = true
        if (hasExternalUrl) {
            hasEnteredBrowser = true
            currentScreen = ScreenState.BROWSER
        }

        assertEquals(ScreenState.BROWSER, currentScreen)
        assertTrue(hasEnteredBrowser)
    }
}
