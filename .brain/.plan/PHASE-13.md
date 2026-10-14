# Phase 13 — Real Engine Policy Isolation & Media-Safe Protection Architecture

**Status:** PLANNED  
**Planning duration:** 3–5 days  
**Goal:** Implement true engine-level policy isolation so that relaxing shields for an origin (or activating Desktop mode) actually updates GeckoView's session policies and live blocking decisions without polluting global engine state or other tabs. Guarantee that first-party media CDN, authentication tokens, cookies, service workers, WebSockets, and Widevine license flows are never blocked or broken.

## Scope and tasks
1. Wire `setOriginException()` in [GeckoEngineAdapter.kt](file:///c:/Users/adity/OneDrive/Desktop/Musify/app/src/main/java/com/musify/browser/engine/GeckoEngineAdapter.kt) to actively adjust the session's content blocking policy, cookie behavioral policy, and request delegates rather than mutating only an internal flag.
2. Establish a media-safe resource classification policy:
   - Always allow top-level first-party document and application resources.
   - Always allow Widevine license acquisition requests (`/license`, `widevine`, `drm`, `manifest`) and media delivery segments.
   - Preserve first-party authentication credentials, session cookies, service workers, and WebSockets.
3. Decouple origin exceptions across tabs:
   - Verify that relaxing shields on `open.spotify.com` in Tab A does not relax shields for `example.com` or for other tabs.
4. Normalize origins consistently (`https://open.spotify.com`) and enforce that exceptions expire or persist according to explicit user selection.
5. Provide live decision metadata to the playback diagnostics layer:
   - Report whether any blocker rule intercepted a request on the current page, displaying the rule category without logging private query tokens, credentials, or license payloads.

## Required tests (run at phase end)
- [ ] Unit tests verifying that `setOriginException` applies to the active session and preserves media/license requests.
- [ ] Tab isolation tests asserting origin exceptions are strictly bounded per-tab/per-origin.
- [ ] Media safety tests asserting DRM/license challenge URLs are never classified as trackers.
- [ ] Full regression suite run: `./gradlew testDebugUnitTest`.

## Verification Gate
- [ ] Origin exceptions cleanly affect engine session behavior without mutating global runtime defaults.
- [ ] Cross-tab shield state isolation is verified by automated tests.
- [ ] DRM/license endpoints are explicitly preserved.
- [ ] All unit tests pass.

## Deliverables
- Updated `GeckoEngineAdapter.kt` and `TabsViewModel.kt` with live policy isolation.
- Media-safe policy definitions and tests.
- Completed `.brain/.report/PHASE-13-REPORT.md`.
