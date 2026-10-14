# Phase index

- [Phase 00 — Repository & project setup](PHASE-00.md): 1–2 days. Set up the Android project, build tools, Git workflow, and this .brain planning/report structure.
- [Phase 01 — Browser engine feasibility spike](PHASE-01.md): 1–2 weeks. Choose GeckoView Stable as the production candidate, then validate its current artifacts and core behavior before building on it.
- [Phase 02 — Single-tab browser shell](PHASE-02.md): 1–2 weeks. Build the first user-visible browser flow around one safe tab.
- [Phase 03 — Tabs, lifecycle & session recovery](PHASE-03.md): 2–3 weeks. Support multiple browsing tabs and recover normal tabs after app/process restarts.
- [Phase 04 — History, bookmarks & private tabs](PHASE-04.md): 2–3 weeks. Add local browsing utility features and a defensible private-tab lifecycle.
- [Phase 05 — Privacy protections & shield controls](PHASE-05.md): 2–3 weeks. Enable supported general-purpose engine protections and make behavior visible and reversible.
- [Phase 06 — Filter-list pipeline & licensing gate](PHASE-06.md): 2–4 weeks. Evaluate external lists only after confirming engine integration, license obligations, and a safe update/rollback path.
- [Phase 07 — Browser essentials: downloads, permissions & safety](PHASE-07.md): 2–3 weeks. Complete useful browser interactions while minimizing unsafe privileges.
- [Phase 08 — Security, privacy, accessibility & performance hardening](PHASE-08.md): 3–4 weeks. Run a structured release-readiness cycle across browser security, data, usability, and device performance.
- [Phase 09 — Beta, documentation & release decision](PHASE-09.md): 2–4 weeks. Prepare a controlled beta or personal signed build with reproducible evidence; public release is optional and gated.
- [Phase 10 — Desktop Content Mode, Protected Media & Playback Recovery](PHASE-10.md): 2–3 weeks. Enable desktop content mode, media/DRM capability handling, and scoped playback recovery for web media apps like Spotify.
- [Phase 11 — Build Variant & Protected Media Capability Transparency](PHASE-11.md): 2–3 days. Provide full transparency on build variant, engine type, and exact Widevine status; correct inflated claims.
- [Phase 12 — Sanitizing Media Protection & Disabling Invasive Content-Script Hacks](PHASE-12.md): 2–3 days. Eliminate destructive DOM and media tampering, ensuring native media elements are never mutilated.
- [Phase 13 — Real Engine Policy Isolation & Media-Safe Protection Architecture](PHASE-13.md): 3–5 days. Implement true engine-level policy isolation and media-safe request preservation for EME/license flows.
- [Phase 14 — Real-Device Spotify Verification & Diagnostic Test Hardening](PHASE-14.md): 3–5 days. Execute end-to-end verification, compile all three APK variants, and harden real-device testing evidence.
