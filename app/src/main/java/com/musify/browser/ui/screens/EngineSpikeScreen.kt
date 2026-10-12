package com.musify.browser.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.EngineType
import com.musify.browser.engine.EngineView
import com.musify.browser.engine.GeckoEngineAdapter
import com.musify.browser.engine.WebViewAdapter
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
fun EngineSpikeScreen(
    modifier: Modifier = Modifier
) {
    val isGecko = remember { com.musify.browser.tabs.TabEnginePool.isGeckoAvailable() }
    var selectedEngineType by remember { mutableStateOf(if (isGecko) EngineType.GECKO_VIEW else EngineType.WEB_VIEW) }

    val geckoAdapter = remember { if (isGecko) GeckoEngineAdapter() else null }
    val webViewAdapter = remember { WebViewAdapter() }

    val currentAdapter: EngineAdapter = if (selectedEngineType == EngineType.GECKO_VIEW && geckoAdapter != null) {
        geckoAdapter
    } else {
        webViewAdapter
    }

    DisposableEffect(Unit) {
        onDispose {
            geckoAdapter?.destroy()
            webViewAdapter.destroy()
        }
    }

    val navState by currentAdapter.navigationState.collectAsState()
    val secState by currentAdapter.securityState.collectAsState()
    val shieldState by currentAdapter.shieldState.collectAsState()
    val procState by currentAdapter.processState.collectAsState()

    var inputUrl by remember { mutableStateOf("") }
    var showDiagnostics by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Engine Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (selectedEngineType == EngineType.GECKO_VIEW) AccentCyan else AccentPurple)
                        )
                        Text(
                            text = if (selectedEngineType == EngineType.GECKO_VIEW) "GeckoView Stable (Candidate)" else "WebView (Fallback Spike)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (selectedEngineType == EngineType.GECKO_VIEW) "Gecko" else "Web",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Switch(
                            checked = selectedEngineType == EngineType.GECKO_VIEW,
                            onCheckedChange = { isGecko ->
                                selectedEngineType = if (isGecko) EngineType.GECKO_VIEW else EngineType.WEB_VIEW
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BackgroundDark,
                                checkedTrackColor = AccentCyan,
                                uncheckedThumbColor = TextPrimary,
                                uncheckedTrackColor = SurfaceLightDark
                            )
                        )
                    }
                }

                // Address & Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back
                    NavIconButton(
                        text = "◀",
                        enabled = navState.canGoBack,
                        onClick = { currentAdapter.goBack() }
                    )

                    // Forward
                    NavIconButton(
                        text = "▶",
                        enabled = navState.canGoForward,
                        onClick = { currentAdapter.goForward() }
                    )

                    // URL input field
                    TextField(
                        value = if (inputUrl.isEmpty() && navState.url != "about:blank") navState.url else inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        placeholder = { Text("Search or type HTTPS address...", color = TextMuted, fontSize = 13.sp) },
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
                                if (inputUrl.isNotBlank()) {
                                    currentAdapter.loadUrl(inputUrl)
                                    inputUrl = ""
                                }
                            }
                        )
                    )

                    // Reload
                    NavIconButton(
                        text = if (navState.isLoading) "✕" else "↻",
                        enabled = true,
                        onClick = {
                            if (navState.isLoading) currentAdapter.stop() else currentAdapter.reload()
                        }
                    )
                }

                // Quick Target Pills (Corpus)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickPill("Local Test Fixture") {
                        currentAdapter.loadUrl("file:///android_asset/test_fixture.html")
                    }
                    QuickPill("Wikipedia (General)") {
                        currentAdapter.loadUrl("https://en.wikipedia.org")
                    }
                    QuickPill("Mozilla (Standards)") {
                        currentAdapter.loadUrl("https://www.mozilla.org")
                    }
                    QuickPill("BadSSL (Invalid TLS)") {
                        currentAdapter.loadUrl("https://self-signed.badssl.com")
                    }
                }
            }
        }

        // Loading Progress Bar
        if (navState.isLoading) {
            LinearProgressIndicator(
                progress = { navState.progress / 100f },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = AccentCyan,
                trackColor = SurfaceDark
            )
        }

        // Live Engine Surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            EngineView(
                adapter = currentAdapter,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Diagnostics & Shield HUD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Shield Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (shieldState.isProtectionEnabled) StatusGreen.copy(alpha = 0.2f) else SurfaceLightDark)
                                .border(1.dp, if (shieldState.isProtectionEnabled) StatusGreen else CardBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🛡 ${shieldState.blockedCount} requests blocked by this app",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (shieldState.isProtectionEnabled) StatusGreen else TextMuted
                            )
                        }

                        // TLS Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (secState.isSecure) AccentCyan.copy(alpha = 0.15f) else Color(0x33EF4444))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (secState.isSecure) "🔒 ${secState.protocol}" else "⚠ ${secState.tlsError ?: "Insecure"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (secState.isSecure) AccentCyan else Color(0xFFEF4444)
                            )
                        }
                    }

                    Text(
                        text = if (showDiagnostics) "Hide HUD" else "Show HUD",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan,
                        modifier = Modifier.clickable { showDiagnostics = !showDiagnostics }
                    )
                }

                AnimatedVisibility(visible = showDiagnostics) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "URL: ${navState.url}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Renderer State: ${if (procState.isHealthy) "Healthy" else "Crashed"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (procState.isHealthy) StatusGreen else Color(0xFFEF4444)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceLightDark)
                                    .clickable { currentAdapter.simulateRendererLoss() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Simulate Crash",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (enabled) SurfaceLightDark else BackgroundDark)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun QuickPill(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundDark)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
