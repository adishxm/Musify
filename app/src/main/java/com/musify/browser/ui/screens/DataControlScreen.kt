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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.storage.TimeRange
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

/**
 * DataControlScreen provides granular controls to erase local browsing data,
 * clear session state, and review transparent privacy boundary disclosures.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataControlScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    var clearHistory by remember { mutableStateOf(true) }
    var clearBookmarks by remember { mutableStateOf(false) }
    var clearTabs by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.ALL_TIME) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onClose() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Data & Privacy Controls",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    Text(
                        text = "◀ Back",
                        color = AccentCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClose)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .semantics {
                                contentDescription = "Return to browser"
                                role = androidx.compose.ui.semantics.Role.Button
                            }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Privacy Boundary & Disclosure Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = "Transparent Privacy Boundaries",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Zero Telemetry: Musify transmits no analytics, crash logs, or telemetry to any server.\n" +
                               "• Local Data: Browsing history and bookmarks are stored only on this physical device.\n" +
                               "• Private Browsing: Private tabs are ephemeral, kept in RAM only, and leave no local disk trace.\n" +
                               "• Realism Guarantee: No browser provides complete anonymity. Websites, ISPs, and network administrators can still observe network endpoints and IP traffic unless external tunnel software (e.g. Tor or VPN) is used.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Clear Browsing Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Clear Browsing Data",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Time Range Selector
                    Text("Time Range:", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeRange.entries.forEach { range ->
                            val isSelected = selectedTimeRange == range
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BackgroundDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) AccentCyan else CardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedTimeRange = range }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (range) {
                                        TimeRange.LAST_HOUR -> "1 Hour"
                                        TimeRange.LAST_24_HOURS -> "24 Hours"
                                        TimeRange.LAST_7_DAYS -> "7 Days"
                                        TimeRange.ALL_TIME -> "All Time"
                                    },
                                    color = if (isSelected) AccentCyan else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Data Types Checkboxes
                    DataOptionCheckbox(
                        label = "Browsing History",
                        subtitle = "Visits, URLs, page titles",
                        checked = clearHistory,
                        onCheckedChange = { clearHistory = it }
                    )

                    DataOptionCheckbox(
                        label = "Bookmarks",
                        subtitle = "User saved bookmark links",
                        checked = clearBookmarks,
                        onCheckedChange = { clearBookmarks = it }
                    )

                    DataOptionCheckbox(
                        label = "Open Tabs & Sessions",
                        subtitle = "Closes all active tabs and resets session",
                        checked = clearTabs,
                        onCheckedChange = { clearTabs = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = clearHistory || clearBookmarks || clearTabs,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusRed,
                            disabledContainerColor = SurfaceLightDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Clear Selected Data Now",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (actionFeedbackMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = actionFeedbackMessage ?: "",
                            color = StatusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Private Session Quick Action
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕶", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = "Private Tabs Memory Disposal",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instantly release all private GeckoView engine sessions and dispose of ephemeral RAM caches.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.closeAllPrivateTabs()
                            actionFeedbackMessage = "All private tabs closed and memory purged."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Close & Purge All Private Tabs",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Clear Browsing Data", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to delete the selected browsing data (${selectedTimeRange.displayName})? This action is permanent and cannot be undone.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.clearBrowsingData(
                            clearHistory = clearHistory,
                            clearBookmarks = clearBookmarks,
                            clearTabs = clearTabs,
                            timeRange = selectedTimeRange
                        )
                        actionFeedbackMessage = "Selected browsing data successfully cleared."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Clear Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark)
                ) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun DataOptionCheckbox(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentCyan,
                uncheckedColor = TextSecondary,
                checkmarkColor = BackgroundDark
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
    }
}
