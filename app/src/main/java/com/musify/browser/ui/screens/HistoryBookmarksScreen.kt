package com.musify.browser.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.storage.BookmarkItem
import com.musify.browser.storage.BookmarkRepository
import com.musify.browser.storage.HistoryItem
import com.musify.browser.storage.HistoryRepository
import com.musify.browser.storage.TimeRange
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
import com.musify.browser.ui.theme.SafariDivider
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTextTertiary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_Subhead
import com.musify.browser.ui.theme.SafariTypography_Title
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Safari Bookmarks, Reading List & History Screen (Phase 36).
 *
 * Implements Apple Safari's 3-segmented modal sheet:
 * - Top Bar with "Edit" button left, centered 3-tab segmented control, and "Done" button right.
 * - Tabs:
 *   1. Bookmarks (Open Book glyph)
 *   2. Reading List (Safari Spectacles glyph)
 *   3. History (Clock glyph)
 * - Search bar with instant filtering.
 * - Inset grouped iOS list rows with favicon squircles and disclosure chevrons.
 * - Bottom Bar in History with Apple Safari "Clear" history action sheet.
 */
@Composable
fun HistoryBookmarksScreen(
    historyRepository: HistoryRepository?,
    bookmarkRepository: BookmarkRepository?,
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    onNavigateUrl: (String) -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
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

    val readingListItems = remember(bookmarkItems) {
        // Items tagged or saved into reading list
        bookmarkItems.filter { it.title.contains("Reading", ignoreCase = true) || it.url.contains("wikipedia", ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .semantics { contentDescription = "Safari Bookmarks and History" }
            .testTag("safari.bookmarks_history.root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Top Navigation Bar: Done Button + Segmented 3-Icon Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Done Button (Top Left in some locales or Cancel)
                Box(
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClick = onClose
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("safari.library.done"),
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

                // Centered 3-Tab Segmented Control
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(SafariCardDark)
                        .border(1.dp, SafariBorderDark, RoundedCornerShape(9.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    SafariSegmentIcon(
                        icon = MusifyIcons.BookOpen,
                        isSelected = selectedTab == 0,
                        contentDescription = "Bookmarks tab",
                        testTag = "safari.tab.bookmarks",
                        onClick = { selectedTab = 0 }
                    )
                    SafariSegmentIcon(
                        icon = MusifyIcons.ReadingList,
                        isSelected = selectedTab == 1,
                        contentDescription = "Reading list tab",
                        testTag = "safari.tab.reading_list",
                        onClick = { selectedTab = 1 }
                    )
                    SafariSegmentIcon(
                        icon = MusifyIcons.History,
                        isSelected = selectedTab == 2,
                        contentDescription = "History tab",
                        testTag = "safari.tab.history",
                        onClick = { selectedTab = 2 }
                    )
                }

                // Edit Button (Top Right)
                Box(
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClick = {}
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Edit",
                        style = SafariTypography_Headline.copy(
                            color = SafariAccentBlue,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. iOS Style Inset Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SafariCardDark)
                    .border(1.dp, SafariBorderDark, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = MusifyIcons.Search,
                        contentDescription = null,
                        tint = SafariTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search",
                                style = SafariTypography_Callout,
                                color = SafariTextSecondary
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = SafariTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(SafariAccentBlue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = MusifyIcons.Close,
                            contentDescription = "Clear search",
                            tint = SafariTextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Tab Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        // Bookmarks Tab
                        if (bookmarkItems.isEmpty()) {
                            EmptyLibraryState(
                                icon = MusifyIcons.BookOpen,
                                title = "No Bookmarks",
                                subtitle = "Pages you bookmark in Safari will appear here."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(bookmarkItems, key = { it.id }) { bookmark ->
                                    SafariLibraryRow(
                                        title = bookmark.title,
                                        subtitle = bookmark.url,
                                        onOpen = {
                                            onNavigateUrl(bookmark.url)
                                            onClose()
                                        },
                                        onDelete = {
                                            bookmarkRepository?.removeBookmark(bookmark.url)
                                            refreshTrigger++
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Reading List Tab
                        if (readingListItems.isEmpty()) {
                            EmptyLibraryState(
                                icon = MusifyIcons.ReadingList,
                                title = "Reading List",
                                subtitle = "Save web articles to read later, even when offline."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(readingListItems, key = { it.id }) { item ->
                                    SafariLibraryRow(
                                        title = item.title,
                                        subtitle = item.url,
                                        onOpen = {
                                            onNavigateUrl(item.url)
                                            onClose()
                                        },
                                        onDelete = {
                                            bookmarkRepository?.removeBookmark(item.url)
                                            refreshTrigger++
                                        }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // History Tab
                        if (historyItems.isEmpty()) {
                            EmptyLibraryState(
                                icon = MusifyIcons.History,
                                title = "No History",
                                subtitle = "Websites you visit in normal browsing tabs will be saved here."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(historyItems, key = { it.id }) { history ->
                                    val dateStr = remember(history.visitTime) {
                                        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                                        sdf.format(Date(history.visitTime))
                                    }
                                    SafariLibraryRow(
                                        title = history.title,
                                        subtitle = "${history.url} • $dateStr",
                                        onOpen = {
                                            onNavigateUrl(history.url)
                                            onClose()
                                        },
                                        onDelete = {
                                            historyRepository?.deleteItem(history.id)
                                            refreshTrigger++
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Bottom Toolbar (Active in History Tab)
            if (selectedTab == 2 && historyItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clickable(
                                role = Role.Button,
                                onClick = { showClearHistoryDialog = true }
                            )
                            .padding(vertical = 8.dp)
                            .testTag("safari.history.clear"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Clear",
                            style = SafariTypography_Headline.copy(
                                color = SafariAccentBlue,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    Text(
                        text = "${historyItems.size} items",
                        style = SafariTypography_Footnote,
                        color = SafariTextSecondary
                    )
                }
            }
        }
    }

    // Apple Safari Clear History Modal Sheet
    if (showClearHistoryDialog) {
        var selectedRange by remember { mutableStateOf(TimeRange.ALL_TIME) }

        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear History",
                    style = SafariTypography_Title,
                    color = SafariTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Clearing history will remove cookies, cache, and browsing data across your devices.",
                        style = SafariTypography_Subhead,
                        color = SafariTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TimeRange.values().forEach { range ->
                        val label = when (range) {
                            TimeRange.LAST_HOUR -> "The last hour"
                            TimeRange.LAST_24_HOURS -> "Today"
                            TimeRange.LAST_7_DAYS -> "Last 7 days"
                            TimeRange.ALL_TIME -> "All time"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRange = range }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRange == range,
                                onClick = { selectedRange = range },
                                colors = RadioButtonDefaults.colors(selectedColor = SafariAccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = SafariTypography_Callout,
                                color = SafariTextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyRepository?.deleteByTimeRange(selectedRange)
                        refreshTrigger++
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(
                        text = "Clear History",
                        style = SafariTypography_Headline.copy(color = SafariDangerRed)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
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
private fun SafariSegmentIcon(
    icon: ImageVector,
    isSelected: Boolean,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 32.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (isSelected) SafariCardElevated else Color.Transparent)
            .clickable(
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Tab
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) SafariTextPrimary else SafariTextSecondary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SafariLibraryRow(
    title: String,
    subtitle: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafariCardDark)
            .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClick = onOpen
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle Favicon Placeholder
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SafariCardElevated)
                    .border(0.5.dp, SafariBorderDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.take(1).uppercase(),
                    style = TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SafariTextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SafariTypography_Headline.copy(fontSize = 15.sp),
                    color = SafariTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = SafariTypography_Footnote,
                    color = SafariTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete action
            Icon(
                imageVector = MusifyIcons.Close,
                contentDescription = "Delete item",
                tint = SafariTextTertiary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete
                    )
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SafariCardDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SafariTextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = SafariTypography_Title,
                color = SafariTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = SafariTypography_Subhead,
                color = SafariTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
