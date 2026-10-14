package com.musify.browser.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.R
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.HairlineStrong
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.TextSecondary

/**
 * WelcomeMatrixScreen presents the official launch branding interface:
 *  - Charcoal container with bottom warm glowing burnt-orange gradient
 *  - Angled orange sticker badge: "TAKE A PREMIUM FEEL"
 *  - Tilted dashed selection frame with corner anchor handles
 *  - Subtitle: "a premium experience / of listening musics"
 *  - Stylized display title: "Musify" with textured grunge effect
 *  - Outlined orange tagline: "crafted by adishxm"
 *  - Copyright and repository metadata
 *  - High-impact bottom CTA: "SKIP THE MATRIX →"
 */
@Composable
fun WelcomeMatrixScreen(
    onSkipMatrix: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Entrance animations
    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        contentScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    // Subtle ambient glow breathing effect for bottom ember
    val infiniteTransition = rememberInfiniteTransition(label = "ember_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        // Atmospheric warm burnt-orange / amber gradient in the lower portion
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Vertical linear glow
            val verticalBrush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Ink,
                    0.50f to Ink,
                    0.64f to Color(0xFF0A0A0A),
                    0.74f to Color(0xFF141414),
                    0.84f to Color(0xFF1F1F1F),
                    0.92f to Color(0xFF2E2E2E),
                    0.97f to Color(0xFF1A1A1A),
                    1.00f to Color(0xFF0A0A0A)
                ),
                startY = 0f,
                endY = height
            )
            drawRect(brush = verticalBrush)

            // Dynamic ambient radial glow centered at bottom
            val radialBrush = Brush.radialGradient(
                colors = listOf(
                    Accent.copy(alpha = 0.07f * glowPulse),
                    Accent.copy(alpha = 0.04f * glowPulse),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.88f),
                radius = width * 0.85f
            )
            drawRect(brush = radialBrush)
        }

        // Central Graphic Group (Selection Box + Badge + Subtitle + Musify + Tagline + Footer)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-30).dp)
                .alpha(contentAlpha.value)
                .scale(contentScale.value),
            contentAlignment = Alignment.TopStart
        ) {
            val boxWidth = 328.dp
            val boxHeight = 168.dp
            val boxTilt = 4.2f

            // 1. Tilted Selection Bounding Box with Dashed Border & 4 Corner Anchors
            Box(
                modifier = Modifier
                    .width(boxWidth)
                    .height(boxHeight)
                    .rotate(boxTilt)
            ) {
                // Dashed border & corner anchors
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeWidthPx = 1.5.dp.toPx()
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

                    // Dashed rectangular marquee frame
                    drawRect(
                        color = TextSecondary,
                        topLeft = Offset(0f, 0f),
                        size = Size(w, h),
                        style = Stroke(
                            width = strokeWidthPx,
                            pathEffect = dashPathEffect
                        )
                    )

                    // 4 Solid corner handles
                    val handleSizePx = 11.dp.toPx()
                    val halfHandle = handleSizePx / 2f
                    val handleColor = Accent

                    val corners = listOf(
                        Offset(0f, 0f),
                        Offset(w, 0f),
                        Offset(0f, h),
                        Offset(w, h)
                    )

                    for (corner in corners) {
                        drawRect(
                            color = handleColor,
                            topLeft = Offset(corner.x - halfHandle, corner.y - halfHandle),
                            size = Size(handleSizePx, handleSizePx)
                        )
                    }
                }

                // Subtitle inside tilted box: "a premium experience / of listening musics"
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, top = 22.dp)
                ) {
                    Text(
                        text = stringResource(R.string.welcome_subtitle_1),
                        color = Color.White,
                        fontSize = 18.5.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = stringResource(R.string.welcome_subtitle_2),
                        color = Color.White,
                        fontSize = 18.5.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                // Orange Badge Tag: "TAKE A PREMIUM FEEL" (rotated -11° relative to box)
                Box(
                    modifier = Modifier
                        .offset(x = (-12).dp, y = (-12).dp)
                        .rotate(-11.5f)
                        .background(
                            color = Accent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(R.string.take_a_premium_feel),
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            // 2. Upright Content Layer (Horizontal / Unrotated):
            // "Musify" title + "crafted by adishxm" + Copyright
            Column(
                modifier = Modifier
                    .padding(start = 18.dp, top = 100.dp)
            ) {
                // "Musify" Display Title with textured distress finish
                Box {
                    Text(
                        text = stringResource(R.string.welcome_musify_title),
                        color = Color.White,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp,
                        lineHeight = 72.sp
                    )

                    // Distressed / rubber-stamp weathering overlay
                    DistressedTextureOverlay(
                        modifier = Modifier
                            .matchParentSize()
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Outlined Tagline: "crafted by adishxm"
                Box {
                    // Outlined stroke
                    Text(
                        text = stringResource(R.string.welcome_crafted_by),
                        style = TextStyle(
                            color = Accent,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            drawStyle = Stroke(
                                width = 3.2f,
                                join = StrokeJoin.Round
                            )
                        )
                    )
                    // Faint translucent fill for rich letterform interior
                    Text(
                        text = stringResource(R.string.welcome_crafted_by),
                        color = Accent.copy(alpha = 0.10f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Copyright & GitHub Repository info
                Text(
                    text = stringResource(R.string.welcome_copyright),
                    color = TextSecondary.copy(alpha = 0.85f),
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = stringResource(R.string.welcome_github),
                    color = TextSecondary.copy(alpha = 0.85f),
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // Bottom Action Button: "SKIP THE MATRIX →"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 68.dp)
                .alpha(contentAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false, radius = 72.dp),
                        onClick = onSkipMatrix
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Skip the matrix and enter Musify browser"
                    }
            ) {
                Text(
                    text = "SKIP THE",
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "MATRIX →",
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Draws subtle organic micro-weathering specks over the Musify title
 * to match the distressed stencil / rubber-stamp aesthetic in the design.
 */
@Composable
private fun DistressedTextureOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val speckColor = HairlineStrong

        // Deterministic pseudo-random seed points for consistent distressed texture
        val specks = listOf(
            Offset(0.12f, 0.28f) to 1.8f,
            Offset(0.15f, 0.52f) to 1.4f,
            Offset(0.18f, 0.70f) to 2.0f,
            Offset(0.24f, 0.35f) to 1.6f,
            Offset(0.28f, 0.65f) to 1.8f,
            Offset(0.35f, 0.40f) to 2.2f,
            Offset(0.38f, 0.75f) to 1.5f,
            Offset(0.44f, 0.32f) to 1.7f,
            Offset(0.48f, 0.60f) to 2.1f,
            Offset(0.53f, 0.48f) to 1.5f,
            Offset(0.58f, 0.25f) to 1.9f,
            Offset(0.62f, 0.72f) to 2.0f,
            Offset(0.69f, 0.38f) to 1.6f,
            Offset(0.74f, 0.55f) to 1.8f,
            Offset(0.79f, 0.78f) to 2.2f,
            Offset(0.84f, 0.34f) to 1.7f,
            Offset(0.88f, 0.62f) to 1.9f,
            Offset(0.92f, 0.45f) to 1.5f,
            Offset(0.22f, 0.20f) to 1.2f,
            Offset(0.42f, 0.85f) to 1.4f,
            Offset(0.65f, 0.18f) to 1.3f,
            Offset(0.82f, 0.82f) to 1.5f
        )

        for ((normOffset, radius) in specks) {
            drawCircle(
                color = speckColor,
                radius = radius,
                center = Offset(normOffset.x * w, normOffset.y * h)
            )
        }
    }
}
