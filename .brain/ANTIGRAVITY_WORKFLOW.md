# Antigravity workflow for Musify phases

Use one bounded implementation cycle per phase. Paste a prompt like this at the start of each phase:

```text
We are working on Musify, a native Android general-purpose browser. Read `.brain/README.md`, `.brain/.plan/INDEX.md`, `.brain/ANTIGRAVITY_WORKFLOW.md`, and the current phase file `.brain/.plan/PHASE-XX.md` first. Inspect the repository before editing. Implement only the current phase scope; do not begin later phases or add unrelated features. Use the selected engine/architecture and safety limits in the plan.

Before claiming completion, run every required test in the phase plan. Do not invent test results. Record commands, environment, pass/fail, actual observations, artifacts, known issues, and commit hash in `.brain/.report/PHASE-XX-REPORT.md`. If a test fails, stop at the failure, explain it, make the smallest scoped fix, and rerun relevant plus regression tests. Do not mark PASS, commit, or push with any required test failing.

After all required tests pass, fill the report and create a focused commit on the phase branch, then prepare to push that branch to the already configured, user-owned remote as requested in the project workflow. Before pushing, show the branch, remote URL/owner, visibility if known, and exact commit. Stop for confirmation if the remote is missing, unfamiliar, public when that was not explicitly intended, or would push to the default/main branch; do not create or publish a new remote. Record commit and push result in the report. Never commit secrets, keystores, credentials, personal browsing data, or build outputs.
```

## Tool division

- Antigravity: inspect/edit project code, explain diffs, run or prepare tests.
- Android Studio: install/configure SDKs and emulators, Gradle sync, run app, inspect Logcat, profiler and UI/device behavior.
- Git: use a separate branch per phase; do not commit broken/incomplete phases.

Antigravity may not have the same Android SDK or emulator environment as Android Studio. If a required device test cannot run in the IDE, record the limitation and run it in Android Studio before PASS.
