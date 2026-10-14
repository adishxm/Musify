# Phase 18 — App Screens, Monochrome Welcome Matrix, Assets & Release Packaging

**Status:** PLANNED  
**Planning duration:** 2–3 days  
**Goal:** Complete the remaining application screens under the Obsidian design system, transform the intro/welcome matrix into a refined monochrome bloom aesthetic, integrate design documentation and visual assets, verify the full test suite, assemble the three release APK variants, and push the final package to the repository.

## Scope and tasks
1. **Home & Utility Screens:**
   - Redesign `LandingScreen.kt`: Hero emblem, monochrome search pill, speed dials in `MusifyCard` with vector icons, and aggregated privacy stats counter.
   - Redesign `DownloadsScreen.kt`: Empty state with `MusifyIcons.Download`, file cards with `MusifyIcons.Document`, progress indicators, and share/delete actions.
   - Redesign `HistoryBookmarksScreen.kt`: Chronological history cards with `MusifyIcons.Clock`, bookmark list with `MusifyIcons.StarFilled`, and quick-open actions.
   - Redesign `FilterListScreen.kt`: Filter list subscriptions with `CountBadge` rule counts and update timestamps.
   - Redesign `ReleaseInfoScreen.kt`: Version info, changelog cards, and license notices.
   - Redesign `OnboardingDialog.kt`: Welcome walkthrough with high-contrast typography and `MusifyPill` quick setup.
2. **Monochrome Welcome Matrix Redesign (`WelcomeMatrixScreen.kt`):**
   - Replace the legacy bright orange-red gradient background with a pure monochrome black background (`Ink`) featuring subtle radial white bloom effects.
   - Refine text styling to use Inter variable typography, `MusifyIcons.Emblem`, and smooth fade-in animations.
3. **Design Documentation & Showcase Assets:**
   - Copy `UI-DESIGN-SYSTEM.md` and screenshot overview assets from the extracted workspace into `docs/`.
4. **Comprehensive Test Suite & APK Packaging:**
   - Run the complete project test suite across all 18 phases.
   - Clean and assemble all 3 release APKs:
     - `Musify.apk` (Universal release build).
     - `Musify-ARM64.apk` (ARM64 release build).
     - `Musify-Lite.apk` (System WebView release build).
5. **Phase 18 Report & Push:**
   - Author `.brain/.report/PHASE-18-REPORT.md` documenting test results, APK builds, and visual verification.
   - Push commit to GitHub `origin/main`.

## Required tests (run at phase end)
- [ ] `./gradlew testDebugUnitTest` runs with 100% pass rate across the entire test suite.
- [ ] Assembly of all 3 release APK variants succeeds without errors.
- [ ] Verification of APK checksums, sizes, and file structures.

## Verification Gate
- [ ] Every single user-facing screen conforms to the Obsidian Design System.
- [ ] Zero emojis remain in the production codebase.
- [ ] All three release APKs are updated, verified, and ready for deployment.
- [ ] Comprehensive `.brain/.report/PHASE-18-REPORT.md` generated with detailed evidence.
- [ ] Final commit pushed cleanly to GitHub `origin/main`.

## Deliverables
- Fully redesigned application screens: `LandingScreen.kt`, `DownloadsScreen.kt`, `HistoryBookmarksScreen.kt`, `FilterListScreen.kt`, `ReleaseInfoScreen.kt`, `OnboardingDialog.kt`, `WelcomeMatrixScreen.kt`.
- Design system documentation in `docs/UI-DESIGN-SYSTEM.md`.
- 3 updated release APKs: `Musify.apk`, `Musify-ARM64.apk`, `Musify-Lite.apk`.
- Completed `.brain/.report/PHASE-18-REPORT.md`.
