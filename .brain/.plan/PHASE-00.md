# Phase 00 — Repository & project setup

**Planning duration:** 1–2 days (estimate, not a promise)  
**Goal:** Set up the Android project, build tools, Git workflow, and this .brain planning/report structure.

## Scope and tasks
1. Create a native Android project in Android Studio or through Antigravity using Kotlin, Gradle Kotlin DSL, and Jetpack Compose. Keep Antigravity as the coding workspace; use Android Studio for SDK/emulator/device and Gradle diagnostics when useful.
2. Initialize Git, add Gradle wrapper, .gitignore, README, license placeholder, and a distinct applicationId such as a reverse-domain ID you control. Do not freeze public branding yet.
3. Add a minimal landing screen that states Musify is an independent general-purpose browser. Do not use Brave logos, copied UI, or product claims of exact parity.
4. Add .brain/.plan and .brain/.report to the repository. Confirm each plan has a matching report file.
5. Write product scope: arbitrary websites, no single-site special casing, no credential collection, and no unsafe TLS bypass.

## Required tests (run at phase end)
- [ ] ./gradlew testDebugUnitTest
- [ ] ./gradlew assembleDebug
- [ ] Install and launch on one emulator or device.
- [ ] Inspect manifest for unnecessary permissions; inspect Git status and confirm secrets/build outputs are ignored.

## PASS gate — all must be true
- [ ] Gradle build succeeds from a clean checkout using the wrapper.
- [ ] App installs and opens without crashing.
- [ ] Git is initialized and .brain files are tracked.
- [ ] No secrets, signing keys, or local SDK paths are committed.

## If a test fails

Mark the phase **FAIL/BLOCKED**, record actual output in `.brain/.report/PHASE-00-REPORT.md`, fix within this phase, rerun the failed test plus relevant regression tests, and do not push a completion commit until every required check passes.

## Git gate after PASS

- Branch: `phase/00-repository-and-project-setup`
- Review `git status` and `git diff`; exclude secrets, keystores, local config, browsing data and generated build output.
- Fill the report with test evidence and final status **PASS**.
- Commit only this phase: `feat(phase-00): repository & project setup`
- Push the phase branch only after the report and all test results are reviewed. Record the pushed commit hash/remote in the report.
- Do not start the next phase before this gate is complete.

## Deliverables

- Working phase increment in the Android project.
- Completed `.brain/.report/PHASE-00-REPORT.md`.
- Screenshots/logs/test output stored without private URLs, credentials or personal browsing history.
