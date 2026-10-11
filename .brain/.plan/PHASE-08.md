# Phase 08 — Security, privacy, accessibility & performance hardening

**Planning duration:** 3–4 weeks (estimate, not a promise)  
**Goal:** Run a structured release-readiness cycle across browser security, data, usability, and device performance.

## Scope and tasks
1. Create STRIDE checklist covering URLs/history/cookies/tabs/downloads/permissions/filter updates/dependencies/release signing.
2. Review exported components, deep links, TLS behavior, cleartext, native bridges, WebView/engine settings, logging, backups, and dependency notices.
3. Run dependency/SBOM/license and vulnerability scans; fix critical/high issues or document a release block.
4. Test TalkBack, Switch Access, large text, contrast, focus order, reduced motion and keyboard navigation where applicable.
5. Benchmark cold start, navigation, scrolling, multi-tab memory, update overhead, battery/network usage on named devices.
6. Add opt-in redacted diagnostics only if needed; never upload full URLs/history/cookies by default.

## Required tests (run at phase end)
- [ ] TLS/invalid certificate and malicious URL/scheme regression tests.
- [ ] Static/dependency/SBOM scan and zero unresolved critical/high release blockers.
- [ ] Data deletion/clear browsing data test with before/after evidence.
- [ ] Accessibility manual test and automated accessibility checks.
- [ ] Macrobenchmark and 30-minute battery/network comparison; mark targets vs results.
- [ ] Crash/ANR and renderer recovery soak test.

## PASS gate — all must be true
- [ ] No unresolved critical security/privacy defect.
- [ ] Privacy claims match observed storage and network traffic.
- [ ] Accessibility and performance limitations are documented.
- [ ] Only release builds use production config; debug/test trust/logging absent.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-08-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/08-security,-privacy,-accessibility-and-performance-hardening`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-08): security, privacy, accessibility & performance hardening`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-08-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
