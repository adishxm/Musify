# Phase 05 — Privacy protections & shield controls

**Planning duration:** 2–3 weeks (estimate, not a promise)  
**Goal:** Enable supported general-purpose engine protections and make behavior visible and reversible.

## Scope and tasks
1. Use documented engine-native content blocking first; record exactly which categories/API version are used.
2. Add a privacy panel with on/off status, supported blocked-request counter (label accurately), category status, and last decision where available.
3. Add temporary per-origin exception/disable and reload. Never add hard-coded rules for a named production site.
4. Add conservative HTTPS-first behavior only with explicit fallback for HTTP sites; do not break user navigation silently.
5. Apply cookie/storage defaults only where selected engine exposes reliable documented controls; present per-site clear controls.

## Required tests (run at phase end)
- [ ] Local fixture cases for allowed, blocked, unknown, exception, first-party/cross-party, redirect and iframe behavior.
- [ ] Compare filtering enabled/disabled and ensure counter has defined semantics.
- [ ] Per-origin temporary override test; verify no global toggle is accidentally changed.
- [ ] Cookie/storage tests for normal/private contexts.
- [ ] Regression corpus of unrelated sites for login/forms/media/breakage.

## PASS gate — all must be true
- [ ] Every UI claim maps to a documented/tested engine capability.
- [ ] Per-site exception is scoped and reversible.
- [ ] No site-specific exception/rule is embedded in app.
- [ ] Unknown/unobservable decisions are labeled as such, not guessed.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-05-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/05-privacy-protections-and-shield-controls`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-05): privacy protections & shield controls`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-05-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
