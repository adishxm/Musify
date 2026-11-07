package com.musify.browser.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariCardTertiary
import com.musify.browser.ui.theme.SafariDivider
import com.musify.browser.ui.theme.SafariFootnote
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariPurple
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTextTertiary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_LargeTitle
import com.musify.browser.ui.theme.SafariTypography_Subhead
import com.musify.browser.ui.theme.SafariTypography_Title

data class SafariFavorite(
    val id: String,
    val title: String,
    val url: String,
    val icon: ImageVector,
    val iconTint: Color = SafariTextPrimary,
    val backgroundBrush: Brush = Brush.linearGradient(
        listOf(SafariCardElevated, SafariCardTertiary)
    )
)

data class SafariReadingItem(
    val id: String,
    val title: String,
    val domain: String,
    val url: String,
    val timeAgo: String
)

/**
 * Safari Start Page (iOS / macOS HIG Inspiration).
 *
 * Implements Apple Safari's customizable Start Page:
 * - Clean iOS title header with private browsing badge
 * - 4-column Favorites squircle icon grid
 * - Intelligent Tracking Prevention (ITP) Privacy Report card widget
 * - Frequently Visited quick horizontal list
 * - Reading List items
 * - Edit Start Page button pill
 */
@Composable
fun SafariStartPage(
    isPrivate: Boolean = false,
    tabCount: Int = 1,
    canGoBack: Boolean = false,
    addressInput: String = "",
    blockedTrackersCount: Int = 142,
    onAddressChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigate: (String) -> Unit,
    onOpenPrivacyReport: () -> Unit = {},
    onOpenTabSwitcher: () -> Unit = {},
    onOpenShareSheet: () -> Unit = {},
    onOpenCustomization: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val favorites = remember {
        listOf(
            SafariFavorite(
                id = "spotify",
                title = "Spotify",
                url = "https://open.spotify.com/",
                icon = MusifyIcons.Music,
                iconTint = Color(0xFF1ED760),
                backgroundBrush = Brush.linearGradient(listOf(Color(0xFF121212), Color(0xFF1DB954).copy(alpha = 0.25f)))
            ),
            SafariFavorite(
                id = "youtube_music",
                title = "YT Music",
                url = "https://music.youtube.com/",
                icon = MusifyIcons.Music,
                iconTint = Color(0xFFFF0000),
                backgroundBrush = Brush.linearGradient(listOf(Color(0xFF212121), Color(0xFFFF0000).copy(alpha = 0.25f)))
            ),
            SafariFavorite(
                id = "youtube",
                title = "YouTube",
                url = "https://m.youtube.com/",
                icon = MusifyIcons.Desktop,
                iconTint = Color(0xFFFF0000),
                backgroundBrush = Brush.linearGradient(listOf(Color(0xFF1E1E1E), Color(0xFFFF3333).copy(alpha = 0.2f)))
            ),
            SafariFavorite(
                id = "soundcloud",
                title = "SoundCloud",
                url = "https://soundcloud.com/",
                icon = MusifyIcons.Bolt,
                iconTint = Color(0xFFFF5500),
                backgroundBrush = Brush.linearGradient(listOf(Color(0xFF221A16), Color(0xFFFF5500).copy(alpha = 0.25f)))
            ),
            SafariFavorite(
                id = "apple_music",
                title = "Apple Music",
                url = "https://music.apple.com/",
                icon = MusifyIcons.Music,
                iconTint = Color(0xFFFA2D48),
                backgroundBrush = Brush.linearGradient(listOf(Color(0xFF231418), Color(0xFFFA2D48).copy(alpha = 0.25f)))
            ),
            SafariFavorite(
                id = "reddit",
                title = "Reddit",
                url = "https://www.reddit.com/",
                icon = MusifyIcons.ShareIOS,
                iconTint = Color(0xFFFF4500),
                backgroundBrush = Brush.linearGradient(listOf(Color(0xFF1A1A1B), Color(0xFFFF4500).copy(alpha = 0.2f)))
            ),
            SafariFavorite(
                id = "wikipedia",
                title = "Wikipedia",
                url = "https://www.wikipedia.org/",
                icon = MusifyIcons.BookOpen,
                iconTint = SafariTextPrimary,
                backgroundBrush = Brush.linearGradient(listOf(SafariCardElevated, SafariCardTertiary))
            ),
            SafariFavorite(
                id = "github",
                title = "GitHub",
                url = "https://github.com/",
                icon = MusifyIcons.Shield,
                iconTint = SafariTextPrimary,
                backgroundBrush = Brush.linearGradient(listOf(Color(0xFF161B22), Color(0xFF30363D)))
            )
        )
    }

    val readingList = remember {
        listOf(
            SafariReadingItem(
                id = "safari_privacy",
                title = "Safari Intelligent Tracking Prevention Overview",
                domain = "apple.com",
                url = "https://www.apple.com/in/safari/",
                timeAgo = "Saved yesterday"
            ),
            SafariReadingItem(
                id = "brave_shields",
                title = "Brave Shields & Zero-Copy Content Filtering",
                domain = "github.com",
                url = "https://github.com/",
                timeAgo = "Saved 3 days ago"
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .testTag("safari.start_page.root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .padding(bottom = 90.dp) // Room for bottom floating tab bar
        ) {
            // Safari Header Row: Start Page / Favorites + Private Mode Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Favorites",
                        style = SafariTypography_LargeTitle,
                        color = SafariTextPrimary
                    )
                    Text(
                        text = "Designed & Engineered by adishxm",
                        style = SafariTypography_Caption,
                        color = SafariAccentBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isPrivate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SafariPurple.copy(alpha = 0.18f))
                            .border(1.dp, SafariPurple.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .semantics {
                                contentDescription = "10,000x Cryptographic Private Mode"
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MusifyIcon(
                                imageVector = MusifyIcons.Incognito,
                                size = 12.dp,
                                tint = SafariPurple
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "10,000x Private",
                                style = SafariTypography_Caption,
                                color = SafariPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Safari 4-Column Favorites Squircle Grid
            SafariFavoritesGrid(
                favorites = favorites,
                onNavigate = onNavigate
            )

            Spacer(Modifier.height(28.dp))

            // Apple Safari Privacy Report Card Widget
            SafariPrivacyReportCard(
                blockedCount = blockedTrackersCount,
                onClick = onOpenPrivacyReport
            )

            Spacer(Modifier.height(28.dp))

            // Reading List Section
            SafariReadingListSection(
                items = readingList,
                onNavigate = onNavigate
            )

            Spacer(Modifier.height(24.dp))

            // Edit Start Page Button Pill
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SafariCardDark)
                    .border(1.dp, SafariBorderDark, RoundedCornerShape(18.dp))
                    .clickable { onOpenCustomization() }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Customize UI and Start Page"
                    }
            ) {
                Text(
                    text = "Edit",
                    style = SafariTypography_Callout,
                    color = SafariAccentBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SafariFavoritesGrid(
    favorites: List<SafariFavorite>,
    onNavigate: (String) -> Unit
) {
    // 4 columns grid: 2 rows of 4
    val row1 = favorites.take(4)
    val row2 = favorites.drop(4).take(4)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row1.forEach { item ->
                SafariFavoriteItem(item = item, onClick = { onNavigate(item.url) })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row2.forEach { item ->
                SafariFavoriteItem(item = item, onClick = { onNavigate(item.url) })
            }
        }
    }
}

@Composable
private fun SafariFavoriteItem(
    item: SafariFavorite,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "Open favorite ${item.title}"
            }
            .testTag("safari.favorite.${item.id}")
    ) {
        // Squircle Icon Card
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(item.backgroundBrush)
                .border(1.dp, SafariBorderDark.copy(alpha = 0.7f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            MusifyIcon(
                imageVector = item.icon,
                size = 28.dp,
                tint = item.iconTint
            )
        }

        Spacer(Modifier.height(7.dp))

        Text(
            text = item.title,
            style = SafariTypography_Caption,
            color = SafariTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Signature Apple Safari Privacy Report Card Widget.
 * Inspired directly by apple.com/safari Intelligent Tracking Prevention.
 */
@Composable
fun SafariPrivacyReportCard(
    blockedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SafariCardDark)
            .border(1.dp, SafariBorderDark, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
            .semantics {
                role = Role.Button
                contentDescription = "Safari Privacy Report. $blockedCount trackers prevented from profiling you. Tap for details."
            }
            .testTag("safari.privacy_report.card")
    ) {
        Column {
            // Header Row: Shield + Title + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SafariGreen.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        MusifyIcon(
                            imageVector = MusifyIcons.ShieldCheck,
                            size = 18.dp,
                            tint = SafariGreen
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Privacy Report",
                        style = SafariTypography_Headline,
                        color = SafariTextPrimary
                    )
                }

                MusifyIcon(
                    imageVector = MusifyIcons.ArrowRight,
                    size = 16.dp,
                    tint = SafariTextSecondary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Main Metric
            Text(
                text = "In the last 30 days, Safari has prevented $blockedCount trackers from profiling you.",
                style = SafariTypography_Subhead,
                color = SafariTextPrimary,
                lineHeight = 21.sp
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "78% Websites Protected",
                    style = SafariTypography_Footnote,
                    color = SafariGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "•",
                    style = SafariTypography_Footnote,
                    color = SafariTextTertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Zero Profiling Allowed",
                    style = SafariTypography_Footnote,
                    color = SafariTextSecondary
                )
            }
        }
    }
}

@Composable
private fun SafariReadingListSection(
    items: List<SafariReadingItem>,
    onNavigate: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MusifyIcon(
                imageVector = MusifyIcons.ReadingList,
                size = 18.dp,
                tint = SafariTextPrimary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Reading List",
                style = SafariTypography_Title,
                color = SafariTextPrimary,
                fontSize = 19.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SafariCardDark)
                .border(1.dp, SafariBorderDark, RoundedCornerShape(16.dp))
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(item.url) }
                            .padding(14.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Reading list article ${item.title} from ${item.domain}"
                            }
                    ) {
                        Text(
                            text = item.title,
                            style = SafariTypography_Headline,
                            color = SafariTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.domain,
                                style = SafariTypography_Caption,
                                color = SafariTextSecondary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "•",
                                style = SafariTypography_Caption,
                                color = SafariTextTertiary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = item.timeAgo,
                                style = SafariTypography_Caption,
                                color = SafariTextTertiary
                            )
                        }
                    }

                    if (index < items.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(SafariDivider)
                        )
                    }
                }
            }
        }
    }
}
