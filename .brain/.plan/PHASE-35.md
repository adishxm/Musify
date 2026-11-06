# Phase 35 — Apple Safari-Inspired Universal UI/UX Overhaul: Bottom Tab Bar, Safari Start Page, Tab Grid Switcher, Privacy Report & iOS Sheet System

**Status:** PLANNED  
**Execution Date:** November 7, 2026  
**Reference:** [Apple Safari (Official)](https://www.apple.com/in/safari/)  
**Goal:** Completely redesign the entire UI/UX of Musify Browser inspired by Apple Safari (iOS / macOS Human Interface Guidelines). Implement Safari's signature bottom floating address/tab bar, customizable Safari Start Page with Favorites squircle grid, Privacy Report card and interactive modal, 2-column Safari tab card switcher with private browsing toggle, "aA" page settings popover, and iOS 18 style action/share sheet.

---

## 1. Context & Architectural Rationale

Apple Safari represents the pinnacle of mobile browser design, characterized by thumb-centric ergonomy, floating translucent glass surfaces, minimalist typography, and prominent privacy reporting:
- **Bottom Floating Tab Bar:** Pinned within thumb's natural sweep, featuring a rounded glass capsule, HTTPS lock, clean domain presentation, format `"aA"` menu, tab count badge, and share action.
- **Safari Start Page:** Customizable favorites grid with glossy squircles, Intelligent Tracking Prevention privacy summary ("In the last 30 days, Safari has prevented 142 trackers from profiling you"), frequently visited sites, and reading list integration.
- **Safari Tab Switcher Grid:** 2-column card deck with live tab previews, favicon headers, close buttons, private browsing mode toggle, and bottom action bar.
- **Intelligent Tracking Prevention (ITP) Privacy Report:** Interactive modal displaying tracker prevention analytics, top profiling trackers blocked (DoubleClick, Facebook, Criteo), and domain-level privacy health.
- **"aA" Format & Page Settings Menu:** Text zoom, reader mode, desktop website toggle, and privacy settings.
- **iOS Action / Share Sheet:** System-level modal sheet for sharing, bookmarking, and tools.

### Strict Product Invariants:
1. **Musify is a web browser, not a native audio player:** All shortcuts navigate to genuine web origins (`open.spotify.com`, `music.youtube.com`, etc.). Zero fake players or playlists.
2. **Media-safe engine protections:** Widevine DRM CDM license acquisition, EME, and genuine media streams (`videoplayback`, `scdn.co`, `.mpd`, `.m3u8`) must NEVER be blocked or broken.
3. **Multi-variant APK size limit:** All APK variants (`Musify.apk`, `Musify-Lite.apk`, `Musify-ARM64.apk`) must remain strictly under 100 MB and verified via `apksigner`.

---

## 2. Scope and Tasks

### Task 1: Safari HIG Design System Tokens & Typography (`Color.kt`, `Type.kt`, `MusifyIcons.kt`)
- In `Color.kt`, introduce Apple Safari semantic tokens:
  - `SafariBackgroundDark` (`#000000`), `SafariCardDark` (`#1C1C1E`), `SafariCardElevated` (`#2C2C2E`), `SafariBorderDark` (`#38383A`)
  - `SafariAccentBlue` (`#007AFF`), `SafariSuccessGreen` (`#34C759`), `SafariWarningOrange` (`#FF9500`), `SafariDangerRed` (`#FF3B30`), `SafariPrivatePurple` (`#AF52DE`)
  - `SafariBarGlass` (`#E61C1C1E`), `SafariBarBorder` (`#33FFFFFF`)
  - `SafariTextPrimary` (`#FFFFFF`), `SafariTextSecondary` (`#8E8E93`), `SafariTextTertiary` (`#636366`)
- In `Type.kt`, introduce Apple SF Pro / Inter typographic hierarchy:
  - `SafariTitleLarge` (34sp Bold), `SafariTitleMedium` (22sp Bold), `SafariHeadline` (17sp SemiBold), `SafariBody` (17sp Regular), `SafariCallout` (16sp Regular), `SafariSubhead` (15sp Regular), `SafariFootnote` (13sp Regular), `SafariCaption` (11sp Regular)
- In `MusifyIcons.kt`, add Safari-specific icons:
  - `FormatAA`, `ReadingList`, `ShareIOS`, `TabsGrid`, `Compass`, `BookOpen`

### Task 2: Safari Start Page (`SafariStartPage.kt`)
- Implement authentic Safari Start Page:
  - Header with clean wordmark, greeting/date, and private browsing badge.
  - **Favorites Section:** 4-column squircle icon grid (Spotify, YouTube Music, YouTube, SoundCloud, Apple Music, Reddit, Wikipedia, GitHub) with subtle drop shadows, custom brand glyphs, and labels.
  - **Privacy Report Card:** Signature Apple Safari widget showing total trackers blocked, profiling percentage, and link to open full Privacy Report.
  - **Frequently Visited Sites:** Carousel / quick access row.
  - **Reading List & Bookmarks Section:** Quick access rows with favicons and source domain.
  - **Edit Start Page Button:** Bottom pill button to customize visible sections.

### Task 3: Safari Bottom Unified Tab Bar (`SafariBottomTabBar.kt`)
- Implement Safari's signature floating bottom address bar:
  - Capsule shape (`RoundedCornerShape(26.dp)`), frosted glass surface, pinned above bottom system insets.
  - **Left:** `"aA"` Format / Page Settings button.
  - **Center:** HTTPS Lock + clean domain text (e.g. `open.spotify.com`) with Reader mode glyph. Tap triggers full address search sheet.
  - **Right:** Reload / Stop toggle, Tab Switcher badge `[ n ]`, and Share / Action Sheet button (`MusifyIcons.ShareIOS`).
- Support classic Safari 5-button bottom navigation bar mode (Back, Forward, Share, Bookmarks, Tab Switcher).

### Task 4: Safari "aA" Format & Page Settings Sheet (`SafariPageSettingsSheet.kt`)
- Implement Safari's popover menu:
  - Text Zoom stepper: `[ A-   100%   A+ ]`.
  - "Show Reader" / "Hide Reader" toggle.
  - "Privacy Report" button (launches full Privacy Report).
  - "Request Desktop Website" toggle.
  - "Website Settings" (permissions, camera, mic).
  - "Clear History and Website Data".

### Task 5: Safari Tab Switcher Grid (`SafariTabSwitcherScreen.kt`)
- Redesign tab overview inspired by iOS 17/18 Safari:
  - 2-column vertically scrolling deck of 3D floating tab cards.
  - Each card features rounded corners (`16.dp`), favicon + title header, top-right circular close button `(×)`, and live preview canvas.
  - Active tab highlighted with `SafariAccentBlue` outline.
  - Top Bar: "Done" button, Tab Mode Segmented Control (`Tabs (N)` vs `Private (N)` with private browsing shield banner).
  - Bottom Bar: `+` New Tab button, Tab Group selector (`n Tabs ▾`), Close All Tabs option.

### Task 6: Safari Privacy Report Sheet (`SafariPrivacyReportSheet.kt`)
- Redesign the Privacy Shields Sheet into Safari's Intelligent Tracking Prevention Report:
  - Hero metric: "Musify prevented X trackers from profiling you across websites".
  - Tracker profiling breakdown (Ads, Analytic Trackers, Social Widgets, Fingerprinters).
  - Top profiling tracker domains (Google DoubleClick, Facebook Graph, Google Analytics, Criteo, Amazon AdSystem).
  - Per-site privacy controls and shield toggles.

### Task 7: Safari Share & Action Sheet (`SafariShareSheet.kt`)
- Implement iOS 18 style modal bottom sheet:
  - Top URL preview card (Favicon, Title, Host Origin).
  - Quick actions row: Copy Link, Add Bookmark, Add to Favorites, Add to Reading List, Find in Page.
  - System tools list: Desktop Site, Media & Playback Diagnostics, Site Permissions, Sandboxed Downloads, Filter Lists, About Musify.

### Task 8: Integration into `BrowserShellScreen.kt`
- Wire `SafariStartPage`, `SafariBottomTabBar`, `SafariPageSettingsSheet`, `SafariTabSwitcherScreen`, `SafariPrivacyReportSheet`, and `SafariShareSheet` into `BrowserShellScreen.kt`.
- Ensure clean state synchronization across active tab, navigation history, bookmarking, and private browsing.

### Task 9: Unit Testing & Verification (`Phase35SafariRedesignTest.kt`)
- Add comprehensive test suite verifying Safari tokens, bottom tab bar semantics, Start page favorites, tab switcher operations, and privacy report stats.
- Run complete `./gradlew.bat testDebugUnitTest` ensuring 100% test pass rate across all suites.

### Task 10: Multi-Variant Release Assembly & Emulator Verification
- Build all 3 release APKs (`Musify.apk`, `Musify-Lite.apk`, `Musify-ARM64.apk`).
- Verify `apksigner` (v2 & v3 schemes) and size < 100 MB.
- Install on Android emulator (`Medium_Phone_API_36.1`), take verification screenshots.
- Write `.brain/.report/PHASE-35-REPORT.md`.
- Commit and push to Git.

---

## 3. Verification Gate (Run at Phase End)
- [ ] Safari design tokens, typography, and icons integrated.
- [ ] `SafariStartPage.kt` matches Apple Safari Start Page layout with Favorites, Privacy Report card, and Reading List.
- [ ] `SafariBottomTabBar.kt` provides thumb-friendly floating navigation, domain pill, "aA" menu, and tab badge.
- [ ] `SafariTabSwitcherScreen.kt` provides 2-column card grid with Private Browsing mode.
- [ ] `SafariPrivacyReportSheet.kt` provides Intelligent Tracking Prevention breakdown.
- [ ] All unit test suites pass 100% in `./gradlew.bat testDebugUnitTest`.
- [ ] All 3 release APKs signed and verified under 100 MB.
- [ ] Live verification on Android emulator with screenshots captured.
- [ ] Documentation and git commit pushed to `origin/main`.

