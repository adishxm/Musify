package com.musify.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.engine.CapabilityStatus
import com.musify.browser.engine.ContentMode
import com.musify.browser.engine.DnsProtectionMode
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.AccentCyan
import com.musify.browser.ui.theme.AccentPurple
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

@Composable
fun PlaybackCompatibilityDialog(
    viewModel: TabsViewModel,
    onDismiss: () -> Unit
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val activeTab = tabsState.selectedTab
    val contentMode by viewModel.activeContentMode.collectAsState()
    val mediaCap by viewModel.activeMediaCapabilityState.collectAsState()
    val dnsState by viewModel.dnsProtectionState.collectAsState()
    val isShieldEnabled = viewModel.isShieldEnabledForCurrentTab()

    var showDnsSettingsDialog by remember { mutableStateOf(false) }

    val currentOrigin = when {
        activeTab == null || activeTab.url == "about:blank" -> "musify://home"
        else -> activeTab.origin
    }

    val isDesktop = contentMode == ContentMode.DESKTOP
    val isPrivate = activeTab?.isPrivate == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎵", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                Column {
                    Text(
                        text = "Playback & Media Diagnostics",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = currentOrigin,
                        color = AccentCyan,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Honest Engineering Disclosure
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Web Player Diagnostics Guide",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Services like Spotify Web Player require both Desktop presentation and hardware Widevine CDM support. Statuses below reflect honest, measured capabilities from this Android device.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Measured Capability Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CapabilityRow(
                            label = "Content Presentation",
                            statusText = if (isDesktop) "Desktop 🖥️" else "Mobile 📱 (May block player)",
                            color = if (isDesktop) StatusGreen else StatusOrange
                        )

                        val widevineStatusColor = when (mediaCap.widevineStatus) {
                            CapabilityStatus.SUPPORTED -> StatusGreen
                            CapabilityStatus.UNSUPPORTED -> StatusRed
                            CapabilityStatus.FAILED -> StatusRed
                            CapabilityStatus.UNKNOWN -> StatusOrange
                        }
                        CapabilityRow(
                            label = "Device Widevine CDM",
                            statusText = when (mediaCap.widevineStatus) {
                                CapabilityStatus.SUPPORTED -> "Supported (OS Verified)"
                                CapabilityStatus.UNSUPPORTED -> "Unsupported on device"
                                CapabilityStatus.FAILED -> "Probe Failed"
                                CapabilityStatus.UNKNOWN -> "Pending Check"
                            },
                            color = widevineStatusColor
                        )

                        val emeStatusColor = when (mediaCap.encryptedMediaStatus) {
                            CapabilityStatus.SUPPORTED -> StatusGreen
                            CapabilityStatus.UNSUPPORTED -> StatusRed
                            CapabilityStatus.FAILED -> StatusRed
                            CapabilityStatus.UNKNOWN -> StatusOrange
                        }
                        CapabilityRow(
                            label = "Encrypted Media (EME)",
                            statusText = when (mediaCap.encryptedMediaStatus) {
                                CapabilityStatus.SUPPORTED -> "Granted & Active"
                                CapabilityStatus.UNSUPPORTED -> "Unavailable"
                                CapabilityStatus.FAILED -> "Failed"
                                CapabilityStatus.UNKNOWN -> "Pending Request"
                            },
                            color = emeStatusColor
                        )

                        val drmProvider = mediaCap.deviceDrmProvider
                        if (!drmProvider.isNullOrBlank()) {
                            CapabilityRow(
                                label = "DRM Provider",
                                statusText = drmProvider,
                                color = TextSecondary
                            )
                        }

                        CapabilityRow(
                            label = "Browser Engine",
                            statusText = mediaCap.engineDetails ?: "GeckoView 135",
                            color = TextSecondary
                        )

                        CapabilityRow(
                            label = "Audio Autoplay",
                            statusText = if (mediaCap.isAutoplayAllowed) "Allowed" else "Blocked",
                            color = if (mediaCap.isAutoplayAllowed) StatusGreen else StatusOrange
                        )

                        CapabilityRow(
                            label = "Privacy Profile",
                            statusText = if (isPrivate) "Private Mode (Ephemeral)" else "Normal Mode (Persistent)",
                            color = if (!isPrivate) StatusGreen else StatusOrange
                        )

                        CapabilityRow(
                            label = "DNS Resolution Layer",
                            statusText = if (dnsState.mode == DnsProtectionMode.ADGUARD_PRIVATE_DNS) "AdGuard Private DNS" else "System DNS",
                            color = if (dnsState.mode == DnsProtectionMode.ADGUARD_PRIVATE_DNS) StatusGreen else TextSecondary
                        )

                        CapabilityRow(
                            label = "In-Page Shields",
                            statusText = if (isShieldEnabled) "Shields Active" else "Shields Relaxed for Origin",
                            color = if (isShieldEnabled) StatusGreen else StatusOrange
                        )
                    }
                }

                // Error Callout if any failure occurred
                val failureReason = mediaCap.lastMediaFailureReason
                if (!failureReason.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Playback Limitation Reason",
                                color = StatusRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = failureReason,
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Desktop Mode Toggle Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDesktop) StatusGreen.copy(alpha = 0.12f) else SurfaceLightDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDesktop) StatusGreen.copy(alpha = 0.4f) else CardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Request Desktop Site",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isDesktop) "Desktop viewport and user-agent active." else "Switch to desktop view for open.spotify.com",
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
                                checkedThumbColor = StatusGreen,
                                checkedTrackColor = StatusGreen.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceDark
                            )
                        )
                    }
                }

                // AdGuard DNS Guidance Button
                Button(
                    onClick = { showDnsSettingsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🛡️ Configure AdGuard / System DNS",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // One-Tap Optimize Button
                Button(
                    onClick = {
                        viewModel.optimizePlaybackForCurrentSite()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ Apply Desktop Mode & Relax Shields",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", color = TextPrimary)
            }
        },
        containerColor = SurfaceDark
    )

    if (showDnsSettingsDialog) {
        DnsSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showDnsSettingsDialog = false }
        )
    }
}

@Composable
private fun CapabilityRow(label: String, statusText: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(
            text = statusText,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
