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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.engine.ContentMode
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.AccentCyan
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
fun PlaybackCompatibilityDialog(
    viewModel: TabsViewModel,
    onDismiss: () -> Unit
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val activeTab = tabsState.selectedTab
    val contentMode by viewModel.activeContentMode.collectAsState()
    val mediaCap by viewModel.activeMediaCapabilityState.collectAsState()
    val isShieldEnabled = viewModel.isShieldEnabledForCurrentTab()

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
                        text = "Playback & Media Compatibility",
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
                // Explanation Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Web Player Compatibility Guide",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Streaming services like Spotify Web Player restrict mobile browser user-agents and require Encrypted Media Extensions (EME) & desktop presentation. Enabling Desktop Mode and media permissions restores playback.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Status Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusRow(
                            label = "Display Content Mode",
                            value = if (isDesktop) "Desktop 🖥️" else "Mobile 📱 (May block web player)",
                            isPositive = isDesktop
                        )
                        StatusRow(
                            label = "Encrypted Media (EME)",
                            value = if (mediaCap.isEncryptedMediaSupported) "Supported & Allowed" else "Unavailable",
                            isPositive = mediaCap.isEncryptedMediaSupported
                        )
                        StatusRow(
                            label = "Media Keys / DRM Access",
                            value = "Granted on Request",
                            isPositive = true
                        )
                        StatusRow(
                            label = "Audio Autoplay",
                            value = if (mediaCap.isAutoplayAllowed) "Allowed" else "Blocked",
                            isPositive = mediaCap.isAutoplayAllowed
                        )
                        StatusRow(
                            label = "Tab Privacy Profile",
                            value = if (isPrivate) "Private Tab (DRM licenses may not persist)" else "Normal Tab (Persistent DRM supported)",
                            isPositive = !isPrivate
                        )
                        StatusRow(
                            label = "Privacy Shields",
                            value = if (isShieldEnabled) "Shields Active" else "Shields Relaxed for this Origin",
                            isPositive = true
                        )
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
                        text = "⚡ Optimize & Recover Web Playback",
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
}

@Composable
private fun StatusRow(label: String, value: String, isPositive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(
            text = value,
            color = if (isPositive) StatusGreen else StatusOrange,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
