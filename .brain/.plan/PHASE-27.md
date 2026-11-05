# Phase 27 — Universal Privacy Shields Harmonization & Multi-Variant Release Assembly

**Status:** PLANNED  
**Planning duration:** 1–2 days  
**Goal:** Harmonize all multi-tiered privacy shields across Spotify, YouTube, YouTube Music, SoundCloud, and general web browsing into a unified, high-performance shielding engine. Ensure full parity across GeckoView (Universal & ARM64) and WebView (Lite) engine variants, compile all release APKs, verify signature integrity with `apksigner`, catalog SHA-256 checksums, and execute live emulator regression testing.

## Scope and tasks
1. **Shield Harmonization & Unified Shield Controller:**
   - Verify unified operation of WebExtension background blockers, content script proxies, Kotlin-level `MediaSafeResourcePolicy.kt`, and `TrackingParamSanitizer.kt`.
   - Ensure media playback across Spotify (Widevine DRM), YouTube (SABR / DASH), and SoundCloud runs uninterrupted without ads or anti-adblock freezes.
   - Verify zero destructive clock tampering and 100% audio playback fidelity.

2. **Full Test Suite Execution & Hardening:**
   - Run complete suite of 27 unit test classes covering Phases 00 to 27.
   - Assert 100% pass rate with zero flaky or skipped tests.

3. **Multi-Variant Release Packaging:**
   - Build all 3 release APK variants:
     - `Musify.apk` (Universal release build).
     - `Musify-ARM64.apk` (GeckoView release build).
     - `Musify-Lite.apk` (WebView release build).
   - Sign, zipalign, and verify with `apksigner`.
   - Calculate and record SHA-256 digests in report.

4. **Live Emulator Testing:**
   - Install release APK on `emulator-5554`.
   - Verify playback on Spotify Web and YouTube Music Web.

5. **Phase Completion & Deliverables:**
   - Author `.brain/.report/PHASE-27-REPORT.md`.
   - Create Git commit and push to remote.

## Required tests (run at phase end)
- [ ] Complete test suite `./gradlew.bat testDebugUnitTest` passes 100%.
- [ ] All 3 release APKs successfully built and verified with `apksigner`.
- [ ] Live emulator verification confirms ad-free playback.

## Verification Gate
- [ ] Seamless ad-free playback on Spotify, YouTube, and YouTube Music.
- [ ] Clean build and packaging for all three release APK variants.
- [ ] `.brain/.report/PHASE-27-REPORT.md` documented with full test evidence.
- [ ] Git commit pushed to remote repository.
