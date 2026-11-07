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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.storage.TimeRange
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
import com.musify.browser.ui.theme.SafariDivider
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

/**
 * Safari Website Data & Privacy Screen (Phase 36).
 *
 * Implements Apple Safari's Settings -> Advanced -> Website Data:
 * - Top Bar: "Done" text button, "Website Data" title.
 * - Inset grouped iOS lists for clearing History, Cookies/Cache, Open Tabs, and Permissions.
 * - Time range selector.
 * - Destructive "Remove All Website Data" action button with confirmation dialog.
 */
@Composable
fun DataControlScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    var clearHistory by remember { mutableStateOf(true) }
    var clearBookmarks by remember { mutableStateOf(false) }
    var clearTabs by remember { mutableStateOf(false) }
    var clearDownloads by remember { mutableStateOf(false) }
    var clearSitePermissions by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.ALL_TIME) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onClose() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .semantics { contentDescription = "Safari Website Data" }
            .testTag("safari.website_data.root")
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
                        .testTag("safari.website_data.done"),
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
                    text = "Website Data",
                    style = SafariTypography_Title.copy(fontSize = 19.sp),
                    color = SafariTextPrimary
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Inset Grouped Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Data Types Group
                Text(
                    text = "DATA TYPES TO REMOVE",
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
                        SafariToggleRow(
                            title = "Browsing History",
                            subtitle = "List of visited websites and search queries",
                            checked = clearHistory,
                            onCheckedChange = { clearHistory = it }
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                        SafariToggleRow(
                            title = "Website Cookies & Cache",
                            subtitle = "Stored DOM storage, session tokens, and cached media",
                            checked = clearBookmarks,
                            onCheckedChange = { clearBookmarks = it }
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                        SafariToggleRow(
                            title = "Open Tab Sessions",
                            subtitle = "Restore state for background normal tabs",
                            checked = clearTabs,
                            onCheckedChange = { clearTabs = it }
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                        SafariToggleRow(
                            title = "Download History",
                            subtitle = "Records of downloaded files (files are kept)",
                            checked = clearDownloads,
                            onCheckedChange = { clearDownloads = it }
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                        SafariToggleRow(
                            title = "Website Permissions",
                            subtitle = "Camera, microphone, and location permissions",
                            checked = clearSitePermissions,
                            onCheckedChange = { clearSitePermissions = it }
                        )
                    }
                }

                // Section 2: Time Range
                Text(
                    text = "TIME RANGE",
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
                        TimeRange.values().forEachIndexed { index, range ->
                            val label = when (range) {
                                TimeRange.LAST_HOUR -> "The last hour"
                                TimeRange.LAST_24_HOURS -> "Today (Last 24 Hours)"
                                TimeRange.LAST_7_DAYS -> "Today and yesterday (Last 7 Days)"
                                TimeRange.ALL_TIME -> "All time"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTimeRange = range }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = SafariTypography_Callout,
                                    color = SafariTextPrimary
                                )
                                if (selectedTimeRange == range) {
                                    Icon(
                                        imageVector = MusifyIcons.Check,
                                        contentDescription = "Selected",
                                        tint = SafariAccentBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (index < TimeRange.values().size - 1) {
                                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                            }
                        }
                    }
                }

                // Section 3: Privacy Notice Footnote
                Text(
                    text = "Safari uses on-device Intelligent Tracking Prevention to block data profiling. Removing website data clears persistent storage and may sign you out of streaming services.",
                    style = SafariTypography_Footnote,
                    color = SafariTextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Feedback Message if available
                if (actionFeedbackMessage != null) {
                    Text(
                        text = actionFeedbackMessage ?: "",
                        style = SafariTypography_Footnote.copy(color = SafariGreen),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Destructive "Remove All Website Data" Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafariCardDark)
                        .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = { showConfirmDialog = true }
                        )
                        .testTag("safari.website_data.remove_all"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Remove All Website Data",
                        style = SafariTypography_Headline.copy(color = SafariDangerRed)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmation Alert
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Remove Website Data?",
                    style = SafariTypography_Title,
                    color = SafariTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will clear selected browsing data, cookies, and website storage for the chosen time range.",
                    style = SafariTypography_Subhead,
                    color = SafariTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearBrowsingData(
                            clearHistory = clearHistory,
                            clearBookmarks = clearBookmarks,
                            clearTabs = clearTabs,
                            clearDownloads = clearDownloads,
                            clearSitePermissions = clearSitePermissions,
                            timeRange = selectedTimeRange
                        )
                        actionFeedbackMessage = "Website data removed successfully."
                        showConfirmDialog = false
                    }
                ) {
                    Text(
                        text = "Remove Now",
                        style = SafariTypography_Headline.copy(color = SafariDangerRed)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        style = SafariTypography_Headline.copy(color = SafariAccentBlue)
                    )
                }
            },
            containerColor = SafariCardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SafariToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SafariTypography_Callout,
                color = SafariTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = SafariTypography_Footnote,
                color = SafariTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SafariGreen,
                uncheckedThumbColor = Color(0xFF8E8E93),
                uncheckedTrackColor = Color(0xFF38383A)
            )
        )
    }
}
