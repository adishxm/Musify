package com.musify.browser.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.MusifyIcon
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
import com.musify.browser.ui.theme.SafariDivider
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_Subhead

/**
 * Safari "aA" Format & Page Settings Popover Menu (iOS HIG Architecture).
 *
 * Implements Apple Safari's iconic "aA" menu:
 * - Text zoom stepper: [ A-  100%  A+ ]
 * - Show Reader Mode
 * - Privacy Report (Apple Green Shield)
 * - Request Desktop Website
 * - Website Settings
 * - Clear History & Website Data
 */
@Composable
fun SafariPageSettingsSheet(
    origin: String,
    isDesktop: Boolean,
    onToggleDesktop: () -> Unit,
    onOpenPrivacyReport: () -> Unit,
    onOpenSitePermissions: () -> Unit,
    onClearData: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textZoomPercent by remember { mutableIntStateOf(100) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(SafariCardDark)
            .border(1.dp, SafariBorderDark, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("safari.page_settings.sheet")
    ) {
        Column {
            // Sheet Handle
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(SafariTextSecondary.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // Origin Label
            Text(
                text = if (origin.isNotBlank() && origin != "about:blank") origin else "Current Page",
                style = SafariTypography_Footnote,
                color = SafariTextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(14.dp))

            // Group 1: Text Zoom Stepper [ A-  100%  A+ ]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SafariCardElevated)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Small A
                    Text(
                        text = "A",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (textZoomPercent > 50) SafariAccentBlue else SafariTextSecondary,
                        modifier = Modifier
                            .clickable(enabled = textZoomPercent > 50) {
                                textZoomPercent -= 10
                            }
                            .padding(8.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Decrease text size"
                            }
                    )

                    // Percentage
                    Text(
                        text = "$textZoomPercent%",
                        style = SafariTypography_Headline,
                        color = SafariTextPrimary
                    )

                    // Large A
                    Text(
                        text = "A",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (textZoomPercent < 200) SafariAccentBlue else SafariTextSecondary,
                        modifier = Modifier
                            .clickable(enabled = textZoomPercent < 200) {
                                textZoomPercent += 10
                            }
                            .padding(8.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Increase text size"
                            }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Group 2: Action Items Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SafariCardElevated)
            ) {
                Column {
                    // Privacy Report
                    SafariMenuRow(
                        title = "Privacy Report",
                        icon = MusifyIcons.ShieldCheck,
                        iconTint = SafariGreen,
                        onClick = {
                            onDismiss()
                            onOpenPrivacyReport()
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                    // Request Desktop Website
                    SafariMenuRow(
                        title = if (isDesktop) "Request Mobile Website" else "Request Desktop Website",
                        icon = MusifyIcons.Desktop,
                        iconTint = if (isDesktop) SafariAccentBlue else SafariTextPrimary,
                        trailingText = if (isDesktop) "On" else "Off",
                        onClick = {
                            onToggleDesktop()
                            onDismiss()
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                    // Website Settings
                    SafariMenuRow(
                        title = "Website Settings",
                        icon = MusifyIcons.Settings,
                        iconTint = SafariTextPrimary,
                        onClick = {
                            onDismiss()
                            onOpenSitePermissions()
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SafariDivider))

                    // Clear Data
                    SafariMenuRow(
                        title = "Clear History and Data",
                        icon = MusifyIcons.Trash,
                        iconTint = SafariDangerRed,
                        onClick = {
                            onDismiss()
                            onClearData()
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Done Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SafariCardElevated)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 14.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Close page settings"
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done",
                    style = SafariTypography_Headline,
                    color = SafariAccentBlue
                )
            }
        }
    }
}

@Composable
private fun SafariMenuRow(
    title: String,
    icon: ImageVector,
    iconTint: Color = SafariTextPrimary,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics {
                role = Role.Button
                contentDescription = title
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusifyIcon(
                imageVector = icon,
                size = 20.dp,
                tint = iconTint
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                style = SafariTypography_Callout,
                color = if (iconTint == SafariDangerRed) SafariDangerRed else SafariTextPrimary
            )
        }

        if (trailingText != null) {
            Text(
                text = trailingText,
                style = SafariTypography_Subhead,
                color = SafariTextSecondary
            )
        } else {
            MusifyIcon(
                imageVector = MusifyIcons.ArrowRight,
                size = 14.dp,
                tint = SafariTextSecondary
            )
        }
    }
}
