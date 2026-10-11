# Phase 06 — Filter-list pipeline & licensing gate

**Planning duration:** 2–4 weeks (estimate, not a promise)  
**Goal:** Evaluate external lists only after confirming engine integration, license obligations, and a safe update/rollback path.

## Scope and tasks
1. Inventory candidate general-purpose lists, exact source URL, license, attribution, redistribution/modification rules, and update cadence.
2. First verify whether the selected GeckoView integration can consume the intended lists. If not, do not claim list-based coverage.
3. Treat a custom parser/native adblock-rust bridge as a separately scoped experiment behind an adapter; defer if the engine-native feature set meets MVP needs.
4. If implementation is approved: parse/compile off the UI thread, bound file/rule sizes, validate metadata and counts, and keep last-known-good list.
5. Update via HTTPS with timeout/backoff; stage, validate against golden tests, atomically activate, preserve rollback, and expose source/version/attribution.

## Required tests (run at phase end)
- [ ] License/provenance checklist approved before bundling or transforming any list.
- [ ] Golden tests for supported rule types, exceptions, unsupported syntax, malformed and oversized inputs.
- [ ] Update tests: timeout, bad status, corrupt list, parser failure, rollback, older known-good version.
- [ ] Benchmark list load and per-request decision overhead on named device.
- [ ] Verify source/license/version appears in app information screen if lists ship.

## PASS gate — all must be true
- [ ] No external list is included until licensing is reviewed and engine compatibility is proven.
- [ ] Failed update cannot replace active working list.
- [ ] Unsupported rules are counted and documented; no false parity claim.
- [ ] If bridge is not viable, keep engine-native protection and move bridge to future research.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-06-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/06-filter-list-pipeline-and-licensing-gate`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-06): filter-list pipeline & licensing gate`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-06-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
