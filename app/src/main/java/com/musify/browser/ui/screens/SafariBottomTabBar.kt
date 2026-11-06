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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariGlass
import com.musify.browser.ui.theme.SafariGlassBorder
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
import com.musify.browser.ui.theme.SafariTypography_Subhead

/**
 * Safari Bottom Unified Tab Bar / Address Bar (iOS HIG Architecture).
 *
 * Implements Apple Safari's signature bottom floating capsule:
 * - Floating ergonomic thumb placement
 * - Left: "aA" Format / Page Settings button
 * - Center: HTTPS Lock + Clean Host Domain / Tap-to-edit URL bar
 * - Right: Reload/Stop icon, Tab Switcher badge [ n ], and iOS Share button
 * - Supports expansion into address search bar with Cancel button
 */
@Composable
fun SafariBottomTabBar(
    url: String,
    origin: String,
    isSecure: Boolean,
    isPrivate: Boolean,
    isLoading: Boolean,
    tabCount: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onOpenPageSettings: () -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onOpenShareSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditingAddress by remember { mutableStateOf(false) }
    var currentEditQuery by remember(url) {
        val initial = if (url == "about:blank") "" else url
        mutableStateOf(initial)
    }
    val focusRequester = remember { FocusRequester() }

    val cleanDomain = remember(url, origin) {
        when {
            url.isBlank() || url == "about:blank" -> "Search or enter website name"
            origin.isNotBlank() -> origin.removePrefix("https://").removePrefix("http://").removePrefix("www.")
            else -> url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        }
    }

    val isHome = url.isBlank() || url == "about:blank"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("safari.bottom_bar.root")
    ) {
        if (isEditingAddress) {
            // Safari Expanded Address Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(SafariGlass)
                    .border(1.dp, SafariGlassBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusifyIcon(
                    imageVector = MusifyIcons.Search,
                    size = 18.dp,
                    tint = SafariTextSecondary
                )

                Spacer(Modifier.width(10.dp))

                BasicTextField(
                    value = currentEditQuery,
                    onValueChange = { currentEditQuery = it },
                    singleLine = true,
                    textStyle = SafariTypography_Body.copy(color = SafariTextPrimary),
                    cursorBrush = SolidColor(SafariAccentBlue),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Go,
                        keyboardType = KeyboardType.Uri
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (currentEditQuery.isNotBlank()) {
                                onNavigate(currentEditQuery.trim())
                                isEditingAddress = false
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("safari.address_input")
                )

                if (currentEditQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SafariCardElevated)
                            .clickable { currentEditQuery = "" }
                            .semantics {
                                role = Role.Button
                                contentDescription = "Clear address input"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        MusifyIcon(
                            imageVector = MusifyIcons.Close,
                            size = 12.dp,
                            tint = SafariTextSecondary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                }

                // Cancel Button
                Text(
                    text = "Cancel",
                    style = SafariTypography_Callout,
                    color = SafariAccentBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { isEditingAddress = false }
                        .semantics {
                            role = Role.Button
                            contentDescription = "Cancel URL edit"
                        }
                )
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            // Safari Resting Floating Bottom Tab Bar Capsule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(SafariGlass)
                    .border(1.dp, SafariGlassBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: "aA" Format / Page Settings Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenPageSettings)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Page settings and formatting menu"
                        }
                        .testTag("safari.button.format_aa"),
                    contentAlignment = Alignment.Center
                ) {
                    MusifyIcon(
                        imageVector = MusifyIcons.FormatAA,
                        size = 20.dp,
                        tint = SafariTextPrimary
                    )
                }

                // Center: Lock / Domain capsule (Tap to edit URL)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            currentEditQuery = if (url == "about:blank") "" else url
                            isEditingAddress = true
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Address bar: $cleanDomain"
                        }
                        .testTag("safari.capsule.domain"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!isHome) {
                        MusifyIcon(
                            imageVector = if (isSecure) MusifyIcons.Lock else MusifyIcons.LockOpen,
                            size = 13.dp,
                            tint = if (isSecure) SafariGreen else SafariTextSecondary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    Text(
                        text = cleanDomain,
                        style = if (isHome) SafariTypography_Footnote else SafariTypography_Headline.copy(fontSize = 15.sp),
                        color = if (isHome) SafariTextSecondary else SafariTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Right Row: Reload/Stop + Tab Switcher + Share
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Reload / Stop button (only on web pages)
                    if (!isHome) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onReloadOrStop)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = if (isLoading) "Stop loading" else "Reload page"
                                }
                                .testTag("safari.button.reload"),
                            contentAlignment = Alignment.Center
                        ) {
                            MusifyIcon(
                                imageVector = if (isLoading) MusifyIcons.Close else MusifyIcons.Reload,
                                size = 18.dp,
                                tint = SafariTextPrimary
                            )
                        }
                    }

                    // Share button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenShareSheet)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Share or browser actions"
                        }
                        .testTag("safari.button.share"),
                        contentAlignment = Alignment.Center
                    ) {
                        MusifyIcon(
                            imageVector = MusifyIcons.ShareIOS,
                            size = 19.dp,
                            tint = SafariTextPrimary
                        )
                    }

                    // Tab Switcher button with [ N ] badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenTabSwitcher)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Tab switcher, $tabCount tabs open"
                            }
                            .testTag("safari.button.tabs"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(
                                    1.6.dp,
                                    if (isPrivate) SafariPurple else SafariTextPrimary,
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$tabCount",
                                style = SafariTypography_Caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPrivate) SafariPurple else SafariTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
