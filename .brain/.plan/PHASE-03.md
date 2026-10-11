# Phase 03 — Tabs, lifecycle & session recovery

**Planning duration:** 2–3 weeks (estimate, not a promise)  
**Goal:** Support multiple browsing tabs and recover normal tabs after app/process restarts.

## Scope and tasks
1. Add tab model and switcher with deterministic selected-tab state; cap open tabs to limit memory.
2. Map each tab to its own engine session according to selected engine guidance.
3. Persist only the minimum normal-tab metadata required for restore; do not persist page contents or secrets by default.
4. Handle rotation, background/foreground, process death, renderer death, and app restart.
5. Add close-tab and close-all behavior; private-mode separation will be introduced in a later phase.

## Required tests (run at phase end)
- [ ] Unit tests for tab reducer/state transitions and maximum-tab policy.
- [ ] Instrumented tests for create/switch/close/reopen tabs and selected tab correctness.
- [ ] Process-kill/relaunch test restores safe normal tab metadata.
- [ ] Rotation/background/foreground and renderer failure tests.
- [ ] Memory observation with a small multi-tab corpus.

## PASS gate — all must be true
- [ ] Tab operations preserve correct URL/title/session association.
- [ ] Process recovery does not create duplicate tabs or navigation loops.
- [ ] No crash/leak in repeated tab lifecycle test.
- [ ] Report device/API level and measured limits.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-03-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/03-tabs,-lifecycle-and-session-recovery`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-03): tabs, lifecycle & session recovery`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-03-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
