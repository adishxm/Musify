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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.engine.EngineView
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
 * BrowserShellScreen hosts the multi-tab navigation chrome, Omnibar,
 * active engine renderer, private browsing indicators, privacy shield sheet, and overflow menu.
 */
@Composable
fun BrowserShellScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDataControl: () -> Unit,
    onOpenFilterLists: (() -> Unit)? = null,
    onOpenSpikeDebug: (() -> Unit)? = null,
    onOpenRoadmap: (() -> Unit)? = null
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val activeTab = tabsState.selectedTab
    val activeAdapter = viewModel.getActiveEngineAdapter()
    val activeShieldState by viewModel.activeShieldState.collectAsState()
    val httpFallbackPrompt by viewModel.httpFallbackPrompt.collectAsState()

    var showTabSwitcher by remember { mutableStateOf(false) }
    var addressInput by remember { mutableStateOf("") }
    var isAddressBarFocused by remember { mutableStateOf(false) }
    var showShieldSheet by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Synchronize address bar and bookmark star state when active tab updates
    if (!isAddressBarFocused && activeTab != null) {
        val displayUrl = if (activeTab.url == "about:blank") "" else activeTab.url
        if (addressInput != displayUrl && displayUrl.isNotEmpty()) {
            addressInput = displayUrl
        }
        isBookmarked = viewModel.isCurrentTabBookmarked()
    }

    // Intercept hardware back button: close shield sheet, back in web page, or close switcher
    BackHandler(enabled = showShieldSheet || showTabSwitcher || (activeTab?.canGoBack == true)) {
        if (showShieldSheet) {
            showShieldSheet = false
        } else if (showTabSwitcher) {
            showTabSwitcher = false
        } else if (activeTab?.canGoBack == true) {
            viewModel.goBack()
        }
    }

    if (showTabSwitcher) {
        TabSwitcherScreen(
            viewModel = viewModel,
            modifier = modifier,
            onCloseSwitcher = { showTabSwitcher = false }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            // Omnibar Top Chrome
            SurfaceDarkHeader(
                activeTab = activeTab,
                tabCount = tabsState.tabCount,
                maxTabs = tabsState.maxTabs,
                addressInput = addressInput,
                isAddressBarFocused = isAddressBarFocused,
                isBookmarked = isBookmarked,
                shieldBlockedCount = activeShieldState.blockedCount,
                isShieldEnabled = viewModel.isShieldEnabledForCurrentTab(),
                onAddressChange = { addressInput = it },
                onFocusChange = { isAddressBarFocused = it },
                onSubmit = {
                    if (addressInput.isNotBlank()) {
                        viewModel.navigateActiveTab(addressInput)
                        focusManager.clearFocus()
                    }
                },
                onToggleBookmark = {
                    isBookmarked = viewModel.toggleBookmarkCurrentTab()
                },
                onOpenTabSwitcher = { showTabSwitcher = true },
                onNewTab = {
                    val created = viewModel.createNewTab(
                        url = "about:blank",
                        isPrivate = activeTab?.isPrivate == true,
                        selectImmediately = true
                    )
                    if (created) addressInput = ""
                },
                onGoBack = { viewModel.goBack() },
                onGoForward = { viewModel.goForward() },
                onReloadOrStop = {
                    if (activeTab?.isLoading == true) viewModel.stop() else viewModel.reload()
                },
                onToggleShield = { showShieldSheet = !showShieldSheet },
                onOpenBookmarks = onOpenBookmarks,
                onOpenHistory = onOpenHistory,
                onOpenDataControl = onOpenDataControl,
                onOpenFilterLists = onOpenFilterLists,
                onNewPrivateTab = {
                    viewModel.createNewTab(url = "about:blank", isPrivate = true, selectImmediately = true)
                    addressInput = ""
                },
                onOpenRoadmap = onOpenRoadmap,
                onOpenSpikeDebug = onOpenSpikeDebug,
                onNavigate = { target ->
                    viewModel.navigateActiveTab(target)
                    focusManager.clearFocus()
                }
            )

            // Loading Progress Bar
            if (activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    progress = { (activeTab.progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (activeTab.isPrivate) AccentPurple else AccentCyan,
                    trackColor = BackgroundDark
                )
            }

            // Shield Details Sheet (Collapsible)
            AnimatedVisibility(visible = showShieldSheet) {
                PrivacyShieldSheet(
                    viewModel = viewModel,
                    onClose = { showShieldSheet = false }
                )
            }

            // Main Web Engine Canvas or Crash State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                if (activeAdapter != null && activeTab?.isRendererCrashed == false) {
                    // Render Active Tab Engine View
                    EngineView(
                        adapter = activeAdapter,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Blank Tab Home Screen
                if (activeTab?.url == "about:blank" && !activeTab.isLoading) {
                    MusifyHomeScreen(
                        isPrivate = activeTab.isPrivate,
                        onNavigate = { target ->
                            viewModel.navigateActiveTab(target)
                        }
                    )
                }

                // Renderer Crash Overlay
                if (activeTab?.isRendererCrashed == true) {
                    RendererCrashOverlay(
                        onRecover = { viewModel.recoverRenderer() }
                    )
                }
            }
        }
    }

    // Conservative HTTPS-First Explicit HTTP Fallback Dialog
    if (httpFallbackPrompt != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissHttpFallback() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = "Insecure Connection Warning",
                        color = StatusOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The address '${httpFallbackPrompt?.host}' does not use HTTPS encryption.",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Information sent over unencrypted HTTP (such as passwords, login tokens, or form data) can be inspected or intercepted by anyone on your network.\n\nDo you wish to proceed to this unencrypted site?",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmHttpFallback() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Proceed Insecurely", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.dismissHttpFallback() },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun SurfaceDarkHeader(
    activeTab: com.musify.browser.tabs.TabSession?,
    tabCount: Int,
    maxTabs: Int,
    addressInput: String,
    isAddressBarFocused: Boolean,
    isBookmarked: Boolean,
    shieldBlockedCount: Int,
    isShieldEnabled: Boolean,
    onAddressChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onNewTab: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onToggleShield: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDataControl: () -> Unit,
    onOpenFilterLists: (() -> Unit)? = null,
    onNewPrivateTab: () -> Unit,
    onOpenRoadmap: (() -> Unit)?,
    onOpenSpikeDebug: (() -> Unit)?,
    onNavigate: (String) -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    val isPrivate = activeTab?.isPrivate == true
    val activeAccent = if (isPrivate) AccentPurple else AccentCyan

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top Row: Origin Security Status + Private Badge + Shields + Tab Switcher Badge + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Origin & Security Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isPrivate) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentPurple.copy(alpha = 0.2f))
                                .border(1.dp, AccentPurple, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .semantics {
                                    contentDescription = "Private browsing mode active"
                                }
                        ) {
                            Text(
                                text = "🕶 Private",
                                color = AccentPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = if (activeTab?.isSecure == true) "🔒" else "⚠",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = when {
                            activeTab == null || activeTab.url == "about:blank" -> "musify://home"
                            else -> activeTab.origin
                        },
                        color = if (activeTab?.isSecure == true) StatusGreen else StatusOrange,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Shields Button with Blocked Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isShieldEnabled) StatusOrange.copy(alpha = 0.15f) else SurfaceLightDark)
                        .border(
                            1.dp,
                            if (!isShieldEnabled) StatusOrange else StatusGreen.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onToggleShield)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = if (isShieldEnabled) "Privacy shields active, $shieldBlockedCount blocked" else "Privacy shields disabled"
                            role = androidx.compose.ui.semantics.Role.Button
                        }
                ) {
                    Text(
                        text = if (!isShieldEnabled) "🛡 Off" else if (shieldBlockedCount > 0) "🛡 $shieldBlockedCount" else "🛡 Shields",
                        color = if (!isShieldEnabled) StatusOrange else StatusGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tab Switcher Button with Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceLightDark)
                        .border(1.dp, activeAccent, RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenTabSwitcher)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = "Tab Switcher: $tabCount open tabs"
                            role = androidx.compose.ui.semantics.Role.Button
                        }
                ) {
                    Text(
                        text = "$tabCount",
                        color = activeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Overflow Menu Button '⋮'
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SurfaceLightDark)
                        .clickable { showOverflowMenu = true }
                        .semantics {
                            contentDescription = "Browser menu options"
                            role = androidx.compose.ui.semantics.Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⋮", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("★ Bookmarks", color = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenBookmarks()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🕒 History", color = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🛡 Data & Privacy Controls", color = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenDataControl()
                            }
                        )
                        if (onOpenFilterLists != null) {
                            DropdownMenuItem(
                                text = { Text("🛡 Filter Lists & Licenses", color = TextPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenFilterLists()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("🕶 + New Private Tab", color = AccentPurple) },
                            onClick = {
                                showOverflowMenu = false
                                onNewPrivateTab()
                            }
                        )
                        if (onOpenRoadmap != null) {
                            DropdownMenuItem(
                                text = { Text("🗺 Roadmap & Architecture", color = AccentCyan) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenRoadmap()
                                }
                            )
                        }
                        if (onOpenSpikeDebug != null) {
                            DropdownMenuItem(
                                text = { Text("🧪 GeckoView Spike Console", color = TextSecondary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenSpikeDebug()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Row: Back, Forward, Address Input + Bookmark Star, Reload/Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Back Button
                NavCircleButton(
                    symbol = "◀",
                    contentDescription = "Navigate back",
                    enabled = activeTab?.canGoBack == true,
                    onClick = onGoBack
                )

                // Forward Button
                NavCircleButton(
                    symbol = "▶",
                    contentDescription = "Navigate forward",
                    enabled = activeTab?.canGoForward == true,
                    onClick = onGoForward
                )

                // Address / Search Input
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = onAddressChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { onFocusChange(it.isFocused) }
                        .semantics {
                            contentDescription = "Address and search input field"
                        },
                    placeholder = {
                        Text(
                            text = if (isPrivate) "Search privately or enter URL..." else "Search DuckDuckGo or enter URL...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    trailingIcon = {
                        // Bookmark Star Button
                        if (activeTab != null && activeTab.url.isNotBlank() && activeTab.url != "about:blank") {
                            Text(
                                text = if (isBookmarked) "★" else "☆",
                                color = if (isBookmarked) Color(0xFFFBBF24) else TextMuted,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onToggleBookmark)
                                    .padding(6.dp)
                                    .semantics {
                                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark"
                                        role = androidx.compose.ui.semantics.Role.Button
                                    }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BackgroundDark,
                        unfocusedContainerColor = BackgroundDark,
                        focusedBorderColor = activeAccent,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Reload / Stop Button
                NavCircleButton(
                    symbol = if (activeTab?.isLoading == true) "✕" else "↻",
                    contentDescription = if (activeTab?.isLoading == true) "Stop loading" else "Reload page",
                    enabled = true,
                    onClick = onReloadOrStop
                )
            }

            // Quick Nav Chips when blank or address bar focused
            AnimatedVisibility(visible = isAddressBarFocused || activeTab?.url == "about:blank") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickChip("🧪 Fixture", activeAccent) { onNavigate("file:///android_asset/test_fixture.html") }
                    QuickChip("🔒 Wikipedia", activeAccent) { onNavigate("https://en.wikipedia.org") }
                    QuickChip("🦊 Mozilla", activeAccent) { onNavigate("https://www.mozilla.org") }
                    QuickChip("🛑 BadSSL", activeAccent) { onNavigate("https://expired.badssl.com") }
                }
            }
        }
    }
}

@Composable
private fun NavCircleButton(
    symbol: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (enabled) SurfaceLightDark else BackgroundDark)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                this.role = androidx.compose.ui.semantics.Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun QuickChip(label: String, accentColor: Color = AccentCyan, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDark)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics {
                this.contentDescription = "Quick shortcut: $label"
                this.role = androidx.compose.ui.semantics.Role.Button
            }
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = accentColor)
    }
}

@Composable
private fun MusifyHomeScreen(isPrivate: Boolean = false, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = if (isPrivate) "🕶" else "🛡", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isPrivate) "Musify Private Tab" else "Musify Browser",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isPrivate)
                    "RAM-Only Ephemeral Mode • No History • Session Disposed on Close"
                else
                    "Zero Telemetry • Multi-Tab Isolation • Strict TLS",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onNavigate("https://duckduckgo.com") },
                colors = ButtonDefaults.buttonColors(containerColor = if (isPrivate) AccentPurple else AccentCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🦆 DuckDuckGo Search", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RendererCrashOverlay(onRecover: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Web Renderer Process Lost", color = StatusRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The web content process was terminated. The browser shell protected your session.",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRecover,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("↻ Recover Tab", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}
