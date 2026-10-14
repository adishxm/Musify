package com.musify.browser.ui.theme

import android.app.Activity
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ---------------------------------------------------------------------------
// Colour schemes
// ---------------------------------------------------------------------------

private val ObsidianColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = ElevatedHigh,
    onPrimaryContainer = TextPrimary,

    secondary = AccentMuted,
    onSecondary = OnAccent,
    secondaryContainer = Elevated,
    onSecondaryContainer = TextPrimary,

    tertiary = AccentMuted,
    onTertiary = OnAccent,

    background = Ink,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Elevated,
    onSurfaceVariant = TextSecondary,

    surfaceContainerLowest = Ink,
    surfaceContainerLow = Surface,
    surfaceContainer = SurfaceRaised,
    surfaceContainerHigh = Elevated,
    surfaceContainerHighest = ElevatedHigh,

    error = Danger,
    onError = Color.White,
    outline = HairlineStrong,
    outlineVariant = Hairline,
    scrim = Scrim,
    inverseSurface = TextPrimary,
    inverseOnSurface = Ink
)

private val PaperColorScheme = lightColorScheme(
    primary = PaperAccent,
    onPrimary = PaperOnAccent,
    primaryContainer = PaperElevatedHigh,
    onPrimaryContainer = PaperTextPrimary,

    secondary = PaperAccentMuted,
    onSecondary = PaperOnAccent,
    secondaryContainer = PaperElevated,
    onSecondaryContainer = PaperTextPrimary,

    tertiary = PaperAccentMuted,
    onTertiary = PaperOnAccent,

    background = PaperInk,
    onBackground = PaperTextPrimary,
    surface = PaperSurface,
    onSurface = PaperTextPrimary,
    surfaceVariant = PaperElevated,
    onSurfaceVariant = PaperTextSecondary,

    surfaceContainerLowest = PaperInk,
    surfaceContainerLow = PaperSurface,
    surfaceContainer = PaperSurfaceRaised,
    surfaceContainerHigh = PaperElevated,
    surfaceContainerHighest = PaperElevatedHigh,

    error = Danger,
    onError = Color.White,
    outline = PaperHairlineStrong,
    outlineVariant = PaperHairline,
    scrim = Color(0x66000000),
    inverseSurface = PaperTextPrimary,
    inverseOnSurface = PaperInk
)

// ---------------------------------------------------------------------------
// Shape scale
//
// A deliberately short ladder. Every surface in the app is one of these five
// radii — that consistency is a large part of why a UI reads as "designed".
// ---------------------------------------------------------------------------

object MusifyShapes {
    /** 1dp hairlines, tags, tiny chips. */
    val xs = RoundedCornerShape(6.dp)

    /** Buttons, inputs, small controls. */
    val sm = RoundedCornerShape(10.dp)

    /** Cards, list rows, grouped panels. */
    val md = RoundedCornerShape(14.dp)

    /** Large cards, hero panels. */
    val lg = RoundedCornerShape(20.dp)

    /** Bottom sheets, dialogs. */
    val xl = RoundedCornerShape(28.dp)

    /** Fully rounded — pills and icon buttons. */
    val pill = RoundedCornerShape(percent = 50)
}

private val MusifyMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ---------------------------------------------------------------------------
// Motion
//
// Fast, crisp, slightly overshooting on entrances. Nothing bounces for longer
// than ~300ms — restraint reads as speed.
// ---------------------------------------------------------------------------

object MusifyMotion {
    /** The house curve: quick out of the gate, gently settled. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** For elements that leave the screen. */
    val Exit: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    val fast = tween<Float>(durationMillis = 120, easing = Standard)
    val medium = tween<Float>(durationMillis = 220, easing = Standard)
    val slow = tween<Float>(durationMillis = 340, easing = Standard)

    /** Springy entrance for sheets, dialogs, and expanding panels. */
    fun <T> enter() = spring<T>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow
    )
}

// ---------------------------------------------------------------------------
// Spacing
//
// A 4dp base grid. Using these instead of ad-hoc dp values is what removes the
// "slightly off" feeling from a screen.
// ---------------------------------------------------------------------------

object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 28.dp
    val xxxl = 40.dp
}

// ---------------------------------------------------------------------------
// Theme
// ---------------------------------------------------------------------------

@Composable
fun MusifyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Musify ships dark-first: the black canvas is the product's identity.
    val colorScheme = if (darkTheme) ObsidianColorScheme else PaperColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // Light bars are *not* wanted on a black canvas — force light glyphs
            // in dark mode and dark glyphs in light mode.
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MusifyMaterialShapes,
        content = content
    )
}

/** Convenience accessor so screens can read the active scheme tersely. */
val musifyColors: androidx.compose.material3.ColorScheme
    @Composable get() = MaterialTheme.colorScheme
