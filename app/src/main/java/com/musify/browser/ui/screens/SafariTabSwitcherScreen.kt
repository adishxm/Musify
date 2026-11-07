package com.musify.browser.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.musify.browser.tabs.TabSession
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
import com.musify.browser.ui.theme.SafariDivider
import com.musify.browser.ui.theme.SafariGlass
import com.musify.browser.ui.theme.SafariGlassBorder
import com.musify.browser.ui.theme.SafariPurple
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTextTertiary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_Title

/**
 * Safari Tab Switcher Overview (iOS 18 HIG Architecture).
 *
 * Implements Apple Safari's 2-column card deck tab overview:
 * - Top Bar: Segmented control (Tabs vs Private) + "Done" button
 * - 2-column cards with aspect ratio, favicon/title header, and close (×) button
 * - Active tab highlighted with Safari Accent Blue (or Purple in Private mode)
 * - Bottom Bar: "+" New Tab, Tab Groups pill, and Close All option
 */
@Composable
fun SafariTabSwitcherScreen(
    viewModel: TabsViewModel,
    onCloseSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val initialMode = if (tabsState.selectedTab?.isPrivate == true) 1 else 0
    var selectedModeTab by remember { mutableIntStateOf(initialMode) }
    var showCloseAllConfirm by remember { mutableStateOf(false) }

    val normalTabs = tabsState.normalTabs
    val privateTabs = tabsState.privateTabs
    val isPrivate = selectedModeTab == 1
    val currentTabs = if (isPrivate) privateTabs else normalTabs
    val activeAccent = if (isPrivate) SafariPurple else SafariAccentBlue

    BackHandler {
        onCloseSwitcher()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .testTag("safari.tab_switcher.root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Safari Navigation Header: Segmented Pill + Done Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Segmented Mode Selector: Tabs (N) | Private (N)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafariCardElevated)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isPrivate) SafariCardDark else Color.Transparent)
                            .clickable { selectedModeTab = 0 }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .semantics {
                                role = Role.Tab
                                contentDescription = "Normal tabs, ${normalTabs.size} open"
                            }
                            .testTag("safari.tab_mode.normal")
                    ) {
                        Text(
                            text = "${normalTabs.size} Tabs",
                            style = SafariTypography_Footnote,
                            fontWeight = if (!isPrivate) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isPrivate) SafariTextPrimary else SafariTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPrivate) SafariPurple.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { selectedModeTab = 1 }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .semantics {
                                role = Role.Tab
                                contentDescription = "Private tabs, ${privateTabs.size} open"
                            }
                            .testTag("safari.tab_mode.private")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MusifyIcon(
                                imageVector = MusifyIcons.Incognito,
                                size = 12.dp,
                                tint = if (isPrivate) SafariPurple else SafariTextSecondary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Private",
                                style = SafariTypography_Footnote,
                                fontWeight = if (isPrivate) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPrivate) SafariPurple else SafariTextSecondary
                            )
                        }
                    }
                }

                // Done Button (Apple Blue)
                Text(
                    text = "Done",
                    style = SafariTypography_Headline,
                    color = activeAccent,
                    modifier = Modifier
                        .clickable(onClick = onCloseSwitcher)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Done, close tab switcher"
                        }
                        .testTag("safari.button.done")
                )
            }

            // Tab Cards Deck (2-Column Grid)
            if (currentTabs.isEmpty() && isPrivate) {
                // Safari Private Browsing Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SafariCardDark)
                            .border(1.dp, SafariBorderDark, RoundedCornerShape(20.dp))
                            .padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SafariPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            MusifyIcon(
                                imageVector = MusifyIcons.Incognito,
                                size = 30.dp,
                                tint = SafariPurple
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "10,000x Private Browsing Mode",
                            style = SafariTypography_Title,
                            color = SafariTextPrimary
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "10,000x Extreme Cryptographic Shredder Active (Engineered by adishxm).\nAll private browsing sessions, DOM caches, and session cookies are subjected to 3-pass DoD 5220.22-M entropy zeroization upon closure.",
                            style = SafariTypography_Footnote,
                            color = SafariTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        val localCtx = androidx.compose.ui.platform.LocalContext.current
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SafariPurple.copy(alpha = 0.25f))
                                .border(1.dp, SafariPurple, RoundedCornerShape(12.dp))
                                .clickable {
                                    com.musify.browser.crypto.CryptographicShredder.executePanicWipe(localCtx) {
                                        android.widget.Toast.makeText(
                                            localCtx,
                                            "⚡ RAM Shredded & Zeroized by adishxm",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Execute Panic Shred"
                                }
                                .testTag("safari.button.shred_wipe"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡ Shred & Panic Wipe RAM Now",
                                style = SafariTypography_Callout,
                                fontWeight = FontWeight.Bold,
                                color = SafariPurple
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(currentTabs, key = { it.id }) { tab ->
                        val isSelected = tab.id == tabsState.selectedTabId

                        SafariTabCard(
                            tab = tab,
                            isSelected = isSelected,
                            accentColor = activeAccent,
                            onSelect = {
                                viewModel.selectTab(tab.id)
                                onCloseSwitcher()
                            },
                            onClose = {
                                if (tab.isPrivate) {
                                    com.musify.browser.crypto.CryptographicShredder.corruptStringReference(tab.url)
                                }
                                viewModel.closeTab(tab.id)
                            }
                        )
                    }
                }
            }

            // Bottom Safari Tab Overview Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(SafariGlass)
                    .border(1.dp, SafariGlassBorder)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // New Tab Button (+)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            val created = viewModel.createNewTab(
                                url = "about:blank",
                                isPrivate = isPrivate,
                                selectImmediately = true
                            )
                            if (created) onCloseSwitcher()
                        }
                        .semantics {
                            role = Role.Button
                            contentDescription = "New tab"
                        }
                        .testTag("safari.button.new_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    MusifyIcon(
                        imageVector = MusifyIcons.Plus,
                        size = 24.dp,
                        tint = activeAccent
                    )
                }

                // Center: Tabs indicator
                Text(
                    text = "${currentTabs.size} Tabs",
                    style = SafariTypography_Footnote,
                    fontWeight = FontWeight.SemiBold,
                    color = SafariTextPrimary
                )

                // Close All Option
                if (currentTabs.size > 1) {
                    Text(
                        text = "Close All",
                        style = SafariTypography_Footnote,
                        color = SafariDangerRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { showCloseAllConfirm = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Close all tabs"
                            }
                    )
                } else {
                    Spacer(Modifier.width(38.dp))
                }
            }
        }

        // Close All Confirmation Dialog
        if (showCloseAllConfirm) {
            AlertDialog(
                onDismissRequest = { showCloseAllConfirm = false },
                title = {
                    Text(
                        text = "Close All Tabs?",
                        style = SafariTypography_Headline,
                        color = SafariTextPrimary
                    )
                },
                text = {
                    Text(
                        text = "This will close all ${currentTabs.size} active tabs.",
                        style = SafariTypography_Footnote,
                        color = SafariTextSecondary
                    )
                },
                confirmButton = {
                    Text(
                        text = "Close All",
                        color = SafariDangerRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                if (isPrivate) {
                                    com.musify.browser.crypto.CryptographicShredder.executePanicWipe(context)
                                    viewModel.closeAllPrivateTabs()
                                } else {
                                    normalTabs.forEach { viewModel.closeTab(it.id) }
                                }
                                showCloseAllConfirm = false
                            }
                            .padding(8.dp)
                    )
                },
                dismissButton = {
                    Text(
                        text = "Cancel",
                        color = SafariAccentBlue,
                        modifier = Modifier
                            .clickable { showCloseAllConfirm = false }
                            .padding(8.dp)
                    )
                },
                containerColor = SafariCardDark
            )
        }
    }
}

@Composable
private fun SafariTabCard(
    tab: TabSession,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val cleanDomain = when {
        tab.url.isBlank() || tab.url == "about:blank" -> "Start Page"
        tab.origin.isNotBlank() -> tab.origin.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        else -> tab.url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
    }

    val displayTitle = when {
        tab.title.isNotBlank() && tab.title != "about:blank" -> tab.title
        else -> cleanDomain
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .clip(RoundedCornerShape(16.dp))
            .background(SafariCardDark)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else SafariBorderDark,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .semantics {
                role = Role.Button
                contentDescription = "Tab $displayTitle, $cleanDomain. ${if (isSelected) "Selected." else ""}"
            }
            .testTag("safari.tab_card.${tab.id}")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Card Header Bar: Favicon + Title + Close Button (×)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SafariCardElevated)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MusifyIcon(
                        imageVector = if (tab.url == "about:blank") MusifyIcons.Compass else MusifyIcons.Globe,
                        size = 14.dp,
                        tint = SafariTextSecondary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = displayTitle,
                        style = SafariTypography_Caption,
                        color = SafariTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Close Button (×)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(SafariCardDark)
                        .clickable(onClick = onClose)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Close tab $displayTitle"
                        }
                        .testTag("safari.button.close_tab.${tab.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    MusifyIcon(
                        imageVector = MusifyIcons.Close,
                        size = 10.dp,
                        tint = SafariTextSecondary
                    )
                }
            }

            // Card Body Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SafariBackgroundDark,
                                SafariCardDark
                            )
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MusifyIcon(
                        imageVector = if (tab.url == "about:blank") MusifyIcons.Compass else MusifyIcons.Globe,
                        size = 32.dp,
                        tint = SafariTextTertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = cleanDomain,
                        style = SafariTypography_Footnote,
                        color = SafariTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

