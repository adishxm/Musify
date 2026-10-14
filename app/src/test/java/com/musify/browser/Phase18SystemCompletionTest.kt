package com.musify.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Phase18SystemCompletionTest {

    private fun resolveDir(pathWithinApp: String): File {
        return if (File("src/main/java").exists()) {
            File(pathWithinApp.removePrefix("app/"))
        } else {
            File(pathWithinApp)
        }
    }

    @Test
    fun testZeroCartoonEmojisAcrossAllAppScreens() {
        val screensDir = resolveDir("app/src/main/java/com/musify/browser/ui/screens")
        assertTrue("Screens directory must exist", screensDir.exists() && screensDir.isDirectory)

        val screenFiles = screensDir.listFiles { file -> file.extension == "kt" } ?: emptyArray()
        assertTrue("Must have multiple screen files", screenFiles.isNotEmpty())

        val cartoonEmojis = listOf(
            "🛡", "🕶", "🔍", "🔒", "🔓", "➕", "✕", "▲", "▼", "↻", "🦆", "⚙", "⭐", "📂", "🎵",
            "⚡", "🗑", "📷", "🎤", "📍", "💾", "⚠", "🌐", "🖥", "📱", "🛑", "📄", "🔗", "✔"
        )

        val violations = mutableListOf<String>()

        screenFiles.forEach { file ->
            val content = file.readText()
            cartoonEmojis.forEach { emoji ->
                if (content.contains("\"$emoji\"") || content.contains("'$emoji'")) {
                    violations.add("${file.name} contains emoji '$emoji'")
                }
            }
        }

        assertTrue("All cartoon emojis must be retired across all screens. Violations: $violations", violations.isEmpty())
    }

    @Test
    fun testWelcomeScreenMonochromeGuarantees() {
        val welcomeFile = resolveDir("app/src/main/java/com/musify/browser/ui/screens/WelcomeScreen.kt")
        assertTrue("WelcomeScreen.kt must exist", welcomeFile.exists())

        val content = welcomeFile.readText()

        // Must NOT contain legacy saturated red/orange gradient stops
        assertFalse("No orange red gradient 0xFFFF4500", content.contains("0xFFFF4500"))
        assertFalse("No neon pink gradient 0xFFFF0055", content.contains("0xFFFF0055"))
        assertFalse("No crimson gradient 0xFFCC0000", content.contains("0xFFCC0000"))

        // The retired launch screen must be gone, not merely unused
        assertFalse(
            "WelcomeMatrixScreen.kt must be deleted",
            resolveDir("app/src/main/java/com/musify/browser/ui/screens/WelcomeMatrixScreen.kt").exists()
        )

        // Must use Obsidian monochrome tokens, and no raw colour literals at all
        assertTrue("Uses WelcomeSurface token", content.contains("WelcomeSurface"))
        assertTrue("Uses TextPrimary token", content.contains("TextPrimary"))
        assertFalse("No hardcoded colour literals", content.contains("0xFF"))
    }

    @Test
    fun testDesignSystemSpecificationDocumented() {
        val rootDir = if (File("docs").exists()) File("docs") else File("../docs")
        val specFile = File(rootDir, "UI-DESIGN-SYSTEM.md")

        assertTrue("UI-DESIGN-SYSTEM.md must be documented in docs/", specFile.exists())
        val text = specFile.readText()
        assertTrue("Mentions Obsidian Design System", text.contains("Obsidian"))
        assertTrue("Documents 44 hand-built vector icons", text.contains("44"))
        assertTrue("Documents Inter variable font", text.contains("inter_variable.ttf"))
    }
}
