# Phase 31 — Cookie Notice & Mobile Annoyance Shielding

**Status:** COMPLETED  
**Execution Date:** November 6, 2026  
**Goal:** Implement cosmetic and procedural defusal of intrusive GDPR cookie consent dialogs, backdrop blurs, and "Open in App" mobile nag interstitials using rules ported from Brave's Cookie Notice Blocker (`cdbbhgbmjhfnhnmgeddbliobbofkgdhe`) and Mobile App Promo Blocker (`bfpgedeaaibpoidldhjcknekahbikncb`).

---

## 1. Context & Reverse-Engineered Mechanism

Modern web navigation is inundated with GDPR consent banners, full-page modal takeovers, and mobile app download nags (e.g. Reddit, Twitter, news portals).

Brave provides dedicated component filters:
1. `cdbbhgbmjhfnhnmgeddbliobbofkgdhe` (1.62 MB): Hides cookie consent dialogs from OneTrust, Cookiebot, TrustArc, Didomi, Quantcast, and native CSS consent overlays.
2. `bfpgedeaaibpoidldhjcknekahbikncb` (50.7 KB): Suppresses floating "Use App" pills and sticky app banners.

---

## 2. Scope and Tasks

1. **Annoyance Filter Catalog Integration (`AnnoyanceFilterRepository.kt`):**
   - Curate top high-impact cosmetic rules for cookie consent modals and mobile app nags.
   - Inject cosmetic CSS rules into `document_start` on web navigations.

2. **User Preferences & Shield Sheet Controls:**
   - Add granular switches to `PrivacyShieldSheet.kt`:
     - "Block Cookie Notices"
     - "Block Mobile App Promos"

3. **Telemetry & Controller Integration:**
   - Add telemetry counters: `cookieBannersBlocked` and `appPromosBlocked`.

4. **Automated Testing (`Phase31AnnoyanceShieldTest.kt`):**
   - Unit tests validating CSS injection, preference persistence, and rule application.
   - Run complete `./gradlew.bat testDebugUnitTest` (33 test suites, 229 tests passing 100%).

5. **Phase Report & Multi-Variant Release Assembly:**
   - Document `.brain/.report/PHASE-31-REPORT.md`.
   - Compile signed release APKs and push to Git.

---

## 3. Verification Gate (Run at Phase End)
- [x] Cookie consent banners and backdrop blurs suppressed on major portals.
- [x] Mobile "Open in App" prompts hidden.
- [x] Complete unit test suite passes 100% (33 test suites, 229 unit tests).
- [x] `.brain/.report/PHASE-31-REPORT.md` documented and committed.

