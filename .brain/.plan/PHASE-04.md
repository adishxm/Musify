# Phase 04 — History, bookmarks & private tabs

**Planning duration:** 2–3 weeks (estimate, not a promise)  
**Goal:** Add local browsing utility features and a defensible private-tab lifecycle.

## Scope and tasks
1. Add local history and bookmarks with search, delete-one, clear-by-time, and clear-all.
2. Implement private tabs as a separate engine profile/session store where supported. Do not label private mode as anonymous.
3. On private close, clear private history/cookies/storage according to documented engine APIs; explain downloads and user-saved bookmarks may persist.
4. Add a visible private indicator that cannot be confused with normal tabs.
5. Add a data-control screen with clear browsing data actions.

## Required tests (run at phase end)
- [ ] Database unit tests and migration tests.
- [ ] Isolation tests: visit same fixture in normal and private profiles; verify cookies/history/storage do not cross where supported.
- [ ] Close private tab/app, inspect history and storage through supported test hooks.
- [ ] Deletion tests for clear one/site/time/all.
- [ ] UI test ensures private status remains visible during navigation.

## PASS gate — all must be true
- [ ] Normal history/bookmarks persist locally and can be deleted.
- [ ] Private tab data is not written to normal history in tested cases.
- [ ] Private-mode limits are disclosed; no anonymity promise.
- [ ] Any engine limitation is documented, not hidden.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-04-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/04-history,-bookmarks-and-private-tabs`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-04): history, bookmarks & private tabs`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-04-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
