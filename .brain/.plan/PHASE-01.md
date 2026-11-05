**Status:** COMPLETED  
**Planning duration:** 1–2 weeks (estimate, not a promise)  
**Goal:** Choose GeckoView Stable as the production candidate, then validate its current artifacts and core behavior before building on it.

## Scope and tasks
1. Check official GeckoView Stable and Mozilla Android Components release/dependency instructions at implementation time; pin exact versions.
2. Create a temporary EngineAdapter boundary so UI code is not tied directly to the engine implementation.
3. Load a small corpus of unrelated sites and a locally controlled test page. Exercise navigation, forms, JavaScript, media, permissions, downloads, and process loss.
4. On two Android API/device profiles, record APK size, startup, memory, renderer behavior, and the currently supported content-blocking APIs.
5. Keep WebView as a bounded comparison/fallback spike. Do not fork Chromium. Decide based on observed test evidence, not assumptions.

## Required tests (run at phase end)
- [x] Clean Gradle build with pinned engine dependencies.
- [x] Engine smoke test: HTTPS navigation, back/forward, JavaScript form submit, media test fixture, and download callback.
- [x] Process/renderer loss test: show recoverable error/reopen state without reload loop.
- [x] Run on two API/device profiles; save versions and observations.
- [x] Verify dependency licenses and source notices.

## PASS gate — all must be true
- [x] Engine loads representative unrelated sites and core browser events are observable.
- [x] Security gate: invalid TLS is cancelled; no blanket SSL proceed option or arbitrary JS-native bridge.
- [x] Engine choice and limitations are written in report.
- [x] If GeckoView fails a critical requirement, report concrete evidence and choose WebView fallback only after documenting limitations.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-01-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/01-browser-engine-feasibility-spike`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-01): browser engine feasibility spike`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-01-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
