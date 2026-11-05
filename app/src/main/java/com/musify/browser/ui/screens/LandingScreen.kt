package com.musify.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.IconLabel
import com.musify.browser.ui.theme.M6Canvas
import com.musify.browser.ui.theme.M6Card
import com.musify.browser.ui.theme.M6CatalogTag
import com.musify.browser.ui.theme.M6Elevated
import com.musify.browser.ui.theme.M6Emerald
import com.musify.browser.ui.theme.M6ExhibitionCard
import com.musify.browser.ui.theme.M6Hairline
import com.musify.browser.ui.theme.M6SeismographBar
import com.musify.browser.ui.theme.M6SignalOrange
import com.musify.browser.ui.theme.M6SoundViolet
import com.musify.browser.ui.theme.M6TextMuted
import com.musify.browser.ui.theme.M6TextSecondary
import com.musify.browser.ui.theme.M6Typography_ExhibitionSubtitle
import com.musify.browser.ui.theme.M6Typography_ExhibitionTitle
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.MusifyTheme
import com.musify.browser.ui.theme.TextPrimary

/**
 * LandingScreen — Redesigned according to the M6 UX/UI Exhibition design language.
 * Architectural museum-grade digital sound archive and kinetic privacy pavilion.
 */
@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    onOpenBrowser: () -> Unit = {},
    onLaunchSpike: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = M6Canvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Exhibition Catalog Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    M6CatalogTag(
                        text = "[M6 // SOUND ARCHIVE]",
                        accentColor = M6Emerald
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "MUSIFY EXHIBITION",
                        style = M6Typography_ExhibitionTitle,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Autonomous Privacy Browser & DRM Sound Archive",
                        style = M6Typography_ExhibitionSubtitle,
                        color = M6TextSecondary
                    )
                }
            }

            // Real-Time Seismograph Shield Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                M6SeismographBar(
                    label = "Privacy Shields",
                    value = "100% ACTIVE",
                    sublabel = "Zero-latency mock",
                    accentColor = M6Emerald,
                    modifier = Modifier.weight(1f)
                )
                M6SeismographBar(
                    label = "Widevine DRM",
                    value = "L1/L3 CRYPTO",
                    sublabel = "Lossless stream",
                    accentColor = M6SoundViolet,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Exhibition Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenBrowser,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    IconLabel(
                        MusifyIcons.Globe,
                        "Launch Exhibition Shell",
                        tint = M6Canvas,
                        iconSize = 16.dp,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onLaunchSpike,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = M6Elevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    IconLabel(
                        MusifyIcons.Bolt,
                        "Engine Spike",
                        tint = TextPrimary,
                        iconSize = 15.dp,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Curated Sound & Media Exhibits Bento Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CURATED EXHIBIT PAVILIONS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = M6TextMuted
                    )
                    Text(
                        text = "4 ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = M6Emerald
                    )
                }

                M6ExhibitionBentoTile(
                    catalogId = "EXHIBIT // 01",
                    title = "Spotify Web Player",
                    domain = "open.spotify.com",
                    accentColor = M6Emerald,
                    description = "Lossless Widevine DRM license acquisition with Brave-aligned audio ad auto-suppressor and non-destructive rate playback.",
                    onClick = onOpenBrowser
                )

                M6ExhibitionBentoTile(
                    catalogId = "EXHIBIT // 02",
                    title = "YouTube Music Web",
                    domain = "music.youtube.com",
                    accentColor = M6SignalOrange,
                    description = "Continuous background audio playback, Page Visibility API keepalive, and first-party anti-freeze JSON mock responses.",
                    onClick = onOpenBrowser
                )

                M6ExhibitionBentoTile(
                    catalogId = "EXHIBIT // 03",
                    title = "YouTube Video Stream",
                    domain = "m.youtube.com",
                    accentColor = M6SignalOrange,
                    description = "Protobuf Field 4 (0x20 varint) SABR streaming interceptor with dynamic backoff rewrite (50-150ms) and zero player buffering.",
                    onClick = onOpenBrowser
                )

                M6ExhibitionBentoTile(
                    catalogId = "EXHIBIT // 04",
                    title = "SoundCloud Streaming",
                    domain = "soundcloud.com",
                    accentColor = M6SoundViolet,
                    description = "Direct CDN streaming manifest delivery, tracking parameter stripping (Clean URLs), and bounce redirect bypass.",
                    onClick = onOpenBrowser
                )
            }

            // Architectural Guarantees Card
            M6ExhibitionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = M6Card,
                borderColor = M6Hairline
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ARCHITECTURAL SPECIFICATIONS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = TextPrimary
                        )
                        M6CatalogTag(
                            text = "[PHASE 28 // M6]",
                            accentColor = M6SignalOrange
                        )
                    }

                    M6SpecificationRow(
                        number = "01",
                        title = "Zero Destructive DOM Tampering",
                        description = "Strict preservation of HTMLMediaElement rate, volume, and clock. Playback streams remain 100% bite-for-byte lossless."
                    )

                    M6SpecificationRow(
                        number = "02",
                        title = "Universal Privacy Shields Harmonization",
                        description = "Multi-tier network filters, anti-freeze JSON mocking, Clean URLs parameter sanitization, and debounce tracking bypass."
                    )

                    M6SpecificationRow(
                        number = "03",
                        title = "Full Multi-Variant Release Parity",
                        description = "Universal & ARM64 GeckoView releases for hardware Widevine DRM + featherweight WebView Lite edition under 10 MB."
                    )
                }
            }

            // Catalog Metadata Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M6 EXHIBITION • APPLICATION ID: com.musify.browser • ANDROID SDK 35",
                    style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.8.sp),
                    color = M6TextMuted
                )
            }
        }
    }
}

@Composable
private fun M6ExhibitionBentoTile(
    catalogId: String,
    title: String,
    domain: String,
    accentColor: Color,
    description: String,
    onClick: () -> Unit
) {
    M6ExhibitionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = M6Card,
        borderColor = M6Hairline,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = catalogId,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = M6TextMuted
                    )
                }
                Text(
                    text = domain,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = M6TextSecondary
            )
        }
    }
}

@Composable
private fun M6SpecificationRow(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = M6Emerald
            )
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = M6TextSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    MusifyTheme {
        LandingScreen()
    }
}
