package com.musify.browser.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.engine.DnsProtectionMode
import com.musify.browser.storage.DnsSettingsRepository
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.Hairline
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.Private
import com.musify.browser.ui.theme.Success
import com.musify.browser.ui.theme.Warning
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.Elevated
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary
@Composable
fun DnsSettingsDialog(
    viewModel: TabsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dnsState by viewModel.dnsProtectionState.collectAsState()
    var selectedMode by remember { mutableStateOf(dnsState.mode) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied $text to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun openAndroidPrivateDnsSettings() {
        val intent = Intent("android.settings.PRIVATE_DNS_SETTINGS")
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            } catch (_: Exception) {
                try {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                } catch (_: Exception) {
                    Toast.makeText(context, "Could not open system settings automatically", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MusifyIcon(
                    MusifyIcons.Shield,
                    size = 20.dp,
                    tint = Accent,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = "AdGuard & System DNS",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Operating-system level network resolution layer",
                        color = Accent,
                        fontSize = 11.sp
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
                // Architectural separation callout
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Layer Separation Guarantee",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AdGuard Private DNS operates at the OS domain resolution level (DoT). In-page Shields operate inside the browser engine. DNS lookups cannot be attributed per-webpage, so their statuses remain strictly separate.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Configuration Mode Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Elevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Active DNS Guidance Mode",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMode = DnsProtectionMode.ADGUARD_PRIVATE_DNS
                                    viewModel.setDnsMode(DnsProtectionMode.ADGUARD_PRIVATE_DNS)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == DnsProtectionMode.ADGUARD_PRIVATE_DNS,
                                onClick = {
                                    selectedMode = DnsProtectionMode.ADGUARD_PRIVATE_DNS
                                    viewModel.setDnsMode(DnsProtectionMode.ADGUARD_PRIVATE_DNS)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Success)
                            )
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text("AdGuard Private DNS (Recommended)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Filtered DoT hostname: ${DnsSettingsRepository.ADGUARD_STANDARD_HOST}", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMode = DnsProtectionMode.SYSTEM_DEFAULT
                                    viewModel.setDnsMode(DnsProtectionMode.SYSTEM_DEFAULT)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == DnsProtectionMode.SYSTEM_DEFAULT,
                                onClick = {
                                    selectedMode = DnsProtectionMode.SYSTEM_DEFAULT
                                    viewModel.setDnsMode(DnsProtectionMode.SYSTEM_DEFAULT)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Success)
                            )
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text("System Default / Automatic", color = TextPrimary, fontSize = 12.sp)
                                Text("Standard ISP or Wi-Fi assigned DNS resolver", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // AdGuard Hostnames with 1-tap copy
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Official AdGuard DNS Hostnames",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        DnsHostCopyRow(
                            title = "Standard AdGuard DNS (Ad/Tracker Filtered)",
                            host = DnsSettingsRepository.ADGUARD_STANDARD_HOST,
                            onCopy = { copyToClipboard(DnsSettingsRepository.ADGUARD_STANDARD_HOST, "AdGuard DNS") }
                        )

                        DnsHostCopyRow(
                            title = "Troubleshooting DNS (Non-Filtering)",
                            host = DnsSettingsRepository.ADGUARD_UNFILTERED_HOST,
                            onCopy = { copyToClipboard(DnsSettingsRepository.ADGUARD_UNFILTERED_HOST, "AdGuard Unfiltered DNS") }
                        )
                    }
                }

                // Android Setup Instructions & Intent Button
                Button(
                    onClick = { openAndroidPrivateDnsSettings() },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Open Android Private DNS Settings",
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "Steps: Tap button above -> Select 'Private DNS provider hostname' -> Paste 'dns.adguard-dns.com' -> Save.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Elevated),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", color = TextPrimary)
            }
        },
        containerColor = Surface
    )
}

@Composable
private fun DnsHostCopyRow(
    title: String,
    host: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .padding(8.dp)
    ) {
        Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = host, color = Accent, fontSize = 11.sp)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Elevated)
                    .clickable(onClick = onCopy)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Copy", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
