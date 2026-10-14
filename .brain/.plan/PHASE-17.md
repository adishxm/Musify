# Phase 17 — Privacy Shields, Playback Diagnostics & Security Sheets Redesign

**Status:** PLANNED  
**Planning duration:** 2–3 days  
**Goal:** Redesign the privacy controls, security configuration dialogs, and diagnostic sheets to align with the Obsidian design system. Crucially, retain and elevate all build variant diagnostics and Widevine media capability features implemented in Phases 11–13 while replacing legacy dialog layouts with sleek `MusifyCard`, `SettingRow`, `CountBadge`, and jewel-tone security accents.

## Scope and tasks
1. **Privacy Shield Bottom Sheet (`PrivacyShieldSheet.kt`):**
   - Redesign bottom sheet with `Surface` container, top drag handle, and `MusifyShapes.xl` (28dp) rounded corners.
   - Hero status card featuring shields master switch, live tracker block count formatted with `CountBadge` / `MusifyTypography_Numeric`, and `MusifyIcons.ShieldCheck` (with `Success` tint) / `MusifyIcons.ShieldOff` (with `Warning` tint).
   - Category toggles (Ads/Trackers, Third-Party Cookies, Script Blocking, Fingerprinting Protection) rendered via clean `SettingRow` items with vector icons.
2. **DNS Settings Dialog (`DnsSettingsDialog.kt`):**
   - Transform into an Obsidian modal dialog with `SurfaceRaised` card styling, `Hairline` borders, and `MusifyIcons.Bolt`.
   - Provider selection radio list (Cloudflare, Quad9, Google, Custom DoH) with `MusifyPill` latency badges and status chips.
3. **Site Permissions & Data Control Screens:**
   - Redesign `SitePermissionsScreen.kt` with permission categories (Camera, Mic, Location, Storage) using vector icons (`Location`, `Lock`, `Shield`).
   - Redesign `DataControlScreen.kt` (Cache, Cookies, History clearing, Storage usage) using `MusifyCard` and `StatRow`.
4. **Playback Compatibility & Media Diagnostics Dialog (`PlaybackCompatibilityDialog.kt`):**
   - **Feature Preservation:** Preserve all Phase 11–13 functionality:
     - `BuildVariantInfo.detect()` showing Installed Build Variant (Universal, ARM64, or Lite), active engine type, ABI, and SDK version.
     - Amber `protectedPlaybackLimitationNotice` banner explaining System WebView Widevine limitations when running on Lite.
     - Media capability states: Autoplay, DRM Probe (Widevine L1/L3 status), MediaSession, Scoped Origin Exception, Live Block Category, and Cookie Policy.
   - **Obsidian Restyling:**
     - Replace emojis with `MusifyIcons.Music`, `MusifyIcons.Warning`, `MusifyIcons.ShieldCheck`, `MusifyIcons.Desktop`, `MusifyIcons.Bolt`.
     - Use `MusifyCard` containers with subtle `Hairline` borders.
     - Highlight statuses using jewel tones: `Success` for verified active features, `Warning` for caution/exceptions, `Danger` for blocked/unsupported.
5. **Phase 17 Test Suite:**
   - Add `Phase17PrivacyDiagnosticsTest.kt` validating shield toggling state, DNS provider selection, and full compatibility dialog data binding (including `BuildVariantInfo` and `MediaCapabilityState`).

## Required tests (run at phase end)
- [ ] `./gradlew testDebugUnitTest` runs with 100% pass rate including `Phase17PrivacyDiagnosticsTest`.
- [ ] Verification that all Phase 11–13 diagnostic elements remain present and functional in `PlaybackCompatibilityDialog`.

## Verification Gate
- [ ] No regression in privacy shield toggling or media playback compatibility logic.
- [ ] All sheets and dialogs render in Obsidian theme without white flashes or legacy styling.
- [ ] Comprehensive `.brain/.report/PHASE-17-REPORT.md` generated with detailed evidence.
- [ ] Branch/commit created and pushed cleanly to GitHub `origin/main`.

## Deliverables
- Redesigned `PrivacyShieldSheet.kt`, `DnsSettingsDialog.kt`, `SitePermissionsScreen.kt`, `DataControlScreen.kt`, and `PlaybackCompatibilityDialog.kt`.
- Unit test suite `Phase17PrivacyDiagnosticsTest.kt`.
- `.brain/.report/PHASE-17-REPORT.md`.
