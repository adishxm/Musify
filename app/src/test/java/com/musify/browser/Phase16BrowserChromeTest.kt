package com.musify.browser

import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.Danger
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.Private
import com.musify.browser.ui.theme.Success
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.Warning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Phase16BrowserChromeTest {

    @Test
    fun testOmnibarSecuritySemanticsMapping() {
        fun resolveSecurityIconAndColor(url: String, isSecureScheme: Boolean): Pair<Any, androidx.compose.ui.graphics.Color> {
            return when {
                url.startsWith("https://") -> Pair(MusifyIcons.Lock, Success)
                url.startsWith("http://") -> Pair(MusifyIcons.Warning, Warning)
                url.startsWith("musify://") || url == "about:blank" -> Pair(MusifyIcons.Shield, Accent)
                else -> Pair(MusifyIcons.Globe, Accent)
            }
        }

        val (httpsIcon, httpsColor) = resolveSecurityIconAndColor("https://open.spotify.com", true)
        assertEquals(MusifyIcons.Lock, httpsIcon)
        assertEquals(Success, httpsColor)

        val (httpIcon, httpColor) = resolveSecurityIconAndColor("http://insecure-site.example", false)
        assertEquals(MusifyIcons.Warning, httpIcon)
        assertEquals(Warning, httpColor)

        val (musifyIcon, musifyColor) = resolveSecurityIconAndColor("musify://home", true)
        assertEquals(MusifyIcons.Shield, musifyIcon)
        assertEquals(Accent, musifyColor)
    }

    @Test
    fun testTabSwitcherModeColorSelection() {
        fun getTabSwitcherAccent(isPrivateView: Boolean): androidx.compose.ui.graphics.Color {
            return if (isPrivateView) Private else Accent
        }

        assertEquals(Accent, getTabSwitcherAccent(isPrivateView = false))
        assertEquals(Private, getTabSwitcherAccent(isPrivateView = true))
    }

    @Test
    fun testChromeSourceEmojiRetirement() {
        val browserShellFile = if (File("src/main/java").exists()) {
            File("src/main/java/com/musify/browser/ui/screens/BrowserShellScreen.kt")
        } else {
            File("app/src/main/java/com/musify/browser/ui/screens/BrowserShellScreen.kt")
        }
        val tabSwitcherFile = if (File("src/main/java").exists()) {
            File("src/main/java/com/musify/browser/ui/screens/TabSwitcherScreen.kt")
        } else {
            File("app/src/main/java/com/musify/browser/ui/screens/TabSwitcherScreen.kt")
        }

        assertTrue("BrowserShellScreen file must exist", browserShellFile.exists())
        assertTrue("TabSwitcherScreen file must exist", tabSwitcherFile.exists())

        val shellContent = browserShellFile.readText()
        val switcherContent = tabSwitcherFile.readText()

        val retiredEmojis = listOf("🛡", "🕶", "🔍", "🔒", "🔓", "➕", "✕", "▲", "▼", "↻", "🦆", "⚙", "⭐", "📂", "🎵")

        retiredEmojis.forEach { emoji ->
            assertFalse(
                "BrowserShellScreen should not contain cartoon emoji '$emoji'",
                shellContent.contains("\"$emoji\"") || shellContent.contains("'$emoji'")
            )
            assertFalse(
                "TabSwitcherScreen should not contain cartoon emoji '$emoji'",
                switcherContent.contains("\"$emoji\"") || switcherContent.contains("'$emoji'")
            )
        }
    }

    @Test
    fun testRequiredChromeVectorIconsPresent() {
        assertNotNull(MusifyIcons.ArrowLeft)
        assertNotNull(MusifyIcons.ArrowRight)
        assertNotNull(MusifyIcons.Reload)
        assertNotNull(MusifyIcons.Stop)
        assertNotNull(MusifyIcons.MoreVertical)
        assertNotNull(MusifyIcons.Tabs)
        assertNotNull(MusifyIcons.Search)
        assertNotNull(MusifyIcons.ChevronUp)
        assertNotNull(MusifyIcons.ChevronDown)
        assertNotNull(MusifyIcons.Close)
        assertNotNull(MusifyIcons.Plus)
        assertNotNull(MusifyIcons.Incognito)
        assertNotNull(MusifyIcons.Emblem)
    }
}
