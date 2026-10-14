package com.musify.browser.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Musify design language — "Obsidian".
 *
 * A strictly monochrome system: black, white, and a carefully stepped ladder of
 * neutral greys. Hierarchy is expressed through *elevation* (how much light a
 * surface kicks back) and *hairlines* (1dp borders at very low contrast) rather
 * than through hue.
 *
 * Colour is reserved exclusively for security semantics — a lock, a warning, a
 * danger action — so that when a hue does appear it carries real meaning.
 * Everything else is achromatic.
 *
 * ---------------------------------------------------------------------------
 * DARK  (the canonical Musify appearance — true black for OLED)
 * ---------------------------------------------------------------------------
 */

/** Page background. True black — the deepest possible canvas on OLED. */
val Ink = Color(0xFF000000)

/** Persistent chrome: the browser top bar, sheet headers, navigation rails. */
val Surface = Color(0xFF0A0A0A)

/** Cards and grouped content sitting on top of [Ink] / [Surface]. */
val SurfaceRaised = Color(0xFF111111)

/** Inputs, chips, nested cards, and any interactive fill at rest. */
val Elevated = Color(0xFF171717)

/** Hover / pressed / selected fills. */
val ElevatedHigh = Color(0xFF1F1F1F)

/** Hairline divider and resting border. Barely there — by design. */
val Hairline = Color(0xFF242424)

/** Stronger border for focus rings, active selections, and emphasis. */
val HairlineStrong = Color(0xFF343434)

/** Highest-contrast text. Headlines, values, active labels. */
val TextPrimary = Color(0xFFFAFAFA)

/** Supporting copy, captions, metadata. */
val TextSecondary = Color(0xFFA3A3A3)

/** De-emphasised text, disabled states, fine print. */
val TextMuted = Color(0xFF6E6E6E)

/** The primary accent in a monochrome system: pure white. */
val Accent = Color(0xFFFFFFFF)

/** Content placed *on* [Accent] — inverted, near-black for a printed feel. */
val OnAccent = Color(0xFF0A0A0A)

/** Secondary accent — a soft neutral, used for less prominent emphasis. */
val AccentMuted = Color(0xFFD4D4D4)

/**
 * The launch screen's flat field. A soft charcoal one step above Ink, so the
 * first frame reads as a surface rather than a hole in the screen.
 * Same hex as [Hairline], but a page colour and a border colour are different
 * jobs — if the welcome screen's field ever needs to move, it moves here alone.
 */
val WelcomeSurface = Color(0xFF242424)

/** Sheet / dialog scrim. */
val Scrim = Color(0xE6000000)

// ---------------------------------------------------------------------------
// LIGHT  (Paper — the same system, inverted)
// ---------------------------------------------------------------------------

val PaperInk = Color(0xFFFFFFFF)
val PaperSurface = Color(0xFFFAFAFA)
val PaperSurfaceRaised = Color(0xFFFFFFFF)
val PaperElevated = Color(0xFFF4F4F5)
val PaperElevatedHigh = Color(0xFFEBEBEC)
val PaperHairline = Color(0xFFE4E4E7)
val PaperHairlineStrong = Color(0xFFD4D4D8)
val PaperTextPrimary = Color(0xFF09090B)
val PaperTextSecondary = Color(0xFF52525B)
val PaperTextMuted = Color(0xFF8A8A94)
val PaperAccent = Color(0xFF09090B)
val PaperOnAccent = Color(0xFFFFFFFF)
val PaperAccentMuted = Color(0xFF404040)

// ---------------------------------------------------------------------------
// SECURITY SEMANTICS
//
// Muted jewel tones, deliberately desaturated so they sit calmly inside a
// monochrome field. These are the *only* chromatic values in the product.
//
// To go 100% achromatic, map each of these to a grey value — nothing else in
// the app needs to change.
// ---------------------------------------------------------------------------

/** Secure origin (HTTPS), shields active, permission granted. */
val Success = Color(0xFF4FCB92)

/** Caution: insecure HTTP, shields off, degraded state. */
val Warning = Color(0xFFDFB356)

/** Danger and destructive actions. */
val Danger = Color(0xFFE0645B)

/** Private / incognito mode. */
val Private = Color(0xFFB0A0F0)

// ---------------------------------------------------------------------------
// Alpha helpers used for tinted fills and focus rings.
// ---------------------------------------------------------------------------

private const val TINT_SOFT = 0.10f
private const val TINT_MEDIUM = 0.18f
private const val TINT_STRONG = 0.32f

fun Color.softTint(): Color = copy(alpha = TINT_SOFT)
fun Color.mediumTint(): Color = copy(alpha = TINT_MEDIUM)
fun Color.strongTint(): Color = copy(alpha = TINT_STRONG)

// ---------------------------------------------------------------------------
// Backward-compatible aliases for legacy references during migration
// ---------------------------------------------------------------------------
val BackgroundDark = Ink
val SurfaceDark = Surface
val SurfaceLightDark = Elevated
val AccentCyan = Accent
val AccentBlue = AccentMuted
val AccentPurple = Private
val StatusGreen = Success
val StatusOrange = Warning
val StatusRed = Danger
val CardBorder = Hairline
