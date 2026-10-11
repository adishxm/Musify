# Phase 09 — Beta, documentation & release decision

**Planning duration:** 2–4 weeks (estimate, not a promise)  
**Goal:** Prepare a controlled beta or personal signed build with reproducible evidence; public release is optional and gated.

## Scope and tasks
1. Run a diverse unrelated-site corpus; triage each failure as engine limitation, app bug, filter breakage, or unsupported feature.
2. Prepare onboarding, privacy policy, known limitations, filter-source/license notices, support/feedback channel, and uninstall/data-clear guidance.
3. Create signed APK for controlled testing; maintain signing key securely and test upgrade/rollback. Use Play internal/closed testing only after current policy review.
4. Complete name/applicationId checks. Treat Musify as a working name until market/trademark/package clearance; do not imply affiliation with Brave.
5. Hold a final go/no-go review against security, privacy, feature, test, and distribution evidence.

## Required tests (run at phase end)
- [ ] Clean install, upgrade from prior build, rollback/recovery, uninstall and data-clear verification.
- [ ] Repeat critical journey on at least two device/API profiles.
- [ ] Review list attributions, third-party notices, privacy policy, screenshots and app metadata.
- [ ] Beta feedback triage and issue severity review.
- [ ] If Play distribution is considered, re-check current target API, Data Safety and Device/Network Abuse policies at submission time.

## PASS gate — all must be true
- [ ] All phase reports complete; no open critical/high blocker.
- [ ] Known limitations and measured results are published honestly.
- [ ] Signing/build process is reproducible and keys are not in Git.
- [ ] Decision is recorded: personal build, closed beta, public release after clearance, or pause.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-09-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/09-beta,-documentation-and-release-decision`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-09): beta, documentation & release decision`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-09-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
