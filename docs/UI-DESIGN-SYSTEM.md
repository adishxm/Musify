# Musify UI — The "Obsidian" Design System

A strictly monochrome interface: black, white, and a short ladder of neutral greys.
Hierarchy comes from **light** (how much a surface kicks back) and **hairlines**
(1dp borders at very low contrast) — never from hue.

Colour is reserved for one job only: **security semantics**. When a hue appears,
it means something.

```
Platform   Kotlin 2.0.21 · Jetpack Compose (BOM 2024.10.01) · Material 3
Type       Inter 4.0, bundled as a variable font (860 KB, weights 100–800)
Icons      44 hand-built 24dp vectors, 1.7 stroke, round caps
Verified   :app:compileDebugKotlin → BUILD SUCCESSFUL
```

---

## 1. Colour

### Surface ladder (dark / "Obsidian" — the canonical appearance)

| Token | Value | Used for |
|---|---|---|
| `Ink` | `#000000` | Page background. True black — the deepest canvas OLED can render. |
| `Surface` | `#0A0A0A` | Persistent chrome: top bar, sheet headers, nav rails. |
| `SurfaceRaised` | `#111111` | Cards and grouped content. |
| `Elevated` | `#171717` | Inputs, chips, nested cards — any interactive fill at rest. |
| `ElevatedHigh` | `#1F1F1F` | Hover / pressed / selected fills. |
| `Hairline` | `#242424` | Divider and resting border. Barely there, by design. |
| `WelcomeSurface` | `#242424` | Minimalist launch surface canvas (Phase 20). |
| `HairlineStrong` | `#343434` | Focus rings and active selections. |

### Text ladder

| Token | Value | Contrast on `Ink` |
|---|---|---|
| `TextPrimary` | `#FAFAFA` | ~20.4:1 |
| `TextSecondary` | `#A3A3A3` | ~11.1:1 |
| `TextMuted` | `#6E6E6E` | ~5.7:1 |

All three clear WCAG AA for body text; `TextMuted` is used only at ≥11sp.

### Accent

`Accent` is **pure white** — there is no brand hue. In a monochrome system,
inversion (`white fill + OnAccent text`) is the strongest available emphasis, so
it is reserved for primary actions and the active tab.

### Security semantics — the only chroma

| Token | Value | Meaning |
|---|---|---|
| `Success` | `#4FCB92` | HTTPS origin, shields active, permission granted |
| `Warning` | `#DFB356` | Insecure HTTP, shields off, degraded state |
| `Danger` | `#E0645B` | Danger, destructive actions, TLS failure |
| `Private` | `#B0A0F0` | Private / incognito browsing |

These are deliberately desaturated so they read as calm jewel tones against black
rather than neon alerts.

### Going 100% achromatic

Four values control the entire chromatic surface of the product. Map them to greys
and the app becomes fully greyscale — **nothing else needs to change**:

```kotlin
val Success = Color(0xFFC9C9C9)
val Warning = Color(0xFF9A9A9A)
val Danger  = Color(0xFFE8E8E8)
val Private = Color(0xFFB4B4B4)
```

### Tints

`Color.softTint()` / `.mediumTint()` / `.strongTint()` → alpha `0.10` / `0.18` / `0.32`.
Always express a tinted fill through these rather than hand-writing an alpha, so
tint weight stays consistent across the app.

---

## 2. Typography

Inter is bundled as a **single variable font** (`res/font/inter_variable.ttf`) and
instantiated at eight weights through `FontVariation`. One 860 KB file replaces
four static faces and keeps the APK lean.

> `Font(resId, weight, style, variationSettings)` is annotated `@ExperimentalTextApi`,
> so `Type.kt` carries a file-level opt-in.

Two rules keep the scale feeling editorial rather than generic:

1. **Large text tightens, small text opens up.** Tracking runs from `-1.3sp` at
   display sizes to `+0.7sp` at `labelSmall`.
2. **Weight never exceeds 700.** Heavy weights are reserved for moments that
   genuinely need to shout.

| Role | Size / weight | Tracking |
|---|---|---|
| `displayLarge` → `displaySmall` | 40 / 34 / 28 · W700/700/600 | −1.3 → −0.7 |
| `headlineLarge` → `headlineSmall` | 26 / 21 / 18 · W700/600/600 | −0.6 → −0.3 |
| `titleLarge` → `titleSmall` | 16 / 14.5 / 13 · W600 | −0.2 → 0 |
| `bodyLarge` → `bodySmall` | 15 / 13.5 / 12 · W400 | 0 → +0.05 |
| `labelLarge` → `labelSmall` | 13.5 / 11.5 / 10 · W600/600/700 | +0.1 → +0.7 |

Extras:

- `MusifyTypography_Overline` — 10sp / W700 / 1.4sp tracking. All-caps eyebrows.
- `MusifyTypography_Mono` — URL, hostname and hash readouts.
- `MusifyTypography_Numeric` — 30sp / W700 with `"tnum"`, so digits never jitter
  while a counter animates.

---

## 3. Shape & spacing

```kotlin
MusifyShapes.xs   = 6.dp    // tags, hairline chips
MusifyShapes.sm   = 10.dp   // buttons, inputs
MusifyShapes.md   = 14.dp   // cards, list rows
MusifyShapes.lg   = 20.dp   // hero panels
MusifyShapes.xl   = 28.dp   // bottom sheets, dialogs
MusifyShapes.pill = 50%     // pills, icon buttons
```

`Space` is a 4dp grid: `xxs 2 · xs 4 · sm 8 · md 12 · lg 16 · xl 20 · xxl 28 · xxxl 40`.

Before this pass the app used nine different corner radii (3, 4, 6, 8, 10, 12, 14,
16, 24dp) and seventeen distinct font sizes. Collapsing those onto a single short
ladder is most of what makes a UI stop looking improvised.

---

## 4. Motion

```kotlin
MusifyMotion.Standard  // cubic-bezier(0.2, 0, 0, 1) — quick out, gently settled
MusifyMotion.Exit      // cubic-bezier(0.4, 0, 1, 1)
MusifyMotion.fast      // 120ms
MusifyMotion.medium    // 220ms
MusifyMotion.slow      // 340ms
MusifyMotion.enter<T>()// spring, damping 0.9
```

Nothing bounces for longer than ~300ms. Restraint reads as speed.

---

## 5. Icons

`MusifyIcons` replaces **145 emoji** across 16 screens.

Emoji were the single largest obstacle to a premium feel: they render as
full-colour cartoon art that differs per device and OEM, they ignore `tint`, and
they sit optically out of line with text. A vector icon is simply another shade
of the surface it sits on.

Rules:

- 24×24 viewport, artwork inside a 2dp safe area.
- One stroke weight (`1.7f`), round caps and joins everywhere.
- Outlined by default; filled only for stars, dots and the brand emblem, where
  solid mass *is* the point.
- Built on a 4dp-ish grid so icons align to each other at a glance.

```kotlin
Icon(MusifyIcons.ShieldCheck, tint = Success)
```

Available: `Shield`, `ShieldCheck`, `ShieldOff`, `Lock`, `LockOpen`, `Warning`,
`Incognito`, `Blocked`, `ArrowLeft`, `ArrowRight`, `ChevronRight`, `ChevronDown`,
`ChevronUp`, `Close`, `Plus`, `MoreVertical`, `Tabs`, `Reload`, `Stop`, `Desktop`,
`Mobile`, `Globe`, `Search`, `BookmarkOutline`, `StarFilled`, `StarOutline`,
`Clock`, `Download`, `Trash`, `Document`, `Link`, `ExternalLink`, `Check`,
`Settings`, `Help`, `Info`, `Map`, `Location`, `Bolt`, `Flask`, `Music`, `Dot`,
`Emblem`.

> Implementation note: `path { }` inside `ImageVector.Builder` is a **top-level
> extension** in `androidx.compose.ui.graphics.vector`, not a member function.
> `MusifyIcons.kt` imports it explicitly.

---

## 6. Shared primitives

`ui/theme/Components.kt` exists so that a "card", a "chip", or a "section label"
looks identical everywhere. Per-screen ad-hoc styling is the biggest single cause
of a large UI drifting away from feeling designed.

| Component | Purpose |
|---|---|
| `MusifyIcon` | Monochrome icon with explicit optical size |
| `IconLabel` | Icon + label on one baseline — replaces the `<emoji> Label` string pattern |
| `Hairline` / `VHairline` | 1dp separators |
| `Overline` | All-caps eyebrow label |
| `SectionHeader` | Eyebrow + title + supporting copy on a strict rhythm |
| `MusifyCard` | Standard raised surface with hairline border |
| `SettingRow` | Label / subtitle / leading icon / trailing slot / divider |
| `MusifyPill` | Selectable chip; selection inverts to white |
| `CountBadge` | Tabular-figure counter |
| `StatRow` | Label / value pair for dense metadata |
| `EmptyState` | Icon + headline + supporting copy |

---

## 7. What changed

| Area | Before | After |
|---|---|---|
| Palette | Navy `#0F172A` + cyan/blue/violet/green/amber/red | Monochrome ladder + 4 security semantics |
| Accent | Cyan `#06B6D4` | White `#FFFFFF` |
| Type | Roboto at system weights | Inter variable font, full scale, tuned tracking |
| Icons | 145 emoji across 16 screens | 44 vector icons |
| Radii | 9 distinct values | 6-token ladder |
| Colour literals | 24 hardcoded `Color(0x…)` in screens | 6 gradient stops (all greyscale) |
| Intro screen | Orange-red Matrix gradient | Monochrome white bloom |
| Theming | Dark only, forced | Dark-first, light "Paper" scheme available |
| Window theme | `Theme.Material.Light` | `Theme.Material` + black window background (no white launch flash) |

---

## 8. Files

```
ui/theme/
  Color.kt          Palette + tint helpers
  Type.kt           Inter family + type scale
  Theme.kt          Dark/light schemes, shapes, motion, spacing
  MusifyIcons.kt    44 vector icons
  Components.kt     Shared primitives
res/font/
  inter_variable.ttf   Variable font, weights 100–800
res/values/
  themes.xml        Window theme, black background
  colors.xml        Platform-visible token mirrors
```

---

## 9. Building

```bash
# Compile Kotlin sources only (fast feedback)
./gradlew :app:compileDebugKotlin

# Build an APK
./gradlew :app:assembleDebug
```

Inter is licensed under the SIL Open Font License 1.1. A copy of the licence
should ship alongside the font when distributing the APK.
