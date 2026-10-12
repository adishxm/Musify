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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.release.ReleaseInfoRegistry
import com.musify.browser.ui.theme.AccentCyan
import com.musify.browser.ui.theme.AccentBlue
import com.musify.browser.ui.theme.AccentPurple
import com.musify.browser.ui.theme.BackgroundDark
import com.musify.browser.ui.theme.CardBorder
import com.musify.browser.ui.theme.StatusGreen
import com.musify.browser.ui.theme.StatusOrange
import com.musify.browser.ui.theme.SurfaceDark
import com.musify.browser.ui.theme.SurfaceLightDark
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary

@Composable
fun ReleaseInfoScreen(
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    onClose: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf("ℹ About", "📜 Privacy", "⚠ Limitations", "❓ Data & Help")

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .clickable(onClick = onClose)
                            .semantics {
                                contentDescription = "Back to browser"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("←", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Column {
                        Text(
                            text = "Release & Privacy",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Musify ${ReleaseInfoRegistry.METADATA.versionName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentCyan
                        )
                    }
                }
            }

            // Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = AccentCyan,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) AccentCyan else TextSecondary,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (selectedTab) {
                    0 -> AboutTabContent()
                    1 -> PrivacyTabContent()
                    2 -> LimitationsTabContent()
                    3 -> HelpAndDataClearTabContent()
                }
            }
        }
    }
}

@Composable
private fun AboutTabContent() {
    val scrollState = rememberScrollState()
    val meta = ReleaseInfoRegistry.METADATA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AccentCyan, AccentBlue, AccentPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                }

                Text(
                    text = meta.appName,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "v${meta.versionName} (Build ${meta.versionCode})",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentCyan
                )
                Text(
                    text = meta.applicationId,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Engine & Platform Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Engine & Platform",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                SpecRow(label = "Web Engine", value = meta.engineName)
                SpecRow(label = "Engine Version", value = meta.engineVersion)
                SpecRow(label = "Target SDK", value = "Android 15 (API ${meta.targetSdk})")
                SpecRow(label = "Minimum SDK", value = "Android 8.0 (API ${meta.minSdk})")
                SpecRow(label = "License", value = meta.license)
            }
        }

        // Trademark & Independence Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Independence & Trademarks",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = ReleaseInfoRegistry.TRADEMARK_AND_AFFILIATION_NOTICE,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PrivacyTabContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Privacy Guarantees",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Musify is engineered with strict privacy defaults. We believe what you browse is strictly your business.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        ReleaseInfoRegistry.PRIVACY_POLICY.forEach { section ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🛡", fontSize = 16.sp)
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = StatusGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = section.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = section.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LimitationsTabContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Honest Limitations Disclosure",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We disclose known technical boundaries transparently rather than making exaggerated marketing claims.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        ReleaseInfoRegistry.KNOWN_LIMITATIONS.forEach { limitation ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚠", fontSize = 16.sp)
                        Text(
                            text = limitation.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = StatusOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = limitation.impact,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "Roadmap Note: ${limitation.workAroundOrRoadmap}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpAndDataClearTabContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Data Erasure & Uninstall Guide",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Permanent Removal Steps",
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ReleaseInfoRegistry.UNINSTALL_AND_DATA_CLEAR_GUIDE,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Support & Issue Reporting",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Found a bug or compatibility issue with a website? Musify is open-source and issues can be reported at:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = ReleaseInfoRegistry.METADATA.issueTrackerUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}
