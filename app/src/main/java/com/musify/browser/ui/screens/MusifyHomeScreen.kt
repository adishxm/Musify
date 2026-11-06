package com.musify.browser.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.EditorialTypography_Headline
import com.musify.browser.ui.theme.EditorialTypography_Metadata
import com.musify.browser.ui.theme.EditorialTypography_SectionHeader
import com.musify.browser.ui.theme.EditorialTypography_ShortcutDomain
import com.musify.browser.ui.theme.EditorialTypography_ShortcutTitle
import com.musify.browser.ui.theme.EditorialTypography_Subtitle
import com.musify.browser.ui.theme.EditorialTypography_Wordmark
import com.musify.browser.ui.theme.HomeCanvas
import com.musify.browser.ui.theme.HomeDivider
import com.musify.browser.ui.theme.HomeDockBorder
import com.musify.browser.ui.theme.HomeDockSurface
import com.musify.browser.ui.theme.HomeSurface
import com.musify.browser.ui.theme.HomeTextPrimary
import com.musify.browser.ui.theme.HomeTextSecondary
import com.musify.browser.ui.theme.Inter
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.Private

/**
 * Editorial Dark New-Tab Home Screen (Phase 34).
 *
 * Implements the approved visual reference 01_Home_Dark_Selected.png:
 * - DM Serif Display typography for wordmark, headline, section titles, and card headers.
 * - Procedural canvas vinyl hero with metallic grooves, dust particles, and editorial copy.
 * - Single address/search omnibox pill wired to active tab navigation.
 * - 2x2 Bento shortcut grid navigating to real streaming endpoints (Spotify, YouTube Music, YouTube, SoundCloud).
 * - Floating glassmorphic 4-action capsule dock with Back, Shields, Tabs badge, and More.
 */
@Composable
fun MusifyHomeScreen(
    isPrivate: Boolean = false,
    tabCount: Int = 1,
    canGoBack: Boolean = false,
    addressInput: String = "",
    onAddressChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit = {},
    onOpenTabSwitcher: () -> Unit = {},
    onOpenActionMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showShieldInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeCanvas)
            .semantics { contentDescription = "Musify Home Screen" }
            .testTag("home.root")
    ) {
        // Vertically scrollable main editorial content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // 1. Header row: "Musify" wordmark left, hairline divider + metadata tag right
            HomeHeaderRow(isPrivate = isPrivate)

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Headline & Subtitle
            Text(
                text = "Find your sound.",
                style = EditorialTypography_Headline,
                color = HomeTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "A quieter way to browse.",
                style = EditorialTypography_Subtitle,
                color = HomeTextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Cinematic Vinyl Hero Artwork (Canvas procedural rendering)
            DecorativeVinylHero()

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Single Search / Address Field (Omnibox)
            HomeOmnibox(
                addressInput = addressInput,
                onAddressChange = onAddressChange,
                onSubmit = onSubmit
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 5. "Your places" Section Header & 2x2 Bento Grid
            YourPlacesHeader()

            Spacer(modifier = Modifier.height(12.dp))

            BentoShortcutGrid(onNavigate = onNavigate)

            // Bottom clearance so content scrolls completely above the floating dock
            Spacer(modifier = Modifier.height(100.dp))
        }

        // 6. Centered Floating Action Dock (anchored 16 dp above navigation bar)
        HomeActionDock(
            canGoBack = canGoBack,
            tabCount = tabCount,
            onGoBack = onGoBack,
            onShieldClick = { showShieldInfoDialog = true },
            onOpenTabs = onOpenTabSwitcher,
            onOpenMore = onOpenActionMenu,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // Shield Explanation Modal on Blank Home Tab
    if (showShieldInfoDialog) {
        AlertDialog(
            onDismissRequest = { showShieldInfoDialog = false },
            title = {
                Text(
                    text = "Privacy Shields",
                    style = EditorialTypography_SectionHeader.copy(fontSize = 20.sp),
                    color = HomeTextPrimary
                )
            },
            text = {
                Text(
                    text = "Open a website to view its shields. Musify's high-performance ad, tracker, and cookie annoyance shielding will automatically activate on web pages.",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = HomeTextSecondary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showShieldInfoDialog = false }) {
                    Text(
                        text = "Understood",
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = HomeTextPrimary
                        )
                    )
                }
            },
            containerColor = HomeSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Header Row
// ---------------------------------------------------------------------------

@Composable
private fun HomeHeaderRow(isPrivate: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Musify",
            style = EditorialTypography_Wordmark,
            color = HomeTextPrimary
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(HomeDivider)
            )
            Text(
                text = if (isPrivate) "PRIVATE" else "HOME",
                style = EditorialTypography_Metadata,
                color = if (isPrivate) Private else HomeTextSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Decorative Vinyl Hero Artwork
// ---------------------------------------------------------------------------

@Composable
private fun DecorativeVinylHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0C0B0E))
            .border(1.dp, HomeDivider, RoundedCornerShape(14.dp))
    ) {
        // Procedural Vinyl Grooves Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(x = size.width * 0.22f, y = size.height * 0.65f)

            // Draw concentric record lathe grooves with subtle metallic luster
            val grooveColors = listOf(
                Color(0x1CF3EEEA),
                Color(0x35C9A063),
                Color(0x18F3EEEA),
                Color(0x28A88B58),
                Color(0x12FFFFFF),
                Color(0x30C9A063)
            )

            for (i in 1..40) {
                val radius = 24f + i * 9.5f
                val strokeColor = grooveColors[i % grooveColors.size]
                val strokeWidth = if (i % 5 == 0) 1.6f else 0.8f
                drawCircle(
                    color = strokeColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            }

            // Central spindle shadow
            drawCircle(
                color = Color(0xCC000000),
                radius = 28f,
                center = center
            )
            drawCircle(
                color = Color(0xFF1E1D22),
                radius = 16f,
                center = center
            )
            drawCircle(
                color = Color(0x66C9A063),
                radius = 5f,
                center = center
            )

            // Shimmering reflection arcs across the grooves
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x40E0C58A), Color.Transparent),
                    center = center,
                    radius = 320f
                ),
                startAngle = -45f,
                sweepAngle = 75f,
                useCenter = true,
                topLeft = Offset(center.x - 320f, center.y - 320f),
                size = Size(640f, 640f)
            )

            // Fine stippled dust / star particles
            val particleOffsets = listOf(
                Offset(size.width * 0.12f, size.height * 0.28f),
                Offset(size.width * 0.19f, size.height * 0.42f),
                Offset(size.width * 0.27f, size.height * 0.19f),
                Offset(size.width * 0.38f, size.height * 0.52f),
                Offset(size.width * 0.45f, size.height * 0.34f),
                Offset(size.width * 0.53f, size.height * 0.68f),
                Offset(size.width * 0.61f, size.height * 0.22f),
                Offset(size.width * 0.69f, size.height * 0.55f),
                Offset(size.width * 0.76f, size.height * 0.38f),
                Offset(size.width * 0.84f, size.height * 0.72f)
            )
            for (p in particleOffsets) {
                drawCircle(
                    color = Color(0x80F3EEEA),
                    radius = 1.2f,
                    center = p
                )
            }
        }

        // Top-Left Inset Label: "MUSIC + THE WEB"
        Text(
            text = "MUSIC + THE WEB",
            style = EditorialTypography_Metadata.copy(fontSize = 10.sp, letterSpacing = 1.6.sp),
            color = HomeTextPrimary.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        )

        // Top-Right Inset Overlapping Circles Glyph
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 14.dp)
                .size(36.dp, 22.dp)
        ) {
            val r = 9.dp.toPx()
            val stroke = Stroke(width = 1.dp.toPx())
            drawCircle(
                color = Color(0x99B4AFB2),
                radius = r,
                center = Offset(11.dp.toPx(), 11.dp.toPx()),
                style = stroke
            )
            drawCircle(
                color = Color(0x99B4AFB2),
                radius = r,
                center = Offset(23.dp.toPx(), 11.dp.toPx()),
                style = stroke
            )
        }

        // Right Editorial Vertical Caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val verticalLines = listOf(
                "A",
                "WIDER",
                "WEB",
                "FOR",
                "DEEPER",
                "LISTENING",
                "—"
            )
            for (line in verticalLines) {
                Text(
                    text = line,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 7.5.sp,
                        letterSpacing = 1.2.sp,
                        color = HomeTextSecondary.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Search / Address Field (Omnibox)
// ---------------------------------------------------------------------------

@Composable
private fun HomeOmnibox(
    addressInput: String,
    onAddressChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(HomeSurface)
            .border(1.dp, HomeDivider, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp)
            .testTag("home.search"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = MusifyIcons.Search,
                contentDescription = null,
                tint = HomeTextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (addressInput.isEmpty()) {
                    Text(
                        text = "Search or enter address",
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = HomeTextSecondary
                        )
                    )
                }

                BasicTextField(
                    value = addressInput,
                    onValueChange = onAddressChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = HomeTextPrimary
                    ),
                    cursorBrush = SolidColor(HomeTextPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (addressInput.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = MusifyIcons.Close,
                    contentDescription = "Clear input",
                    tint = HomeTextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onAddressChange("") }
                        )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// "Your places" Section Header
// ---------------------------------------------------------------------------

@Composable
private fun YourPlacesHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Your places",
            style = EditorialTypography_SectionHeader,
            color = HomeTextPrimary
        )

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(HomeDivider)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "04",
            style = EditorialTypography_Metadata.copy(fontSize = 12.sp),
            color = HomeTextSecondary
        )
    }
}

// ---------------------------------------------------------------------------
// Bento Shortcut Grid
// ---------------------------------------------------------------------------

private enum class MotifType {
    Crescent,
    WaveMesh,
    SectorRays,
    GranularNebula
}

@Composable
private fun BentoShortcutGrid(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Spotify + YouTube Music
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BentoShortcutCard(
                title = "Spotify",
                domain = "open.spotify.com",
                url = "https://open.spotify.com/",
                testTag = "home.shortcut.spotify",
                motif = MotifType.Crescent,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("https://open.spotify.com/") }
            )

            BentoShortcutCard(
                title = "YouTube Music",
                domain = "music.youtube.com",
                url = "https://music.youtube.com/",
                testTag = "home.shortcut.youtube_music",
                motif = MotifType.WaveMesh,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("https://music.youtube.com/") }
            )
        }

        // Row 2: YouTube + SoundCloud
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BentoShortcutCard(
                title = "YouTube",
                domain = "m.youtube.com",
                url = "https://m.youtube.com/",
                testTag = "home.shortcut.youtube",
                motif = MotifType.SectorRays,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("https://m.youtube.com/") }
            )

            BentoShortcutCard(
                title = "SoundCloud",
                domain = "soundcloud.com",
                url = "https://soundcloud.com/",
                testTag = "home.shortcut.soundcloud",
                motif = MotifType.GranularNebula,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("https://soundcloud.com/") }
            )
        }
    }
}

@Composable
private fun BentoShortcutCard(
    title: String,
    domain: String,
    url: String,
    testTag: String,
    motif: MotifType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131215))
            .border(1.dp, HomeDivider, RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = "Open $title, $domain"
            }
            .testTag(testTag)
    ) {
        // Abstract procedural textural motif
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (motif) {
                MotifType.Crescent -> {
                    // Soft illuminated crescent / vinyl disc curvature
                    val c = Offset(size.width * 0.9f, size.height * 0.9f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x38F3EEEA), Color(0x18F3EEEA), Color.Transparent),
                            center = c,
                            radius = size.width * 0.85f
                        ),
                        radius = size.width * 0.85f,
                        center = c
                    )
                    drawArc(
                        color = Color(0x55F3EEEA),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(c.x - size.width * 0.75f, c.y - size.width * 0.75f),
                        size = Size(size.width * 1.5f, size.width * 1.5f),
                        style = Stroke(width = 1.2f)
                    )
                }

                MotifType.WaveMesh -> {
                    // Dotted sinusoidal wave ribbons
                    val points = 24
                    val step = size.width / points
                    for (row in 0..4) {
                        val rowOffset = row * 5.dp.toPx()
                        for (i in 0..points) {
                            val x = i * step
                            val y = 22.dp.toPx() + rowOffset + kotlin.math.sin((i.toDouble() + row) * 0.45).toFloat() * 12.dp.toPx()
                            drawCircle(
                                color = Color(0x40F3EEEA),
                                radius = 1.1f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                MotifType.SectorRays -> {
                    // Geometric light prism sectors
                    val apex = Offset(size.width * 0.5f, size.height * 0.35f)
                    val rays = listOf(
                        Offset(size.width * 0.1f, 0f),
                        Offset(size.width * 0.9f, 0f),
                        Offset(size.width, size.height * 0.8f),
                        Offset(0f, size.height * 0.7f)
                    )
                    for (target in rays) {
                        drawLine(
                            color = Color(0x30F3EEEA),
                            start = apex,
                            end = target,
                            strokeWidth = 1.0f
                        )
                    }
                }

                MotifType.GranularNebula -> {
                    // Particle wave cloud
                    val particles = listOf(
                        Offset(size.width * 0.2f, size.height * 0.3f),
                        Offset(size.width * 0.35f, size.height * 0.25f),
                        Offset(size.width * 0.5f, size.height * 0.4f),
                        Offset(size.width * 0.65f, size.height * 0.28f),
                        Offset(size.width * 0.8f, size.height * 0.45f),
                        Offset(size.width * 0.4f, size.height * 0.5f),
                        Offset(size.width * 0.75f, size.height * 0.35f)
                    )
                    for (p in particles) {
                        drawCircle(
                            color = Color(0x38F3EEEA),
                            radius = 3.5f,
                            center = p
                        )
                    }
                }
            }
        }

        // Bottom gradient scrim for high contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xCC0E0D10),
                            Color(0xF609090A)
                        )
                    )
                )
        )

        // Labels at bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 10.dp)
        ) {
            Text(
                text = title,
                style = EditorialTypography_ShortcutTitle,
                color = HomeTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = domain,
                style = EditorialTypography_ShortcutDomain,
                color = HomeTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Floating Action Dock (HomeActionDock)
// ---------------------------------------------------------------------------

@Composable
fun HomeActionDock(
    canGoBack: Boolean,
    tabCount: Int,
    onGoBack: () -> Unit,
    onShieldClick: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(294.dp)
            .height(58.dp)
            .clip(CircleShape)
            .background(HomeDockSurface)
            .border(1.dp, HomeDockBorder, CircleShape)
    ) {
        // Subtle ambient violet/warm bottom gradient reflection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x14A855F7),
                            Color(0x248B5CF6)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Back button (disabled when !canGoBack)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        enabled = canGoBack,
                        role = Role.Button,
                        onClick = onGoBack
                    )
                    .semantics {
                        contentDescription = if (canGoBack) "Go back" else "Back disabled"
                        role = Role.Button
                    }
                    .testTag("home.back"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MusifyIcons.ArrowLeft,
                    contentDescription = null,
                    tint = if (canGoBack) HomeTextPrimary else HomeTextSecondary.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Shields button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        role = Role.Button,
                        onClick = onShieldClick
                    )
                    .semantics {
                        contentDescription = "Privacy shields"
                        role = Role.Button
                    }
                    .testTag("home.shields"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MusifyIcons.Shield,
                    contentDescription = null,
                    tint = HomeTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 3. Tabs button with live count badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        role = Role.Button,
                        onClick = onOpenTabs
                    )
                    .semantics {
                        contentDescription = "$tabCount open tabs"
                        role = Role.Button
                    }
                    .testTag("home.tabs"),
                contentAlignment = Alignment.Center
            ) {
                // Rounded square badge with tab count
                Box(
                    modifier = Modifier
                        .size(23.dp)
                        .border(1.6.dp, HomeTextPrimary, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabCount.coerceAtLeast(1).toString(),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = HomeTextPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 4. More options button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        role = Role.Button,
                        onClick = onOpenMore
                    )
                    .semantics {
                        contentDescription = "More browser options"
                        role = Role.Button
                    }
                    .testTag("home.more"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MusifyIcons.MoreVertical,
                    contentDescription = null,
                    tint = HomeTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

