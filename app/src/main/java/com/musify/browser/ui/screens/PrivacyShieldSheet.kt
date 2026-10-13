package com.musify.browser.ui.screens

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.AccentCyan
import com.musify.browser.ui.theme.BackgroundDark
import com.musify.browser.ui.theme.CardBorder
import com.musify.browser.ui.theme.StatusGreen
import com.musify.browser.ui.theme.StatusOrange
import com.musify.browser.ui.theme.StatusRed
import com.musify.browser.ui.theme.SurfaceDark
import com.musify.browser.ui.theme.SurfaceLightDark
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary

/**
 * PrivacyShieldSheet presents live privacy protections, per-origin exception toggles,
 * accurate category counters, and transparent decision reporting.
 */
@Composable
fun PrivacyShieldSheet(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val activeTab = tabsState.selectedTab
    val shieldState by viewModel.activeShieldState.collectAsState()

    var isShieldEnabled by remember(activeTab?.origin) {
        mutableStateOf(viewModel.isShieldEnabledForCurrentTab())
    }
    val contentMode by viewModel.activeContentMode.collectAsState()
    val isDesktop = contentMode == com.musify.browser.engine.ContentMode.DESKTOP
    var showPlaybackDialog by remember { mutableStateOf(false) }
    var showClearCookiesNotice by remember { mutableStateOf(false) }

    val currentOrigin = when {
        activeTab == null || activeTab.url == "about:blank" -> "musify://home"
        else -> activeTab.origin
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isShieldEnabled) StatusGreen.copy(alpha = 0.5f) else StatusOrange.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isShieldEnabled) "🛡" else "⚠",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "Privacy Shields",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = currentOrigin,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SurfaceLightDark)
                        .clickable(onClick = onClose)
                        .semantics {
                            contentDescription = "Close privacy shields panel"
                            role = androidx.compose.ui.semantics.Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Per-Origin Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isShieldEnabled) StatusGreen.copy(alpha = 0.1f) else StatusOrange.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isShieldEnabled) StatusGreen.copy(alpha = 0.3f) else StatusOrange.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isShieldEnabled) "Shields Active on this Site" else "Shields Disabled on this Site",
                            color = if (isShieldEnabled) StatusGreen else StatusOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isShieldEnabled)
                                "Cross-site trackers and fingerprinters blocked."
                            else
                                "Protections paused for this site. Page reloaded without blocking.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isShieldEnabled,
                        onCheckedChange = {
                            isShieldEnabled = viewModel.toggleShieldForCurrentOrigin()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StatusGreen,
                            checkedTrackColor = StatusGreen.copy(alpha = 0.4f),
                            uncheckedThumbColor = StatusOrange,
                            uncheckedTrackColor = SurfaceLightDark
                        )
                    )
                }
            }

            // Desktop Web Mode Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDesktop) AccentCyan.copy(alpha = 0.1f) else SurfaceLightDark
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDesktop) AccentCyan.copy(alpha = 0.4f) else CardBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDesktop) "Desktop Site Active 🖥️" else "Mobile Site Active 📱",
                            color = if (isDesktop) AccentCyan else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isDesktop)
                                "Desktop user agent and viewport active for this site."
                            else
                                "Request desktop site for web apps requiring full layout (e.g., Spotify Web).",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isDesktop,
                        onCheckedChange = {
                            viewModel.toggleContentModeCurrentTab()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = AccentCyan.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceDark
                        )
                    )
                }
            }

            // Blocked Requests Counter Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Requests blocked on this page",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (shieldState.blockedCount > 0) StatusGreen.copy(alpha = 0.2f) else SurfaceLightDark)
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${shieldState.blockedCount}",
                                color = if (shieldState.blockedCount > 0) StatusGreen else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Breakdown
                    CategoryCounterRow("Advertising & Analytics", shieldState.blockedAds)
                    CategoryCounterRow("Cross-Site Social Trackers", shieldState.blockedSocial)
                    CategoryCounterRow("Cryptomining Scripts", shieldState.blockedCryptominers)
                    CategoryCounterRow("Browser Fingerprinters", shieldState.blockedFingerprinters)
                }
            }

            // Last Blocked Decision Card
            if (shieldState.lastBlockedUrl != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Last Blocked Decision",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category: ${shieldState.lastBlockCategory ?: "Tracker"}",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = shieldState.lastBlockedUrl ?: "",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Playback Compatibility & Recovery Action
            Button(
                onClick = { showPlaybackDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎵 Web Playback & Media Diagnostics",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Per-Origin Clear Storage Action
            Button(
                onClick = {
                    viewModel.reload()
                    showClearCookiesNotice = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "↻ Reload & Reset Page Protections",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showClearCookiesNotice) {
                Text(
                    text = "Page reloaded. Active protections and counters synchronized.",
                    color = StatusGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Transparent Engine Disclosure
            Text(
                text = "Musify enforces GeckoView Strict Content Blocking natively. Decisions are executed on-device without telemetry or remote browsing surveillance.",
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }

    if (showPlaybackDialog) {
        PlaybackCompatibilityDialog(
            viewModel = viewModel,
            onDismiss = { showPlaybackDialog = false }
        )
    }
}

@Composable
private fun CategoryCounterRow(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(
            text = "$count",
            color = if (count > 0) StatusGreen else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal
        )
    }
}
