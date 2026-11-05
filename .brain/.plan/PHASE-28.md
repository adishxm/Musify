# Phase 28 — M6 Exhibition UI/UX Redesign: Museum-Grade Digital Sound Archive & Kinetic Shield Interface

**Status:** COMPLETED  
**Planning duration:** 1–2 days  
**Goal:** Redesign the entire UI/UX of Musify Browser inspired by the M6 UX/UI Exhibition design language: an architectural, museum-grade sound archive and kinetic shield interface. Deep high-contrast obsidian canvases, architectural typography with tight tracking, monospaced metadata badges, curated media exhibit bento grids, a floating exhibition capsule dock, and real-time seismograph telemetry shields.

---

## 1. Design Language & Visual Architecture

### A. Architectural Exhibition Canvas & Palette (`Color.kt`)
- `M6Canvas`: `#08080A` — Deep exhibition gallery void; optimized for OLED displays.
- `M6Surface`: `#101014` — Primary exhibition structure (top bars, sheets, docked panels).
- `M6Card`: `#16161C` — Curated media exhibit cards and bento grid containers.
- `M6Elevated`: `#1E1E26` — Interactive buttons, omnibar search pill, and active inputs.
- `M6Hairline`: `#282832` — 1dp architectural structural grid dividers.
- `M6HairlineStrong`: `#3E3E4C` — Selected item borders, focus indicators, and active cards.

### B. Seismic Jewel Semantic Chroma
- `M6Emerald`: `#00F59B` / `#10B981` — HTTPS encrypted origin, zero trackers, secure streaming.
- `M6SignalOrange`: `#FF5C00` / `#FF7A29` — Active privacy shields, ad-defusal counters, real-time protection.
- `M6SignalRed`: `#FF3B30` — Blocked surveillance hosts, security warnings, bounce redirects defused.
- `M6SoundViolet`: `#A855F7` — Hardware-accelerated Widevine DRM, lossless streams, SABR backoff bypass.

### C. Exhibition Typography & Monospaced Metadata (`Type.kt`)
- Variable Inter typography with tight tracking on large exhibition headers (`displayLarge`, `headlineMedium`).
- Monospaced technical exhibition labels (`[M6 // SOUND ARCHIVE]`, `[DRM // WIDEVINE ACTIVE]`, `[SHIELD // ZERO-LATENCY]`, `[BUFFER // 100%]`).
- High-contrast text ladder: `#FFFFFF` (Exhibition Display), `#E2E2E8` (Primary Body), `#9898A6` (Secondary Metadata), `#5C5C6E` (Muted Captions).

---

## 2. Core UI/UX Redesign Scope

### A. Exhibition Welcome & Sound Archive Pavilion (`WelcomeScreen.kt` & `LandingScreen.kt`)
1. **Catalog Identity Header:** Architectural `MUSIFY // M6 SOUND ARCHIVE` title with live exhibition status badge (`[LIVE SEISMOGRAPH // SHIELDS ACTIVE]`).
2. **Curated Media Bento Grid:**
   - **Spotify Web Player:** Emerald-accented streaming exhibit card (`open.spotify.com`) with Widevine DRM badge and ad-mute status.
   - **YouTube Music:** Coral-accented audio exhibit card (`music.youtube.com`) with background playback active indicator.
   - **YouTube:** Coral-accented video streaming exhibit card (`m.youtube.com`) with anti-freeze SABR defense badge.
   - **SoundCloud:** Amber-accented indie audio stream exhibit card (`soundcloud.com`).
3. **Live Seismograph Telemetry Card:** Real-time visual metrics showing total trackers defused, media streams secured, and bounce hops neutralized.
4. **Exhibition Omnibar Search Pill:** Centered floating capsule with search icon, instant URL input, and clear action.

### B. Floating Exhibition Capsule Dock (`MusifyActionBar.kt` & `BrowserShellScreen.kt`)
1. Floating frosted capsule navigation dock with backdrop blur and hairline border.
2. Embedded Omnibar with HTTPS shield dot and security protocol display.
3. Quick-access navigation: Back, Forward, New Exhibit Tab, Tabs Counter, and Privacy Shields button.

### C. M6 Seismograph Privacy Shield Sheet (`PrivacyShieldSheet.kt`)
1. Exhibition-grade slide-up sheet displaying live origin seismograph.
2. Real-time protection metrics: Ad Network Requests Defused, Anti-Freeze Mock Responses Served, Bounce Trackers Bypassed, Tracking URL Parameters Stripped, Widevine DRM Protected.
3. Master shield toggle with kinetic switch animation and granular controls for Ad Blocking, Anti-Freeze Mocking, and Tracking Parameters.

### D. Exhibition Tab Gallery (`TabSwitcherScreen.kt`)
1. 2-column bento gallery showcasing active browsing sessions as exhibition archive entries.
2. High-contrast active tab border (`#FF5C00` or `#00F59B`), private browsing pill, and quick close button.
3. Bottom floating capsule to launch a new tab or close all tabs.

---

## 3. Scope and Tasks

1. **Phase Specification & Git Commit:** Commit Phase 28 plan and roadmap update (`push on git then work`).
2. **Design Tokens & Theme Foundation:** Update `Color.kt`, `Type.kt`, and `Components.kt`.
3. **Screen Redesigns:**
   - Update `WelcomeScreen.kt` and `LandingScreen.kt`.
   - Update `MusifyActionBar.kt` and `BrowserShellScreen.kt`.
   - Update `PrivacyShieldSheet.kt`.
   - Update `TabSwitcherScreen.kt`.
4. **Automated Unit Testing:**
   - Create `Phase28M6ExhibitionDesignTest.kt`.
   - Run `./gradlew.bat testDebugUnitTest` across all 30 test suites.
5. **Emulator Verification:** Test live on Android emulator via `test-on-emulator.bat`.
6. **Report & Git Push:** Author `.brain/.report/PHASE-28-REPORT.md`, compile release APKs, commit, and push to GitHub.

---

## 4. Verification Gate (Run at Phase End)
- [x] Complete test suite `./gradlew.bat testDebugUnitTest` passes 100% (all 29 test suites, 203 tests).
- [x] M6 Exhibition design tokens integrated across all Compose screens.
- [x] All 3 release APK variants compiled and verified with `apksigner`.
- [x] Curated media exhibits (Spotify, YouTube Music, YouTube, SoundCloud) active on landing & home.
- [x] `.brain/.report/PHASE-28-REPORT.md` documented and committed.
