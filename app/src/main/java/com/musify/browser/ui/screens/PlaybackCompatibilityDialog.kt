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
import com.musify.browser.engine.BuildVariantInfo
import com.musify.browser.engine.CapabilityStatus
import com.musify.browser.engine.ContentMode
import com.musify.browser.engine.DnsProtectionMode
import com.musify.browser.engine.EngineType
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.Danger
import com.musify.browser.ui.theme.Elevated
import com.musify.browser.ui.theme.Hairline
import com.musify.browser.ui.theme.IconLabel
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.MusifyCard
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.MusifyShapes
import com.musify.browser.ui.theme.MusifyTypography_Mono
import com.musify.browser.ui.theme.Private
import com.musify.browser.ui.theme.Space
import com.musify.browser.ui.theme.Success
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.SurfaceRaised
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary
import com.musify.browser.ui.theme.Warning
import com.musify.browser.ui.theme.mediumTint
import com.musify.browser.ui.theme.softTint

/**
 * PlaybackCompatibilityDialog provides real-time media pipeline observability,
 * build variant disclosures, Widevine CDM hardware probes, and origin shield controls.
 */
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
        containerColor = Surface,
        shape = MusifyShapes.xl,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MusifyIcon(
                    MusifyIcons.Music,
                    size = 20.dp,
                    tint = Accent,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = "Playback & Media Diagnostics",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = currentOrigin,
                        color = TextSecondary,
                        style = MusifyTypography_Mono,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            val activeEngineType = viewModel.getActiveEngineAdapter()?.engineType ?: EngineType.GECKO_VIEW
            val buildInfo = remember(activeEngineType) { BuildVariantInfo.detect(activeEngineType) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Honest Engineering Disclosure & Build Variant
                MusifyCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SurfaceRaised,
                    borderColor = Hairline,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Space.md)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Installed Build Variant",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = buildInfo.variant.displayName,
                                color = Accent,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${buildInfo.engineVersion} • Android ${buildInfo.androidRelease} (API ${buildInfo.androidApiLevel}, ${buildInfo.primaryAbi})",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // If running on Lite/WebView, show prominent Widevine limitation notice
                if (buildInfo.protectedPlaybackLimitationNotice != null) {
                    MusifyCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Warning.softTint(),
                        borderColor = Warning.mediumTint(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(Space.md)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MusifyIcon(MusifyIcons.Warning, size = 15.dp, tint = Warning)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "System WebView Limitation",
                                    color = Warning,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = buildInfo.protectedPlaybackLimitationNotice,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Measured Capability Breakdown
                MusifyCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SurfaceRaised,
                    borderColor = Hairline,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Space.md)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ObsidianCapabilityRow(
                            label = "Content Presentation",
                            statusText = if (isDesktop) "Desktop" else "Mobile (May block player)",
                            color = if (isDesktop) Success else Warning
                        )

                        val widevineStatusColor = when (mediaCap.widevineStatus) {
                            CapabilityStatus.SUPPORTED -> Success
                            CapabilityStatus.UNSUPPORTED -> Danger
                            CapabilityStatus.FAILED -> Danger
                            CapabilityStatus.UNKNOWN -> Warning
                        }
                        ObsidianCapabilityRow(
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
                            CapabilityStatus.SUPPORTED -> Success
                            CapabilityStatus.UNSUPPORTED -> Danger
                            CapabilityStatus.FAILED -> Danger
                            CapabilityStatus.UNKNOWN -> Warning
                        }
                        ObsidianCapabilityRow(
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
                            ObsidianCapabilityRow(
                                label = "DRM Provider",
                                statusText = drmProvider,
                                color = TextSecondary
                            )
                        }

                        ObsidianCapabilityRow(
                            label = "Browser Engine",
                            statusText = mediaCap.engineDetails ?: "GeckoView 135",
                            color = TextSecondary
                        )

                        ObsidianCapabilityRow(
                            label = "Audio Autoplay",
                            statusText = if (mediaCap.isAutoplayAllowed) "Allowed" else "Blocked",
                            color = if (mediaCap.isAutoplayAllowed) Success else Warning
                        )

                        ObsidianCapabilityRow(
                            label = "Privacy Profile",
                            statusText = if (isPrivate) "Private Mode (Ephemeral)" else "Normal Mode (Persistent)",
                            color = if (!isPrivate) Success else Private
                        )

                        ObsidianCapabilityRow(
                            label = "DNS Resolution Layer",
                            statusText = if (dnsState.mode == DnsProtectionMode.ADGUARD_PRIVATE_DNS) "AdGuard Private DNS" else "System DNS",
                            color = if (dnsState.mode == DnsProtectionMode.ADGUARD_PRIVATE_DNS) Success else TextSecondary
                        )

                        ObsidianCapabilityRow(
                            label = "In-Page Shields",
                            statusText = if (isShieldEnabled) "Shields Active" else "Shields Relaxed for Origin",
                            color = if (isShieldEnabled) Success else Warning
                        )

                        ObsidianCapabilityRow(
                            label = "Session Engine Policy",
                            statusText = mediaCap.cookiePolicy,
                            color = if (mediaCap.isOriginExceptionActive) Warning else Success
                        )

                        val liveRule = mediaCap.liveBlockCategory
                        if (!liveRule.isNullOrBlank()) {
                            ObsidianCapabilityRow(
                                label = "Latest Shield Interception",
                                statusText = liveRule,
                                color = Accent
                            )
                        }
                    }
                }

                // Error Callout if any failure occurred
                val failureReason = mediaCap.lastMediaFailureReason
                if (!failureReason.isNullOrBlank()) {
                    MusifyCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Danger.softTint(),
                        borderColor = Danger.mediumTint(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(Space.md)
                    ) {
                        Column {
                            Text(
                                text = "Playback Limitation Reason",
                                color = Danger,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = failureReason,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Quick Action Cards
                MusifyCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SurfaceRaised,
                    borderColor = Hairline,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Space.md)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Relax Shields for Origin",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Allows cross-origin media tokens & DRM auth",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = !isShieldEnabled,
                                onCheckedChange = {
                                    viewModel.toggleShieldForCurrentOrigin()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Accent,
                                    checkedTrackColor = Elevated,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Request Desktop Site",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Bypasses mobile app download interstitials",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = isDesktop,
                                onCheckedChange = {
                                    viewModel.toggleContentModeCurrentTab()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Accent,
                                    checkedTrackColor = Elevated,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }

                        Button(
                            onClick = { showDnsSettingsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Elevated),
                            shape = MusifyShapes.sm
                        ) {
                            IconLabel(
                                MusifyIcons.Bolt,
                                "DNS Shielding Quick Settings",
                                tint = TextPrimary,
                                iconSize = 14.dp,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.optimizePlaybackForCurrentSite()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = MusifyShapes.sm
                        ) {
                            IconLabel(
                                MusifyIcons.Reload,
                                "Apply Settings & Reload Tab",
                                tint = Ink,
                                iconSize = 14.dp,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Elevated),
                shape = MusifyShapes.sm
            ) {
                Text("Dismiss", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
            }
        }
    )

    if (showDnsSettingsDialog) {
        DnsSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showDnsSettingsDialog = false }
        )
    }
}

@Composable
private fun ObsidianCapabilityRow(
    label: String,
    statusText: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                color = color,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
