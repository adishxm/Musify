package com.musify.browser.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musify.browser.R
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.WelcomeSurface

/**
 * The launch screen — the first frame of the app, and the last screen that was
 * still carrying the old branding.
 *
 * Deliberately bare. A flat field, two lines of type sitting at 30% of the
 * height, and the credit line at 85%. That is the whole composition; the
 * positions are held as fractions of the screen so it lands the same way on a
 * short phone as on a tall one.
 *
 * There is no button. The entire surface is the tap target — see [onEnter].
 *
 * Typography comes from the theme (Inter, see Type.kt); the explicit sizes here
 * are the reference measurements, not new values.
 */
@Composable
fun WelcomeScreen(
    onEnter: () -> Unit,
    modifier: Modifier = Modifier
) {
    // One fade. No scale, no slide — the screen is already quiet, and movement
    // would be the loudest thing on it.
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(WelcomeSurface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onEnter
            )
            .semantics {
                role = Role.Button
                contentDescription = "Enter Musify"
            }
    ) {
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 34.dp)
                .offset(y = screenHeight * 0.30f)
                .alpha(contentAlpha.value)
        ) {
            Text(
                text = stringResource(R.string.welcome_hello),
                color = TextPrimary,
                fontSize = 24.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = stringResource(R.string.welcome_musify_title),
                color = TextPrimary,
                fontSize = 24.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp
            )
        }

        Text(
            text = stringResource(R.string.welcome_crafted_by),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = screenHeight * 0.135f)
                .alpha(contentAlpha.value)
        )
    }
}
