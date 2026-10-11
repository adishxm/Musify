package com.musify.browser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.filterlist.EngineBridgeStatus
import com.musify.browser.filterlist.FilterEngine
import com.musify.browser.filterlist.FilterListMetadata
import com.musify.browser.filterlist.FilterListRegistry
import com.musify.browser.filterlist.FilterListUpdater
import com.musify.browser.filterlist.UpdateResult
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilterListScreen(
    filterEngine: FilterEngine,
    filterUpdater: FilterListUpdater?,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val lists by filterEngine.listsState.collectAsState()
    val scope = rememberCoroutineScope()
    var updateStatusMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    Text("←", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Filter Lists & Licenses",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pipeline provenance, rule statistics, and licensing gates",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Engine Architecture & Bridge Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Engine Architecture & Defense Model",
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Primary Browser Protection: GeckoView Built-in ETP (Disconnect.me tables)\n" +
                                   "• Rule Pipeline: Musify Off-Thread Rule Compiler & Evaluator\n" +
                                   "• Native Bridge Status: C++ adblock-rust JNI bridge cataloged for future research; current engine-native ETP fulfills all MVP protection needs with zero battery penalty.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Status message if update was run
            if (updateStatusMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLightDark),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = updateStatusMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Section: Audited Filter Lists
            item {
                Text(
                    text = "Audited Filter Lists (${lists.size} active)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(lists) { metadata ->
                FilterListCard(
                    metadata = metadata,
                    onToggleEnabled = { enabled ->
                        val updated = metadata.copy(isEnabled = enabled)
                        val compiled = filterEngine.getCompiledList(metadata.id)
                        if (compiled != null) {
                            // Register updated metadata
                            filterEngine.registerList(
                                com.musify.browser.filterlist.CompiledFilterList(
                                    metadata = updated,
                                    stats = compiled.stats,
                                    domainBlocks = emptySet(),
                                    domainExceptions = emptySet(),
                                    pathPatternBlocks = emptyList(),
                                    pathPatternExceptions = emptyList()
                                )
                            )
                        }
                    }
                )
            }

            // Provenance and Licensing Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📜 Licensing Compliance & Attribution",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Peter Lowe's Blocklist: Permissive / Attribution License.\n" +
                                   "   Redistribution permitted with credit to Peter Lowe (pgl@yoyo.org).\n\n" +
                                   "2. EasyList & EasyPrivacy: Dual-licensed CC BY-SA 3.0 / GPLv3.\n" +
                                   "   Supported as remote subscription channels with strict attribution.\n\n" +
                                   "3. Zero Telemetry Guarantee: Filter compilation and evaluation occur entirely on this device. No browsing destinations or URLs are transmitted to list maintainers or third parties.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (filterUpdater != null && !isUpdating) {
                        isUpdating = true
                        updateStatusMessage = "Checking filter updates via HTTPS..."
                        scope.launch {
                            val seedTarget = FilterListRegistry.PETER_LOWE_SEED
                            val result = filterUpdater.updateList(seedTarget)
                            when (result) {
                                is UpdateResult.Success -> {
                                    updateStatusMessage = "Update successful! Activated version: ${result.metadata.version}"
                                }
                                is UpdateResult.Rollback -> {
                                    updateStatusMessage = "Update safely rolled back to last-known-good: ${result.reason}"
                                }
                                is UpdateResult.Failed -> {
                                    updateStatusMessage = "Update failed: ${result.error}"
                                }
                            }
                            isUpdating = false
                        }
                    } else if (filterUpdater == null) {
                        updateStatusMessage = "Offline demonstration mode: Bundled seed active with LKG backup."
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(8.dp),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Updating...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Text("Check for Updates", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onClose,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text("Close", color = TextPrimary)
            }
        }
    }
}

@Composable
private fun FilterListCard(
    metadata: FilterListMetadata,
    onToggleEnabled: (Boolean) -> Unit
) {
    val dateStr = if (metadata.lastUpdatedEpochMs > 0) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(metadata.lastUpdatedEpochMs))
    } else {
        "Bundled seed"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metadata.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version: ${metadata.version} • Updated: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = metadata.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = AccentCyan.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Transparent Rule Counters (Honest No False Parity Disclosures)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Supported Network Rules",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${metadata.supportedNetworkRules}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Exception Rules",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${metadata.supportedExceptionRules}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Unsupported Cosmetic",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${metadata.unsupportedCosmeticRules}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attribution & License tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceLightDark)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = metadata.license,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = metadata.attribution,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
