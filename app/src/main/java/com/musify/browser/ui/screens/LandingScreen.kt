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
import com.musify.browser.ui.theme.AccentCyan
import com.musify.browser.ui.theme.AccentBlue
import com.musify.browser.ui.theme.AccentPurple
import com.musify.browser.ui.theme.BackgroundDark
import com.musify.browser.ui.theme.CardBorder
import com.musify.browser.ui.theme.MusifyTheme
import com.musify.browser.ui.theme.StatusGreen
import com.musify.browser.ui.theme.SurfaceDark
import com.musify.browser.ui.theme.SurfaceLightDark
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary

@Composable
fun LandingScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundDark
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
                                    listOf(AccentCyan, AccentBlue, AccentPurple)
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
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Phase 00",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                }
            }

            // Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StatusGreen.copy(alpha = 0.5f), CardBorder)))
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
                            .background(StatusGreen)
                    )
                    Column {
                        Text(
                            text = "Foundation & Workspace Ready",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Kotlin DSL, Jetpack Compose, and test suites initialized cleanly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Scope & Commitments Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, SurfaceLightDark)))
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
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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
                    PhaseRow("Phase 01", "GeckoView Feasibility Spike", isCurrent = true)
                    PhaseRow("Phase 02", "Single-Tab Browser Shell", isUpcoming = true)
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
                .background(AccentCyan)
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
                isComplete -> StatusGreen
                isCurrent -> AccentCyan
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
            isComplete -> StatusGreen
            isCurrent -> AccentCyan
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
