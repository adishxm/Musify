# Phase 02 — Single-tab browser shell

**Planning duration:** 1–2 weeks (estimate, not a promise)  
**Goal:** Build the first user-visible browser flow around one safe tab.

## Scope and tasks
1. Compose screen with address/search bar, origin display, back, forward, reload/stop, progress, and clear error state.
2. Implement URL-versus-search parsing and allow only http/https top-level browsing by default; handle external intents safely.
3. Show full origin clearly, including scheme and hostname; protect browser chrome from page-controlled spoofing.
4. Handle network errors, invalid URLs, offline state, and renderer loss. Avoid hiding certificate errors behind a proceed button.
5. Create a local HTTPS fixture for test pages; keep test CA/config debug-only.

## Required tests (run at phase end)
- [ ] Unit tests for URL/search parsing, URI schemes, IDN-ish host display, and malformed input.
- [ ] UI tests: enter URL, search query, back, forward, reload, stop, loading state, error state.
- [ ] Security tests: javascript:, file:, content:, intent:, data: top-level inputs are rejected or safely handed off; TLS test cert errors cancel.
- [ ] Manual TalkBack smoke test for address/navigation controls.

## PASS gate — all must be true
- [ ] Single-tab navigation works repeatedly on fixture and unrelated ordinary sites.
- [ ] No unsafe scheme reaches the engine as a top-level navigation.
- [ ] Loading/error/security state is understandable and accessible.
- [ ] No release build trusts test certificates.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-02-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/02-single-tab-browser-shell`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-02): single-tab browser shell`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-02-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
