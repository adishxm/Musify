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
// M6 EXHIBITION DESIGN SYSTEM TOKENS
// Museum-Grade Digital Sound Archive & Kinetic Shield Interface (Phase 28)
// ---------------------------------------------------------------------------

/** Deep gallery void for OLED displays in the M6 exhibition theme. */
val M6Canvas = Color(0xFF08080A)

/** Primary exhibition architectural surface for chrome and persistent panels. */
val M6Surface = Color(0xFF101014)

/** Exhibition card and bento tile container background. */
val M6Card = Color(0xFF16161C)

/** Elevated interactive elements, input fills, and omnibar search pills. */
val M6Elevated = Color(0xFF1E1E26)

/** Resting architectural hairline grid boundary. */
val M6Hairline = Color(0xFF282832)

/** Emphasized boundary for active selections, focus rings, and active tabs. */
val M6HairlineStrong = Color(0xFF3E3E4C)

// Exhibition Jewel Semantics
/** Seismic Emerald: Secure HTTPS origin, zero surveillance, protected audio streams. */
val M6Emerald = Color(0xFF00F59B)

/** Signal Orange: Real-time shield activation, ad defusal, kinetic protection telemetry. */
val M6SignalOrange = Color(0xFFFF5C00)

/** Signal Red: Critical threats blocked, malicious redirect hops neutralized. */
val M6SignalRed = Color(0xFFFF3B30)

/** Sound Violet: Widevine DRM hardware acceleration, lossless stream preservation. */
val M6SoundViolet = Color(0xFFA855F7)

/** High-legibility exhibition metadata copy. */
val M6TextSecondary = Color(0xFF9898A6)

/** De-emphasized technical exhibition captions and timestamps. */
val M6TextMuted = Color(0xFF5C5C6E)

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

// ---------------------------------------------------------------------------
// EDITORIAL DARK PALETTE TOKENS (Phase 34 / Part 1 Reference)
// ---------------------------------------------------------------------------

/** Deep warm obsidian background (#09090A). */
val HomeCanvas = Color(0xFF09090A)

/** Search field and fallback dock surface (#18181A). */
val HomeSurface = Color(0xFF18181A)

/** Pressed / raised interactive surfaces (#242326). */
val HomeSurfaceRaised = Color(0xFF242326)

/** Ivory white primary text (#F3EEEA). */
val HomeTextPrimary = Color(0xFFF3EEEA)

/** Muted grey secondary text and domains (#B4AFB2). */
val HomeTextSecondary = Color(0xFFB4AFB2)

/** Subtle hairline divider rules and card borders (#3C3A3E). */
val HomeDivider = Color(0xFF3C3A3E)

/** Control outline and focus border (#A9A5AA). */
val HomeControlOutline = Color(0xFFA9A5AA)

/** Floating dock translucent dark background (approx 94% opacity of #1B1A1C). */
val HomeDockSurface = Color(0xF01B1A1C)

/** Floating dock 1dp border. */
val HomeDockBorder = Color(0x33F3EEEA)

/** Editorial Caution indicator (#F0BE63). */
val HomeWarning = Color(0xFFF0BE63)

/** Editorial Destructive / Error indicator (#F17E74). */
val HomeDanger = Color(0xFFF17E74)

// ---------------------------------------------------------------------------
// APPLE SAFARI HIG DESIGN TOKENS (Phase 35)
// ---------------------------------------------------------------------------

/** Apple Safari pure black background canvas (#000000). */
val SafariCanvas = Color(0xFF000000)
val SafariBackgroundDark = SafariCanvas

/** Apple Safari secondary system background for cards and tiles (#1C1C1E). */
val SafariCard = Color(0xFF1C1C1E)
val SafariCardDark = SafariCard

/** Apple Safari tertiary system background for elevated plates and inputs (#2C2C2E). */
val SafariCardElevated = Color(0xFF2C2C2E)

/** Apple Safari quaternary system fill (#3A3A3C). */
val SafariCardTertiary = Color(0xFF3A3A3C)

/** Apple Safari system separator and hairline (#38383A). */
val SafariBorder = Color(0xFF38383A)
val SafariBorderDark = SafariBorder

/** Apple Safari subtle divider rule (#2C2C2E). */
val SafariDivider = Color(0xFF2C2C2E)

/** Apple Safari signature iOS blue accent (#007AFF). */
val SafariBlue = Color(0xFF007AFF)
val SafariAccentBlue = SafariBlue

/** Apple Safari secure lock and verified shields green (#34C759). */
val SafariGreen = Color(0xFF34C759)
val SafariSuccessGreen = SafariGreen

/** Apple Safari security warning and insecure origin orange (#FF9500). */
val SafariOrange = Color(0xFFFF9500)
val SafariWarningOrange = SafariOrange

/** Apple Safari destructive action and tracker alert red (#FF3B30). */
val SafariRed = Color(0xFFFF3B30)
val SafariDangerRed = SafariRed

/** Apple Safari private browsing purple accent (#AF52DE). */
val SafariPurple = Color(0xFFAF52DE)

/** Apple Safari translucent frosted glass surface for bottom tab bar (92% #1C1C1E). */
val SafariGlass = Color(0xEB1C1C1E)
val SafariBarGlass = SafariGlass

/** Apple Safari translucent frosted glass border. */
val SafariGlassBorder = Color(0x33FFFFFF)
val SafariBarBorder = SafariGlassBorder

/** Apple Safari primary label (#FFFFFF). */
val SafariTextPrimary = Color(0xFFFFFFFF)

/** Apple Safari secondary label (#8E8E93). */
val SafariTextSecondary = Color(0xFF8E8E93)
val SafariFootnote = SafariTextSecondary

/** Apple Safari tertiary label (#636366). */
val SafariTextTertiary = Color(0xFF636366)



