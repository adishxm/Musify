# Phase 10 — Desktop Content Mode, Protected Media & Playback Recovery

**Status:** IMPLEMENTATION_VERIFIED / PHYSICAL_DEVICE_PLAYBACK_UNVERIFIED (Honest Baseline per ROADMAP-SPOTIFY-WEB-COMPATIBILITY.md)  
**Planning duration:** 2–3 weeks (estimate, not a promise)  
**Goal:** Enable desktop content mode, media/DRM capability handling, and scoped playback recovery so complex web media applications (e.g., Spotify Web Player at open.spotify.com) run reliably without compromising universal privacy protections or masking DRM limitations.

## Scope and tasks
1. Add `ContentMode` (`MOBILE`, `DESKTOP`) to tab state and implement persistent per-origin content mode storage (`SiteContentModeRepository`).
2. Implement desktop viewport, user-agent configuration, and desktop mode toggles in `GeckoEngineAdapter` (`USER_AGENT_MODE_DESKTOP`, `VIEWPORT_MODE_DESKTOP`, and desktop UA override) and `WebViewAdapter` (`useWideViewPort`, `loadWithOverviewMode`, and desktop UA).
3. Implement `GeckoSession.PermissionDelegate` in `GeckoEngineAdapter` with real hardware `MediaDrm` Widevine probe (`edef8ba9-79d6-4ace-a3c8-27dcd51d21ed`) and `CapabilityStatus` (`UNKNOWN`, `SUPPORTED`, `UNSUPPORTED`, `FAILED`).
4. Decouple shared `GeckoRuntimeSettings` mutation from `setProtectionEnabled()` to prevent cross-session/cross-tab policy contamination.
5. Update `GeckoRuntimeSettings` content blocking cookie behavior to `ACCEPT_NON_TRACKERS` to preserve required first-party media CDN, session token, and license requests while isolating cross-site trackers.
6. Replace hardcoded `spotify.com` / `scdn.co` substring bypasses in `WebViewAdapter` with generic first-party and main-frame resource preservation.
7. Implement AdGuard DNS companion layer (`DnsSettingsRepository`, `DnsSettingsDialog`) utilizing Android Private DNS (`Settings.ACTION_PRIVATE_DNS_SETTINGS`), strictly separating DNS resolution status from in-page Shields blocking counters.
8. Build a user-facing **Playback Compatibility & Recovery** UI and top-level toolbar **Site Mode (Desktop/Mobile)** toggle to allow transparent, one-tap recovery on sites like Spotify without global privacy bypasses.
9. Report measured EME, Widevine, DRM provider, engine details, content mode, and DNS status transparently.

## Required tests (run at phase end)
- [x] Unit tests for `ContentMode` persistence, tab state isolation, and reducer transitions.
- [x] Engine adapter tests verifying `setContentMode` applies correct user agent and viewport mode in both GeckoView and WebView.
- [x] Permission delegate and capability tests verifying media key system access and `CapabilityStatus` transitions.
- [x] Playback compatibility state tests verifying correct reporting of private mode, desktop mode, and media capabilities.
- [x] Regression test verifying privacy shields and origin exceptions remain isolated across tabs.
- [x] Unit tests for `DnsSettingsRepository` and layer segregation from browser Shields.

## Verification Gate — Baseline Truth
- [x] Desktop mode is visible, reversible, and isolated per-origin without disabling shields.
- [x] `PERMISSION_MEDIA_KEY_SYSTEM_ACCESS` and audio autoplay requests are cleanly handled by the engine adapter.
- [x] Software contracts for desktop mode, session persistence, and scoped exceptions are verified by automated tests.
- [x] Measured `CapabilityStatus` (`UNKNOWN`, `SUPPORTED`, `UNSUPPORTED`, `FAILED`) is exposed instead of hardcoded `true` flags.
- [x] No hardcoded single-site domain bypasses or blanket SSL compromises are introduced.
- [x] All unit tests pass in `./gradlew.bat testDebugUnitTest`.
- [ ] **Physical Device Audible Playback Gate:** Verification of actual audible audio output upon clicking the Spotify play button on physical Android hardware with Widevine Level 1/3 CDM (Tracked in Roadmap Phase 09 / Phase 12).

## Deliverables
- Working phase increment in the Android project.
- Truthful `.brain/.report/PHASE-10-REPORT.md` reflecting `IMPLEMENTATION_VERIFIED / PHYSICAL_DEVICE_PLAYBACK_UNVERIFIED`.
- Test evidence verifying desktop mode switching, DNS separation, and measured media capability handling.
