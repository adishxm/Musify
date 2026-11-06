# Phase 34 — Editorial New-Tab Home Screen Redesign & DM Serif Display Typography

**Status:** COMPLETED  
**Execution Date:** November 6, 2026  
**Goal:** Redesign Musify's new-tab home screen (`MusifyHomeScreen`) to match the user-selected dark-mode editorial reference (`01_Home_Dark_Selected.png`), inspired by M6 UX/UI Exhibition & Pinterest visual references, featuring DM Serif Display typography, a cinematic vinyl hero, single address/search omnibox, 2x2 Bento shortcut grid, and a floating 4-action capsule dock.

---

## 1. Context & Design Language

The user selected **01 Home (Dark)** from the Part 1 design suite (`media_1793966798184_24e13189.jpg` / `01_Home_Dark_Selected.png`). The aesthetic blends the architectural typography and high contrast of the M6 Exhibition with the cinematic imagery, soft pill surfaces, and glassmorphic floating controls from the Pinterest references.

### Strict Product Invariants:
1. **Musify is a web browser, not a native audio player:** No fake playback controls, fake playlists, fake follower counters, or fake security percentages. Real composables only (no full-screen screenshot backgrounds).
2. **Media-safe engine protections:** Widevine DRM CDM license acquisition, EME, and genuine media streams (`videoplayback`, `scdn.co`, `.mpd`, `.m3u8`) must NEVER be blocked or altered.
3. **Multi-variant APK size limit:** All APK variants (`Musify.apk`, `Musify-Lite.apk`, `Musify-ARM64.apk`) must remain strictly under 100 MB and verified via `apksigner`.

---

## 2. Scope and Tasks

### Task 1: Typography & Design System Tokens
- Copy `DMSerifDisplay-Regular.ttf` to `app/src/main/res/font/dm_serif_display_regular.ttf`.
- Include `DMSerifDisplay-OFL.txt` alongside the font.
- In `Type.kt`, define `DmSerifDisplayFamily` and typographic tokens:
  - `wordmark` (30 sp, tracking -0.5 sp)
  - `homeHeadline` (46 sp, line height 52 sp, tracking -0.2 sp)
  - `homeSubtitle` (20 sp, line height 24 sp)
  - `sectionHeader` (24 sp, line height 28 sp)
  - `shortcutTitle` (16 sp, line height 20 sp)
  - `captionMetadata` (11 sp, letter-spacing 1.5 sp, uppercase)
- In `Color.kt`, expose editorial dark tokens:
  - `HomeCanvas = Color(0xFF09090A)`
  - `HomeSurface = Color(0xFF18181A)`
  - `HomeSurfaceRaised = Color(0xFF242326)`
  - `HomeTextPrimary = Color(0xFFF3EEEA)`
  - `HomeTextSecondary = Color(0xFFB4AFB2)`
  - `HomeDivider = Color(0xFF3C3A3E)`
  - `HomeControlOutline = Color(0xFFA9A5AA)`
  - `HomeDockSurface = Color(0xF21B1A1C)`
  - `HomeDockBorder = Color(0x33F3EEEA)`

### Task 2: Musify Home Screen Composables (`MusifyHomeScreen.kt`)
- Extract and implement dedicated `MusifyHomeScreen.kt` in `app/src/main/java/com/musify/browser/ui/screens/`:
  - **`HomeHeader`**: Top row with `Musify` serif wordmark on the left, vertical hairline divider, and `HOME` spaced metadata label on the right.
  - **`HomeHeadline`**: `Find your sound.` primary title (46 sp) and `A quieter way to browse.` muted subtitle (20 sp).
  - **`DecorativeVinylHero`**: Stylized dark vinyl record artwork drawn with high-performance Android Canvas/Vectors, with metallic lathe grooves, shimmering reflection arcs, concentric circles, `MUSIC + THE WEB` tag, interlocking rings icon, and right-aligned vertical editorial copy ("A WIDER WEB FOR DEEPER LISTENING —").
  - **`HomeOmnibox`**: Rounded pill search field (18 dp radius, `#18181A` background, 1 dp subtle outline) with search glyph and "Search or enter address" placeholder. Submits via `onNavigate(query)`.
  - **`YourPlacesSection`**: Section header with `Your places`, hairline divider rule, and `04` count.
  - **`ShortcutGrid`**: 2x2 Bento grid with cards:
    - Spotify (`https://open.spotify.com/`)
    - YouTube Music (`https://music.youtube.com/`)
    - YouTube (`https://m.youtube.com/`)
    - SoundCloud (`https://soundcloud.com/`)
    Each card styled with 12 dp rounded corners, dark gradient background, distinct waveform/groove visual motif, serif service name, and monospace/sans domain.
  - **`HomeActionDock`**: Centered floating capsule (294 × 58 dp) anchored 16 dp above bottom navigation safe inset:
    - Back button: disabled when `!canGoBack`.
    - Shield button: triggers "Open a site to view its shields" info sheet on blank home.
    - Tabs button: displays live `tabCount` badge.
    - More options button: triggers browser action sheet.

### Task 3: Integration into `BrowserShellScreen.kt`
- Replace legacy `MusifyHomeScreen` implementation in `BrowserShellScreen.kt` with the newly engineered `MusifyHomeScreen`.
- Pass real state: `activeTab.canGoBack`, `tabsState.tabCount`, `addressInput`, `onNavigate`, and action sheet callbacks.
- Ensure bottom padding and viewport insets prevent dock from obscuring content.

### Task 4: Automated Testing (`Phase34HomeScreenRedesignTest.kt`)
- Write unit/semantic verification tests in `app/src/test/java/com/musify/browser/Phase34HomeScreenRedesignTest.kt`:
  - Verify semantic tags: `home.root`, `home.search`, `home.shortcut.spotify`, `home.shortcut.youtube_music`, `home.shortcut.youtube`, `home.shortcut.soundcloud`, `home.back`, `home.shields`, `home.tabs`, `home.more`.
  - Verify shortcut navigation URLs match exact destinations.
  - Verify empty query suppression and valid search submission.
  - Verify live tab count binding and Back button disabled state on fresh tab.
- Run complete `./gradlew.bat testDebugUnitTest` (36 test suites passing 100%).

### Task 5: Multi-Variant Release Assembly & Signature Verification
- Build all three release APKs: `Musify.apk`, `Musify-Lite.apk`, `Musify-ARM64.apk`.
- Verify signatures with `apksigner` (v2 & v3 schemes) and check file sizes (< 100 MB).

### Task 6: Phase Documentation & Git Push
- Document findings and metrics in `.brain/.report/PHASE-34-REPORT.md`.
- Update `.brain/.report/README.md`, `README.md`, and roadmap files.
- Commit and push to `origin/main`.

---

## 3. Verification Gate (Run at Phase End)
- [x] DM Serif Display font integrated under `res/font/` with OFL license.
- [x] `MusifyHomeScreen.kt` cleanly matches the approved editorial dark reference `01_Home_Dark_Selected.png`.
- [x] Shortcut destinations navigate to real web URLs (`open.spotify.com`, etc.) with zero fake native player logic.
- [x] Back button disabled on fresh tab; Tab count dynamically bound to `tabsState.tabCount`.
- [x] All 36 test suites pass 100% in `./gradlew.bat testDebugUnitTest` (249 tests).
- [x] All 3 release APKs signed and verified under 100 MB.
- [x] `.brain/.report/PHASE-34-REPORT.md` written and committed to Git.

