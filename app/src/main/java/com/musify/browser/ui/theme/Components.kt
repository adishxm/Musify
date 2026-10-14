package com.musify.browser.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared presentation primitives.
 *
 * These exist so that a "card", a "chip", or a "section label" looks identical
 * everywhere in the app. Every screen composing its own ad-hoc version of these
 * is the single biggest reason a large UI drifts away from feeling designed.
 */

// ---------------------------------------------------------------------------
// Icons
// ---------------------------------------------------------------------------

/**
 * A monochrome icon. Sizing is explicit because icon optical weight matters:
 * a 1.7f stroke reads correctly at 16-24dp but looks heavy below 14dp.
 */
@Composable
fun MusifyIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = TextPrimary,
    contentDescription: String? = null
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * Icon + label on a single baseline row. This is the replacement for the
 * `<emoji> Label` string pattern used throughout the app.
 */
@Composable
fun IconLabel(
    imageVector: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary,
    iconTint: Color = tint,
    iconSize: Dp = 15.dp,
    spacing: Dp = 8.dp,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MusifyIcon(imageVector, size = iconSize, tint = iconTint)
        Spacer(Modifier.width(spacing))
        Text(text = text, color = tint, style = style)
    }
}

// ---------------------------------------------------------------------------
// Structure
// ---------------------------------------------------------------------------

/** A 1dp hairline. The primary way Musify separates content. */
@Composable
fun Hairline(
    modifier: Modifier = Modifier,
    color: Color = Hairline
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

/** Vertical counterpart to [Hairline]. */
@Composable
fun VHairline(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    color: Color = Hairline
) {
    Box(
        modifier
            .width(1.dp)
            .height(height)
            .background(color)
    )
}

/**
 * All-caps eyebrow label. Establishes section rhythm without visual weight —
 * a quieter and more premium alternative to a bold heading.
 */
@Composable
fun Overline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextMuted
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        style = MusifyTypography_Overline
    )
}

/**
 * Screen-level section heading: an eyebrow, a title, and optional supporting
 * copy — spaced to a strict 4/8/12 rhythm.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    subtitle: String? = null,
    titleColor: Color = TextPrimary
) {
    Column(modifier) {
        if (overline != null) {
            Overline(overline)
            Spacer(Modifier.height(Space.sm))
        }
        Text(title, color = titleColor, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) {
            Spacer(Modifier.height(Space.xs))
            Text(
                subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Surfaces
// ---------------------------------------------------------------------------

/**
 * The standard content card: a raised surface with an optional hairline border.
 * Elevation here is expressed as a lighter fill, not a shadow — shadows are
 * nearly invisible on true black and only add noise.
 */
@Composable
fun MusifyCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceRaised,
    borderColor: Color? = Hairline,
    shape: RoundedCornerShape = MusifyShapes.md,
    contentPadding: PaddingValues = PaddingValues(Space.lg),
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .background(containerColor, shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier
                }
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * A full-bleed settings/list row: label, optional supporting text, optional
 * trailing content, and a hairline divider.
 */
@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingTint: Color = TextSecondary,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                MusifyIcon(leadingIcon, size = 18.dp, tint = leadingTint)
                Spacer(Modifier.width(Space.md))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(Space.xxs))
                    Text(
                        subtitle,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(Space.md))
                trailing()
            }
        }
        if (showDivider) {
            Hairline(Modifier.padding(start = Space.lg))
        }
    }
}

// ---------------------------------------------------------------------------
// Controls
// ---------------------------------------------------------------------------

/**
 * A selectable chip. Selected state inverts to white — in a monochrome system,
 * inversion is the strongest available emphasis.
 */
@Composable
fun MusifyPill(
    text: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val container = if (selected) Accent else Elevated
    val content = if (selected) OnAccent else TextSecondary
    val borderC = if (selected) Accent else Hairline

    Row(
        modifier
            .background(container, MusifyShapes.pill)
            .border(1.dp, borderC, MusifyShapes.pill)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Space.md, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            MusifyIcon(leadingIcon, size = 13.dp, tint = content)
            Spacer(Modifier.width(Space.xs))
        }
        Text(
            text,
            color = content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Count badge. Uses tabular figures so the width never jitters while counting.
 */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = Elevated,
    contentColor: Color = TextPrimary
) {
    Box(
        modifier
            .background(containerColor, MusifyShapes.pill)
            .padding(horizontal = Space.sm, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = contentColor,
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum")
        )
    }
}

/**
 * A label/value pair for dense metadata (blocked counts, byte sizes, DNS state).
 */
@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
    showDivider: Boolean = true
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                value,
                color = valueColor,
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum")
            )
        }
        if (showDivider) Hairline()
    }
}

/** Centred empty state: icon, headline, supporting copy. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    iconSize: Dp = 40.dp
) {
    Column(
        modifier.padding(horizontal = Space.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MusifyIcon(icon, size = iconSize, tint = HairlineStrong)
        Spacer(Modifier.height(Space.lg))
        Text(
            title,
            color = TextSecondary,
            style = MaterialTheme.typography.titleMedium
        )
        if (message != null) {
            Spacer(Modifier.height(Space.sm))
            Text(
                message,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
