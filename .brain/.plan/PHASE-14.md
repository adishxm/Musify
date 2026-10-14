# Phase 14 — Real-Device Spotify Verification & Diagnostic Test Hardening

**Status:** PLANNED  
**Planning duration:** 3–5 days  
**Goal:** Build the complete end-to-end verification and regression suite. Build and package all three release APK variants (`Musify.apk`, `Musify-ARM64.apk`, `Musify-Lite.apk`), execute automated tests, verify on live Android devices/emulators, and produce comprehensive documentation reflecting physical-device compatibility boundaries and release readiness.

## Scope and tasks
1. Rebuild and verify all three APK variants:
   - `Musify.apk` (~153 MB Universal GeckoView build).
   - `Musify-ARM64.apk` (~84 MB 64-bit GeckoView build).
   - `Musify-Lite.apk` (~8.4 MB Ultra-lightweight System WebView build).
2. Execute the complete automated unit and integration test suite across all 14 phases.
3. Validate runtime behavior on `emulator-5554`:
   - Inspect diagnostics dialog showing engine, ABI, API level, and build variant.
   - Verify that Lite build displays the WebView Widevine informational badge.
   - Verify that GeckoView build exposes measured Widevine probe status.
   - Verify navigation to Spotify Web, desktop mode switching, and clean UI without app install popups or broken sections.
4. Document the real-device test matrix, physical-device testing boundaries, and exact verification results in `.brain/.report/PHASE-14-REPORT.md`.
5. Update repository `README.md` and phase roadmap files with the completed, verified state.

## Required tests (run at phase end)
- [ ] `./gradlew testDebugUnitTest` runs with 100% passing tests across all test suites.
- [ ] Release APK assembly and signature verification for all three APK variants.
- [ ] Binary inspection asserting correct ABIs, file sizes, and stripped assets per variant.
- [ ] Live UI verification capture on emulator/device.

## Verification Gate
- [ ] All unit tests pass with zero regressions.
- [ ] All three APKs are cleanly built and signed.
- [ ] Comprehensive `.brain/.report/PHASE-14-REPORT.md` is authored with evidence.
- [ ] Repository is pushed cleanly to GitHub `origin/main`.

## Deliverables
- Three verified release APKs: `Musify.apk`, `Musify-ARM64.apk`, `Musify-Lite.apk`.
- Completed `.brain/.report/PHASE-14-REPORT.md`.
- Updated documentation and clean Git history.
