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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.musify.browser.browser.UrlParser
import com.musify.browser.engine.EngineView
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.AccentCyan
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
 * active engine renderer, and tab switcher integration.
 */
@Composable
fun BrowserShellScreen(
    viewModel: TabsViewModel,
    modifier: Modifier = Modifier,
    onOpenSpikeDebug: (() -> Unit)? = null,
    onOpenRoadmap: (() -> Unit)? = null
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val activeTab = tabsState.selectedTab
    val activeAdapter = viewModel.getActiveEngineAdapter()

    var showTabSwitcher by remember { mutableStateOf(false) }
    var addressInput by remember { mutableStateOf("") }
    var isAddressBarFocused by remember { mutableStateOf(false) }
    var showShieldDetails by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Synchronize address bar with active tab url when not focused
    if (!isAddressBarFocused && activeTab != null) {
        val displayUrl = if (activeTab.url == "about:blank") "" else activeTab.url
        if (addressInput != displayUrl && !displayUrl.isEmpty()) {
            addressInput = displayUrl
        }
    }

    // Intercept hardware back button: back in web page, or close switcher
    BackHandler(enabled = showTabSwitcher || (activeTab?.canGoBack == true)) {
        if (showTabSwitcher) {
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
                onAddressChange = { addressInput = it },
                onFocusChange = { isAddressBarFocused = it },
                onSubmit = {
                    if (addressInput.isNotBlank()) {
                        viewModel.navigateActiveTab(addressInput)
                        focusManager.clearFocus()
                    }
                },
                onOpenTabSwitcher = { showTabSwitcher = true },
                onNewTab = {
                    val created = viewModel.createNewTab(url = "about:blank", selectImmediately = true)
                    if (created) addressInput = ""
                },
                onGoBack = { viewModel.goBack() },
                onGoForward = { viewModel.goForward() },
                onReloadOrStop = {
                    if (activeTab?.isLoading == true) viewModel.stop() else viewModel.reload()
                },
                onToggleShield = { showShieldDetails = !showShieldDetails },
                onOpenRoadmap = onOpenRoadmap,
                onOpenSpikeDebug = onOpenSpikeDebug,
                onNavigate = { target ->
                    viewModel.navigateActiveTab(target)
                    focusManager.clearFocus()
                }
            )

            // Progress Bar
            if (activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    progress = { (activeTab.progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = AccentCyan,
                    trackColor = BackgroundDark
                )
            }

            // Shield Details Sheet (Collapsible)
            AnimatedVisibility(visible = showShieldDetails) {
                ShieldDetailsDropdown(onClose = { showShieldDetails = false })
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
}

@Composable
private fun SurfaceDarkHeader(
    activeTab: com.musify.browser.tabs.TabSession?,
    tabCount: Int,
    maxTabs: Int,
    addressInput: String,
    isAddressBarFocused: Boolean,
    onAddressChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onNewTab: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onToggleShield: () -> Unit,
    onOpenRoadmap: (() -> Unit)?,
    onOpenSpikeDebug: (() -> Unit)?,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top Row: Origin Security Status + Shields + Tab Switcher Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Verified Origin Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
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

                // Shields Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceLightDark)
                        .clickable(onClick = onToggleShield)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = "Shield protections active"
                            role = androidx.compose.ui.semantics.Role.Button
                        }
                ) {
                    Text(
                        text = "🛡 Shields",
                        color = StatusGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tab Switcher Button with Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceLightDark)
                        .border(1.dp, AccentCyan, RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenTabSwitcher)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = "Tab Switcher: $tabCount open tabs"
                            role = androidx.compose.ui.semantics.Role.Button
                        }
                ) {
                    Text(
                        text = "$tabCount",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // New Tab Quick '+' Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (tabCount < maxTabs) AccentCyan else SurfaceLightDark)
                        .clickable(enabled = tabCount < maxTabs, onClick = onNewTab)
                        .semantics {
                            contentDescription = "Open new tab"
                            role = androidx.compose.ui.semantics.Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = if (tabCount < maxTabs) BackgroundDark else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Row: Back, Forward, Address Input, Reload/Stop
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
                        Text("Search DuckDuckGo or enter URL...", color = TextMuted, fontSize = 13.sp)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BackgroundDark,
                        unfocusedContainerColor = BackgroundDark,
                        focusedBorderColor = AccentCyan,
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
                    QuickChip("🧪 Fixture") { onNavigate("file:///android_asset/test_fixture.html") }
                    QuickChip("🔒 Wikipedia") { onNavigate("https://en.wikipedia.org") }
                    QuickChip("🦊 Mozilla") { onNavigate("https://www.mozilla.org") }
                    QuickChip("🛑 BadSSL") { onNavigate("https://expired.badssl.com") }
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
private fun QuickChip(label: String, onClick: () -> Unit) {
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
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = AccentCyan)
    }
}

@Composable
private fun ShieldDetailsDropdown(onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛡 Privacy Shields Active", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("✕", color = TextSecondary, modifier = Modifier.clickable(onClick = onClose))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Cross-site tracking cookies blocked\nCryptominers and fingerprinters blocked\nStrict TLS verification enforced",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MusifyHomeScreen(onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🛡", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Musify Browser", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Zero Telemetry • Multi-Tab Isolation • Strict TLS",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onNavigate("https://duckduckgo.com") },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🦆 DuckDuckGo Search", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
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
