# Phase 37 — Dynamic UI Customization, 10,000x Cryptographic Private Shredder, Background Media Playback & SPA Blackout Resolution

**Status:** IN PROGRESS  
**Execution Date:** November 8, 2026  
**Author & Lead Engineer:** adishxm (`Designed & Engineered by adishxm`)  
**Scope:** Browser Engine Stability, Extreme Privacy Shredding, UI/UX Customization Engine, Background Audio Playback, PiP Hover & Automated Verification

---

## 1. User Directive & Problem Statement

The user reported critical feedback on Phase 36:
1. **Blackout / Freezing on Complex SPAs**: Pages such as `music.youtube.com`, `open.spotify.com`, and `m.youtube.com` suffered from blacked-out screens or unresponsive content carousels due to aggressive cosmetic overflow overrides and recursion bugs in scriptlet injection.
2. **Bottom Navbar Clashes on Music / Web Apps**: Fixed bottom floating capsules covered website-native bottom navbars (e.g. Spotify Web, Instagram Mobile), making bottom web buttons unclickable.
3. **Overly Literal Safari Copy**: The user desired a unique, flexible identity rather than a static duplicate of Safari—specifically requesting comprehensive UI customization so users can tailor every aspect of the interface.
4. **10,000x Extreme Cryptographic Private Mode**: Private browsing needed to go far beyond standard ephemeral cookies; private tab data must be cryptographically corrupted, shredded with high-entropy bytes (DoD/Gutmann multi-pass), and panic-wiped from RAM and disk upon closure so zero trace can ever be recovered.
5. **Background Audio & System Notifications**: Music from YouTube Music, Spotify, and SoundCloud must continue playing seamlessly in the background when the user exits or minimizes the app, accompanied by an interactive system media notification card and floating Picture-in-Picture (PiP) window.
6. **Author Attribution**: Brand the application prominently with `"Designed & Engineered by adishxm"`.

---

## 2. Architectural Blueprint & Deliverables

### Task 1: SPA Blackout Resolution & Media Engine Hardening
- **Annoyance Filter Refactor (`AnnoyanceFilterRepository.kt`)**: Remove global `html, body { overflow: auto !important; position: static !important; }` which broke CSS flex/grid virtual scrolling in YouTube Music and Spotify. Replace with strictly scoped modal unfreezing.
- **Procedural Scriptlet Recursion Guard (`ProceduralScriptletEngine.kt`)**: Implement depth limiter (`depth > 8`) and `try/catch` wrappers around `JSON.parse` and `Response.prototype.json` monkey patches to prevent stack overflows on complex nested JSON payloads.
- **Media Safe Resource Whitelisting (`MediaSafeResourcePolicy.kt`)**: Add YouTube, Google CDN, and Spotify streaming endpoints to first-party auth allowlists while preserving strict telemetry ad blocking.
- **Hardware Acceleration (`WebViewAdapter.kt`)**: Enable `LAYER_TYPE_HARDWARE`, `useWideViewPort`, `loadWithOverviewMode`, and `mediaPlaybackRequiresUserGesture = false`.

### Task 2: UI Customization Engine & Nav Conflict Resolution
- **Customization Settings Screen (`UiCustomizationScreen.kt`)**:
  - Dock Position selector: Bottom Floating Capsule vs Top Unified Header.
  - Auto-Hide Navigation Dock on Scroll: Dynamically animates out of view when scrolling down, revealing underlying website controls.
  - Safe Area Bottom Web Clearance toggle: Injects 68dp webview bottom padding when dock is docked at the bottom.
  - Design Theme Palette selector: Editorial Obsidian, Safari Frosted Glass, Cyberpunk Emerald, Midnight Amethyst.
  - Dedicated author footer: `"Designed & Engineered by adishxm"`.
- **Preferences Storage (`AppPreferencesRepository.kt`)**: Atomic persistent JSON store for all customization options.
- **Shell Dynamic Insets (`BrowserShellScreen.kt`)**: Respect dynamic top/bottom clearance and animated visibility on web scroll.

### Task 3: 10,000x Extreme Cryptographic Private Shredder
- **Entropy Shredder Engine (`CryptographicShredder.kt`)**:
  - Multi-pass overwrite (0x00, 0xFF, Cryptographic Pseudo-Random Entropy from `SecureRandom`).
  - Corruption marker injection: `"[CRYPTOGRAPHICALLY_CORRUPTED_0xDEADBEEF_BY_ADISHXM]"`.
  - Comprehensive purge of `WebStorage`, `CookieManager`, cache directories, and in-memory tab state.
  - Panic Wipe: Immediate memory and disk destruction triggerable from Tab Switcher and Settings.
- **Private Tab Switcher (`SafariTabSwitcherScreen.kt`)**:
  - Glowing 10,000x Extreme Privacy Mode banner.
  - "⚡ Shred & Panic Wipe RAM Now" instant destruction button.

### Task 4: Background Media Playback Service & PiP
- **Foreground Media Service (`MusifyMediaPlaybackService.kt`)**:
  - `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK` compliant service.
  - Interactive media notification with Play, Pause, and Close actions.
  - Foreground CPU `WakeLock` to prevent Android OS audio throttling.
  - Branded: `"Musify • Designed by adishxm"`.
- **System Picture-in-Picture (`MainActivity.kt`)**:
  - Automatic PiP transition on `onUserLeaveHint()` when media is actively playing.

### Task 5: Quality Assurance & Build Verification
- Unit test suite: `Phase37CustomizationAndExtremePrivacyTest.kt` verifying entropy shredder, preference persistence, UI customization, and author branding.
- Full unit test pass: 100% (273/273 tests).
- Release APK builds: `Musify.apk`, `Musify-Lite.apk`, and `Musify-ARM64.apk` ($< 100\text{ MB}$, v2/v3 signatures).
- Live emulator verification on `emulator-5554` (API 36).

