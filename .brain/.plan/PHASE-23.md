# Phase 23 — GeckoView WebExtension Network Interception, Ad-Logic Redirection & Main-World Anti-Adblock Immunity

**Status:** COMPLETED  
**Planning duration:** 1 day  
**Goal:** Eliminate Spotify in-stream visual and audio advertisements ("Shake up your music routine", commercial break cards) on `open.spotify.com` in GeckoView without freezing playback or mutilating HTMLMediaElement clocks. Deploy a multi-layered defense comprising a WebExtension background blocker using `browser.webRequest.onBeforeRequest` with `blocking` filter redirection to synthetic empty JSON (`data:application/json,%7B%7D`), page-level main-world `fetch` & `XMLHttpRequest` proxy interception, cosmetic CSS neutralization, and strict preservation of Widevine DRM license acquisition (`/widevine-license`, `/license`).

## Scope and tasks
1. **Root Cause Analysis & Behavior Reproduction:**
   - Capture live test state on Android emulator when an ad card ("Shake up your music routine") surfaced on Spotify mobile web.
   - Trace why GeckoView permitted the ad: GeckoView relies on WebExtension APIs rather than Android's `WebViewClient.shouldInterceptRequest` for network interception; hence, the existing `WebViewAdapter` Kotlin rules did not filter GeckoView internal network streams without an active WebExtension background service worker/script.
   - Document detailed analysis in `.brain/.report/SPOTIFY-AD-TEST-REPORT.md`.

2. **WebExtension Background Blocker Implementation:**
   - Create `app/src/main/assets/musify_adblocker/background.js`:
     - Register `browser.webRequest.onBeforeRequest` with `blocking` and `["requestBody"]`.
     - Filter URL patterns: `*://spclient.wg.spotify.com/ad-logic/*`, `*://spclient.wg.spotify.com/ads/*`, `*://spclient.wg.spotify.com/v1/ads/*`, `*://spclient.wg.spotify.com/desktop-ad-service/*`, `*://spclient.wg.spotify.com/commercial-break/*`, `*://adeventtracker.spotify.com/*`, `*://adstudio.spotify.com/*`, `*://pixel.spotify.com/*`, `*://analytics.spotify.com/*`, and known tracker networks (`scorecardresearch.com`, `doubleclick.net`, etc.).
     - Redirect matching ad-logic requests to `data:application/json,%7B%7D` (synthetic empty JSON) to satisfy Spotify's client-side state machine with 0 ads, preventing playback freeze loops.
     - Strictly bypass Widevine DRM license endpoints (`spclient.wg.spotify.com/widevine-license`, `/license`, `/drm/`) and legitimate audio streaming segments (`audio-*.scdn.co`).
   - Update `app/src/main/assets/musify_adblocker/manifest.json`:
     - Declare `"webRequest"`, `"webRequestBlocking"`, and `"<all_urls>"` permissions.
     - Register `background.scripts: ["background.js"]`.

3. **Main-World Hook & Script-Level Ad Neutralization:**
   - Update `app/src/main/assets/musify_adblocker/spotify_adblock.js`:
     - Inject page-level `window.fetch` and `XMLHttpRequest` proxies to intercept and mock ad endpoints (`/ad-logic/`, `/commercial-break/`, `/ads/`) directly within the page's execution context.
     - Intercept commercial break sequences and trigger Spotify's native `next` action or mock completion.
     - Maintain strict Phase 12 & Phase 21 invariants: NO destructive tampering with HTMLMediaElement clock/muting on clean tracks (`currentTime = 9999`, forced `playbackRate = 16.0`, forced `volume = 0`).

4. **Unit & Verification Testing:**
   - Author `app/src/test/java/com/musify/browser/Phase23SpotifyAdShieldTest.kt`:
     - Verify background blocker manifests and permissions.
     - Verify URL matching logic for ad-logic, commercial-break, telemetry, and Widevine exemptions.
     - Verify synthetic data URI redirection format (`data:application/json,%7B%7D`).
     - Verify non-destructive invariants (DRM preservation, no clock tampering).
   - Execute `./gradlew.bat testDebugUnitTest` across all 24 test suites (171 tests).

5. **Live Emulator Testing & Deployment:**
   - Assemble `app-debug.apk` and stream-install to `emulator-5554`.
   - Test Spotify playback in both Desktop and Mobile modes on emulator.
   - Verify ad suppression, track playback progression, and seamless playback without ads or playback freezing.

## Required tests (run at phase end)
- [x] `Phase23SpotifyAdShieldTest` passes 100% (6 test cases).
- [x] Full test suite `./gradlew.bat testDebugUnitTest` passes 100% (171 tests across 24 suites).
- [x] Live emulator verification of Spotify playback without ad interruptions.

## Verification Gate
- [x] Ad-logic and commercial-break requests redirected to synthetic `{}` without playback freezes.
- [x] Widevine DRM licenses (`/widevine-license`, `/license`) and CDN audio streams strictly preserved.
- [x] Zero destructive clock tampering on legitimate audio tracks.
- [x] All 24 test suites pass with 100% pass rate.
- [x] Detailed documentation and Git push to `main`.
