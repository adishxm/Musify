# Phase 10 — Desktop Content Mode, Protected Media & Playback Recovery

**Planning duration:** 2–3 weeks (estimate, not a promise)  
**Goal:** Enable desktop content mode, media/DRM capability handling, and scoped playback recovery so complex web media applications (e.g., Spotify Web Player at open.spotify.com) run reliably without compromising universal privacy protections.

## Scope and tasks
1. Add `ContentMode` (`MOBILE`, `DESKTOP`) to tab state and implement persistent per-origin content mode storage (`SiteContentModeRepository`).
2. Implement desktop viewport, user-agent configuration, and desktop mode toggles in `GeckoEngineAdapter` (`USER_AGENT_MODE_DESKTOP`, `VIEWPORT_MODE_DESKTOP`, and desktop UA override) and `WebViewAdapter` (`useWideViewPort`, `loadWithOverviewMode`, and desktop UA).
3. Implement `GeckoSession.PermissionDelegate` in `GeckoEngineAdapter` to allow `PERMISSION_MEDIA_KEY_SYSTEM_ACCESS`, `PERMISSION_AUTOPLAY_AUDIBLE`, `PERMISSION_AUTOPLAY_INAUDIBLE`, `PERMISSION_STORAGE_ACCESS`, and `PERMISSION_PERSISTENT_STORAGE`.
4. Update `GeckoRuntimeSettings` content blocking cookie behavior to `ACCEPT_NON_TRACKERS` to preserve required first-party media CDN, session token, and license requests while isolating cross-site trackers.
5. Define `MediaCapabilityState` on `EngineAdapter` and `TabsViewModel` to expose live media/DRM, private-mode, and content-mode diagnostics.
6. Build a user-facing **Playback Compatibility & Recovery** UI and top-level toolbar **Site Mode (Desktop/Mobile)** toggle to allow transparent, one-tap recovery on sites like Spotify without global privacy bypasses.
7. Implement request/origin classification ensuring media licensing, audio streams, and first-party CDNs are never blocked by ad filters.

## Required tests (run at phase end)
- [ ] Unit tests for `ContentMode` persistence, tab state isolation, and reducer transitions.
- [ ] Engine adapter tests verifying `setContentMode` applies correct user agent and viewport mode in both GeckoView and WebView.
- [ ] Permission delegate tests verifying media key system access and autoplay permissions are granted.
- [ ] Playback compatibility state tests verifying correct reporting of private mode, desktop mode, and media capabilities.
- [ ] Regression test verifying privacy shields and origin exceptions remain isolated across tabs.

## PASS gate — all must be true
- [ ] Desktop mode is visible, reversible, and isolated per-origin without disabling shields.
- [ ] `PERMISSION_MEDIA_KEY_SYSTEM_ACCESS` and audio autoplay requests are cleanly handled by the engine adapter.
- [ ] Spotify web player (`open.spotify.com`) receives desktop presentation and does not hit the mobile playback disabled wall.
- [ ] No hardcoded domain bypasses or blanket SSL compromises are introduced.
- [ ] All unit tests pass in `./gradlew.bat testDebugUnitTest`.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-10-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/10-desktop-mode-and-protected-media-playback`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-10): desktop content mode & protected media recovery`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-10-REPORT.md`.
- Test evidence verifying desktop mode switching and media capability handling.
