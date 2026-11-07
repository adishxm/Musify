package com.musify.browser.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.filterlist.EngineBridgeStatus
import com.musify.browser.filterlist.FilterEngine
import com.musify.browser.filterlist.FilterListMetadata
import com.musify.browser.filterlist.FilterListUpdater
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_Subhead
import com.musify.browser.ui.theme.SafariTypography_Title
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Safari Content Blockers Screen (Phase 36).
 *
 * Implements Apple Safari's Settings -> Safari -> Content Blockers:
 * - Top Bar: "Done" text button, "Content Blockers" title.
 * - Header description explaining Safari content blocking.
 * - Inset grouped iOS list of rule sets with toggle switches.
 * - Engine stats card (compiled rule count, Bloom filter, engine bridge).
 * - "Check for Updates" action.
 */
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

    BackHandler { onClose() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .semantics { contentDescription = "Safari Content Blockers" }
            .testTag("safari.content_blockers.root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClick = onClose
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("safari.content_blockers.done"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Done",
                        style = SafariTypography_Headline.copy(
                            color = SafariAccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Text(
                    text = "Content Blockers",
                    style = SafariTypography_Title.copy(fontSize = 19.sp),
                    color = SafariTextPrimary
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Inset Grouped Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Content blockers prevent trackers, cookies, ads, and other content from loading in Safari.",
                    style = SafariTypography_Subhead,
                    color = SafariTextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Section 1: Active Content Blockers List
                Text(
                    text = "ALLOW CONTENT BLOCKERS ON ALL WEBSITES",
                    style = SafariTypography_Footnote.copy(fontWeight = FontWeight.Medium),
                    color = SafariTextSecondary,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafariCardDark)
                        .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        lists.forEachIndexed { index, list ->
                            SafariContentBlockerRow(
                                metadata = list,
                                onToggle = { enabled ->
                                    filterEngine.setListEnabled(list.id, enabled)
                                }
                            )
                            if (index < lists.size - 1) {
                                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                            }
                        }
                    }
                }

                // Section 2: Engine Status Card
                Text(
                    text = "FILTERING ENGINE STATUS",
                    style = SafariTypography_Footnote.copy(fontWeight = FontWeight.Medium),
                    color = SafariTextSecondary,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafariCardDark)
                        .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Active Shield Rules",
                                style = SafariTypography_Callout,
                                color = SafariTextPrimary
                            )
                            Text(
                                text = "${lists.filter { it.isEnabled }.sumOf { it.totalSupportedRules }} rules",
                                style = SafariTypography_Callout.copy(fontWeight = FontWeight.SemiBold),
                                color = SafariGreen
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "High-Speed Bloom Filter",
                                style = SafariTypography_Callout,
                                color = SafariTextPrimary
                            )
                            Text(
                                text = "32 KB In-Memory",
                                style = SafariTypography_Callout,
                                color = SafariTextSecondary
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Engine Interception Bridge",
                                style = SafariTypography_Callout,
                                color = SafariTextPrimary
                            )
                            Text(
                                text = "Active (Gecko & WebView)",
                                style = SafariTypography_Callout,
                                color = SafariAccentBlue
                            )
                        }
                    }
                }

                // Section 3: Check for Updates Button
                if (filterUpdater != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SafariCardDark)
                            .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                            .clickable(
                                enabled = !isUpdating,
                                role = Role.Button,
                                onClick = {
                                    scope.launch {
                                        isUpdating = true
                                        updateStatusMessage = null
                                        var updatedCount = 0
                                        for (list in lists) {
                                            val result = filterUpdater.updateList(list)
                                            if (result is com.musify.browser.filterlist.UpdateResult.Success) {
                                                updatedCount++
                                            }
                                        }
                                        updateStatusMessage = if (updatedCount > 0) {
                                            "Updated $updatedCount rule catalogs successfully."
                                        } else {
                                            "Rules are up to date."
                                        }
                                        isUpdating = false
                                    }
                                }
                            )
                            .testTag("safari.content_blockers.update"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SafariAccentBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Check for Rule Updates",
                                style = SafariTypography_Headline.copy(color = SafariAccentBlue)
                            )
                        }
                    }

                    if (updateStatusMessage != null) {
                        Text(
                            text = updateStatusMessage ?: "",
                            style = SafariTypography_Footnote.copy(color = SafariGreen),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SafariContentBlockerRow(
    metadata: FilterListMetadata,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!metadata.isEnabled) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metadata.name,
                style = SafariTypography_Callout.copy(fontWeight = FontWeight.Medium),
                color = SafariTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${metadata.totalSupportedRules} rules • ${metadata.license}",
                style = SafariTypography_Footnote,
                color = SafariTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = metadata.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SafariGreen,
                uncheckedThumbColor = Color(0xFF8E8E93),
                uncheckedTrackColor = Color(0xFF38383A)
            )
        )
    }
}
