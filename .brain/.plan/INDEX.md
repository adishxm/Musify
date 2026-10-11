# Musify Browser — .brain Project Workspace

This folder is the planning and evidence workspace for building **Musify**, a native Android general-purpose browser with selected privacy/ad/tracker protections. “Musify” is the current working name; complete package/trademark/store searches before public release. The roadmap targets an independently designed browser with selected Brave-like *feature categories*, not an exact Brave clone or affiliation.

## How to use

1. Keep `.brain/` at the root of the Android Git repository.
2. Work through `.plan/PHASE-00.md` to `.plan/PHASE-09.md` in order.
3. At phase start, have Antigravity inspect the current repo and implement only that phase. Android Studio remains available for SDK/emulator setup, Gradle sync, profiler and device tests.
4. Run every listed phase test. Record commands, environment, expected/actual results, evidence, defects and final status in the matching `.report/PHASE-XX-REPORT.md`.
5. **Only when all mandatory checks pass and the report is complete**, create a focused Git commit and push the phase branch. If any required test fails, mark FAIL/BLOCKED, fix and rerun; do not push a phase as complete.
6. Start the next phase only after the current phase is PASS and its commit/push is recorded.

## Structure

- `.plan/`: phase-by-phase scope, implementation tasks, tests and acceptance gates.
- `.report/`: one matching test/completion report per phase; blank templates are intentional.
- `ANTIGRAVITY_WORKFLOW.md`: reusable AI coding instructions.

## Core project constraints

- General-purpose browsing across unrelated websites; no hard-coded target-site rules.
- Recommended production candidate: GeckoView Stable, subject to Phase 01 tests. WebView is a constrained prototype/fallback. Do not fork Chromium for the MVP.
- Use a distinct UI/brand/assets; do not copy Brave's interface or claim exact parity.
- No blanket SSL bypass, unsafe native bridge, unnecessary broad storage permissions, or silent private-mode promises.
- Verify filter engine compatibility and every list/dependency license before shipping.
- Provisional performance targets are hypotheses until measured.

## Git rule

Suggested branch per phase: `phase/00-foundation`, `phase/01-engine-spike`, etc. Suggested commit: `feat(phase-XX): <short outcome>`. A phase is not complete because code compiles alone: tests, report, and review are part of its definition of done. Push only after PASS. This pack creates plans/templates only; it does not initialize a repository or push anything.
