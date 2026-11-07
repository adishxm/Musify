package com.musify.browser.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.crypto.CryptographicShredder
import com.musify.browser.media.MusifyMediaPlaybackService
import com.musify.browser.storage.AppPreferencesData
import com.musify.browser.storage.AppPreferencesRepository
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
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariPurple
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_Subhead
import com.musify.browser.ui.theme.SafariTypography_Title

/**
 * UiCustomizationScreen provides comprehensive personalization of Musify Browser.
 *
 * Designed & Engineered by adishxm.
 *
 * Key Capabilities:
 * 1. Navigation Dock Customization (Bottom Floating Capsule vs Top Header).
 * 2. Auto-hide dock on scroll (Solves bottom navbar obstruction on Spotify, YouTube Music, Instagram).
 * 3. Safe area inset web clearance buffer.
 * 4. 10,000x Extreme Cryptographic Shredder & Ephemeral Zeroization with Panic Wipe.
 * 5. Background Media Streaming & System Media Notification (Spotify & YouTube Premium Parity).
 * 6. Picture-in-Picture (PiP) Video Hover on back / minimize.
 * 7. Luxury UI Theme Palette switcher.
 */
@Composable
fun UiCustomizationScreen(
    preferencesRepository: AppPreferencesRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var prefs by remember { mutableStateOf(preferencesRepository.getPreferences()) }
    var panicWipedCount by remember { mutableStateOf(0) }

    fun updatePrefs(transform: (AppPreferencesData) -> AppPreferencesData) {
        preferencesRepository.updatePreferences(transform)
        prefs = preferencesRepository.getPreferences()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .testTag("musify.customization.screen")
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Customization & UI Settings",
                    style = SafariTypography_Title.copy(fontSize = 20.sp),
                    color = SafariTextPrimary
                )
                Text(
                    text = "Designed & Engineered by adishxm",
                    style = SafariTypography_Caption,
                    color = SafariAccentBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SafariCardElevated)
                    .clickable(onClick = onClose)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Done"
                    }
                    .testTag("musify.customization.done"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done",
                    style = SafariTypography_Headline.copy(fontSize = 14.sp),
                    color = SafariAccentBlue
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Navigation Dock & Layout (Solves Spotify/Instagram Bottom Nav Collision)
            CustomizationSection(
                title = "NAVIGATION DOCK & ERGONOMICS",
                subtitle = "Configure placement and behavior to avoid web navbar collisions"
            ) {
                // Dock Placement: Bottom vs Top
                Text(
                    text = "Dock Position",
                    style = SafariTypography_Headline.copy(fontSize = 14.sp),
                    color = SafariTextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafariCardDark)
                        .border(1.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isBottom = prefs.navBarPosition != "TOP"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBottom) SafariAccentBlue else Color.Transparent)
                            .clickable {
                                updatePrefs { it.copy(navBarPosition = "BOTTOM") }
                            }
                            .padding(vertical = 10.dp)
                            .semantics {
                                role = Role.RadioButton
                                contentDescription = "Bottom Floating Capsule"
                            }
                            .testTag("musify.customization.dock_bottom"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bottom Capsule",
                            style = SafariTypography_Callout,
                            fontWeight = if (isBottom) FontWeight.Bold else FontWeight.Normal,
                            color = if (isBottom) Color.White else SafariTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBottom) SafariAccentBlue else Color.Transparent)
                            .clickable {
                                updatePrefs { it.copy(navBarPosition = "TOP") }
                            }
                            .padding(vertical = 10.dp)
                            .semantics {
                                role = Role.RadioButton
                                contentDescription = "Top Header Navigation"
                            }
                            .testTag("musify.customization.dock_top"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Top Header",
                            style = SafariTypography_Callout,
                            fontWeight = if (!isBottom) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isBottom) Color.White else SafariTextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Auto-Hide on Scroll Toggle
                CustomizationSwitchRow(
                    title = "Auto-Hide Dock on Scroll",
                    description = "Smoothly hides navigation dock when scrolling down on Spotify, YouTube, or articles, restoring on upward swipe.",
                    checked = prefs.autoHideNavBarOnScroll,
                    onCheckedChange = { checked ->
                        updatePrefs { it.copy(autoHideNavBarOnScroll = checked) }
                    },
                    testTag = "musify.customization.switch_autohide"
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                // Bottom Clearance Inset Toggle
                CustomizationSwitchRow(
                    title = "Web Safe Area Clearance Buffer",
                    description = "Adds 64dp clearance so websites with sticky bottom tabs (Spotify, Instagram, Twitter) are 100% unobstructed.",
                    checked = prefs.webBottomClearance,
                    onCheckedChange = { checked ->
                        updatePrefs { it.copy(webBottomClearance = checked) }
                    },
                    testTag = "musify.customization.switch_clearance"
                )
            }

            // Section 2: 10,000x Extreme Cryptographic Private Mode
            CustomizationSection(
                title = "10,000X EXTREME CRYPTOGRAPHIC PRIVATE ENGINE",
                subtitle = "Engineered by adishxm for zero-trace cryptographic memory destruction"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafariPurple.copy(alpha = 0.15f))
                        .border(1.dp, SafariPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    MusifyIcon(
                        imageVector = MusifyIcons.Incognito,
                        size = 20.dp,
                        tint = SafariPurple
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "10,000X CSPRNG ENTROPY MEMORY SHREDDER ACTIVE",
                        style = SafariTypography_Caption,
                        fontWeight = FontWeight.Bold,
                        color = SafariPurple
                    )
                }

                Spacer(Modifier.height(12.dp))

                CustomizationSwitchRow(
                    title = "DoD 5220.22-M Multi-Pass Shredder",
                    description = "Overwrites all RAM buffers, DOM caches, and session cookies with 3-pass entropy upon closing private tabs. Zero disk trace.",
                    checked = prefs.extremePrivateMode,
                    onCheckedChange = { checked ->
                        updatePrefs { it.copy(extremePrivateMode = checked) }
                    },
                    testTag = "musify.customization.switch_shredder"
                )

                Spacer(Modifier.height(10.dp))

                // Panic Wipe Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SafariDangerRed.copy(alpha = 0.8f), SafariPurple.copy(alpha = 0.8f))
                            )
                        )
                        .clickable {
                            CryptographicShredder.executePanicWipe(context) {
                                panicWipedCount++
                                Toast.makeText(
                                    context,
                                    "⚡ Memory Shredded & Corrupted by adishxm: Zero Traces Remain",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        .padding(vertical = 14.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Execute Cryptographic Panic Wipe"
                        }
                        .testTag("musify.customization.button_panic_wipe"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MusifyIcon(
                            imageVector = MusifyIcons.ShieldOff,
                            size = 18.dp,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "⚡ Shred & Panic Wipe RAM & Cache Now",
                            style = SafariTypography_Headline.copy(fontSize = 14.sp),
                            color = Color.White
                        )
                    }
                }
            }

            // Section 3: Background Media Playback & Picture-in-Picture
            CustomizationSection(
                title = "BACKGROUND MEDIA & SYSTEM NOTIFICATIONS",
                subtitle = "Spotify and YouTube Premium audio playback parity"
            ) {
                CustomizationSwitchRow(
                    title = "Background Audio Playback",
                    description = "Keeps audio and music playing when Musify is minimized, switched to another app, or screen is turned off.",
                    checked = prefs.backgroundPlaybackEnabled,
                    onCheckedChange = { checked ->
                        updatePrefs { it.copy(backgroundPlaybackEnabled = checked) }
                    },
                    testTag = "musify.customization.switch_bg_playback"
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                CustomizationSwitchRow(
                    title = "Picture-in-Picture (PiP) Video Hover",
                    description = "Automatically hovers video player in a floating overlay when pressing back or navigating home.",
                    checked = prefs.showPipHoverOnBack,
                    onCheckedChange = { checked ->
                        updatePrefs { it.copy(showPipHoverOnBack = checked) }
                    },
                    testTag = "musify.customization.switch_pip"
                )

                Spacer(Modifier.height(10.dp))

                // Test Notification Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafariCardElevated)
                        .border(1.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                        .clickable {
                            MusifyMediaPlaybackService.startService(
                                context = context,
                                title = "Spotify & YouTube Stream",
                                site = "Musify • Designed & Engineered by adishxm",
                                isPlaying = true
                            )
                            Toast.makeText(
                                context,
                                "Musify Notification active: check your status bar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .padding(vertical = 12.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Test Musify Media Notification"
                        }
                        .testTag("musify.customization.button_test_notification"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MusifyIcon(
                            imageVector = MusifyIcons.Music,
                            size = 18.dp,
                            tint = SafariAccentBlue
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Test Background Media Notification",
                            style = SafariTypography_Headline.copy(fontSize = 14.sp),
                            color = SafariAccentBlue
                        )
                    }
                }
            }

            // Section 4: Themes & Visual Aesthetics
            CustomizationSection(
                title = "AESTHETICS & THEME PALETTES",
                subtitle = "Choose your preferred visual aura"
            ) {
                val themes = listOf(
                    Triple("EDITORIAL_DARK", "Editorial Obsidian", "adishxm signature • Pure OLED #0A0A0C"),
                    Triple("SAFARI_GLASS", "Safari Frosted Glass", "Apple HIG translucent glassmorphism"),
                    Triple("CYBERPUNK_EMERALD", "Cyberpunk Emerald", "High-contrast neon emerald & stealth gray"),
                    Triple("MIDNIGHT_AMETHYST", "Midnight Amethyst", "Deep royal purple & nebula obsidian")
                )

                themes.forEach { (id, name, desc) ->
                    val isSelected = prefs.uiThemeStyle == id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SafariCardElevated else SafariCardDark)
                            .border(
                                1.2.dp,
                                if (isSelected) SafariAccentBlue else SafariBorderDark,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                updatePrefs { it.copy(uiThemeStyle = id) }
                            }
                            .padding(14.dp)
                            .semantics {
                                role = Role.RadioButton
                                contentDescription = "$name theme"
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = SafariTypography_Headline.copy(fontSize = 15.sp),
                                color = if (isSelected) SafariAccentBlue else SafariTextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = desc,
                                style = SafariTypography_Footnote,
                                color = SafariTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SafariAccentBlue else Color.Transparent)
                                .border(2.dp, if (isSelected) SafariAccentBlue else SafariTextSecondary, CircleShape)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Section 5: Author Signature Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SafariCardDark)
                    .border(1.dp, SafariBorderDark, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Musify Browser",
                        style = SafariTypography_Title.copy(fontSize = 18.sp),
                        color = SafariTextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Designed & Engineered by adishxm",
                        style = SafariTypography_Headline.copy(fontSize = 14.sp),
                        color = SafariAccentBlue
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Brave-grade AdBlock Engine • 10,000x Cryptographic Zeroization • Hardware-Accelerated Web Compositing • Background Media Parity",
                        style = SafariTypography_Caption,
                        color = SafariTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CustomizationSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = SafariTypography_Subhead.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            color = SafariTextSecondary,
            letterSpacing = 1.sp
        )
        Text(
            text = subtitle,
            style = SafariTypography_Footnote,
            color = SafariTextSecondary.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SafariCardDark)
                .border(1.dp, SafariBorderDark, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun CustomizationSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = SafariTypography_Headline.copy(fontSize = 14.sp),
                color = SafariTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = SafariTypography_Footnote,
                color = SafariTextSecondary,
                lineHeight = 16.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SafariGreen,
                uncheckedThumbColor = SafariTextSecondary,
                uncheckedTrackColor = SafariCardElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

