# Phase 24 — Brave-Aligned YouTube & YouTube Music Ad Shield & Background Playback Engine

**Status:** COMPLETED  
**Planning duration:** 1–2 days  
**Goal:** Implement a hardened, Brave-inspired adblocking and background audio engine for YouTube (`www.youtube.com`, `m.youtube.com`) and YouTube Music (`music.youtube.com`) inside Musify. Incorporate reverse-engineered Brave mechanisms: initial player response pruning (`ytInitialPlayerResponse.playerAds`, `adPlacements`, `adSlots`), `JSON.parse` response proxy stripping, cosmetic CSS ad hiding (`.video-ads`, banner overlays, premium promo popups), and continuous background playback preservation via Page Visibility API spoofing and user-activity keep-alive (`window._lact`).

## Scope and tasks
1. **Brave-Aligned Scriptlet & Content Script Architecture:**
   - Create `app/src/main/assets/musify_adblocker/youtube_adblock.js`:
     - **Player Response Stripping**: Trap and neutralize `ytInitialPlayerResponse.playerAds`, `ytInitialPlayerResponse.adPlacements`, and `ytInitialPlayerResponse.adSlots` using property getters/setters and `JSON.parse` interception.
     - **Fetch & XHR Interception**: Intercept YouTube player API calls (`/youtubei/v1/player`, `/player`, `reel_watch_sequence`) and strip `adPlacements`, `playerAds`, `adSlots`, and `isAd` flags from the JSON response before resolution.
     - **Cosmetic CSS Injection**: Inject Brave's element-hiding selectors for YouTube and YouTube Music:
       - `.video-ads`, `.ytp-ad-module`, `.ytp-ad-overlay-container`, `ytd-ad-slot-renderer`, `ytd-in-feed-ad-layout-renderer`, `ytd-banner-promo-renderer`.
       - Dialog and promo suppression: `tp-yt-paper-dialog.ytmusic-popup-container:has-text(/Premium|ad-free|family plan/)`, `tp-yt-paper-dialog.ytd-popup-container`.
     - **Background Playback Preservation**:
       - Override `document.hidden = false` and `document.visibilityState = 'visible'`.
       - Trap `visibilitychange` events and stop immediate propagation.
       - Refresh `window._lact = Date.now()` every 5 minutes to prevent auto-pause idle dialogs.

2. **WebExtension Integration:**
   - Update `app/src/main/assets/musify_adblocker/manifest.json`:
     - Add match patterns: `*://*.youtube.com/*`, `*://*.youtube-nocookie.com/*`.
     - Register `youtube_adblock.js` at `document_start` with `all_frames: true`.
   - Update `app/src/main/assets/musify_adblocker/background.js`:
     - Block known YouTube ad and telemetry tracking endpoints:
       - `*://*.youtube.com/api/stats/ads*`
       - `*://*.youtube.com/pagead/*`
       - `*://*.youtube.com/ptracking*`
       - `*://*.doubleclick.net/*`
       - `*://googleads.g.doubleclick.net/*`
     - Strictly preserve legitimate media playback streams (`googlevideo.com/videoplayback`).

3. **Kotlin Engine & Fallback Harmonization:**
   - Update `MediaSafeResourcePolicy.kt` and `WebViewAdapter.kt` to identify YouTube ad requests and block tracking beacons when running under WebView fallback.

4. **Unit & Verification Testing:**
   - Author `app/src/test/java/com/musify/browser/Phase24YouTubeAdShieldTest.kt`:
     - Verify WebExtension manifest includes YouTube and YouTube Music content scripts.
     - Verify player response pruning rules (`ytInitialPlayerResponse.playerAds = undefined`).
     - Verify cosmetic selectors for `.video-ads`, `ytd-ad-slot-renderer`, and popup dialogs.
     - Verify background playback spoofing (`document.hidden`, `visibilitychange`, `_lact`).
     - Verify network filter blocks telemetry while preserving `videoplayback`.
   - Execute `./gradlew.bat testDebugUnitTest` across all test suites.

5. **Phase Completion & Deliverables:**
   - Author `.brain/.report/PHASE-24-REPORT.md`.
   - Create focused Git commit and push to remote repository.

## Required tests (run at phase end)
- [x] `Phase24YouTubeAdShieldTest` passes 100%.
- [x] Full test suite `./gradlew.bat testDebugUnitTest` passes 100% with zero regressions.
- [x] WebExtension manifest and assets validate successfully.

## Verification Gate
- [x] YouTube and YouTube Music player responses are sanitized before playback.
- [x] Video ad modules, banners, and popup dialogs are hidden via cosmetic CSS.
- [x] Background playback visibility spoofing and `_lact` keep-alive are operational.
- [x] Legitimate media playback streams (`videoplayback`) are never blocked.
- [x] `.brain/.report/PHASE-24-REPORT.md` documented with full test evidence.
- [x] Git commit pushed to remote repository.
