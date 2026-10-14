# Phase 20 — Minimalist Entry Launch Screen, Full System Verification & Multi-Variant Release Assembly

**Status:** PLANNED  
**Planning duration:** 1–2 days  
**Goal:** Implement the approved minimalist `WelcomeScreen` (flat `#242424` launch surface with two-line Inter typography), retire legacy `WelcomeMatrixScreen`, update `MainActivity`, synchronize design showcase documentation and visual assets, run the comprehensive 142+ test suite, package all three release APK variants, install and verify on the running emulator, and push the final package to the repository.

## Scope and tasks
1. **Welcome Screen Architecture Replacement:**
   - Delete legacy `WelcomeMatrixScreen.kt` and obsolete `WelcomeMatrixTest.kt`.
   - Implement `WelcomeScreen.kt` in `ui/screens/`:
     - Flat `#242424` (`WelcomeSurface`) background.
     - Two lines of type at 30% screen height: `welcome_hello` ("Welcome to") and `welcome_musify_title` ("Musify").
     - Credit line at 86.5% screen height: `welcome_crafted_by` ("crafted by adishxm").
     - Full screen clickable tap target without button clutter.
     - Smooth fade-in animation (620ms tween).
   - Update `MainActivity.kt`:
     - Transition `ScreenState.WELCOME_MATRIX` -> `ScreenState.WELCOME`.
     - Mount `WelcomeScreen(onEnter = { ... })`.
2. **Design System Documentation & Asset Synchronization:**
   - Synchronize `docs/UI-DESIGN-SYSTEM.md` with the latest tokens and component details.
   - Copy visual showcase assets (`design/screens/*`, `design/brand/*`, `design/reference/*`) into `docs/`.
   - Update `Phase18SystemCompletionTest.kt` to guard against old welcome screen references and raw `0xFF` literals.
   - Add `WelcomeScreenTest.kt` covering launch state, navigation trigger, string resources, and `WelcomeSurface` token assertions.
3. **Automated Verification:**
   - Run the complete project test suite across all 20+ test classes (`./gradlew testDebugUnitTest`).
   - Confirm 100% pass rate (142+ passing tests).
4. **Multi-Architecture Release APK Packaging:**
   - Rebuild and package all 3 production release APKs:
     - `Musify.apk` (Universal Multi-ABI release build).
     - `Musify-ARM64.apk` (ARM64 release build).
     - `Musify-Lite.apk` (System WebView release build).
   - Verify internal dex files, sizes, and compute SHA256 checksums.
5. **Live Verification on Emulator:**
   - Install the updated `Musify.apk` on `emulator-5554`.
   - Launch app and capture live screenshot of `WelcomeScreen` and the new floating `MusifyActionBar`.
6. **Deliverables & Report:**
   - Author `.brain/.report/PHASE-20-REPORT.md`.
   - Commit and push to GitHub `origin/main`.

## Required tests (run at phase end)
- [ ] `WelcomeScreenTest` (6 tests) passes.
- [ ] `./gradlew testDebugUnitTest` passes 100% with >= 142 tests.
- [ ] All 3 APKs compiled and verified with SHA256 checksums.
- [ ] Live emulator verification with screenshot evidence.

## Verification Gate
- [ ] `WelcomeScreen.kt` replaces `WelcomeMatrixScreen.kt` cleanly.
- [ ] Zero build warnings or test regressions.
- [ ] All 3 release APKs verified and tracked via Git LFS.
- [ ] `.brain/.report/PHASE-20-REPORT.md` generated with full evidence.
- [ ] Final commit pushed to GitHub `origin/main`.
