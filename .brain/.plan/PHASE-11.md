# Phase 11 — Build Variant & Protected Media Capability Transparency

**Status:** IN_PROGRESS  
**Planning duration:** 2–3 days  
**Goal:** Provide full transparency into the running build variant (Universal GeckoView, ARM64 GeckoView, or Lite WebView), engine type, and exact Widevine CDM status (`UNKNOWN`, `SUPPORTED`, `UNSUPPORTED`, `FAILED`). Clearly communicate in the UI and README that `Musify-Lite` (System WebView) does not provide Widevine CDM for protected media, directing users to the full GeckoView builds for protected playback. Soften and correct all release claims to reflect verified reality.

## Scope and tasks
1. Define a robust `BuildVariantInfo` and `RuntimeDiagnostics` data model providing:
   - Variant identity: `Musify-Universal` (GeckoView), `Musify-ARM64` (GeckoView), or `Musify-Lite` (System WebView).
   - Engine type (`GeckoView` vs `WebView`) and pinned engine version.
   - Host Android OS API level, manufacturer, model, and primary ABI.
   - Measured Widevine CDM capability state (`UNKNOWN`, `SUPPORTED`, `UNSUPPORTED`, `FAILED`) rather than hardcoded boolean flags.
2. Update `PlaybackCompatibilityDialog` and `ReleaseInfoScreen` to display this detailed diagnostic breakdown.
3. In `Musify-Lite` (WebView mode), display an explicit, non-misleading informational badge:
   - *"Android System WebView does not bundle Widevine CDM. For protected web media streaming (e.g., Spotify Web Player), install the full GeckoView build (Musify.apk or Musify-ARM64.apk)."*
4. Soften and correct all over-promising release claims in `README.md` and in-app documentation:
   - Replace "Spotify Web that actually plays" with "Spotify Web compatibility is under device-specific validation. Full GeckoView builds are candidates for protected playback; Lite WebView build does not bundle Widevine DRM. Musify does not bypass DRM."
5. Implement unit tests verifying `BuildVariantInfo` resolution, capability status states, and honesty checks.

## Required tests (run at phase end)
- [ ] Unit tests for `BuildVariantInfo` detection and diagnostic data formatting.
- [ ] Unit tests verifying `CapabilityStatus` transitions (`UNKNOWN`, `SUPPORTED`, `UNSUPPORTED`, `FAILED`) without hardcoded true booleans.
- [ ] Diagnostic UI model test asserting Lite build exposes the WebView Widevine limitation notice.
- [ ] Full regression suite run: `./gradlew testDebugUnitTest`.

## Verification Gate
- [ ] Build variant and engine type are prominently visible in diagnostics.
- [ ] Lite build explicitly communicates the Widevine CDM absence instead of masking it.
- [ ] `README.md` accurately reflects testing status and engine distinctions.
- [ ] All unit tests pass.

## Deliverables
- `BuildVariantInfo` and updated `PlaybackDiagnostics` model.
- Updated `PlaybackCompatibilityDialog` and `ReleaseInfoScreen`.
- Honest documentation in `README.md`.
- Completed `.brain/.report/PHASE-11-REPORT.md`.
