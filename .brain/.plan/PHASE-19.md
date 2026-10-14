# Phase 19 — Floating Action Bar, 14-Action Grid Panel & Iridescent Glow Button Component Architecture

**Status:** COMPLETED  
**Planning duration:** 1–2 days  
**Goal:** Implement the Pinterest/Figma reference component architecture from `musify-ui-update.zip`: floating pill `MusifyActionBar`, expanding 14-action grid panel, `MusifyGlowButton` with rotating iridescent sweep gradient, launcher icon updates (monochrome shield-and-note vector emblem across all mipmap densities), launcher background cleanup, dead resource pruning, and wiring the action grid into `BrowserShellScreen`.

## Scope and tasks
1. **Component & Token Additions:**
   - Add `MusifyGlowButton.kt` in `ui/theme/`:
     - Iridescent rotating sweep gradient (`Brush.sweepGradient` across 6 stops: `#60A5FA` -> `#C084FC` -> `#F472B6` -> `#FBBF24` -> `#34D399` -> `#60A5FA`).
     - Styles: `Hero`, `Plate`, `Ghost` with alpha borders and glow animation.
     - `MusifyGlowChip`, `DotMatrixOrnament`, `EditorialMeta`, `TickScrubber`.
   - Add `MusifyActionBar.kt` in `ui/theme/`:
     - Floating pill action bar anchored at the bottom of the browser viewport.
     - Expanding 14-action grid panel with bespoke vector icons, titles, and subtitle metadata.
     - Controls for: New Tab, New Private Tab, Close Tab, Bookmarks, History, Downloads, Shields, DNS Settings, Site Permissions, Clear Data, Playback Diagnostics, Desktop Mode, Reload, and Share.
   - Update `Color.kt`:
     - Add `WelcomeSurface` token (`#242424`).
   - Update `strings.xml`:
     - Add `welcome_hello` string resource.
2. **Resource & Launcher Icon Modernization:**
   - Update `res/drawable/ic_launcher_background.xml`: set background to `#000000` (removes legacy green Spotify cast).
   - Replace legacy green neon launcher webp icons (`res/mipmap-*/ic_launcher*.webp`) across all 5 densities (hdpi, mdpi, xhdpi, xxhdpi, xxxhdpi) with the official monochrome shield-and-note emblem artwork.
   - Apply dead resource pruning per `optional-cleanup.diff`:
     - Remove unused `res/layout/welcome_matrix.xml`.
     - Remove unused `res/drawable/rectangle_2.xml`.
     - Remove obsolete `skip_the_ma` style from `res/values/themes.xml`.
     - Prune orphaned string entries from `res/values/strings.xml`.
3. **Browser Shell Screen Integration:**
   - Update `BrowserShellScreen.kt`:
     - Wire floating `MusifyActionBar` and action grid via `onOpenActionMenu`.
     - Upgrade Home screen search CTA to `MusifyGlowButton(Plate, meta = "SECURE")`.
     - Preserve all existing Phase 11–17 functionality (shields, desktop mode toggle, tab switcher count, omnibar navigation, diagnostics modal).
4. **Testing & Phase Completion:**
   - Author `Phase19ActionBarGlowTest.kt` verifying:
     - Component instantiation, iridescent palette definitions, action bar actions count, launcher background color token, and clean string resources.
   - Run unit test suite: `./gradlew testDebugUnitTest`.
   - Author `.brain/.report/PHASE-19-REPORT.md`.
   - Commit and push to Git repository (`origin/main`).

## Required tests (run at phase end)
- [x] `./gradlew testDebugUnitTest` runs with 100% pass rate.
- [x] `Phase19ActionBarGlowTest` asserts iridescent sweep gradient, floating action bar actions, and launcher icon assets.
- [x] No regression on any existing browser, tabs, shields, or diagnostics functionality.

## Verification Gate
- [x] `MusifyGlowButton.kt` and `MusifyActionBar.kt` compiled cleanly and unit-tested.
- [x] Launcher icons updated to monochrome shield emblem across all 5 mipmap densities.
- [x] Dead XML layouts and unused strings removed.
- [x] `.brain/.report/PHASE-19-REPORT.md` generated with full test evidence.
- [x] Phase committed and pushed to GitHub `origin/main`.
