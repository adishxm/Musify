package com.musify.browser.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.browser.BrowserErrorState
import com.musify.browser.browser.SingleTabViewModel
import com.musify.browser.engine.EngineView
import com.musify.browser.ui.theme.AccentCyan
import com.musify.browser.ui.theme.AccentPurple
import com.musify.browser.ui.theme.BackgroundDark
import com.musify.browser.ui.theme.CardBorder
import com.musify.browser.ui.theme.StatusGreen
import com.musify.browser.ui.theme.SurfaceDark
import com.musify.browser.ui.theme.SurfaceLightDark
import com.musify.browser.ui.theme.TextMuted
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary

@Composable
fun SingleTabBrowserScreen(
    viewModel: SingleTabViewModel,
    modifier: Modifier = Modifier,
    onOpenSpikeDebug: (() -> Unit)? = null,
    onOpenRoadmap: (() -> Unit)? = null
) {
    val tabState by viewModel.tabState.collectAsState()
    val focusManager = LocalFocusManager.current

    var isAddressBarFocused by remember { mutableStateOf(false) }
    var addressInput by remember { mutableStateOf("") }
    var showShieldSheet by remember { mutableStateOf(false) }

    // Intercept hardware / gesture back navigation if page has history
    androidx.activity.compose.BackHandler(enabled = tabState.canGoBack) {
        viewModel.goBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Browser Chrome Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Top Row: Origin & Security Display + Shield Badge + Debug/Roadmap Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isHttps = tabState.isHttps
                    val hasTlsError = tabState.securityState.tlsError != null
                    val originDesc = if (hasTlsError) {
                        "Security Alert: Untrusted certificate on ${tabState.host}"
                    } else if (isHttps) {
                        "Secure HTTPS connection to ${tabState.host}"
                    } else {
                        "Insecure connection to ${tabState.host}"
                    }

                    // Verified Origin Pill (Anti-Spoofing display)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .androidx.compose.ui.semantics.semantics {
                                androidx.compose.ui.semantics.contentDescription = originDesc
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val lockColor = when {
                            hasTlsError -> Color(0xFFEF4444)
                            isHttps -> StatusGreen
                            else -> Color(0xFFF59E0B)
                        }

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(lockColor)
                        )

                        Text(
                            text = when {
                                tabState.host.isNotBlank() -> {
                                    val prefix = if (isHttps) "https://" else "http://"
                                    prefix + tabState.host
                                }
                                tabState.currentUrl == "about:blank" -> "New Tab"
                                else -> tabState.currentUrl
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = if (hasTlsError) Color(0xFFEF4444) else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (tabState.isPunycodeAlert) {
                            Text(
                                text = "⚠ IDN",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Shields Button, Spike & Roadmap Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Privacy Shields Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (tabState.shieldState.isProtectionEnabled) StatusGreen.copy(alpha = 0.15f) else SurfaceLightDark)
                                .border(1.dp, if (tabState.shieldState.isProtectionEnabled) StatusGreen else CardBorder, RoundedCornerShape(12.dp))
                                .clickable { showShieldSheet = !showShieldSheet }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .androidx.compose.ui.semantics.semantics {
                                    androidx.compose.ui.semantics.contentDescription = "Privacy Shield: ${tabState.shieldState.blockedCount} requests blocked"
                                    androidx.compose.ui.semantics.role = androidx.compose.ui.semantics.Role.Button
                                }
                        ) {
                            Text(
                                text = "🛡 ${tabState.shieldState.blockedCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tabState.shieldState.isProtectionEnabled) StatusGreen else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (onOpenRoadmap != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceLightDark)
                                    .clickable(onClick = onOpenRoadmap)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .androidx.compose.ui.semantics.semantics {
                                        androidx.compose.ui.semantics.contentDescription = "View Project Roadmap"
                                        androidx.compose.ui.semantics.role = androidx.compose.ui.semantics.Role.Button
                                    }
                            ) {
                                Text(
                                    text = "Plan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (onOpenSpikeDebug != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceLightDark)
                                    .clickable(onClick = onOpenSpikeDebug)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .androidx.compose.ui.semantics.semantics {
                                        androidx.compose.ui.semantics.contentDescription = "Launch Engine Feasibility Spike"
                                        androidx.compose.ui.semantics.role = androidx.compose.ui.semantics.Role.Button
                                    }
                            ) {
                                Text(
                                    text = "Spike",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Controls & Omnibar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back
                    BrowserButton(
                        text = "◀",
                        contentDescription = "Navigate back",
                        enabled = tabState.canGoBack,
                        onClick = { viewModel.goBack() }
                    )

                    // Forward
                    BrowserButton(
                        text = "▶",
                        contentDescription = "Navigate forward",
                        enabled = tabState.canGoForward,
                        onClick = { viewModel.goForward() }
                    )

                    // Omnibar (Address / Search field)
                    TextField(
                        value = if (isAddressBarFocused) addressInput else (tabState.host.takeIf { it.isNotBlank() } ?: tabState.currentUrl),
                        onValueChange = { addressInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .onFocusChanged { focusState ->
                                isAddressBarFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    addressInput = if (tabState.currentUrl == "about:blank") "" else tabState.currentUrl
                                }
                            }
                            .androidx.compose.ui.semantics.semantics {
                                androidx.compose.ui.semantics.contentDescription = "Address and search bar"
                            },
                        placeholder = {
                            Text("Search DuckDuckGo or type URL...", color = TextMuted, fontSize = 13.sp)
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = AccentCyan,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                if (addressInput.isNotBlank()) {
                                    viewModel.navigate(addressInput)
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    )

                    // Reload / Stop
                    BrowserButton(
                        text = if (tabState.isLoading) "✕" else "↻",
                        contentDescription = if (tabState.isLoading) "Stop loading page" else "Reload page",
                        enabled = true,
                        onClick = {
                            if (tabState.isLoading) viewModel.stop() else viewModel.reload()
                        }
                    )
                }

                // Quick Navigation Test Chips (Shown when blank tab or address bar focused)
                AnimatedVisibility(visible = isAddressBarFocused || tabState.currentUrl == "about:blank") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickChip(label = "🧪 Fixture") {
                            viewModel.loadTestFixture()
                            focusManager.clearFocus()
                        }
                        QuickChip(label = "🔒 Wikipedia") {
                            viewModel.navigate("https://en.wikipedia.org")
                            focusManager.clearFocus()
                        }
                        QuickChip(label = "🦊 Mozilla") {
                            viewModel.navigate("https://www.mozilla.org")
                            focusManager.clearFocus()
                        }
                        QuickChip(label = "🛑 BadSSL Expired") {
                            viewModel.navigate("https://expired.badssl.com")
                            focusManager.clearFocus()
                        }
                    }
                }

                // Shield Details Popdown
                AnimatedVisibility(visible = showShieldSheet) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Privacy Shield Status",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Switch(
                                    checked = tabState.shieldState.isProtectionEnabled,
                                    onCheckedChange = { viewModel.toggleShields(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = BackgroundDark,
                                        checkedTrackColor = StatusGreen
                                    )
                                )
                            }
                            Text(
                                text = "${tabState.shieldState.blockedCount} requests blocked by this app",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StatusGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            tabState.shieldState.lastBlockedUrl?.let { url ->
                                Text(
                                    text = "Latest blocked: $url",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Animated Loading Progress Bar
        if (tabState.isLoading) {
            LinearProgressIndicator(
                progress = { tabState.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = AccentCyan,
                trackColor = SurfaceDark
            )
        }

        // Web Rendering Viewport & Error State Overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Live Engine View
            EngineView(
                adapter = viewModel.adapter,
                modifier = Modifier.fillMaxSize()
            )

            // Comprehensive Error / Warning Screen
            tabState.errorState?.let { error ->
                BrowserErrorOverlay(
                    error = error,
                    onRetry = { viewModel.reload() },
                    onRecover = { viewModel.recoverSession() },
                    onDismiss = { viewModel.dismissError() }
                )
            }
        }
    }
}

@Composable
private fun BrowserErrorOverlay(
    error: BrowserErrorState,
    onRetry: () -> Unit,
    onRecover: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark.copy(alpha = 0.95f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        if (error.isTlsError) Color(0xFFEF4444) else CardBorder,
                        SurfaceLightDark
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (error.isTlsError) Color(0x33EF4444) else Color(0x33F59E0B)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (error.isTlsError) "🔒" else "⚠",
                        fontSize = 22.sp
                    )
                }

                Text(
                    text = error.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                if (error.failingUrl.isNotBlank()) {
                    Text(
                        text = error.failingUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                if (error.isTlsError) {
                    Text(
                        text = "Musify strictly enforces TLS security to protect your privacy. This connection has been terminated to safeguard against interception. No bypass is permitted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (error.canRetry) {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                        ) {
                            Text(text = "Retry", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (error.title == "Render Process Lost") {
                        Button(
                            onClick = onRecover,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                        ) {
                            Text(text = "Recover Tab", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(onClick = onDismiss) {
                        Text(text = "Dismiss", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowserButton(
    text: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (enabled) SurfaceLightDark else BackgroundDark)
            .clickable(enabled = enabled, onClick = onClick)
            .androidx.compose.ui.semantics.semantics {
                this.contentDescription = contentDescription
                this.role = androidx.compose.ui.semantics.Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDark)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .androidx.compose.ui.semantics.semantics {
                this.contentDescription = "Quick target: $label"
                this.role = androidx.compose.ui.semantics.Role.Button
            }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AccentCyan
        )
    }
}
