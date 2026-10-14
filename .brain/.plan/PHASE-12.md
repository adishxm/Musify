# Phase 12 — Sanitizing Media Protection & Disabling Invasive Content-Script Hacks

**Status:** PLANNED  
**Planning duration:** 2–3 days  
**Goal:** Eliminate destructive DOM and media tampering in `spotify_adblock.js` and `WebViewAdapter.kt` that corrupts or breaks audio playback on physical devices (such as seeking `currentTime = 9999`, forced muting, `playbackRate = 16.0`, artificial next clicks, and dangerous container traversal). Replace with clean, pure CSS and non-destructive DOM hiding for ads, "Open App", and "Install App" prompts, preserving native media element behavior.

## Scope and tasks
1. Audit and sanitize [spotify_adblock.js](file:///c:/Users/adity/OneDrive/Desktop/Musify/app/src/main/assets/musify_adblocker/spotify_adblock.js):
   - Remove aggressive media manipulation loops that alter `currentTime`, `volume`, `muted`, or `playbackRate`.
   - Ensure `HTMLMediaElement` methods and properties are never overridden or mutilated.
   - Remove destructive parent container traversal (`cur.parentElement`) that risks hiding legitimate Spotify content sections.
2. Maintain clean, pure CSS injection targeting:
   - Known ad banners, sponsored containers, leaderboard ads, and interstitial overlays.
   - "Open App", "Open in Spotify", "Install App", and "Get our free app" buttons and smart banners.
3. Keep click interception safe: block only genuine navigation to `spotify:`, `intent:`, `spotify.link`, and Play Store intent schemes without intercepting in-page playback actions.
4. Add a safety feature flag `isMediaTamperingEnabled` (defaulted to `false`) ensuring media manipulation is strictly disabled.
5. Mirror the sanitized rules into [WebViewAdapter.kt](file:///c:/Users/adity/OneDrive/Desktop/Musify/app/src/main/java/com/musify/browser/engine/WebViewAdapter.kt) fallback script.

## Required tests (run at phase end)
- [ ] Unit tests verifying that media manipulation logic is inactive by default.
- [ ] Content script verification tests asserting CSS rules only hide ad/app elements and preserve audio/video elements.
- [ ] Regression test ensuring legitimate in-browser playback controls (play, pause, seek, volume) function normally.
- [ ] Full regression suite run: `./gradlew testDebugUnitTest`.

## Verification Gate
- [ ] No `currentTime = 9999` or forced `playbackRate = 16.0` occurs on legitimate media elements.
- [ ] Audio/video elements are never forced into muted state by content scripts.
- [ ] Ads and app-switch buttons remain visually hidden without breaking Spotify page structure.
- [ ] All unit tests pass.

## Deliverables
- Sanitized `spotify_adblock.js` and `WebViewAdapter.kt`.
- Unit tests confirming non-destructive script behavior.
- Completed `.brain/.report/PHASE-12-REPORT.md`.
