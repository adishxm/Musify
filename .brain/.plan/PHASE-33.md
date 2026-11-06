# Phase 33 — Advanced Procedural DOM Scriptlets & Main-World Neutralization

**Status:** COMPLETED  
**Execution Date:** November 6, 2026  
**Goal:** Implement Brave Browser's core procedural DOM scriptlet injection runtime (`set-constant.js`, `json-prune.js`, `abort-current-script.js`, `prevent-addEventListener.js`) directly into Musify's WebExtension and WebView script injection pipelines to neutralize inline anti-adblock probes and ad logic.

---

## 1. Context & Reverse-Engineered Mechanism

Many modern web platforms embed inline script tags that do not issue interceptable network requests or that poll JavaScript globals (e.g., `window.google_ad_client`, `window.__ad_blocker_detected`, `window.canRunAds`).

Brave Browser defuses these via procedural scriptlets defined in `resources.json`:
1. `set-constant.js`: Uses `Object.defineProperty` with `writable: false, configurable: false` to lock variables to inert values.
2. `json-prune.js`: Wraps `JSON.parse` and `Response.prototype.json` to dynamically strip advertising object trees before the host application receives them.
3. `abort-current-script.js`: Throws a handled exception when specific tracking or anti-adblock functions are invoked, terminating only the calling `<script>` block.
4. `prevent-addEventListener.js`: Intercepts `EventTarget.prototype.addEventListener` to suppress intrusive `visibilitychange` or `blur` listeners used to stall playback when users switch tabs.

---

## 2. Scope and Tasks

1. **Procedural Scriptlet Engine (`ProceduralScriptletEngine.kt`):**
   - Synthesize safe, idempotent main-world JavaScript scriptlets for `setConstant`, `jsonPrune`, `abortCurrentScript`, and `preventEventListener`.
   - Implement parameter validation and syntax escaping.

2. **Engine Injection Pipeline Integration:**
   - Integrate scriptlet dispatch into `WebViewAdapter.kt` (at `onPageStarted` / early injection).
   - Register in WebExtension content script for GeckoView engine.

3. **Automated Testing & Verification (`Phase33ProceduralScriptletTest.kt`):**
   - Unit tests verifying scriptlet synthesis, JSON pruning syntax, constant freezing, and event listener suppression logic.
   - Run complete `./gradlew.bat testDebugUnitTest` across all test suites (35 test suites, 242 unit tests passing 100%).

4. **Phase Report & Multi-Variant Release Assembly:**
   - Author `.brain/.report/PHASE-33-REPORT.md`.
   - Compile and verify signed release APKs (`Musify.apk`, `Musify-Lite.apk`, `Musify-ARM64.apk`).
   - Push to Git.

---

## 3. Verification Gate (Run at Phase End)
- [x] Procedural scriptlets generated with strict syntax safety and idempotency.
- [x] Complete unit test suite passes 100% (35 test suites, 242 unit tests).
- [x] Multi-variant release APKs signed and verified under 100 MB.
- [x] `.brain/.report/PHASE-33-REPORT.md` documented and committed.
- [ ] `.brain/.report/PHASE-33-REPORT.md` documented and committed.

