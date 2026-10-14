package com.musify.browser.ui.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * MusifyGlowButton — the "liquid chrome" action button.
 *
 * A pill carrying a circular icon chip and a label. The chip is wrapped in an
 * iridescent ring: a sweep-gradient that reads as an oil-slick or holographic
 * foil catching the light. It is the single point of colour in an otherwise
 * monochrome product, and it is reserved for the primary action on a screen.
 *
 * Why this works inside a black-and-white system: the ring is a *light effect*
 * rather than a brand hue. It behaves like a reflection, so it reads as premium
 * hardware rather than as decoration — and because it appears exactly once per
 * screen, it never competes with the security-semantic colours.
 *
 * Three variants:
 *  - [MusifyButtonVariant.Hero]   dark pill, white label, glowing ring   (on black)
 *  - [MusifyButtonVariant.Plate]  white pill on a white plate, shimmering hairline (on paper)
 *  - [MusifyButtonVariant.Ghost]  hairline pill, no ring                 (secondary)
 */

// ---------------------------------------------------------------------------
// The iridescent palette
// ---------------------------------------------------------------------------

/**
 * Sweep order matters: the gradient is interpolated around the ring, so this
 * sequence produces the blue → violet → magenta → amber → mint → blue loop
 * seen in the reference. The first colour is repeated at the end so the sweep
 * closes without a visible seam.
 */
val Iridescent = listOf(
    Color(0xFF60A5FA), // blue
    Color(0xFFC084FC), // violet
    Color(0xFFF472B6), // magenta
    Color(0xFFFBBF24), // amber
    Color(0xFF34D399), // mint
    Color(0xFF60A5FA)  // closes the loop
)

private fun iridescentBrush(alpha: Float = 1f, turns: Float = 1f): Brush =
    Brush.sweepGradient(
        colors = Iridescent.map { it.copy(alpha = alpha) },
        center = Offset.Unspecified
    )

enum class MusifyButtonVariant { Hero, Plate, Ghost }

// ---------------------------------------------------------------------------
// The button
// ---------------------------------------------------------------------------

@Composable
fun MusifyGlowButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MusifyButtonVariant = MusifyButtonVariant.Hero,
    enabled: Boolean = true,
    /** Optional monospace meta string drawn after the label, e.g. "58%". */
    meta: String? = null,
    /** Slowly rotates the ring so it catches the light. */
    animated: Boolean = true
) {
    val shape = MusifyShapes.pill

    // --- the plate: a slightly larger surface the pill sits on (ref 4) ---
    val plateShape = MusifyShapes.pill

    val container = when (variant) {
        MusifyButtonVariant.Hero -> Surface.copy(alpha = 0.96f)
        MusifyButtonVariant.Plate -> PaperInk
        MusifyButtonVariant.Ghost -> Color.Transparent
    }

    val labelColor = when (variant) {
        MusifyButtonVariant.Hero -> TextPrimary
        MusifyButtonVariant.Plate -> PaperTextPrimary
        MusifyButtonVariant.Ghost -> TextSecondary
    }

    Box(
        modifier = modifier
            .clip(plateShape)
            // the plate is a whisper of a second layer, not a heavy shadow
            .background(
                when (variant) {
                    MusifyButtonVariant.Plate -> PaperSurface
                    MusifyButtonVariant.Hero -> Ink
                    MusifyButtonVariant.Ghost -> Color.Transparent
                },
                plateShape
            )
            .padding(if (variant == MusifyButtonVariant.Plate) 5.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(container, shape)
                .then(
                    when (variant) {
                        // shimmering hairline: the micro colour accents from ref 4
                        MusifyButtonVariant.Plate -> Modifier.border(
                            1.dp,
                            iridescentBrush(alpha = 0.55f),
                            shape
                        )
                        MusifyButtonVariant.Ghost -> Modifier.border(1.dp, Hairline, shape)
                        MusifyButtonVariant.Hero -> Modifier.border(
                            1.dp,
                            HairlineStrong,
                            shape
                        )
                    }
                )
                .clickable(enabled = enabled, onClick = onClick)
                .padding(
                    start = 6.dp,
                    end = if (meta != null) Space.md else Space.lg,
                    top = 6.dp,
                    bottom = 6.dp
                )
                .semantics {
                    contentDescription = label
                    role = Role.Button
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            MusifyGlowChip(
                icon = icon,
                variant = variant,
                enabled = enabled,
                animated = animated
            )

            Spacer(Modifier.width(Space.md))

            Text(
                text = label,
                color = if (enabled) labelColor else labelColor.copy(alpha = 0.4f),
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.2).sp
            )

            if (meta != null) {
                Spacer(Modifier.width(Space.sm))
                Text(
                    text = meta,
                    color = labelColor.copy(alpha = 0.45f),
                    style = MusifyTypography_Mono.copy(fontSize = 11.sp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// The chip and its ring
// ---------------------------------------------------------------------------

@Composable
fun MusifyGlowChip(
    icon: ImageVector,
    variant: MusifyButtonVariant,
    enabled: Boolean,
    animated: Boolean,
    size: Dp = 40.dp,
    /** Extra rotation applied to the glyph itself (not the ring). */
    iconRotation: Float = 0f
) {
    // A slow rotation makes the ring read as a physical reflection rather than
    // a painted gradient. 8s is slow enough to be felt, not watched.
    val angle = if (animated) {
        val transition = rememberInfiniteTransition(label = "glowRing")
        val a by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "glowRingAngle"
        )
        a
    } else {
        0f
    }

    val ringAlpha = if (enabled) 1f else 0.25f

    Box(contentAlignment = Alignment.Center) {
        // --- bloom -------------------------------------------------------
        // Modifier.blur needs API 31, and Musify supports 26. Instead we fake
        // the glow with two larger, very translucent rings. On OLED the
        // result is indistinguishable and it costs two draw calls.
        if (variant == MusifyButtonVariant.Hero) {
            Box(
                Modifier
                    .size(size + 10.dp)
                    .rotate(angle)
                    .drawBehind {
                        drawCircle(
                            brush = iridescentBrush(alpha = 0.11f * ringAlpha),
                            radius = this.size.minDimension / 2f,
                            style = Stroke(width = 9f)
                        )
                    }
            )
            Box(
                Modifier
                    .size(size + 5.dp)
                    .rotate(angle)
                    .drawBehind {
                        drawCircle(
                            brush = iridescentBrush(alpha = 0.20f * ringAlpha),
                            radius = this.size.minDimension / 2f,
                            style = Stroke(width = 5f)
                        )
                    }
            )
        }

        // --- the chip ----------------------------------------------------
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    when (variant) {
                        MusifyButtonVariant.Hero -> Elevated
                        MusifyButtonVariant.Plate -> PaperElevated
                        MusifyButtonVariant.Ghost -> Elevated
                    },
                    CircleShape
                )
                .then(
                    if (variant == MusifyButtonVariant.Ghost) {
                        Modifier.border(1.dp, Hairline, CircleShape)
                    } else {
                        Modifier.drawBehind {
                            drawCircle(
                                brush = iridescentBrush(alpha = 0.85f * ringAlpha),
                                radius = this.size.minDimension / 2f,
                                style = Stroke(width = 2.2f)
                            )
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            MusifyIcon(
                imageVector = icon,
                size = size * 0.45f,
                tint = when (variant) {
                    MusifyButtonVariant.Plate -> PaperTextPrimary
                    else -> TextPrimary
                },
                modifier = Modifier.rotate(iconRotation)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Editorial helpers (ref 5 — brutalist light mode)
// ---------------------------------------------------------------------------

/**
 * A dot-matrix ornament: a small field of dots on a regular grid.
 *
 * Lifted from the editorial reference, where scattered dot fields act as a
 * texture between content blocks. It is pure decoration that costs one loop,
 * and it gives an otherwise empty area a sense of intent.
 */
@Composable
fun DotMatrixOrnament(
    modifier: Modifier = Modifier,
    columns: Int = 7,
    rows: Int = 3,
    dotSize: Dp = 2.dp,
    spacing: Dp = 5.dp,
    color: Color = HairlineStrong
) {
    Column(modifier) {
        repeat(rows) {
            Row {
                repeat(columns) {
                    Box(
                        Modifier
                            .size(dotSize)
                            .background(color, CircleShape)
                    )
                    Spacer(Modifier.width(spacing))
                }
            }
            Spacer(Modifier.height(spacing))
        }
    }
}

/**
 * Monospace metadata label — the "MONDAY / 01 MAY / 9:PM" treatment.
 * Uppercase, tracked out, small. Used for timestamps, states, and counts so
 * that machine-ish facts look machine-ish.
 */
@Composable
fun EditorialMeta(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextMuted
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        style = MusifyTypography_Mono.copy(
            fontSize = 10.sp,
            letterSpacing = 1.2.sp
        )
    )
}

/**
 * A ruled tick-track scrubber, as seen at the foot of the editorial layout.
 * Marks are evenly spaced and the filled portion is drawn solid.
 *
 * @param progress 0f..1f
 */
@Composable
fun TickScrubber(
    progress: Float,
    modifier: Modifier = Modifier,
    ticks: Int = 9,
    activeColor: Color = TextPrimary,
    inactiveColor: Color = HairlineStrong,
    onSeek: ((Float) -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(ticks) { i ->
            val filled = (i.toFloat() / (ticks - 1)) <= progress
            Box(
                Modifier
                    .size(width = 2.dp, height = if (filled) 14.dp else 8.dp)
                    .background(if (filled) activeColor else inactiveColor)
                    .then(
                        if (onSeek != null) {
                            Modifier.clickable { onSeek(i.toFloat() / (ticks - 1)) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}
