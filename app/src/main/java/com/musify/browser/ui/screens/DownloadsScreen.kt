package com.musify.browser.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.downloads.DownloadItem
import com.musify.browser.downloads.DownloadRepository
import com.musify.browser.downloads.DownloadStatus
import com.musify.browser.downloads.MusifyDownloadManager
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Safari Downloads Screen (Phase 36).
 *
 * Implements Apple Safari's Downloads popover/sheet:
 * - Top Bar: "Done" text button, "Downloads" title, and "Clear" button.
 * - Inset grouped list of downloaded items with document icons, file sizes, and status.
 * - Apple Safari pill actions for Open, Share, and Cancel.
 * - Clean iOS empty state ("No Downloads").
 */
@Composable
fun DownloadsScreen(
    downloadManager: MusifyDownloadManager,
    downloadRepository: DownloadRepository = downloadManager.repository,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val downloads by downloadRepository.downloadsFlow.collectAsState()
    val context = LocalContext.current

    BackHandler { onClose() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .semantics { contentDescription = "Safari Downloads" }
            .testTag("safari.downloads.root")
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
                // Done Button (Left)
                Box(
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClick = onClose
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("safari.downloads.done"),
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
                    text = "Downloads",
                    style = SafariTypography_Title.copy(fontSize = 19.sp),
                    color = SafariTextPrimary
                )

                // Clear Button (Right)
                Box(
                    modifier = Modifier
                        .clickable(
                            enabled = downloads.isNotEmpty(),
                            role = Role.Button,
                            onClick = { downloadRepository.clearAll() }
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("safari.downloads.clear"),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Clear",
                        style = SafariTypography_Headline.copy(
                            color = if (downloads.isNotEmpty()) SafariAccentBlue else SafariTextSecondary.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Download Items List or Empty State
            if (downloads.isEmpty()) {
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
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(SafariCardDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MusifyIcons.Download,
                                contentDescription = null,
                                tint = SafariTextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Downloads",
                            style = SafariTypography_Title,
                            color = SafariTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Files you download from websites will appear here.",
                            style = SafariTypography_Subhead,
                            color = SafariTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(downloads, key = { it.id }) { item ->
                        SafariDownloadCard(
                            item = item,
                            onOpen = {
                                val uri = android.net.Uri.parse(item.url)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, item.mimeType ?: "*/*")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, item.url)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Download"))
                            },
                            onCancel = {
                                downloadManager.cancelDownload(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SafariDownloadCard(
    item: DownloadItem,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onCancel: () -> Unit
) {
    val dateStr = remember(item.createdAtEpochMs) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(item.createdAtEpochMs))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafariCardDark)
            .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Icon Squircle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(SafariCardElevated)
                        .border(0.5.dp, SafariBorderDark, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MusifyIcons.Download,
                        contentDescription = null,
                        tint = SafariAccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        style = SafariTypography_Headline.copy(fontSize = 15.sp),
                        color = SafariTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dateStr • ${item.status}",
                        style = SafariTypography_Footnote,
                        color = when (item.status) {
                            DownloadStatus.COMPLETED -> SafariGreen
                            DownloadStatus.FAILED -> SafariDangerRed
                            DownloadStatus.DOWNLOADING -> SafariAccentBlue
                            else -> SafariTextSecondary
                        }
                    )
                }
            }

            // Progress bar if currently downloading
            if (item.status == DownloadStatus.DOWNLOADING) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { item.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SafariAccentBlue,
                    trackColor = SafariCardElevated
                )
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.status == DownloadStatus.DOWNLOADING) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SafariCardElevated)
                            .clickable(onClick = onCancel)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = SafariTypography_Footnote.copy(color = SafariDangerRed)
                        )
                    }
                } else if (item.status == DownloadStatus.COMPLETED) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SafariCardElevated)
                            .clickable(onClick = onShare)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Share",
                            style = SafariTypography_Footnote.copy(color = SafariAccentBlue)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SafariAccentBlue)
                            .clickable(onClick = onOpen)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Open",
                            style = SafariTypography_Footnote.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}
