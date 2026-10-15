# Phase 21 — Brave-Aligned Spotify Ad-Shield & Non-Destructive Audio Ad Suppressor

**Status:** PLANNED  
**Planning duration:** 1–2 days  
**Goal:** Analyze Brave Browser's ad-blocking architecture from `nottopushongit/com.brave.browser_*.apkm` (native `adblock-rust`, component-driven filter lists, cosmetic stylesheet injection, and scriptlet defusing) and implement a hardened, Brave-inspired multi-tier shield for `open.spotify.com` in Musify. Deliver advanced cosmetic ad hiding, an intelligent non-destructive audio ad suppressor (auto-mute & auto-skip during commercials without media element destruction), modal/nag dismissal, and intent scheme sandboxing.

## Scope and tasks
1. **Brave Architecture & Tech Analysis:**
   - Audit Brave APK assets (`brave_resources.pak`, `libchrome.so`, `adblock-rust` pipeline, component updater).
   - Document how Brave Shields separates network blocking from cosmetic filtering and why hard-blocking `spclient.wg.spotify.com` triggers Spotify's anti-adblock "Playback Paused" freeze.
2. **Hardened Content Shield (`spotify_adblock.js`):**
   - Inject comprehensive Brave-aligned cosmetic stylesheets hiding:
     - `[data-testid="ad-companion-banner"]`, `[data-testid="ad-iframe"]`, `[data-testid="interstitial-ad"]`.
     - `[aria-label="Advertisement"]`, `.Root__ads-container`, `div[data-testid="advertisement-row"]`.
     - `aside[aria-label="Upgrade to Premium"]`, `div[data-test="upsell-container"]`, `.upsell-banner`.
     - `[class*="AdsContainer"]`, `[aria-label="Sponsored"]`.
   - Implement **Intelligent Non-Destructive Audio Ad Suppressor**:
     - Monitor DOM and player state for active advertisement tracks (`[data-testid="track-info-advertiser"]`, track title "Advertisement", or aria-label containing "Advertisement").
     - When an ad is detected: safely mute `HTMLMediaElement` (`media.muted = true`) so the user hears zero commercials.
     - Detect and safely trigger forward skip (`[data-testid="control-button-skip-forward"]`) if clickable.
     - As soon as the legitimate track resumes: smoothly restore user audio (`media.muted = false`).
     - Strictly avoid media clock tampering (`currentTime = 9999`, forced `playbackRate = 16.0`) to protect Widevine CDM hardware decryption.
   - Modal and App-Nag Suppression:
     - Auto-dismiss "Get Premium", "Upgrade", "Install App" dialogs.
     - Block navigation to `spotify:`, `intent:`, `spotify.link`, and Play Store intent schemes.
3. **WebExtension & Engine Manifest Parity:**
   - Update `app/src/main/assets/musify_adblocker/manifest.json`:
     - Set `run_at: "document_start"`.
     - Ensure matching on `*://*.spotify.com/*`.
4. **Unit & Verification Testing:**
   - Author `app/src/test/java/com/musify/browser/Phase21BraveSpotifyAdBlockTest.kt` asserting:
     - Existence and syntax of `spotify_adblock.js` and `manifest.json`.
     - Presence of Brave-aligned cosmetic selectors.
     - Audio ad detection markers and non-destructive mute controller logic.
     - Prohibition of media clock tampering keywords.
     - Playback control exemption and intent blocking.
5. **Phase Completion & Deliverables:**
   - Run `./gradlew testDebugUnitTest`.
   - Author `.brain/.report/PHASE-21-REPORT.md`.
   - Commit and push to Git (`feat(phase-21): brave-aligned spotify ad-shield and non-destructive audio ad suppressor`).

## Required tests (run at phase end)
- [ ] `Phase21BraveSpotifyAdBlockTest` passes 100%.
- [ ] Full project test suite passes: `./gradlew testDebugUnitTest`.
- [ ] Zero regression in Phase 12 media protection sanitization rules.

## Verification Gate
- [ ] All visual ad and upgrade banners on `open.spotify.com` are hidden via CSS.
- [ ] Audio ads are automatically muted and restored when normal music plays.
- [ ] No `currentTime = 9999` or forced `playbackRate = 16.0` tampering exists.
- [ ] `.brain/.report/PHASE-21-REPORT.md` documented with full test evidence.
- [ ] Focused Git commit pushed to remote repository.
