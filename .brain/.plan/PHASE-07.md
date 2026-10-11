# Phase 07 — Browser essentials: downloads, permissions & safety

**Planning duration:** 2–3 weeks (estimate, not a promise)  
**Goal:** Complete useful browser interactions while minimizing unsafe privileges.

## Scope and tasks
1. Add user-confirmed downloads with safe filename handling, progress, cancel, destination choice, and explicit open/share flow.
2. Add file chooser mediated by Android picker; do not expose raw filesystem access to pages.
3. Implement geolocation, camera, microphone, notification and other supported prompts with exact-origin display, deny by default, and runtime permission only after user action.
4. Handle popup/new-window requests, external app links, custom schemes, and redirects safely.
5. Add find-in-page, share, landscape support, dark theme, and clear accessible labels where time permits.

## Required tests (run at phase end)
- [ ] Download MIME/name/redirect/cancel/open-intent matrix; no automatic execution.
- [ ] File chooser tests with cancel, multi-select if supported, and no broad storage permission.
- [ ] Permission tests for deny, grant, partial grant, origin change, and revoke.
- [ ] Intent tests for benign and malicious-looking schemes; external handoff asks user as appropriate.
- [ ] Manual form/login/upload and download tests on unrelated sites.

## PASS gate — all must be true
- [ ] No permission is granted to a page without matching user/app consent.
- [ ] Downloads require explicit user action and safe destination.
- [ ] Unsupported behaviors fail closed with readable explanation.
- [ ] No broad storage permission without documented necessity.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-07-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/07-browser-essentials:-downloads,-permissions-and-safety`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-07): browser essentials: downloads, permissions & safety`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-07-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
