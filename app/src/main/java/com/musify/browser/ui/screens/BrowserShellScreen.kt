package com.musify.browser.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.musify.browser.downloads.DownloadPromptRequest
import com.musify.browser.engine.EngineView
import com.musify.browser.permissions.SitePermissionPromptRequest
import com.musify.browser.safety.FindInPageState
import com.musify.browser.safety.SchemeDecision
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.Danger
import com.musify.browser.ui.theme.Elevated
import com.musify.browser.ui.theme.Hairline
import com.musify.browser.ui.theme.IconLabel
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.Private
import com.musify.browser.ui.theme.Success
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary
import com.musify.browser.ui.theme.Warning

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
    onOpenDownloads: (() -> Unit)? = null,
    onOpenSitePermissions: (() -> Unit)? = null,
    onOpenSpikeDebug: (() -> Unit)? = null,
    onOpenRoadmap: (() -> Unit)? = null,
    onOpenReleaseInfo: (() -> Unit)? = null,
    onOpenWelcome: (() -> Unit)? = null
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val activeTab = tabsState.selectedTab
    val activeAdapter = viewModel.getActiveEngineAdapter()
    val activeShieldState by viewModel.activeShieldState.collectAsState()
    val activeContentMode by viewModel.activeContentMode.collectAsState()
    val isDesktop = activeContentMode == com.musify.browser.engine.ContentMode.DESKTOP
    val httpFallbackPrompt by viewModel.httpFallbackPrompt.collectAsState()
    val downloadPrompt by viewModel.pendingDownloadPrompt.collectAsState()
    val sitePermissionPrompt by viewModel.pendingSitePermissionPrompt.collectAsState()
    val externalSchemePrompt by viewModel.pendingExternalSchemePrompt.collectAsState()
    val findState by viewModel.findInPageManager.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showTabSwitcher by remember { mutableStateOf(false) }
    var addressInput by remember { mutableStateOf("") }
    var isAddressBarFocused by remember { mutableStateOf(false) }
    var showShieldSheet by remember { mutableStateOf(false) }
    var showPlaybackDialog by remember { mutableStateOf(false) }
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

    // Intercept hardware back button: close dialogs, shield sheet, back in web page, or close switcher
    BackHandler(enabled = showPlaybackDialog || showShieldSheet || showTabSwitcher || findState.isActive || (activeTab?.canGoBack == true)) {
        if (showPlaybackDialog) {
            showPlaybackDialog = false
        } else if (showShieldSheet) {
            showShieldSheet = false
        } else if (findState.isActive) {
            viewModel.findInPageManager.close()
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
                .background(Ink)
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
                isDesktop = isDesktop,
                onToggleDesktopMode = { viewModel.toggleContentModeCurrentTab() },
                onOpenPlaybackCompatibility = { showPlaybackDialog = true },
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
                onOpenDownloads = onOpenDownloads,
                onOpenSitePermissions = onOpenSitePermissions,
                onFindInPage = { viewModel.findInPageManager.startFind() },
                onSharePage = {
                    val urlToShare = activeTab?.url
                    if (!urlToShare.isNullOrBlank() && urlToShare != "about:blank") {
                        try {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, urlToShare)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Page"))
                        } catch (_: Exception) {
                        }
                    }
                },
                onNewPrivateTab = {
                    viewModel.createNewTab(url = "about:blank", isPrivate = true, selectImmediately = true)
                    addressInput = ""
                },
                onOpenRoadmap = onOpenRoadmap,
                onOpenSpikeDebug = onOpenSpikeDebug,
                onOpenReleaseInfo = onOpenReleaseInfo,
                onOpenWelcome = onOpenWelcome,
                onNavigate = { target ->
                    viewModel.navigateActiveTab(target)
                    focusManager.clearFocus()
                }
            )

            // Find In Page Active Search Bar
            AnimatedVisibility(visible = findState.isActive) {
                FindInPageBar(
                    state = findState,
                    onQueryChange = { viewModel.findInPageManager.updateQuery(it) },
                    onNext = { viewModel.findInPageManager.nextMatch() },
                    onPrevious = { viewModel.findInPageManager.previousMatch() },
                    onClose = { viewModel.findInPageManager.close() }
                )
            }

            // Loading Progress Bar
            if (activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    progress = { (activeTab.progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (activeTab.isPrivate) Private else Accent,
                    trackColor = Ink
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
                    MusifyIcon(
                    MusifyIcons.Warning,
                    size = 20.dp,
                    tint = Warning,
                    modifier = Modifier.padding(end = 8.dp)
                )
                    Text(
                        text = "Insecure Connection Warning",
                        color = Warning,
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
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Proceed Insecurely", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.dismissHttpFallback() },
                    colors = ButtonDefaults.buttonColors(containerColor = Elevated),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = Surface
        )
    }

    // User-Confirmed Sandboxed Download Dialog
    if (downloadPrompt != null) {
        val req = downloadPrompt!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissDownloadPrompt() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MusifyIcon(
                    MusifyIcons.Download,
                    size = 20.dp,
                    tint = Accent,
                    modifier = Modifier.padding(end = 8.dp)
                )
                    Text(
                        text = "Download File",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = req.suggestedFileName,
                        color = Accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Origin: ${req.origin}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Estimated Size: ${if (req.contentLength > 0) "${req.contentLength / 1024} KB" else "Unknown"}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (req.isExecutableWarning) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Danger.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Danger),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Executable File Warning",
                                    color = Danger,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.3.sp
                                )
                                Text(
                                    text = "This file type can execute code on your device. Musify will not automatically run or install this file.",
                                    color = TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = "Download will be stored safely in Musify's private sandboxed downloads directory.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDownload(req) },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Download", color = Ink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.dismissDownloadPrompt() },
                    colors = ButtonDefaults.buttonColors(containerColor = Elevated),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = Surface
        )
    }

    // Deny-by-Default Web Origin Site Permission Dialog
    if (sitePermissionPrompt != null) {
        val perm = sitePermissionPrompt!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissSitePermission() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MusifyIcon(
                    MusifyIcons.Location,
                    size = 20.dp,
                    tint = Accent,
                    modifier = Modifier.padding(end = 8.dp)
                )
                    Text(
                        text = "Site Permission Prompt",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = perm.origin,
                        color = Accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "This website is requesting permission to access your ${perm.type.label}.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Musify enforces deny-by-default for all origins. You can review or revoke this permission at any time.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.grantSitePermission(perm) },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Allow", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.denySitePermission(perm) },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Deny", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Surface
        )
    }

    // Fail-Closed External Scheme Confirmation Dialog
    if (externalSchemePrompt != null) {
        val scheme = externalSchemePrompt!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissExternalScheme() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MusifyIcon(
                    MusifyIcons.ExternalLink,
                    size = 20.dp,
                    tint = Accent,
                    modifier = Modifier.padding(end = 8.dp)
                )
                    Text(
                        text = "Open External App?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "External Scheme: ${scheme.scheme}",
                        color = Accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scheme.uri,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Musify protects your privacy by preventing websites from launching apps on your device without your explicit consent.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissExternalScheme()
                        try {
                            val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme.uri)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(launchIntent)
                        } catch (_: Exception) {
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Open App", color = Ink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.dismissExternalScheme() },
                    colors = ButtonDefaults.buttonColors(containerColor = Elevated),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = Surface
        )
    }

    // Playback Compatibility & Web Media Diagnostics Dialog
    if (showPlaybackDialog) {
        PlaybackCompatibilityDialog(
            viewModel = viewModel,
            onDismiss = { showPlaybackDialog = false }
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
    isDesktop: Boolean,
    onToggleDesktopMode: () -> Unit,
    onOpenPlaybackCompatibility: () -> Unit,
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
    onOpenDownloads: (() -> Unit)? = null,
    onOpenSitePermissions: (() -> Unit)? = null,
    onFindInPage: () -> Unit,
    onSharePage: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onOpenRoadmap: (() -> Unit)?,
    onOpenSpikeDebug: (() -> Unit)?,
    onOpenReleaseInfo: (() -> Unit)? = null,
    onOpenWelcome: (() -> Unit)? = null,
    onNavigate: (String) -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    val isPrivate = activeTab?.isPrivate == true
    val activeAccent = if (isPrivate) Private else Accent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
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
                                .background(Private.copy(alpha = 0.2f))
                                .border(1.dp, Private, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .semantics {
                                    contentDescription = "Private browsing mode active"
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MusifyIcon(
                                    MusifyIcons.Incognito,
                                    size = 11.dp,
                                    tint = Private
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "Private",
                                    color = Private,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    MusifyIcon(
                        imageVector = if (activeTab?.isSecure == true) MusifyIcons.Lock else MusifyIcons.LockOpen,
                        size = 12.dp,
                        tint = if (activeTab?.isSecure == true) Success else Warning,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(
                        text = when {
                            activeTab == null || activeTab.url == "about:blank" -> "musify://home"
                            else -> activeTab.origin
                        },
                        color = if (activeTab?.isSecure == true) Success else Warning,
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
                        .background(if (!isShieldEnabled) Warning.copy(alpha = 0.15f) else Elevated)
                        .border(
                            1.dp,
                            if (!isShieldEnabled) Warning else Success.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onToggleShield)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = if (isShieldEnabled) "Privacy shields active, $shieldBlockedCount blocked" else "Privacy shields disabled"
                            role = androidx.compose.ui.semantics.Role.Button
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MusifyIcon(
                            imageVector = if (!isShieldEnabled) MusifyIcons.ShieldOff else MusifyIcons.ShieldCheck,
                            size = 12.dp,
                            tint = if (!isShieldEnabled) Warning else Success
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (!isShieldEnabled) "Off"
                            else if (shieldBlockedCount > 0) "$shieldBlockedCount"
                            else "Shields",
                            color = if (!isShieldEnabled) Warning else Success,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Desktop / Mobile Mode Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDesktop) Accent.copy(alpha = 0.2f) else Elevated)
                        .border(
                            1.dp,
                            if (isDesktop) Accent else Hairline,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onToggleDesktopMode)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = if (isDesktop) "Desktop mode active" else "Mobile mode active"
                            role = androidx.compose.ui.semantics.Role.Button
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MusifyIcon(
                            imageVector = if (isDesktop) MusifyIcons.Desktop else MusifyIcons.Mobile,
                            size = 12.dp,
                            tint = if (isDesktop) Accent else TextSecondary
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isDesktop) "Desktop" else "Mobile",
                            color = if (isDesktop) Accent else TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tab Switcher Button with Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Elevated)
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
                        .background(Elevated)
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
                        modifier = Modifier.background(Surface)
                    ) {
                        DropdownMenuItem(
                            text = { IconLabel(MusifyIcons.StarFilled, "Bookmarks", tint = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenBookmarks()
                            }
                        )
                        DropdownMenuItem(
                            text = { IconLabel(MusifyIcons.Clock, "History", tint = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconLabel(
                                        MusifyIcons.Desktop,
                                        if (isDesktop) "Desktop Site (Active)" else "Request Desktop Site",
                                        tint = TextPrimary
                                    )
                                    if (isDesktop) {
                                        MusifyIcon(MusifyIcons.Check, size = 16.dp, tint = Accent)
                                    }
                                }
                            },
                            onClick = {
                                showOverflowMenu = false
                                onToggleDesktopMode()
                            }
                        )
                        DropdownMenuItem(
                            text = { IconLabel(MusifyIcons.Music, "Media & Playback Recovery", tint = Accent) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenPlaybackCompatibility()
                            }
                        )
                        if (onOpenDownloads != null) {
                            DropdownMenuItem(
                                text = { IconLabel(MusifyIcons.Download, "Downloads", tint = TextPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenDownloads()
                                }
                            )
                        }
                        if (onOpenSitePermissions != null) {
                            DropdownMenuItem(
                                text = { IconLabel(MusifyIcons.Location, "Site Permissions", tint = TextPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenSitePermissions()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { IconLabel(MusifyIcons.Search, "Find in Page", tint = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onFindInPage()
                            }
                        )
                        DropdownMenuItem(
                            text = { IconLabel(MusifyIcons.Link, "Share Page", tint = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onSharePage()
                            }
                        )
                        DropdownMenuItem(
                            text = { IconLabel(MusifyIcons.Shield, "Data & Privacy Controls", tint = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                onOpenDataControl()
                            }
                        )
                        if (onOpenFilterLists != null) {
                            DropdownMenuItem(
                                text = { IconLabel(MusifyIcons.Shield, "Filter Lists & Licenses", tint = TextPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenFilterLists()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { IconLabel(MusifyIcons.Incognito, "+ New Private Tab", tint = Private) },
                            onClick = {
                                showOverflowMenu = false
                                onNewPrivateTab()
                            }
                        )
                        if (onOpenRoadmap != null) {
                            DropdownMenuItem(
                                text = { IconLabel(MusifyIcons.Map, "Roadmap & Architecture", tint = Accent) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenRoadmap()
                                }
                            )
                        }
                        if (onOpenReleaseInfo != null) {
                            DropdownMenuItem(
                                text = { IconLabel(MusifyIcons.Info, "About & Privacy Policy", tint = Accent) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenReleaseInfo()
                                }
                            )
                        }
                        if (onOpenWelcome != null) {
                            DropdownMenuItem(
                                text = { IconLabel(MusifyIcons.Bolt, "Welcome Matrix", tint = Warning) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenWelcome()
                                }
                            )
                        }
                        if (onOpenSpikeDebug != null) {
                            DropdownMenuItem(
                                text = { IconLabel(MusifyIcons.Flask, "GeckoView Spike Console", tint = TextSecondary) },
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
                    icon = MusifyIcons.ArrowLeft,
                    contentDescription = "Navigate back",
                    enabled = activeTab?.canGoBack == true,
                    onClick = onGoBack
                )

                // Forward Button
                NavCircleButton(
                    icon = MusifyIcons.ArrowRight,
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
                            MusifyIcon(
                                imageVector = if (isBookmarked) MusifyIcons.StarFilled else MusifyIcons.StarOutline,
                                size = 16.dp,
                                tint = if (isBookmarked) Accent else TextMuted,
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
                        focusedContainerColor = Ink,
                        unfocusedContainerColor = Ink,
                        focusedBorderColor = activeAccent,
                        unfocusedBorderColor = Hairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Reload / Stop Button
                NavCircleButton(
                    icon = if (activeTab?.isLoading == true) MusifyIcons.Close else MusifyIcons.Reload,
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
                    QuickChip("Fixture", activeAccent, MusifyIcons.Flask) { onNavigate("file:///android_asset/test_fixture.html") }
                    QuickChip("Wikipedia", activeAccent, MusifyIcons.Lock) { onNavigate("https://en.wikipedia.org") }
                    QuickChip("Mozilla", activeAccent, MusifyIcons.Globe) { onNavigate("https://www.mozilla.org") }
                    QuickChip("BadSSL", activeAccent, MusifyIcons.Blocked) { onNavigate("https://expired.badssl.com") }
                }
            }
        }
    }
}

@Composable
private fun NavCircleButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) Elevated else Ink)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                this.role = androidx.compose.ui.semantics.Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        MusifyIcon(
            imageVector = icon,
            size = 17.dp,
            tint = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun QuickChip(
    label: String,
    accentColor: Color = Accent,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Ink)
            .border(1.dp, Hairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics {
                this.contentDescription = "Quick shortcut: $label"
                this.role = androidx.compose.ui.semantics.Role.Button
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                MusifyIcon(icon, size = 11.dp, tint = accentColor)
                Spacer(Modifier.width(4.dp))
            }
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = accentColor)
        }
    }
}

@Composable
private fun MusifyHomeScreen(isPrivate: Boolean = false, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MusifyIcon(
                imageVector = if (isPrivate) MusifyIcons.Incognito else MusifyIcons.Emblem,
                size = 64.dp,
                tint = if (isPrivate) Private else TextPrimary
            )
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
                colors = ButtonDefaults.buttonColors(containerColor = if (isPrivate) Private else Accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                IconLabel(
                    MusifyIcons.Search,
                    "DuckDuckGo Search",
                    tint = Ink,
                    iconSize = 16.dp,
                    spacing = 8.dp,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun RendererCrashOverlay(onRecover: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MusifyIcon(MusifyIcons.Warning, size = 48.dp, tint = Warning)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Web Renderer Process Lost", color = Danger, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The web content process was terminated. The browser shell protected your session.",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRecover,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                IconLabel(
                    MusifyIcons.Reload,
                    "Recover Tab",
                    tint = Ink,
                    iconSize = 16.dp,
                    spacing = 8.dp,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun FindInPageBar(
    state: FindInPageState,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MusifyIcon(MusifyIcons.Search, size = 15.dp, tint = TextMuted)
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Find in page...", color = TextMuted, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Ink,
                    unfocusedContainerColor = Ink,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Hairline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            if (state.displayMatchText.isNotBlank()) {
                Text(
                    text = state.displayMatchText,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            NavCircleButton(
                icon = MusifyIcons.ChevronUp,
                contentDescription = "Previous match",
                enabled = state.totalMatches > 0,
                onClick = onPrevious
            )

            NavCircleButton(
                icon = MusifyIcons.ChevronDown,
                contentDescription = "Next match",
                enabled = state.totalMatches > 0,
                onClick = onNext
            )

            NavCircleButton(
                icon = MusifyIcons.Close,
                contentDescription = "Close find in page",
                enabled = true,
                onClick = onClose
            )
        }
    }
}

