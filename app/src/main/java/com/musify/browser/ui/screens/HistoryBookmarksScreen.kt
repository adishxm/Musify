package com.musify.browser.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.storage.BookmarkItem
import com.musify.browser.storage.BookmarkRepository
import com.musify.browser.storage.HistoryItem
import com.musify.browser.storage.HistoryRepository
import com.musify.browser.storage.TimeRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.Hairline
import com.musify.browser.ui.theme.IconLabel
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.Private
import com.musify.browser.ui.theme.Danger
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.Elevated
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBookmarksScreen(
    historyRepository: HistoryRepository?,
    bookmarkRepository: BookmarkRepository?,
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    onNavigateUrl: (String) -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    BackHandler { onClose() }

    val historyItems = remember(searchQuery, refreshTrigger) {
        historyRepository?.getHistory(searchQuery) ?: emptyList()
    }

    val bookmarkItems = remember(searchQuery, refreshTrigger) {
        bookmarkRepository?.getBookmarks(searchQuery) ?: emptyList()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Ink,
        topBar = {
            Column(modifier = Modifier.background(Surface)) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedTab == 0) "Bookmarks" else "Browsing History",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconLabel(
                            MusifyIcons.ArrowLeft,
                            "Back",
                            tint = Accent,
                            iconSize = 14.dp,
                            style = MaterialTheme.typography.labelLarge,
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
                    actions = {
                        if (selectedTab == 1 && historyItems.isNotEmpty()) {
                            Text(
                                text = "Clear...",
                                color = Danger,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showClearHistoryDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .semantics {
                                        contentDescription = "Clear browsing history"
                                        role = androidx.compose.ui.semantics.Role.Button
                                    }
                            )
                        } else if (selectedTab == 0 && bookmarkItems.isNotEmpty()) {
                            Text(
                                text = "Clear All",
                                color = Danger,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        bookmarkRepository?.clearAll()
                                        refreshTrigger++
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ${if (selectedTab == 0) "bookmarks" else "history"}...", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Hairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Tabs: Bookmarks | History
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Surface,
                    contentColor = Accent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Accent
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            IconLabel(
                                MusifyIcons.StarFilled,
                                "Bookmarks (${bookmarkItems.size})",
                                tint = TextPrimary,
                                iconSize = 15.dp,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            IconLabel(
                                MusifyIcons.Clock,
                                "History (${historyItems.size})",
                                tint = TextPrimary,
                                iconSize = 15.dp,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == 0) {
                // Bookmarks list
                if (bookmarkItems.isEmpty()) {
                    EmptyStoragePlaceholder(
                        icon = MusifyIcons.StarFilled,
                        title = "No Bookmarks Yet",
                        subtitle = "Tap the star icon in the address bar while browsing to save bookmarks."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bookmarkItems, key = { it.id }) { item ->
                            BookmarkRow(
                                item = item,
                                onClick = { onNavigateUrl(item.url) },
                                onDelete = {
                                    bookmarkRepository?.removeBookmark(item.url)
                                    refreshTrigger++
                                }
                            )
                        }
                    }
                }
            } else {
                // History list
                if (historyItems.isEmpty()) {
                    EmptyStoragePlaceholder(
                        icon = MusifyIcons.Clock,
                        title = "No History Records",
                        subtitle = "Visited pages in normal tabs appear here. Private tabs never save history."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyItems, key = { it.id }) { item ->
                            HistoryRow(
                                item = item,
                                onClick = { onNavigateUrl(item.url) },
                                onDelete = {
                                    historyRepository?.deleteItem(item.id)
                                    refreshTrigger++
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        ClearHistoryDialog(
            onDismiss = { showClearHistoryDialog = false },
            onClear = { range ->
                showClearHistoryDialog = false
                historyRepository?.deleteByTimeRange(range)
                refreshTrigger++
            }
        )
    }
}

@Composable
private fun BookmarkRow(
    item: BookmarkItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MusifyIcon(
                            MusifyIcons.StarFilled,
                            size = 18.dp,
                            tint = Accent,
                            modifier = Modifier.padding(end = 12.dp)
                        )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.url,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Elevated)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                MusifyIcon(MusifyIcons.Close, size = 14.dp, tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: HistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(item.visitTime) {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(item.visitTime))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.url,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = dateString, color = TextMuted, fontSize = 11.sp)
                    if (item.visitCount > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Elevated)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${item.visitCount} visits",
                                color = Accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Elevated)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                MusifyIcon(MusifyIcons.Close, size = 14.dp, tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun EmptyStoragePlaceholder(icon: ImageVector, title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MusifyIcon(icon, size = 48.dp, tint = TextMuted)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun ClearHistoryDialog(
    onDismiss: () -> Unit,
    onClear: (TimeRange) -> Unit
) {
    var selectedRange by remember { mutableStateOf(TimeRange.ALL_TIME) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Browsing History", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select time range to clear:", color = TextSecondary, fontSize = 13.sp)
                TimeRange.entries.forEach { range ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedRange == range) Elevated else Ink)
                            .clickable { selectedRange = range }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedRange == range) "● " else "○ ",
                            color = if (selectedRange == range) Accent else TextSecondary
                        )
                        Text(
                            text = range.displayName,
                            color = if (selectedRange == range) TextPrimary else TextSecondary,
                            fontWeight = if (selectedRange == range) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onClear(selectedRange) },
                colors = ButtonDefaults.buttonColors(containerColor = Danger)
            ) {
                Text("Clear History", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Elevated)
            ) {
                Text("Cancel", color = TextPrimary)
            }
        },
        containerColor = Surface
    )
}
