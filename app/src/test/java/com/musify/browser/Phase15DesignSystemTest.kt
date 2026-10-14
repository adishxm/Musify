package com.musify.browser

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.AccentMuted
import com.musify.browser.ui.theme.BackgroundDark
import com.musify.browser.ui.theme.CardBorder
import com.musify.browser.ui.theme.Danger
import com.musify.browser.ui.theme.Elevated
import com.musify.browser.ui.theme.ElevatedHigh
import com.musify.browser.ui.theme.Hairline
import com.musify.browser.ui.theme.HairlineStrong
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.MusifyShapes
import com.musify.browser.ui.theme.MusifyTypography_Mono
import com.musify.browser.ui.theme.MusifyTypography_Numeric
import com.musify.browser.ui.theme.MusifyTypography_Overline
import com.musify.browser.ui.theme.OnAccent
import com.musify.browser.ui.theme.Private
import com.musify.browser.ui.theme.Space
import com.musify.browser.ui.theme.Success
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.SurfaceDark
import com.musify.browser.ui.theme.SurfaceRaised
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary
import com.musify.browser.ui.theme.Typography
import com.musify.browser.ui.theme.Warning
import com.musify.browser.ui.theme.mediumTint
import com.musify.browser.ui.theme.softTint
import com.musify.browser.ui.theme.strongTint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase15DesignSystemTest {

    @Test
    fun testObsidianSurfaceLadderAndAchromaticValues() {
        assertEquals(Color(0xFF000000), Ink)
        assertEquals(Color(0xFF0A0A0A), Surface)
        assertEquals(Color(0xFF111111), SurfaceRaised)
        assertEquals(Color(0xFF171717), Elevated)
        assertEquals(Color(0xFF1F1F1F), ElevatedHigh)
        assertEquals(Color(0xFF242424), Hairline)
        assertEquals(Color(0xFF343434), HairlineStrong)

        assertEquals(Color(0xFFFAFAFA), TextPrimary)
        assertEquals(Color(0xFFA3A3A3), TextSecondary)
        assertEquals(Color(0xFF6E6E6E), TextMuted)

        assertEquals(Color(0xFFFFFFFF), Accent)
        assertEquals(Color(0xFF0A0A0A), OnAccent)
        assertEquals(Color(0xFFD4D4D4), AccentMuted)
    }

    @Test
    fun testSecuritySemanticsJewelTones() {
        assertEquals(Color(0xFF4FCB92), Success)
        assertEquals(Color(0xFFDFB356), Warning)
        assertEquals(Color(0xFFE0645B), Danger)
        assertEquals(Color(0xFFB0A0F0), Private)

        // Tint alpha checks (allowing 0.01f delta for 8-bit ARGB quantization)
        val base = Success
        assertEquals(0.10f, base.softTint().alpha, 0.01f)
        assertEquals(0.18f, base.mediumTint().alpha, 0.01f)
        assertEquals(0.32f, base.strongTint().alpha, 0.01f)
    }

    @Test
    fun testBackwardCompatibilityAliases() {
        assertEquals(Ink, BackgroundDark)
        assertEquals(Surface, SurfaceDark)
        assertEquals(Hairline, CardBorder)
    }

    @Test
    fun testShapeScaleAndSpacingLadder() {
        assertEquals(6.dp, MusifyShapes.xs.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)).dp)
        assertEquals(10.dp, MusifyShapes.sm.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)).dp)
        assertEquals(14.dp, MusifyShapes.md.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)).dp)
        assertEquals(20.dp, MusifyShapes.lg.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)).dp)
        assertEquals(28.dp, MusifyShapes.xl.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)).dp)

        assertEquals(2.dp, Space.xxs)
        assertEquals(4.dp, Space.xs)
        assertEquals(8.dp, Space.sm)
        assertEquals(12.dp, Space.md)
        assertEquals(16.dp, Space.lg)
        assertEquals(20.dp, Space.xl)
        assertEquals(28.dp, Space.xxl)
        assertEquals(40.dp, Space.xxxl)
    }

    @Test
    fun testMusifyIconCatalogIntegrity() {
        val allIcons: List<Pair<String, ImageVector>> = listOf(
            "Shield" to MusifyIcons.Shield,
            "ShieldCheck" to MusifyIcons.ShieldCheck,
            "ShieldOff" to MusifyIcons.ShieldOff,
            "Lock" to MusifyIcons.Lock,
            "LockOpen" to MusifyIcons.LockOpen,
            "Warning" to MusifyIcons.Warning,
            "Incognito" to MusifyIcons.Incognito,
            "Blocked" to MusifyIcons.Blocked,
            "ArrowLeft" to MusifyIcons.ArrowLeft,
            "ArrowRight" to MusifyIcons.ArrowRight,
            "ChevronRight" to MusifyIcons.ChevronRight,
            "ChevronDown" to MusifyIcons.ChevronDown,
            "ChevronUp" to MusifyIcons.ChevronUp,
            "Close" to MusifyIcons.Close,
            "Plus" to MusifyIcons.Plus,
            "MoreVertical" to MusifyIcons.MoreVertical,
            "Tabs" to MusifyIcons.Tabs,
            "Reload" to MusifyIcons.Reload,
            "Stop" to MusifyIcons.Stop,
            "Desktop" to MusifyIcons.Desktop,
            "Mobile" to MusifyIcons.Mobile,
            "Globe" to MusifyIcons.Globe,
            "Search" to MusifyIcons.Search,
            "BookmarkOutline" to MusifyIcons.BookmarkOutline,
            "StarFilled" to MusifyIcons.StarFilled,
            "StarOutline" to MusifyIcons.StarOutline,
            "Clock" to MusifyIcons.Clock,
            "Download" to MusifyIcons.Download,
            "Trash" to MusifyIcons.Trash,
            "Document" to MusifyIcons.Document,
            "Link" to MusifyIcons.Link,
            "ExternalLink" to MusifyIcons.ExternalLink,
            "Check" to MusifyIcons.Check,
            "Settings" to MusifyIcons.Settings,
            "Help" to MusifyIcons.Help,
            "Info" to MusifyIcons.Info,
            "Map" to MusifyIcons.Map,
            "Location" to MusifyIcons.Location,
            "Bolt" to MusifyIcons.Bolt,
            "Flask" to MusifyIcons.Flask,
            "Music" to MusifyIcons.Music,
            "Dot" to MusifyIcons.Dot,
            "Emblem" to MusifyIcons.Emblem
        )

        assertEquals("Catalog must contain at least 43 designated icons", 43, allIcons.size)
        allIcons.forEach { (name, icon) ->
            assertNotNull("Icon $name should not be null", icon)
            assertEquals("Icon $name defaultWidth should be 24dp", 24.dp, icon.defaultWidth)
            assertEquals("Icon $name defaultHeight should be 24dp", 24.dp, icon.defaultHeight)
            assertEquals("Icon $name viewportWidth should be 24f", 24f, icon.viewportWidth, 0.001f)
            assertEquals("Icon $name viewportHeight should be 24f", 24f, icon.viewportHeight, 0.001f)
        }
    }

    @Test
    fun testEditorialTypographyScale() {
        assertEquals(40.sp, Typography.displayLarge.fontSize)
        assertEquals(26.sp, Typography.headlineLarge.fontSize)
        assertEquals(16.sp, Typography.titleLarge.fontSize)
        assertEquals(15.sp, Typography.bodyLarge.fontSize)
        assertEquals(13.5.sp, Typography.labelLarge.fontSize)

        assertEquals(10.sp, MusifyTypography_Overline.fontSize)
        assertEquals(12.sp, MusifyTypography_Mono.fontSize)
        assertEquals(30.sp, MusifyTypography_Numeric.fontSize)
        assertEquals("tnum", MusifyTypography_Numeric.fontFeatureSettings)
    }
}
