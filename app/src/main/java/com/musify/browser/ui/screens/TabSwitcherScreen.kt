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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
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
import com.musify.browser.ui.theme.AccentCyan
import com.musify.browser.ui.theme.AccentPurple
import com.musify.browser.ui.theme.BackgroundDark
import com.musify.browser.ui.theme.CardBorder
import com.musify.browser.ui.theme.StatusRed
import com.musify.browser.ui.theme.SurfaceDark
import com.musify.browser.ui.theme.SurfaceLightDark
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
    onCloseSwitcher: () -> Unit
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val initialTabMode = if (tabsState.selectedTab?.isPrivate == true) 1 else 0
    var selectedModeTab by remember { mutableIntStateOf(initialTabMode) }
    var showCloseAllConfirm by remember { mutableStateOf(false) }

    val normalTabs = tabsState.normalTabs
    val privateTabs = tabsState.privateTabs
    val isPrivateView = selectedModeTab == 1
    val currentModeTabs = if (isPrivateView) privateTabs else normalTabs

    val activeAccent = if (isPrivateView) AccentPurple else AccentCyan

    BackHandler {
        onCloseSwitcher()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            Column(modifier = Modifier.background(BackgroundDark)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tabs",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceLightDark)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${tabsState.tabCount}/${tabsState.maxTabs}",
                                    color = if (tabsState.isAtMaxCapacity) StatusRed else activeAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    actions = {
                        if (isPrivateView && privateTabs.isNotEmpty()) {
                            Text(
                                text = "Close All Private",
                                color = StatusRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.closeAllPrivateTabs() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .semantics {
                                        contentDescription = "Close all private tabs"
                                        role = androidx.compose.ui.semantics.Role.Button
                                    }
                            )
                        } else if (!isPrivateView && normalTabs.size > 1) {
                            Text(
                                text = "Close All",
                                color = StatusRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCloseAllConfirm = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .semantics {
                                        contentDescription = "Close all normal tabs"
                                        role = androidx.compose.ui.semantics.Role.Button
                                    }
                            )
                        }

                        Text(
                            text = "Done",
                            color = activeAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onCloseSwitcher)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .semantics {
                                    contentDescription = "Return to active browser tab"
                                    role = androidx.compose.ui.semantics.Role.Button
                                }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
                )

                // Tab Mode Selector: Normal Tabs | Private Tabs
                TabRow(
                    selectedTabIndex = selectedModeTab,
                    containerColor = BackgroundDark,
                    contentColor = activeAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedModeTab]),
                            color = activeAccent
                        )
                    }
                ) {
                    Tab(
                        selected = selectedModeTab == 0,
                        onClick = { selectedModeTab = 0 },
                        text = {
                            Text(
                                text = "Normal (${normalTabs.size})",
                                color = if (selectedModeTab == 0) AccentCyan else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedModeTab == 1,
                        onClick = { selectedModeTab = 1 },
                        text = {
                            Text(
                                text = "🕶 Private (${privateTabs.size})",
                                color = if (selectedModeTab == 1) AccentPurple else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val created = viewModel.createNewTab(
                            url = "about:blank",
                            isPrivate = isPrivateView,
                            selectImmediately = true
                        )
                        if (created) {
                            onCloseSwitcher()
                        }
                    },
                    enabled = !tabsState.isAtMaxCapacity,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeAccent,
                        disabledContainerColor = SurfaceLightDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .semantics {
                            contentDescription = if (tabsState.isAtMaxCapacity) {
                                "New tab button disabled: maximum 20 tabs reached"
                            } else if (isPrivateView) {
                                "Open a new private browsing tab"
                            } else {
                                "Open a new normal browsing tab"
                            }
                        }
                ) {
                    Text(
                        text = when {
                            tabsState.isAtMaxCapacity -> "⚠ Max 20 Tabs Limit Reached"
                            isPrivateView -> "+ New Private Tab 🕶"
                            else -> "+ New Tab"
                        },
                        color = if (tabsState.isAtMaxCapacity) TextSecondary else BackgroundDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Memory Cap Warning Banner
            if (tabsState.isAtMaxCapacity) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusRed.copy(alpha = 0.15f))
                        .border(1.dp, StatusRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Memory Cap Active: Tab limit reached (${tabsState.maxTabs} tabs). Close tabs before opening new ones.",
                        color = StatusRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Private Mode Info Banner
            if (isPrivateView) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentPurple.copy(alpha = 0.15f))
                        .border(1.dp, AccentPurple.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🕶 Private Browsing: Tabs, history, and cookies are kept in memory only and permanently erased when closed.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            if (currentModeTabs.isEmpty()) {
                // Empty state for this mode
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isPrivateView) "🕶" else "🌐",
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isPrivateView) "No Private Tabs Open" else "No Normal Tabs Open",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPrivateView)
                                "Private tabs leave no history or cookies on this device."
                            else
                                "Tap '+ New Tab' below to start browsing.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val created = viewModel.createNewTab(
                                    url = "about:blank",
                                    isPrivate = isPrivateView,
                                    selectImmediately = true
                                )
                                if (created) onCloseSwitcher()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isPrivateView) "Open Private Tab" else "Open New Tab",
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentModeTabs, key = { it.id }) { tab ->
                        TabCard(
                            tab = tab,
                            isSelected = tab.id == tabsState.selectedTabId,
                            accentColor = activeAccent,
                            onSelect = {
                                viewModel.selectTab(tab.id)
                                onCloseSwitcher()
                            },
                            onClose = {
                                viewModel.closeTab(tab.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCloseAllConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCloseAllConfirm = false },
            title = { Text("Close All Tabs?", color = TextPrimary) },
            text = { Text("This will close all open normal tabs and reset to a single clean tab.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showCloseAllConfirm = false
                        viewModel.closeAllTabs()
                        onCloseSwitcher()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Close All", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCloseAllConfirm = false },
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
private fun TabCard(
    tab: TabSession,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val borderColor = if (isSelected) accentColor else CardBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .semantics {
                contentDescription = "${if (tab.isPrivate) "Private " else ""}Tab: ${tab.title}, URL: ${tab.url}" +
                        if (isSelected) " (Active)" else ""
                role = androidx.compose.ui.semantics.Role.Button
            },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tab.isPrivate) "🕶 " else if (tab.isSecure) "🔒 " else "⚠ ",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                    Text(
                        text = if (tab.url == "about:blank") "New Tab" else tab.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Close tab button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SurfaceLightDark)
                        .clickable(onClick = onClose)
                        .semantics {
                            contentDescription = "Close tab: ${tab.title}"
                            role = androidx.compose.ui.semantics.Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Visual card thumbnail preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (tab.isPrivate) "🕶" else if (tab.url == "about:blank") "🌐" else "📄",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (tab.url == "about:blank") "Musify Home" else tab.origin,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tab.isRendererCrashed) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠ Crashed",
                            color = StatusRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer: URL indicator
            Text(
                text = if (tab.url == "about:blank") "about:blank" else tab.url,
                color = if (isSelected) accentColor else TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
