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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.ui.theme.MusifyIcons
import com.musify.browser.ui.theme.SafariAccentBlue
import com.musify.browser.ui.theme.SafariBackgroundDark
import com.musify.browser.ui.theme.SafariBorderDark
import com.musify.browser.ui.theme.SafariCardDark
import com.musify.browser.ui.theme.SafariCardElevated
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariPurple
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Callout
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_LargeTitle
import com.musify.browser.ui.theme.SafariTypography_Subhead
import com.musify.browser.ui.theme.SafariTypography_Title

/**
 * About Safari / Musify Screen (Phase 36).
 *
 * Implements Apple's macOS / iOS About Safari dialog:
 * - Compass emblem with specular glow.
 * - Cupertino typography and version metadata.
 * - Inset grouped system architecture disclosures (ITP, Media DRM, Privacy).
 * - Legal & open-source acknowledgments.
 */
@Composable
fun ReleaseInfoScreen(
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    onClose: () -> Unit = {}
) {
    BackHandler { onClose() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .semantics { contentDescription = "About Safari" }
            .testTag("safari.about.root")
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
                        .testTag("safari.about.done"),
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
                    text = "About Safari",
                    style = SafariTypography_Title.copy(fontSize = 19.sp),
                    color = SafariTextPrimary
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Scrollable Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Safari Compass Emblem
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0x1F007AFF))
                        .border(1.5.dp, SafariAccentBlue.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MusifyIcons.Compass,
                        contentDescription = "Safari Compass",
                        tint = SafariAccentBlue,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "Safari",
                    style = SafariTypography_LargeTitle,
                    color = SafariTextPrimary
                )

                Text(
                    text = "Version 1.0 (Build 36.0) • Android Universal",
                    style = SafariTypography_Footnote,
                    color = SafariTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Inset Group 1: Core Technologies
                Text(
                    text = "ENGINE & CAPABILITIES",
                    style = SafariTypography_Footnote.copy(fontWeight = FontWeight.Medium),
                    color = SafariTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafariCardDark)
                        .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        SafariAboutRow(
                            label = "Design Inspiration",
                            value = "Apple Safari (iOS / macOS HIG)"
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                        SafariAboutRow(
                            label = "Privacy Protection",
                            value = "Intelligent Tracking Prevention (ITP)"
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                        SafariAboutRow(
                            label = "Protected Media",
                            value = "Widevine CDM / EME Playback"
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SafariBorderDark))
                        SafariAboutRow(
                            label = "Rendering Pipeline",
                            value = "GeckoView & System WebView Dual Engine"
                        )
                    }
                }

                // Inset Group 2: Privacy Commitments
                Text(
                    text = "PRIVACY & SECURITY",
                    style = SafariTypography_Footnote.copy(fontWeight = FontWeight.Medium),
                    color = SafariTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafariCardDark)
                        .border(0.5.dp, SafariBorderDark, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Musify is designed from the ground up to protect your privacy. Safari's Intelligent Tracking Prevention uses machine learning to identify and block data profiling trackers. Browsing sessions, cookies, and local data stay completely on your device.",
                        style = SafariTypography_Subhead,
                        color = SafariTextSecondary,
                        lineHeight = 20.sp
                    )
                }

                // Legal Notice
                Text(
                    text = "Inspired by Apple Safari. Built for seamless web audio streaming and private browsing on Android.\nPeter Lowe's List • Brave Software Filter Lists • SIL Open Font License",
                    style = SafariTypography_Caption,
                    color = SafariTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SafariAboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = SafariTypography_Callout,
            color = SafariTextPrimary
        )
        Text(
            text = value,
            style = SafariTypography_Footnote,
            color = SafariTextSecondary
        )
    }
}
