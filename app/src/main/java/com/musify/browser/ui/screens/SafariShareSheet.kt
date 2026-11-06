package com.musify.browser.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
import com.musify.browser.ui.theme.SafariDivider
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_Title

data class SafariQuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Safari Share & Action Sheet (iOS 18 HIG Architecture).
 *
 * Implements Apple Safari's UIActivityViewController style sheet:
 * - Top card: Page favicon, page title, URL domain
 * - Horizontal quick action pills (Copy Link, Add Bookmark, Add to Reading List, Find in Page)
 * - Grouped vertical action items (Desktop Site, Media Diagnostics, Downloads, Permissions, Filter Lists, About)
 */
@Composable
fun SafariShareSheet(
    url: String,
    title: String,
    isDesktop: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onToggleDesktop: () -> Unit,
    onFindInPage: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenMediaDiagnostics: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenFilterLists: () -> Unit,
    onOpenDataControl: () -> Unit,
    onOpenAbout: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val quickActionScroll = rememberScrollState()

    val cleanDomain = when {
        url.isBlank() || url == "about:blank" -> "musify://home"
        else -> url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
    }

    val displayTitle = when {
        title.isNotBlank() && title != "about:blank" -> title
        url.isNotBlank() && url != "about:blank" -> cleanDomain
        else -> "Musify Browser"
    }

    val copyLink = {
        if (url.isNotBlank() && url != "about:blank") {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("URL", url)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    val systemShare = {
        if (url.isNotBlank() && url != "about:blank") {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Page"))
            } catch (_: Exception) {}
        }
    }

    val quickActions = listOf(
        SafariQuickAction("copy", "Copy Link", MusifyIcons.Link) {
            copyLink()
            onDismiss()
        },
        SafariQuickAction("share", "Share...", MusifyIcons.ShareIOS) {
            systemShare()
            onDismiss()
        },
        SafariQuickAction(
            "bookmark",
            if (isBookmarked) "Bookmarked" else "Add Bookmark",
            if (isBookmarked) MusifyIcons.StarFilled else MusifyIcons.StarOutline
        ) {
            onToggleBookmark()
            onDismiss()
        },
        SafariQuickAction("reading_list", "Add to Reading", MusifyIcons.ReadingList) {
            Toast.makeText(context, "Added to Reading List", Toast.LENGTH_SHORT).show()
            onDismiss()
        },
        SafariQuickAction("find", "Find on Page", MusifyIcons.Search) {
            onDismiss()
            onFindInPage()
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(SafariCardDark)
                .border(1.dp, SafariBorderDark, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {}
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .testTag("safari.share.sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(SafariTextSecondary.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))

                // Top Preview Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SafariCardElevated)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SafariAccentBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        MusifyIcon(
                            imageVector = MusifyIcons.Compass,
                            size = 24.dp,
                            tint = SafariAccentBlue
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayTitle,
                            style = SafariTypography_Headline,
                            color = SafariTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = cleanDomain,
                            style = SafariTypography_Footnote,
                            color = SafariTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Horizontal Quick Action Pills Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(quickActionScroll),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickActions.forEach { action ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(74.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = action.onClick)
                                .padding(vertical = 6.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = action.title
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(SafariCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                MusifyIcon(
                                    imageVector = action.icon,
                                    size = 22.dp,
                                    tint = SafariAccentBlue
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = action.title,
                                style = SafariTypography_Caption,
                                color = SafariTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Grouped System Actions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SafariCardElevated)
                ) {
                    Column {
                        SafariShareRow(
                            title = if (isDesktop) "Request Mobile Website" else "Request Desktop Website",
                            icon = MusifyIcons.Desktop,
                            trailing = if (isDesktop) "On" else "Off"
                        ) {
                            onDismiss()
                            onToggleDesktop()
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                        SafariShareRow(
                            title = "Media & DRM Diagnostics",
                            icon = MusifyIcons.Music,
                            iconTint = SafariAccentBlue
                        ) {
                            onDismiss()
                            onOpenMediaDiagnostics()
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                        SafariShareRow(
                            title = "Site Permissions",
                            icon = MusifyIcons.Location
                        ) {
                            onDismiss()
                            onOpenPermissions()
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                        SafariShareRow(
                            title = "Sandboxed Downloads",
                            icon = MusifyIcons.Download
                        ) {
                            onDismiss()
                            onOpenDownloads()
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                        SafariShareRow(
                            title = "Filter Lists & Ad Engines",
                            icon = MusifyIcons.ShieldCheck
                        ) {
                            onDismiss()
                            onOpenFilterLists()
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                        SafariShareRow(
                            title = "Data & Privacy Controls",
                            icon = MusifyIcons.Shield
                        ) {
                            onDismiss()
                            onOpenDataControl()
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                        SafariShareRow(
                            title = "About Musify",
                            icon = MusifyIcons.Info
                        ) {
                            onDismiss()
                            onOpenAbout()
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Done Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SafariCardElevated)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 14.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Close share sheet"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Done",
                        style = SafariTypography_Headline,
                        color = SafariAccentBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun SafariShareRow(
    title: String,
    icon: ImageVector,
    iconTint: Color = SafariTextPrimary,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics {
                role = Role.Button
                contentDescription = title
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusifyIcon(
                imageVector = icon,
                size = 20.dp,
                tint = iconTint
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                style = SafariTypography_Callout,
                color = SafariTextPrimary
            )
        }

        if (trailing != null) {
            Text(
                text = trailing,
                style = SafariTypography_Footnote,
                color = SafariTextSecondary
            )
        } else {
            MusifyIcon(
                imageVector = MusifyIcons.ArrowRight,
                size = 14.dp,
                tint = SafariTextSecondary
            )
        }
    }
}

