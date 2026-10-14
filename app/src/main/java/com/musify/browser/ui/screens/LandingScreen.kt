package com.musify.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.AccentMuted
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.Private
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.Hairline
import com.musify.browser.ui.theme.IconLabel
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.MusifyTheme
import com.musify.browser.ui.theme.Success
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.Elevated
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary
@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    onOpenBrowser: () -> Unit = {},
    onLaunchSpike: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Ink
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Accent, AccentMuted, Private)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Musify",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Privacy-Focused Browser Shell",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Phase tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Phase 02",
                        style = MaterialTheme.typography.labelSmall,
                        color = Accent
                    )
                }
            }

            // Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Success.copy(alpha = 0.5f), Hairline)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Success)
                    )
                    Column {
                        Text(
                            text = "Single-Tab Browser Active",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Safe URL/search routing, strict TLS cancel, anti-spoofing origin display, and GeckoView engine active.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Primary & Secondary CTAs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenBrowser,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    IconLabel(
                        MusifyIcons.Globe,
                        "Open Browser",
                        tint = Ink,
                        iconSize = 16.dp,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = onLaunchSpike,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Elevated),
                    shape = RoundedCornerShape(14.dp)
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

            // Scope & Commitments Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Hairline, Elevated)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Architectural Guarantees",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    ScopeItem(
                        title = "General-Purpose Browsing",
                        description = "Engineered for arbitrary web navigation, standard forms, media, and web applications without single-site special casing."
                    )

                    ScopeItem(
                        title = "Zero Telemetry & Credential Harvesting",
                        description = "No telemetry, behavioral analytics, or third-party cloud synchronization. Browsing data remains strictly on-device."
                    )

                    ScopeItem(
                        title = "Strict TLS & Connection Integrity",
                        description = "Enforced TLS certificate validation. Zero silent overrides or unsafe blanket SSL proceed mechanisms."
                    )

                    ScopeItem(
                        title = "Independent Privacy Protection Model",
                        description = "Transparent request blocking reporting 'requests blocked by this app'. Independent codebase and UI identity."
                    )
                }
            }

            // Roadmap Progression Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Phased Execution Roadmap",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    PhaseRow("Phase 00", "Repository & Project Setup", isComplete = true)
                    PhaseRow("Phase 01", "GeckoView Feasibility Spike", isComplete = true)
                    PhaseRow("Phase 02", "Single-Tab Browser Shell", isCurrent = true)
                    PhaseRow("Phase 03", "Tabs & Lifecycle Recovery", isUpcoming = true)
                    PhaseRow("Phase 04", "Bookmarks & Disposable Sessions", isUpcoming = true)
                    PhaseRow("Phase 05", "Shields & Content Blocking", isUpcoming = true)
                }
            }

            // Build metadata
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Application ID: com.musify.browser • Target: Android 35",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ScopeItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Accent)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PhaseRow(
    phaseId: String,
    title: String,
    isComplete: Boolean = false,
    isCurrent: Boolean = false,
    isUpcoming: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val statusColor = when {
                isComplete -> Success
                isCurrent -> Accent
                else -> TextMuted
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = "$phaseId: $title",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUpcoming) TextSecondary else TextPrimary,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        val badgeText = when {
            isComplete -> "VERIFIED"
            isCurrent -> "NEXT"
            else -> "QUEUED"
        }
        val badgeColor = when {
            isComplete -> Success
            isCurrent -> Accent
            else -> TextMuted
        }

        Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    MusifyTheme {
        LandingScreen()
    }
}
