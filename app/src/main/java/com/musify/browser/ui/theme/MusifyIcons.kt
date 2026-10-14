package com.musify.browser.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Musify's icon system.
 *
 * Replaces the emoji glyphs the app previously used. Emoji render as full-colour
 * cartoon art that differs per device and vendor, which fights a monochrome
 * identity. These icons are single-weight, optically consistent, and recolour
 * with `tint`, so an icon is simply another shade of the surface it sits on.
 *
 * Design rules:
 *   - 24x24 viewport, artwork kept inside a 2dp safe area.
 *   - One stroke weight (1.7f) and round caps/joins everywhere.
 *   - Outlined, never filled — except stars, bullets and the brand emblem,
 *     where solid mass is the whole point.
 *   - Drawn on a 4dp-ish grid so icons align to each other at a glance.
 */

private const val VIEWPORT = 24f
private const val DEFAULT_STROKE = 1.7f

// ---------------------------------------------------------------------------
// Construction helpers
// ---------------------------------------------------------------------------

private fun icon(
    name: String,
    block: ImageVector.Builder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VIEWPORT,
    viewportHeight = VIEWPORT
).apply(block).build()

/** A stroked path — the default for Musify icons. */
private fun ImageVector.Builder.line(
    width: Float = DEFAULT_STROKE,
    cap: StrokeCap = StrokeCap.Round,
    join: StrokeJoin = StrokeJoin.Round,
    block: PathBuilder.() -> Unit
) = path(
    fill = null,
    stroke = SolidColor(Color.White),
    strokeLineWidth = width,
    strokeLineCap = cap,
    strokeLineJoin = join,
    pathBuilder = block
)

/** A filled path. */
private fun ImageVector.Builder.fill(block: PathBuilder.() -> Unit) = path(
    fill = SolidColor(Color.White),
    stroke = null,
    pathBuilder = block
)

/** Circle drawn as two half-arcs. */
private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcTo(r, r, 0f, true, true, cx + r, cy)
    arcTo(r, r, 0f, true, true, cx - r, cy)
    close()
}

// ---------------------------------------------------------------------------
// The set
// ---------------------------------------------------------------------------

object MusifyIcons {

    // -- Security & identity ------------------------------------------------

    /** The product mark: an outlined shield. */
    val Shield: ImageVector by lazy {
        icon("Musify.Shield") {
            line {
                moveTo(12f, 2.6f)
                lineTo(20f, 5.9f)
                lineTo(20f, 11.8f)
                curveTo(20f, 16.7f, 16.6f, 20.3f, 12f, 21.6f)
                curveTo(7.4f, 20.3f, 4f, 16.7f, 4f, 11.8f)
                lineTo(4f, 5.9f)
                close()
            }
        }
    }

    /** Shield with a confirmed check — protections verified active. */
    val ShieldCheck: ImageVector by lazy {
        icon("Musify.ShieldCheck") {
            line {
                moveTo(12f, 2.6f)
                lineTo(20f, 5.9f)
                lineTo(20f, 11.8f)
                curveTo(20f, 16.7f, 16.6f, 20.3f, 12f, 21.6f)
                curveTo(7.4f, 20.3f, 4f, 16.7f, 4f, 11.8f)
                lineTo(4f, 5.9f)
                close()
            }
            line {
                moveTo(8.9f, 11.9f)
                lineTo(11.2f, 14.3f)
                lineTo(15.5f, 9.3f)
            }
        }
    }

    /** Shield struck through — protections disabled for this site. */
    val ShieldOff: ImageVector by lazy {
        icon("Musify.ShieldOff") {
            line {
                moveTo(12f, 2.6f)
                lineTo(20f, 5.9f)
                lineTo(20f, 11.8f)
                curveTo(20f, 16.7f, 16.6f, 20.3f, 12f, 21.6f)
                curveTo(7.4f, 20.3f, 4f, 16.7f, 4f, 11.8f)
                lineTo(4f, 5.9f)
                close()
            }
            line {
                moveTo(4.4f, 4.4f)
                lineTo(19.6f, 19.6f)
            }
        }
    }

    /** Closed padlock — origin is HTTPS-secured. */
    val Lock: ImageVector by lazy {
        icon("Musify.Lock") {
            line {
                moveTo(8f, 10.4f)
                lineTo(8f, 7.6f)
                curveTo(8f, 5.2f, 9.7f, 3.6f, 12f, 3.6f)
                curveTo(14.3f, 3.6f, 16f, 5.2f, 16f, 7.6f)
                lineTo(16f, 10.4f)
            }
            line {
                moveTo(5.6f, 10.4f)
                lineTo(18.4f, 10.4f)
                lineTo(18.4f, 20f)
                lineTo(5.6f, 20f)
                close()
            }
        }
    }

    /** Open padlock — plaintext HTTP, no transport security. */
    val LockOpen: ImageVector by lazy {
        icon("Musify.LockOpen") {
            line {
                moveTo(8f, 10.4f)
                lineTo(8f, 7.6f)
                curveTo(8f, 5.2f, 9.7f, 3.6f, 12f, 3.6f)
                curveTo(14.3f, 3.6f, 16f, 5.2f, 16f, 7.8f)
            }
            line {
                moveTo(5.6f, 10.4f)
                lineTo(18.4f, 10.4f)
                lineTo(18.4f, 20f)
                lineTo(5.6f, 20f)
                close()
            }
        }
    }

    /** Triangle warning — degraded or unsafe state. */
    val Warning: ImageVector by lazy {
        icon("Musify.Warning") {
            line {
                moveTo(12f, 3.4f)
                lineTo(21.2f, 19.6f)
                lineTo(2.8f, 19.6f)
                close()
            }
            line {
                moveTo(12f, 9.4f)
                lineTo(12f, 14f)
            }
            line(width = 2.1f, cap = StrokeCap.Round) {
                moveTo(12f, 16.7f)
                lineTo(12f, 16.75f)
            }
        }
    }

    /** Eye struck through — private / incognito browsing. */
    val Incognito: ImageVector by lazy {
        icon("Musify.Incognito") {
            line {
                moveTo(2.6f, 12f)
                curveTo(4.9f, 8f, 8.2f, 6.1f, 12f, 6.1f)
                curveTo(15.8f, 6.1f, 19.1f, 8f, 21.4f, 12f)
                curveTo(19.1f, 16f, 15.8f, 17.9f, 12f, 17.9f)
                curveTo(8.2f, 17.9f, 4.9f, 16f, 2.6f, 12f)
                close()
            }
            line {
                circle(12f, 12f, 2.5f)
            }
            line {
                moveTo(4.4f, 4.4f)
                lineTo(19.6f, 19.6f)
            }
        }
    }

    /** Universal "no" — blocked request, denied permission. */
    val Blocked: ImageVector by lazy {
        icon("Musify.Blocked") {
            line { circle(12f, 12f, 9f) }
            line {
                moveTo(5.6f, 5.6f)
                lineTo(18.4f, 18.4f)
            }
        }
    }

    // -- Navigation ---------------------------------------------------------

    val ArrowLeft: ImageVector by lazy {
        icon("Musify.ArrowLeft") {
            line {
                moveTo(19.4f, 12f)
                lineTo(4.8f, 12f)
            }
            line {
                moveTo(11f, 5.6f)
                lineTo(4.8f, 12f)
                lineTo(11f, 18.4f)
            }
        }
    }

    val ArrowRight: ImageVector by lazy {
        icon("Musify.ArrowRight") {
            line {
                moveTo(4.6f, 12f)
                lineTo(19.2f, 12f)
            }
            line {
                moveTo(13f, 5.6f)
                lineTo(19.2f, 12f)
                lineTo(13f, 18.4f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        icon("Musify.ChevronRight") {
            line {
                moveTo(9.4f, 5.4f)
                lineTo(16f, 12f)
                lineTo(9.4f, 18.6f)
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        icon("Musify.ChevronDown") {
            line {
                moveTo(5.4f, 9.4f)
                lineTo(12f, 16f)
                lineTo(18.6f, 9.4f)
            }
        }
    }

    val ChevronUp: ImageVector by lazy {
        icon("Musify.ChevronUp") {
            line {
                moveTo(5.4f, 14.6f)
                lineTo(12f, 8f)
                lineTo(18.6f, 14.6f)
            }
        }
    }

    val Close: ImageVector by lazy {
        icon("Musify.Close") {
            line {
                moveTo(6.4f, 6.4f)
                lineTo(17.6f, 17.6f)
            }
            line {
                moveTo(17.6f, 6.4f)
                lineTo(6.4f, 17.6f)
            }
        }
    }

    val Plus: ImageVector by lazy {
        icon("Musify.Plus") {
            line {
                moveTo(12f, 5.2f)
                lineTo(12f, 18.8f)
            }
            line {
                moveTo(5.2f, 12f)
                lineTo(18.8f, 12f)
            }
        }
    }

    val MoreVertical: ImageVector by lazy {
        icon("Musify.MoreVertical") {
            line(width = 2.2f) { moveTo(12f, 5.4f); lineTo(12f, 5.45f) }
            line(width = 2.2f) { moveTo(12f, 12f); lineTo(12f, 12.05f) }
            line(width = 2.2f) { moveTo(12f, 18.6f); lineTo(12f, 18.65f) }
        }
    }

    /** Two overlapping panels — tab switcher, tab count. */
    val Tabs: ImageVector by lazy {
        icon("Musify.Tabs") {
            line {
                moveTo(8.4f, 8.4f)
                lineTo(8.4f, 3.4f)
                lineTo(20.6f, 3.4f)
                lineTo(20.6f, 15.6f)
                lineTo(15.6f, 15.6f)
            }
            line {
                moveTo(3.4f, 8.4f)
                lineTo(15.6f, 8.4f)
                lineTo(15.6f, 20.6f)
                lineTo(3.4f, 20.6f)
                close()
            }
        }
    }

    // -- Browser chrome -----------------------------------------------------

    val Reload: ImageVector by lazy {
        icon("Musify.Reload") {
            fill {
                moveTo(17.65f, 6.35f)
                curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
                curveTo(7.58f, 4f, 4.01f, 7.58f, 4f, 12f)
                curveTo(3.99f, 16.42f, 7.58f, 20f, 12f, 20f)
                curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f)
                lineTo(17.65f, 14f)
                curveTo(16.83f, 16.33f, 14.61f, 18f, 12f, 18f)
                curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
                curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
                curveTo(13.66f, 6f, 15.14f, 6.69f, 16.22f, 7.78f)
                lineTo(13f, 11f)
                lineTo(20f, 11f)
                lineTo(20f, 4f)
                close()
            }
        }
    }

    /** Rounded stop square — cancel an in-flight page load. */
    val Stop: ImageVector by lazy {
        icon("Musify.Stop") {
            line {
                moveTo(7.4f, 7.4f)
                lineTo(16.6f, 7.4f)
                lineTo(16.6f, 16.6f)
                lineTo(7.4f, 16.6f)
                close()
            }
        }
    }

    val Desktop: ImageVector by lazy {
        icon("Musify.Desktop") {
            line {
                moveTo(2.8f, 4.4f)
                lineTo(21.2f, 4.4f)
                lineTo(21.2f, 16.2f)
                lineTo(2.8f, 16.2f)
                close()
            }
            line {
                moveTo(12f, 16.2f)
                lineTo(12f, 19.6f)
            }
            line {
                moveTo(9f, 19.6f)
                lineTo(15f, 19.6f)
            }
        }
    }

    val Mobile: ImageVector by lazy {
        icon("Musify.Mobile") {
            line {
                moveTo(7f, 2.8f)
                lineTo(17f, 2.8f)
                lineTo(17f, 21.2f)
                lineTo(7f, 21.2f)
                close()
            }
            line {
                moveTo(10.6f, 18.4f)
                lineTo(13.4f, 18.4f)
            }
        }
    }

    val Globe: ImageVector by lazy {
        icon("Musify.Globe") {
            line { circle(12f, 12f, 9f) }
            line {
                moveTo(3f, 12f)
                lineTo(21f, 12f)
            }
            line {
                moveTo(12f, 3f)
                curveTo(8.7f, 3f, 8.7f, 21f, 12f, 21f)
            }
            line {
                moveTo(12f, 3f)
                curveTo(15.3f, 3f, 15.3f, 21f, 12f, 21f)
            }
        }
    }

    val Search: ImageVector by lazy {
        icon("Musify.Search") {
            line { circle(10.8f, 10.8f, 7.2f) }
            line {
                moveTo(16.2f, 16.2f)
                lineTo(21f, 21f)
            }
        }
    }

    // -- Content & lists ----------------------------------------------------

    val BookmarkOutline: ImageVector by lazy {
        icon("Musify.BookmarkOutline") {
            line {
                moveTo(6.4f, 3f)
                lineTo(17.6f, 3f)
                lineTo(17.6f, 21f)
                lineTo(12f, 16.6f)
                lineTo(6.4f, 21f)
                close()
            }
        }
    }

    /** Solid five-point star. */
    val StarFilled: ImageVector by lazy {
        icon("Musify.StarFilled") {
            fill {
                moveTo(12f, 3.5f)
                lineTo(14.12f, 9.59f)
                lineTo(20.56f, 9.72f)
                lineTo(15.42f, 13.61f)
                lineTo(17.29f, 19.78f)
                lineTo(12f, 16.1f)
                lineTo(6.71f, 19.78f)
                lineTo(8.58f, 13.61f)
                lineTo(3.44f, 9.72f)
                lineTo(9.88f, 9.59f)
                close()
            }
        }
    }

    val StarOutline: ImageVector by lazy {
        icon("Musify.StarOutline") {
            line(width = 1.6f) {
                moveTo(12f, 3.5f)
                lineTo(14.12f, 9.59f)
                lineTo(20.56f, 9.72f)
                lineTo(15.42f, 13.61f)
                lineTo(17.29f, 19.78f)
                lineTo(12f, 16.1f)
                lineTo(6.71f, 19.78f)
                lineTo(8.58f, 13.61f)
                lineTo(3.44f, 9.72f)
                lineTo(9.88f, 9.59f)
                close()
            }
        }
    }

    val Clock: ImageVector by lazy {
        icon("Musify.Clock") {
            line { circle(12f, 12f, 9f) }
            line {
                moveTo(12f, 6.8f)
                lineTo(12f, 12.2f)
                lineTo(15.9f, 14.5f)
            }
        }
    }

    val Download: ImageVector by lazy {
        icon("Musify.Download") {
            line {
                moveTo(12f, 3.2f)
                lineTo(12f, 15.2f)
            }
            line {
                moveTo(7.4f, 10.6f)
                lineTo(12f, 15.2f)
                lineTo(16.6f, 10.6f)
            }
            line {
                moveTo(4f, 17.2f)
                lineTo(4f, 20.4f)
                lineTo(20f, 20.4f)
                lineTo(20f, 17.2f)
            }
        }
    }

    val Trash: ImageVector by lazy {
        icon("Musify.Trash") {
            line {
                moveTo(4.4f, 6.6f)
                lineTo(19.6f, 6.6f)
            }
            line {
                moveTo(9.4f, 6.6f)
                lineTo(9.4f, 4.2f)
                lineTo(14.6f, 4.2f)
                lineTo(14.6f, 6.6f)
            }
            line {
                moveTo(6.6f, 6.6f)
                lineTo(7.8f, 20.4f)
                lineTo(16.2f, 20.4f)
                lineTo(17.4f, 6.6f)
            }
        }
    }

    val Document: ImageVector by lazy {
        icon("Musify.Document") {
            line {
                moveTo(6.2f, 2.8f)
                lineTo(14.4f, 2.8f)
                lineTo(18.6f, 7f)
                lineTo(18.6f, 21.2f)
                lineTo(6.2f, 21.2f)
                close()
            }
            line {
                moveTo(14.4f, 2.8f)
                lineTo(14.4f, 7f)
                lineTo(18.6f, 7f)
            }
            line {
                moveTo(9.4f, 11.6f)
                lineTo(15.4f, 11.6f)
            }
            line {
                moveTo(9.4f, 15.4f)
                lineTo(15.4f, 15.4f)
            }
        }
    }

    val Link: ImageVector by lazy {
        icon("Musify.Link") {
            fill {
                moveTo(3.9f, 12f)
                curveTo(3.9f, 10.29f, 5.29f, 8.9f, 7f, 8.9f)
                lineTo(11f, 8.9f)
                lineTo(11f, 7f)
                lineTo(7f, 7f)
                curveTo(4.24f, 7f, 2f, 9.24f, 2f, 12f)
                curveTo(2f, 14.76f, 4.24f, 17f, 7f, 17f)
                lineTo(11f, 17f)
                lineTo(11f, 15.1f)
                lineTo(7f, 15.1f)
                curveTo(5.29f, 15.1f, 3.9f, 13.71f, 3.9f, 12f)
                close()
                moveTo(8f, 13f)
                lineTo(16f, 13f)
                lineTo(16f, 11f)
                lineTo(8f, 11f)
                close()
                moveTo(17f, 7f)
                lineTo(13f, 7f)
                lineTo(13f, 8.9f)
                lineTo(17f, 8.9f)
                curveTo(18.71f, 8.9f, 20.1f, 10.29f, 20.1f, 12f)
                curveTo(20.1f, 13.71f, 18.71f, 15.1f, 17f, 15.1f)
                lineTo(13f, 15.1f)
                lineTo(13f, 17f)
                lineTo(17f, 17f)
                curveTo(19.76f, 17f, 22f, 14.76f, 22f, 12f)
                curveTo(22f, 9.24f, 19.76f, 7f, 17f, 7f)
                close()
            }
        }
    }

    val ExternalLink: ImageVector by lazy {
        icon("Musify.ExternalLink") {
            line {
                moveTo(13.4f, 3.6f)
                lineTo(20.4f, 3.6f)
                lineTo(20.4f, 10.6f)
            }
            line {
                moveTo(20.4f, 3.6f)
                lineTo(11.2f, 12.8f)
            }
            line {
                moveTo(17.2f, 14.2f)
                lineTo(17.2f, 19.8f)
                lineTo(4.4f, 19.8f)
                lineTo(4.4f, 7f)
                lineTo(10f, 7f)
            }
        }
    }

    val Check: ImageVector by lazy {
        icon("Musify.Check") {
            line {
                moveTo(4.8f, 12.6f)
                lineTo(9.6f, 17.4f)
                lineTo(19.2f, 6.6f)
            }
        }
    }

    val Settings: ImageVector by lazy {
        icon("Musify.Settings") {
            line {
                moveTo(3.6f, 7.4f)
                lineTo(20.4f, 7.4f)
            }
            line {
                moveTo(3.6f, 16.6f)
                lineTo(20.4f, 16.6f)
            }
            line { circle(9.4f, 7.4f, 2.4f) }
            line { circle(15.2f, 16.6f, 2.4f) }
        }
    }

    val Help: ImageVector by lazy {
        icon("Musify.Help") {
            line { circle(12f, 12f, 9f) }
            line {
                moveTo(9.4f, 9.4f)
                curveTo(9.4f, 7.4f, 10.6f, 6.4f, 12f, 6.4f)
                curveTo(13.4f, 6.4f, 14.6f, 7.4f, 14.6f, 8.9f)
                curveTo(14.6f, 10.9f, 12f, 11.2f, 12f, 13.4f)
            }
            line(width = 2.1f, cap = StrokeCap.Round) {
                moveTo(12f, 16.4f)
                lineTo(12f, 16.45f)
            }
        }
    }

    val Info: ImageVector by lazy {
        icon("Musify.Info") {
            line { circle(12f, 12f, 9f) }
            line {
                moveTo(12f, 11.2f)
                lineTo(12f, 16.6f)
            }
            line(width = 2.1f, cap = StrokeCap.Round) {
                moveTo(12f, 7.6f)
                lineTo(12f, 7.65f)
            }
        }
    }

    val Map: ImageVector by lazy {
        icon("Musify.Map") {
            line {
                moveTo(2.8f, 6.2f)
                lineTo(9f, 3.4f)
                lineTo(15f, 6.2f)
                lineTo(21.2f, 3.4f)
                lineTo(21.2f, 17.8f)
                lineTo(15f, 20.6f)
                lineTo(9f, 17.8f)
                lineTo(2.8f, 20.6f)
                close()
            }
            line {
                moveTo(9f, 3.4f)
                lineTo(9f, 17.8f)
            }
            line {
                moveTo(15f, 6.2f)
                lineTo(15f, 20.6f)
            }
        }
    }

    val Location: ImageVector by lazy {
        icon("Musify.Location") {
            line {
                moveTo(12f, 21.4f)
                curveTo(12f, 21.4f, 19f, 14.6f, 19f, 9.6f)
                curveTo(19f, 5.7f, 15.9f, 2.6f, 12f, 2.6f)
                curveTo(8.1f, 2.6f, 5f, 5.7f, 5f, 9.6f)
                curveTo(5f, 14.6f, 12f, 21.4f, 12f, 21.4f)
                close()
            }
            line { circle(12f, 9.6f, 2.6f) }
        }
    }

    val Bolt: ImageVector by lazy {
        icon("Musify.Bolt") {
            fill {
                moveTo(13.4f, 2.6f)
                lineTo(5.2f, 13.4f)
                lineTo(11.2f, 13.4f)
                lineTo(10.6f, 21.4f)
                lineTo(18.8f, 10.6f)
                lineTo(12.8f, 10.6f)
                close()
            }
        }
    }

    val Flask: ImageVector by lazy {
        icon("Musify.Flask") {
            line {
                moveTo(9.4f, 2.8f)
                lineTo(14.6f, 2.8f)
            }
            line {
                moveTo(10.6f, 2.8f)
                lineTo(10.6f, 9.4f)
                lineTo(4.4f, 19.2f)
                curveTo(3.8f, 20.4f, 4.6f, 21.2f, 5.8f, 21.2f)
                lineTo(18.2f, 21.2f)
                curveTo(19.4f, 21.2f, 20.2f, 20.4f, 19.6f, 19.2f)
                lineTo(13.4f, 9.4f)
                lineTo(13.4f, 2.8f)
            }
            line {
                moveTo(7.4f, 15.4f)
                lineTo(16.6f, 15.4f)
            }
        }
    }

    /** Beamed eighth notes — media and playback surfaces. */
    val Music: ImageVector by lazy {
        icon("Musify.Music") {
            line {
                moveTo(10.9f, 16.6f)
                lineTo(10.9f, 5.4f)
                lineTo(20f, 3.4f)
                lineTo(20f, 14.6f)
            }
            line { circle(8.3f, 16.6f, 2.6f) }
            line { circle(17.4f, 14.6f, 2.6f) }
        }
    }

    /** Solid dot — list bullets and inline separators. */
    val Dot: ImageVector by lazy {
        icon("Musify.Dot") {
            fill { circle(12f, 12f, 3.2f) }
        }
    }

    // -- Brand --------------------------------------------------------------

    /**
     * The Musify emblem: a shield (privacy) enclosing a music note (music).
     * Used at large sizes on the landing screen and in onboarding.
     */
    val Emblem: ImageVector by lazy {
        icon("Musify.Emblem") {
            line(width = 1.5f) {
                moveTo(12f, 1.8f)
                lineTo(21.2f, 5.5f)
                lineTo(21.2f, 12f)
                curveTo(21.2f, 17.6f, 17.3f, 21.7f, 12f, 23.2f)
                curveTo(6.7f, 21.7f, 2.8f, 17.6f, 2.8f, 12f)
                lineTo(2.8f, 5.5f)
                close()
            }
            line(width = 1.5f) {
                moveTo(11.1f, 16.2f)
                lineTo(11.1f, 9.2f)
                lineTo(15.4f, 8.3f)
            }
            line(width = 1.5f) { circle(9.6f, 16.2f, 1.5f) }
        }
    }
}
