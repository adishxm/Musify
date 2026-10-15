# Phase 22 — Spotify Network Shield Harmonization, Anti-Freeze Mocking & Multi-Engine Packaging

**Status:** PLANNED  
**Planning duration:** 1–2 days  
**Goal:** Align Musify's engine-level network request filtering in `WebViewAdapter.kt` and `MediaSafeResourcePolicy.kt` with Brave Browser's network filtering methodology. Block tracker/telemetry subdomains cleanly, eliminate the anti-adblock "Playback Paused" freeze by synthesizing clean 200 OK empty JSON `{}` payloads for `spclient.wg.spotify.com/ads` and `/ad-logic`, preserve Widevine DRM license acquisition routes, verify the full regression test suite, and rebuild all three multi-architecture release APK variants.

## Scope and tasks
1. **Network Filtering Harmonization:**
   - Update `WebViewAdapter.kt` and `MediaSafeResourcePolicy.kt`:
     - Cleanly drop telemetry and ad servers: `adeventtracker.spotify.com`, `adstudio.spotify.com`, `pixel.spotify.com`, `analytics.spotify.com`, `crashlytics.com`, `doubleclick.net`.
     - Implement **Anti-Freeze Synthetic Response Engine**: For endpoints like `spclient.wg.spotify.com/ads` and `spclient.wg.spotify.com/ad-logic`, return a synthetic `200 OK` with `application/json` and body `{}` instead of connection dropping / 204 resets that trigger Spotify's anti-adblock freeze loop.
     - Strictly safeguard Widevine DRM license routes: `spclient.wg.spotify.com/widevine-license`, `/license`, `/drm/`, and CDN audio segments.
2. **Unit & Verification Testing:**
   - Author `app/src/test/java/com/musify/browser/Phase22SpotifyNetworkShieldTest.kt`:
     - Test that ad and telemetry domains are classified as ads/trackers.
     - Test that synthetic 200 OK empty JSON is generated for Spotify ad-logic/ads endpoints.
     - Test that Widevine DRM and audio stream requests are preserved.
     - Test that origin exceptions and first-party endpoints function properly.
3. **Multi-Variant Release Packaging:**
   - Compile and package all 3 release APK variants:
     - `Musify.apk` (Universal release build).
     - `Musify-ARM64.apk` (GeckoView release build).
     - `Musify-Lite.apk` (WebView release build).
   - Verify signatures via `apksigner` and catalog SHA256 checksums.
4. **Phase Completion & Deliverables:**
   - Run `./gradlew testDebugUnitTest` across all 23 test suites.
   - Author `.brain/.report/PHASE-22-REPORT.md`.
   - Commit and push to Git (`feat(phase-22): spotify network shield harmonization, anti-freeze mocking and multi-engine packaging`).

## Required tests (run at phase end)
- [ ] `Phase22SpotifyNetworkShieldTest` passes 100%.
- [ ] Full test suite `./gradlew testDebugUnitTest` passes 100%.
- [ ] All 3 release APK variants compiled, verified, and cataloged.

## Verification Gate
- [ ] No anti-adblock playback freeze occurs on Spotify ad-logic requests.
- [ ] DRM license and media streams remain completely unblocked.
- [ ] Full test suite passes with zero regressions.
- [ ] `.brain/.report/PHASE-22-REPORT.md` documented with full test evidence.
- [ ] Focused Git commit pushed to remote repository.
