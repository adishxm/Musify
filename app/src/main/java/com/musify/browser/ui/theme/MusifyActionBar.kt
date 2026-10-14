package com.musify.browser.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * MusifyActionBar — a floating pill of quick actions with a single circular
 * control that expands into a labelled action grid.
 *
 * The pattern: rather than hiding a dozen destinations in a scrolling dropdown,
 * the collapsed bar keeps the four most-used actions one tap away, and the
 * expand control reveals everything else as a grid of large, labelled,
 * thumb-sized targets.
 *
 * Everything is monochrome. Emphasis comes from *inversion* — the expand
 * control is the one white object on the screen, so it reads as the obvious
 * thing to press without needing a brand colour.
 */

/** One destination in the action bar or grid. */
data class MusifyAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    /** Tint for the glyph. Defaults to primary text. */
    val tint: Color = TextPrimary,
    /** Optional weaker tint for the tile behind the glyph. */
    val tileTint: Color? = null,
    val enabled: Boolean = true,
    /** Draws a persistent surface behind the collapsed pill item. */
    val highlighted: Boolean = false
)

// ---------------------------------------------------------------------------
// Collapsed bar
// ---------------------------------------------------------------------------

/**
 * The floating bar itself. Sits above content, inset from the screen edges.
 *
 * @param quickActions 2–4 actions kept permanently visible.
 * @param isExpanded drives the expand control's icon and rotation.
 */
@Composable
fun MusifyActionBar(
    quickActions: List<MusifyAction>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onAction: (MusifyAction) -> Unit,
    modifier: Modifier = Modifier,
    expandIcon: ImageVector = MusifyIcons.Plus,
    expandContentDescription: String = if (isExpanded) "Close quick actions" else "Show all actions"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ---- the pill ----
        Row(
            modifier = Modifier
                .clip(MusifyShapes.pill)
                .background(Surface.copy(alpha = 0.94f), MusifyShapes.pill)
                .border(1.dp, Hairline, MusifyShapes.pill)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickActions.forEach { action ->
                PillActionButton(action = action, onClick = { onAction(action) })
            }
        }

        // ---- the expand control ----
        // The single white object on screen: unmistakably the primary affordance.
        val rotation by animateFloatAsState(
            targetValue = if (isExpanded) 45f else 0f,
            animationSpec = tween(durationMillis = 220, easing = MusifyMotion.Standard),
            label = "actionBarExpandRotation"
        )

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Accent, CircleShape)
                .clickable(onClick = onExpandToggle)
                .semantics {
                    contentDescription = expandContentDescription
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            // The ring is the product's signature: one iridescent light
            // effect, on the control that is always on screen.
            MusifyGlowChip(
                icon = expandIcon,
                variant = MusifyButtonVariant.Hero,
                enabled = true,
                animated = true,
                size = 38.dp,
                iconRotation = rotation
            )
        }
    }
}

@Composable
private fun PillActionButton(action: MusifyAction, onClick: () -> Unit) {
    val bg = when {
        action.highlighted -> ElevatedHigh
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .then(
                if (action.enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription = action.label
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        MusifyIcon(
            imageVector = action.icon,
            size = 19.dp,
            tint = if (action.enabled) action.tint else TextMuted.copy(alpha = 0.45f)
        )
    }
}

// ---------------------------------------------------------------------------
// Expanded grid
// ---------------------------------------------------------------------------

/**
 * Full-screen scrim plus the floating grid panel.
 *
 * Render this *above* the action bar in the composition tree so the scrim
 * covers the bar as it opens — the bar visually folds into the panel.
 */
@Composable
fun MusifyActionGrid(
    actions: List<MusifyAction>,
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onAction: (MusifyAction) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    columns: Int = 4
) {
    // Scrim
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(tween(160, easing = MusifyMotion.Standard)),
        exit = fadeOut(tween(140, easing = MusifyMotion.Exit)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    // Panel
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(tween(140, easing = MusifyMotion.Standard)) +
            slideInVertically(tween(260, easing = MusifyMotion.Standard)) { it / 3 } +
            scaleIn(tween(260, easing = MusifyMotion.Standard), initialScale = 0.94f),
        exit = fadeOut(tween(120, easing = MusifyMotion.Exit)) +
            slideOutVertically(tween(160, easing = MusifyMotion.Exit)) { it / 4 } +
            scaleOut(tween(160, easing = MusifyMotion.Exit), targetScale = 0.96f),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space.lg, vertical = Space.xl)
                    .clip(MusifyShapes.lg)
                    .background(Surface.copy(alpha = 0.98f), MusifyShapes.lg)
                    .border(1.dp, Hairline, MusifyShapes.lg)
                    .padding(horizontal = Space.md, vertical = Space.lg)
            ) {
                if (title != null) {
                    Overline(
                        text = title,
                        modifier = Modifier.padding(
                            start = Space.sm,
                            bottom = Space.md
                        )
                    )
                }

                // Chunked rows rather than a lazy grid: the action count is small
                // and fixed, and this avoids fighting a nested scroll container.
                actions.chunked(columns).forEach { rowActions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        rowActions.forEach { action ->
                            ActionTile(
                                action = action,
                                onClick = { onAction(action) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // keep the last row aligned to the grid
                        repeat(columns - rowActions.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(Space.xs))
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    action: MusifyAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MusifyShapes.md)
            .clickable(enabled = action.enabled, onClick = onClick)
            .padding(vertical = Space.sm)
            .semantics {
                contentDescription = action.label
                role = Role.Button
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(action.tileTint ?: Elevated),
            contentAlignment = Alignment.Center
        ) {
            MusifyIcon(
                imageVector = action.icon,
                size = 21.dp,
                tint = if (action.enabled) action.tint else TextMuted.copy(alpha = 0.45f)
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = action.label,
            color = if (action.enabled) TextSecondary else TextMuted.copy(alpha = 0.6f),
            fontSize = 10.sp,
            lineHeight = 13.sp,
            letterSpacing = 0.1.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
