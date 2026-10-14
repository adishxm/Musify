# Phase 16 — Core Browser Chrome, Omnibar & Tab Switcher Obsidian Redesign

**Status:** PLANNED  
**Planning duration:** 2–3 days  
**Goal:** Redesign the primary browsing experience and core navigation chrome using the Obsidian design language. Transform the top Omnibar, toolbar controls, security/shield indicators, progress bar, tab switcher grid, and single-tab host into sleek, minimalist, monochrome surfaces with vector iconography.

## Scope and tasks
1. **Omnibar & Navigation Chrome (`BrowserShellScreen.kt`):**
   - Redesign the persistent top app bar with `Surface` `#0A0A0A` background and bottom `Hairline` `#242424`.
   - Implement the sleek Omnibar pill container with `Elevated` `#171717` fill, subtle `Hairline` border, and monospaced text rendering for clean URL readability.
   - Replace emoji navigation buttons with `MusifyIcons.ArrowLeft`, `MusifyIcons.ArrowRight`, `MusifyIcons.Reload`, `MusifyIcons.Stop`, and `MusifyIcons.MoreVertical`.
   - Update the security badge indicator (lock / shield) to render `MusifyIcons.Lock`, `MusifyIcons.LockOpen`, or `MusifyIcons.ShieldCheck` tinted with jewel tones `Success` or `Warning`.
   - Redesign tab counter pill with `CountBadge` and tabular numbers.
   - Redesign the browser overflow menu using `MusifyCard` and `SettingRow` rows with vector icons (`BookmarkOutline`, `Download`, `Desktop`, `Settings`, `Shield`, `Music`).
2. **Tab Switcher Grid (`TabSwitcherScreen.kt`):**
   - Implement the Obsidian tab switcher layout with dark OLED canvas (`Ink`), top title bar with active/private filter pills (`MusifyPill`), and close-all action.
   - Design sleek tab preview cards with `SurfaceRaised` fills, `Hairline` borders (highlighted with `Accent` for the selected tab), title typography, origin badges, and `MusifyIcons.Close` buttons.
   - Bottom toolbar featuring the incognito toggle (`MusifyIcons.Incognito` with `Private` tint) and new tab button (`MusifyIcons.Plus`).
3. **Engine Hosting Chrome (`SingleTabBrowserScreen.kt` & `EngineView.kt`):**
   - Ensure the WebView and GeckoView engine containers integrate smoothly into the `Ink` background without bright white flash transitions.
   - Style error pages and loading states using `EmptyState` and monochrome spinners.
4. **Phase 16 Test Suite:**
   - Add `Phase16BrowserChromeTest.kt` verifying tab count formatting, URL bar presentation state, menu action bindings, and theme application across chrome components.

## Required tests (run at phase end)
- [ ] `./gradlew testDebugUnitTest` runs with 100% pass rate including `Phase16BrowserChromeTest`.
- [ ] Visual verification of browser chrome, omnibar interactions, tab switching, and menu states.

## Verification Gate
- [ ] All cartoon emojis removed from navigation and tab switching chrome.
- [ ] Omnibar and tab switcher render with Obsidian tokens and typography.
- [ ] Comprehensive `.brain/.report/PHASE-16-REPORT.md` generated with detailed evidence.
- [ ] Branch/commit created and pushed cleanly to GitHub `origin/main`.

## Deliverables
- Redesigned `BrowserShellScreen.kt`, `TabSwitcherScreen.kt`, `SingleTabBrowserScreen.kt`, and `EngineView.kt`.
- Unit test suite `Phase16BrowserChromeTest.kt`.
- `.brain/.report/PHASE-16-REPORT.md`.
