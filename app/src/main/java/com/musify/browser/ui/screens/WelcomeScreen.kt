package com.musify.browser.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.musify.browser.ui.theme.SafariGreen
import com.musify.browser.ui.theme.SafariPurple
import com.musify.browser.ui.theme.SafariTextPrimary
import com.musify.browser.ui.theme.SafariTextSecondary
import com.musify.browser.ui.theme.SafariTypography_Body
import com.musify.browser.ui.theme.SafariTypography_Caption
import com.musify.browser.ui.theme.SafariTypography_Footnote
import com.musify.browser.ui.theme.SafariTypography_Headline
import com.musify.browser.ui.theme.SafariTypography_LargeTitle
import com.musify.browser.ui.theme.SafariTypography_Subhead
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.WelcomeSurface

/**
 * Safari Welcome & First-Run Screen (Phase 36).
 *
 * Implements Apple Safari's clean first-launch experience:
 * - Safari Compass emblem with specular glow.
 * - Cupertino typography: "Welcome to Safari", "Fast. Private. Beautiful."
 * - Inset squircle feature rows:
 *   1. Lightning-Fast Browsing & Media (Safari Blue)
 *   2. Intelligent Tracking Prevention (Safari Green)
 *   3. Floating Bottom Tab Bar (Safari Purple)
 * - Full-width vibrant Safari Blue "Continue" action button.
 */
@Composable
fun WelcomeScreen(
    onEnter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafariBackgroundDark)
            .semantics { contentDescription = "Welcome to Safari" }
            .testTag("safari.welcome.root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // 1. Safari Compass Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0x1F007AFF))
                    .border(1.5.dp, SafariAccentBlue.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MusifyIcons.Compass,
                    contentDescription = "Safari Compass",
                    tint = SafariAccentBlue,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Titles
            Text(
                text = "Welcome to Safari",
                style = SafariTypography_LargeTitle,
                color = SafariTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Fast. Private. Beautiful.",
                style = SafariTypography_Headline,
                color = SafariTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 3. Feature Highlights (Inset Grouped Rows)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                SafariFeatureRow(
                    icon = MusifyIcons.Compass,
                    iconBg = SafariAccentBlue,
                    title = "Lightning-Fast Browsing",
                    subtitle = "Stream music, videos, and browse the web with industry-leading speed and battery efficiency."
                )

                SafariFeatureRow(
                    icon = MusifyIcons.Shield,
                    iconBg = SafariGreen,
                    title = "Intelligent Tracking Prevention",
                    subtitle = "Safari uses machine learning to stop cross-site tracking, audio commercials, and profiling."
                )

                SafariFeatureRow(
                    icon = MusifyIcons.TabsGrid,
                    iconBg = SafariPurple,
                    title = "Floating Bottom Tab Bar",
                    subtitle = "Designed for effortless one-handed reachability. Easily swipe and search with your thumb."
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // 4. Primary "Continue" Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SafariAccentBlue)
                    .clickable(
                        role = Role.Button,
                        onClick = onEnter
                    )
                    .semantics {
                        contentDescription = "Continue to Safari"
                        role = Role.Button
                    }
                    .testTag("safari.welcome.continue"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue",
                    style = SafariTypography_Headline.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Customize your favorites and search engine anytime in Settings.",
                style = SafariTypography_Caption,
                color = SafariTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SafariFeatureRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg.copy(alpha = 0.18f))
                .border(1.dp, iconBg.copy(alpha = 0.35f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconBg,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SafariTypography_Headline.copy(fontSize = 16.sp),
                color = SafariTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = SafariTypography_Subhead,
                color = SafariTextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}
