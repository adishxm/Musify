# Phase 26 — Universal Tracking Parameter Sanitizer & Bounce Tracker Defuser (Clean URLs & Debouncing)

**Status:** COMPLETED  
**Planning duration:** 1–2 days  
**Goal:** Implement Brave's browser-level query parameter sanitization (`query-filter.json` / `clean-urls.json`) and bounce tracking defense (`debounce.json`) in Musify's navigation and URL resolution engine. Strip tracking parameters (`fbclid`, `gclid`, `msclkid`, `_gl`, `mc_eid`, etc.) upon link navigation, and automatically bypass redirect-based tracking intermediaries (`dpbolvw.net`, `tkqlhce.com`, `avantlink.com`) by navigating straight to the extracted destination target.

## Scope and tasks
1. **Query Parameter Sanitization Engine (`TrackingParamSanitizer.kt`):**
   - Implement tracking token stripper:
     - Broad tokens: `fbclid`, `gclid`, `msclkid`, `dclid`, `irclickid`, `mc_eid`, `_gl`, `_hsenc`, `_openstat`, `s_cid`, `pk_cid`, `hsCtaTracking`.
     - Platform-specific tokens: Facebook (`__cft__`, `__tn__`), Amazon (`ascsubtag`, `pf_rd_p`), Google Search (`ved`, `ei`, `sca_esv`).
   - Hook sanitizer into `UrlParser.kt` and `SingleTabViewModel.kt` during URL entry, external intent launches, and link navigation.

2. **Bounce-Tracking Defense Engine (`DebounceInterceptor.kt`):**
   - Port Brave's `debounce.json` redirect patterns:
     - Identify tracking redirectors (e.g., `*://*.dpbolvw.net/click-*`, `*://*.tkqlhce.com/click-*`, `*://howl.me/link/?*`, `*://*.avantlink.com/click.php?*`, `*://*.jdoqocy.com/click-*`).
     - Extract embedded destination parameter (`url`, `Url`, `btn_url`, `link`, `dest`).
     - Perform immediate direct navigation to destination URL, completely bypassing the tracking intermediary hop.

3. **WebExtension Integration:**
   - Synchronize query sanitization rules with `background.js` `onBeforeRequest` listener for background redirect rewriting.

4. **Unit & Verification Testing:**
   - Author `app/src/test/java/com/musify/browser/Phase26CleanUrlsAndDebounceTest.kt`:
     - Test removal of `fbclid`, `gclid`, `_gl`, and platform tracking tokens from navigation URLs.
     - Test preservation of legitimate functional parameters (e.g. `v=` on YouTube, `q=` on search engines).
     - Test extraction and immediate redirect for bounce tracking intermediaries.
   - Run `./gradlew.bat testDebugUnitTest`.

5. **Phase Completion & Deliverables:**
   - Author `.brain/.report/PHASE-26-REPORT.md`.
   - Commit and push to Git.

## Required tests (run at phase end)
- [x] `Phase26CleanUrlsAndDebounceTest` passes 100%.
- [x] Full test suite `./gradlew.bat testDebugUnitTest` passes 100%.

## Verification Gate
- [x] Tracking query parameters stripped without corrupting search or video IDs.
- [x] Bounce tracking redirect hops successfully defused.
- [x] `.brain/.report/PHASE-26-REPORT.md` documented and committed.
