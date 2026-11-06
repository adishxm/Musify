@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.musify.browser.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.musify.browser.R

/**
 * Inter — bundled as a variable font so every weight comes from a single 860KB
 * file rather than four static instances. Optical sizing is left to the system.
 */
val Inter = FontFamily(
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W100,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(100))
    ),
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W200,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(200))
    ),
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W300,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W400,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W500,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W600,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W700,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight.W800,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    )
)

/**
 * Monospace used for anything the user might need to verify character-by-character:
 * hostnames, URLs, hashes, byte counts.
 */
val MusifyMono = FontFamily.Monospace

/**
 * The Musify type scale.
 *
 * Two rules keep it feeling editorial rather than generic:
 *   1. Large text tightens (negative tracking), small text opens up (positive).
 *   2. Weight never exceeds 700, and heavy weights are reserved for moments
 *      that genuinely need to shout.
 */
val Typography = Typography(

    // --- Display: onboarding, empty states, hero moments -------------------
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W700,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.3).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W700,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1.0).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 28.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.7).sp
    ),

    // --- Headline: screen titles ------------------------------------------
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W700,
        fontSize = 26.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.3).sp
    ),

    // --- Title: card headers, list items ----------------------------------
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 14.5.sp,
        lineHeight = 19.sp,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.sp
    ),

    // --- Body: reading text ------------------------------------------------
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W400,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W400,
        fontSize = 13.5.sp,
        lineHeight = 19.5.sp,
        letterSpacing = 0.05.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.05.sp
    ),

    // --- Label: buttons, chips, captions -----------------------------------
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 13.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W600,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W700,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.7.sp
    )
)

/**
 * All-caps eyebrow text. Used above section headers to establish rhythm
 * without adding visual weight.
 */
val MusifyTypography_Overline = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W700,
    fontSize = 10.sp,
    lineHeight = 13.sp,
    letterSpacing = 1.4.sp
)

/** URLs, hostnames, and any string that benefits from monospaced alignment. */
val MusifyTypography_Mono = TextStyle(
    fontFamily = MusifyMono,
    fontWeight = FontWeight.W400,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
)

/** Large numerical readouts — tabular so digits never jitter as they count. */
val MusifyTypography_Numeric = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W700,
    fontSize = 30.sp,
    lineHeight = 34.sp,
    letterSpacing = (-1.0).sp,
    fontFeatureSettings = "tnum"
)

// ---------------------------------------------------------------------------
// M6 Exhibition Typography Styles (Phase 28)
// ---------------------------------------------------------------------------

/** Monospaced catalog metadata label (e.g. `[M6 // PRIVACY ARCHIVE]`). */
val M6Typography_CatalogTag = TextStyle(
    fontFamily = MusifyMono,
    fontWeight = FontWeight.W600,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.1.sp
)

/** High-contrast architectural exhibition title with tight tracking. */
val M6Typography_ExhibitionTitle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W800,
    fontSize = 28.sp,
    lineHeight = 32.sp,
    letterSpacing = (-1.2).sp
)

/** Editorial exhibition subtitle and section identifier. */
val M6Typography_ExhibitionSubtitle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W500,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.2).sp
)

// ---------------------------------------------------------------------------
// Editorial Home Typography Styles (Phase 34)
// ---------------------------------------------------------------------------

/**
 * DM Serif Display — bundled for editorial headlines, brand wordmarks,
 * and high-contrast titles.
 */
val DmSerifDisplay = FontFamily(
    Font(
        resId = R.font.dm_serif_display_regular,
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    )
)

/** Wordmark in header: "Musify" (30 sp). */
val EditorialTypography_Wordmark = TextStyle(
    fontFamily = DmSerifDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 30.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.5).sp
)

/** Hero Headline: "Find your sound." (46 sp). */
val EditorialTypography_Headline = TextStyle(
    fontFamily = DmSerifDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 46.sp,
    lineHeight = 52.sp,
    letterSpacing = (-0.2).sp
)

/** Editorial Subtitle: "A quieter way to browse." (20 sp). */
val EditorialTypography_Subtitle = TextStyle(
    fontFamily = DmSerifDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

/** Section Header: "Your places" (24 sp). */
val EditorialTypography_SectionHeader = TextStyle(
    fontFamily = DmSerifDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.3).sp
)

/** Shortcut card service title (16 sp). */
val EditorialTypography_ShortcutTitle = TextStyle(
    fontFamily = DmSerifDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp
)

/** Shortcut card domain URL (12.5 sp). */
val EditorialTypography_ShortcutDomain = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 12.5.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
)

/** Metadata tag (e.g., "HOME", "MUSIC + THE WEB", "04"). */
val EditorialTypography_Metadata = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 1.4.sp
)

// ---------------------------------------------------------------------------
// APPLE SAFARI HIG TYPOGRAPHY TOKENS (Phase 35)
// ---------------------------------------------------------------------------

/** Safari Large Title: Start Page main headline (32 sp, Bold). */
val SafariTypography_LargeTitle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.4).sp
)

/** Safari Title: Section headers like "Favorites", "Privacy Report" (22 sp, Bold). */
val SafariTypography_Title = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.3).sp
)

/** Safari Headline: Card titles, toolbar domain labels (17 sp, SemiBold). */
val SafariTypography_Headline = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.2).sp
)

/** Safari Body: Main readable text and dialog body copy (17 sp, Regular). */
val SafariTypography_Body = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

/** Safari Callout: Emphasized list items and buttons (16 sp, Regular). */
val SafariTypography_Callout = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.sp
)

/** Safari Subhead: Action descriptions, secondary sheet labels (15 sp, Regular). */
val SafariTypography_Subhead = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp
)

/** Safari Footnote: Metadata, domain subtext, tracker category tags (13 sp, Regular). */
val SafariTypography_Footnote = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp
)

/** Safari Caption: Shortcut labels, tiny badges, microcopy (11 sp, Medium). */
val SafariTypography_Caption = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.2.sp
)


