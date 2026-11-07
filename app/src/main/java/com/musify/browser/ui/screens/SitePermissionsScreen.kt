package com.musify.browser.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.musify.browser.permissions.SitePermission
import com.musify.browser.permissions.SitePermissionDecision
import com.musify.browser.permissions.SitePermissionRepository
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariDangerRed
import com.musify.browser.ui.theme.SafariGreen
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
 * Safari Website Settings & Permissions Screen (Phase 36).
 *
 * Implements Apple Safari's Settings -> Safari -> Website Settings:
 * - Top Bar: "Done" text button, "Website Settings" title, and "Reset" action.
 * - Inset grouped iOS list of origin permissions with status badges.
 * - Clean iOS empty state ("No Website Permissions").
 */
@Composable
fun SitePermissionsScreen(
    repository: SitePermissionRepository,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val permissions by repository.permissionsFlow.collectAsState()

    BackHandler { onClose() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .semantics { contentDescription = "Safari Website Permissions" }
            .testTag("safari.permissions.root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClick = onClose
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("safari.permissions.done"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Done",
                        style = SafariTypography_Headline.copy(
                            color = SafariAccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Text(
                    text = "Website Settings",
                    style = SafariTypography_Title.copy(fontSize = 19.sp),
                    color = SafariTextPrimary
                )

                Box(
                    modifier = Modifier
                        .clickable(
                            enabled = permissions.isNotEmpty(),
                            role = Role.Button,
                            onClick = { repository.clearAll() }
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("safari.permissions.reset"),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Reset",
                        style = SafariTypography_Headline.copy(
                            color = if (permissions.isNotEmpty()) SafariDangerRed else SafariTextSecondary.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Permissions List or Empty State
            if (permissions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(SafariCardDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MusifyIcons.Lock,
                                contentDescription = null,
                                tint = SafariTextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Website Permissions",
                            style = SafariTypography_Title,
                            color = SafariTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Permissions granted to websites for camera, microphone, or location will be listed here.",
                            style = SafariTypography_Subhead,
                            color = SafariTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = "CONFIGURED WEBSITES",
                    style = SafariTypography_Footnote.copy(fontWeight = FontWeight.Medium),
                    color = SafariTextSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(permissions, key = { "${it.origin}_${it.type}" }) { perm ->
                        SafariPermissionCard(
                            permission = perm,
                            onRevoke = {
                                repository.revoke(perm.origin, perm.type)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SafariPermissionCard(
    permission: SitePermission,
    onRevoke: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafariCardDark)
            .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permission.origin,
                    style = SafariTypography_Headline.copy(fontSize = 15.sp),
                    color = SafariTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${permission.type.name.lowercase().capitalize()} Access",
                    style = SafariTypography_Footnote,
                    color = SafariTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Decision Badge
            val (badgeText, badgeColor) = when (permission.decision) {
                SitePermissionDecision.GRANTED -> "Allowed" to SafariGreen
                SitePermissionDecision.DENIED -> "Blocked" to SafariDangerRed
                else -> "Ask" to SafariTextSecondary
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.16f))
                    .border(0.5.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    style = SafariTypography_Footnote.copy(
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Delete / Revoke Action
            Icon(
                imageVector = MusifyIcons.Close,
                contentDescription = "Revoke permission",
                tint = SafariTextSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRevoke)
            )
        }
    }
}
