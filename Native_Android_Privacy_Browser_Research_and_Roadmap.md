# Native Android Privacy Browser: Research and Roadmap

**Date:** 2026-10-11  
**Audience:** A beginner or small student team using Android Studio, Kotlin, and Jetpack Compose  
**Scope:** A general-purpose Android browser for arbitrary websites, with a modest, independently designed subset of privacy protections comparable in concept to selected Brave features.

> **Important correction:** “Exactly the same as Brave” is not an appropriate product requirement. The practical and lawful goal is an **independently designed browser with selected, measurable privacy features**. Use a distinct name, UI, code, icons, assets, and product language. Do not copy Brave branding, trade dress, screenshots, filter-list presentation, proprietary code, or assets without permission and a license review. “Brave-like” is useful only as an internal comparison of feature categories—not as a claim of parity or affiliation.

## Executive recommendation

Build a **native Android browser shell in Kotlin/Jetpack Compose around GeckoView Stable**, with selected Mozilla Android Components (AC) modules for engine integration, tabs, state/session storage, downloads, prompts, site permissions, toolbar, and tab UI. Pin a stable release, use prebuilt Maven artifacts, and maintain a tested upgrade branch. GeckoView is browser-oriented, self-contained, multi-process, and supports content blocking and WebExtensions; Android Components supplies reusable browser plumbing but is not a finished browser UI. [1][2][3]

Use **WebView as a short learning spike and a fallback adapter**, not the primary shipping engine for a privacy-oriented general-purpose browser. Android documents WebView as an embedded view rather than a fully developed browser, and its interception and storage hooks have important blind spots. [4][5][6]

Keep a **Chromium fork as a contingency only**. It offers maximum engine/ecosystem control, but source checkout, native build, security rebasing, CI, third-party licensing, and maintenance are far beyond a modest beginner MVP. [7][8]

The first release should implement, in this order:

1. A reliable general browser shell: address/search, navigation, tabs, history, bookmarks, downloads, prompts, and recovery.
2. GeckoView’s documented content blocking with transparent status and per-site exceptions.
3. Properly licensed EasyList and EasyPrivacy subscriptions, initially used only where the selected engine integration demonstrably supports them; do not promise complete rule parity.
4. HTTPS-first top-level navigation with an explicit HTTP fallback warning.
5. Third-party-cookie/storage controls and disposable private tabs, with honest limits.
6. A visible per-page count labelled **“requests blocked by this app”**, not “all trackers blocked.”
7. Strong safe defaults: no arbitrary native bridge, no SSL bypass, no unsafe file access, no uncontrolled popups, deny-by-default permissions, and renderer/process recovery.

A focused MVP is feasible in roughly **22–28 weeks** as a planning assumption. A polished Brave-equivalent browser, browser engine fork, full anti-fingerprinting system, Tor product, sync service, password manager, or complete extension ecosystem is not a realistic beginner scope.

---

## 1. Product definition

### 1.1 What is being built

**Working definition:** an Android application with its own browser chrome, tabs, navigation, storage decisions, permissions UI, downloads, privacy controls, and lifecycle handling, using an embedded browser engine to render arbitrary web content.

The product is:

- **Native Android:** Kotlin/Java Android code, Android lifecycle integration, native UI and storage, Android intents, permissions, notifications, and release signing. “Native” does **not** mean writing a new HTML/CSS/JavaScript engine.
- **General-purpose:** intended for unrelated sites such as news, documentation, commerce, logins, media, forms, downloads, and web applications—not a wrapper for one service.
- **Privacy-oriented:** defaults and controls reduce common third-party advertising/tracking and unnecessary data retention while explaining limitations.
- **Independent:** its own product name, visual identity, implementation, feature names, documentation, and release process.
- **Selected rather than exhaustive:** it may provide network/content blocking, HTTPS-first navigation, cookie controls, private tabs, site exceptions, and transparent reporting without claiming every Brave Shield, every filter rule, or perfect anti-fingerprinting.

### 1.2 What “native Android” means in this roadmap

A native Android browser owns the application layer around an engine:

- address/search bar and browser navigation;
- tab/window model;
- history and bookmarks;
- session restore and process-death recovery;
- downloads, file chooser, sharing, and Android intents;
- permission prompts and site settings;
- privacy settings and per-site exceptions;
- local data lifecycle and optional diagnostics;
- accessibility, theming, localization, signing, and release operations.

GeckoView supplies the web platform and rendering engine. A `GeckoSession` maps naturally to a tab/window, while the app owns most browser UI and policy. [1][2] WebView similarly supplies a rendering surface but explicitly does not include an address bar, navigation controls, bookmarks, history UI, or a finished browser experience. [4][5]

### 1.3 Product promise and non-promise

**Promise:** “This browser applies documented privacy protections to ordinary web browsing, shows what it decided where feasible, lets you make site-specific exceptions, and avoids collecting browsing history by default.”

**Do not promise:** complete anonymity, universal tracker detection, Brave parity, perfect blocking, guaranteed fingerprint resistance, total cookie isolation, malware detection for every threat, compatibility with every site/device, or recovery of intentionally deleted private data.

Brave itself describes a broad product policy involving blocking, cookie/storage behavior, fingerprinting defenses, HTTPS upgrades, phishing protection, per-site settings, resource replacement, and other mechanisms. An ad-blocking library alone is not that product. [9][10]

---

## 2. Engine decision

### 2.1 Comparison

| Engine option | Evidence-based strengths | Evidence-based limitations and work | Fit for this product | Decision |
|---|---|---|---|---|
| **Android WebView** | Android provides it on supported devices as an embedded Chromium-based view. It gives full app control over browser chrome and is quick to prototype. The provider is updated as a system component, and Jetpack WebKit can report the provider. [4][6][11] | Android says it is not a fully developed browser and omits browser chrome. `shouldInterceptRequest` is not called for `javascript:`, `blob:`, or app asset URLs; redirects are not all exposed as separate callbacks; it runs off the UI thread. Service-worker interception is a separate global facility that must be installed before pages/workers. [5][12][13][14] WebView data, provider behavior, media, and compatibility vary by device/OS/provider. It does not provide a documented Brave-equivalent privacy model or browser-wide firewall. [6][11] | Good for a learning/demo build or controlled web surface. Risky as the primary foundation for a privacy browser because a simple blocker cannot see/control every request and the app must implement nearly all browser policy. | **Fallback adapter and 1–2 week spike; not the production default.** |
| **GeckoView** | Mozilla positions it as a full-featured, self-contained, standards-oriented browser engine for embedding. It supports `GeckoRuntime`, `GeckoSession`, multi-process operation, permissions, downloads, history/navigation delegates, content blocking, and WebExtensions. [1][2][15][16] Android Components adds browser state, session storage, tabs, toolbar, downloads, prompts, site permissions, and add-ons. [3] | The app still owns browser UI, persistence policy, privacy UX, and release maintenance. Gecko has multiple processes and must recover when services/renderers disappear. Major engine releases occur on a rapid cadence, and future API changes require upgrades/testing. [17][18] Binary size, startup, memory, DRM/media, WebAuthn, and exact compatibility require measurement. | Strongest fit for a student general-purpose browser with browser-oriented APIs and an independent UI. | **Recommended production candidate.** |
| **Chromium fork** | Maximum control over Chromium behavior and deep network/engine integration. Chromium publishes Android build instructions and source licensing terms. [7][8] | Requires large source checkout, Linux build environment, substantial storage/RAM, native build expertise, CI, ongoing rebases and security updates, and complete third-party license review. Brave’s own architecture demonstrates that a full Chromium customization is a large browser project, not a dependency add. [19] | Appropriate only if a later, funded project proves GeckoView cannot meet a documented compatibility, policy, or update requirement. | **Second-stage contingency, not MVP.** |

### 2.2 Decision gate

Retain GeckoView after a two-week spike only if it can, on at least two Android API/device profiles:

- load a representative corpus of unrelated modern sites;
- support back/forward/reload and tab switching;
- restore safe tab metadata after process death;
- complete basic forms, media, downloads, and permission prompts;
- provide the planned content-blocking and exception controls;
- avoid unacceptable P0/P1 security or privacy regressions.

If GeckoView fails, first reduce the feature promise and test the WebView adapter. Move to a Chromium fork only after writing down the requirement that failed, measuring it, and accepting the build/maintenance burden. Do not choose a new engine because it sounds more “native.”

### 2.3 WebView limitations that affect the fallback

If the fallback is used, document these limitations in the code and product design:

- `WebViewClient.shouldInterceptRequest` can return a replacement response or pass through, but it misses `javascript:`, `blob:`, and app asset URLs; redirect callbacks do not provide a complete hop-by-hop audit; and it is not a universal network firewall. [12]
- `shouldOverrideUrlLoading` is incomplete: it is not called for app-initiated `loadUrl()` or POST requests, may see subframes/non-HTTP(S) schemes, and should not be implemented by calling `loadUrl()` and returning `true`. [12]
- Service workers use a separate global `ServiceWorkerController`/`ServiceWorkerClient`; install before any active worker/page and do not assume a late hook covers existing workers. [13][14]
- Cookies are enabled by default; third-party-cookie policy is per WebView, and cookie persistence is managed through a singleton `CookieManager`. Do not promise independent browser profiles from these APIs alone. [20]
- JavaScript is disabled by default, but general browsing usually needs it. Enabling it increases attack surface. Do not expose `addJavascriptInterface` or an equivalent native bridge to arbitrary sites; Android says the object is exposed to every frame without a mechanism to verify the caller origin. [21][22]
- File/content access, mixed content, file-origin cross-access, downloads, permissions, SSL errors, and renderer termination all require explicit host-app decisions. [6][23][24][25]

These are platform limits, not bugs to hide in marketing. A WebView blocker can be useful, but it cannot be described as complete browser-wide request control.

---

## 3. Architecture and request/data flow

### 3.1 Proposed architecture

```text
Android application (Kotlin + Compose)
│
├── Browser UI
│   ├── address/search bar, navigation, tab strip/tray
│   ├── page-info/privacy panel and per-site exceptions
│   ├── downloads, prompts, settings, history, bookmarks
│   └── accessible error/recovery/permission screens
│
├── Browser coordinator/state
│   ├── tab state machine and selected tab
│   ├── navigation/history/bookmark policy
│   ├── normal/private profile boundaries
│   └── lifecycle, process-death and renderer recovery
│
├── EngineAdapter (app-owned interface)
│   ├── GeckoEngineAdapter
│   │   ├── one GeckoRuntime per app process
│   │   └── one GeckoSession per open tab/window
│   └── WebViewAdapter (fallback/demo only)
│
├── Privacy policy layer
│   ├── Gecko content-blocking settings
│   ├── licensed filter-list metadata and compiled state
│   ├── request decisions / exceptions where supported
│   ├── HTTPS-first navigation
│   ├── cookie/storage and private-session policy
│   └── decision counters, reasons and diagnostics
│
├── Data layer
│   ├── settings, site policies, history, bookmarks
│   ├── tab/session metadata and download records
│   ├── list snapshots, hashes, parser diagnostics
│   └── engine-owned cookies/cache/passwords behind explicit controls
│
└── Release/operations
    ├── opt-in redacted crash/ANR diagnostics
    ├── SBOM, license/notice screen and source offers
    ├── signed APK/AAB and update channel
    └── rollback and incident-response process
```

Keep UI code independent of GeckoView classes except behind a small, tested adapter. This makes the fallback possible and prevents an engine API from leaking through every screen.

### 3.2 Navigation flow

1. **Input:** user enters a URL, search phrase, external `ACTION_VIEW` intent, bookmark, history item, or link.
2. **Parse:** classify as search or URI; validate scheme/host; reject unsafe or ambiguous custom schemes unless explicitly supported.
3. **HTTPS-first:** for a typed/link top-level `http://` navigation, attempt `https://` according to policy. If HTTPS fails, show an interstitial before a user-approved HTTP fallback. This is a product policy modeled on documented Brave/Firefox behavior, not a guarantee for every subresource. [9][26]
4. **Engine load:** send the validated URI to the active `GeckoSession`.
5. **Protection decision:** apply GeckoView content-blocking settings and supported policy decisions; record only the minimum decision metadata needed for the visible counter and diagnostics.
6. **Prompts:** permission, download, popup, certificate, and error events go to native UI. Never silently grant camera/microphone/location or proceed through certificate errors.
7. **Render/lifecycle:** show progress/error state, observe navigation/history, and handle process loss by destroying the dead session/view and creating a replacement with safe saved metadata. [17]
8. **Data:** write normal history/bookmarks according to settings; keep private sessions out of normal history and delete disposable data on close.

### 3.3 Privacy decision flow

```text
Page/resource event or engine content-blocking result
                 │
                 ▼
     Identify top-level origin + request context
                 │
                 ▼
  Check global policy → per-site exception → engine capability
                 │
          ┌──────┴──────┐
          │             │
       allow         block/modify
          │             │
          ▼             ▼
   Continue page       Apply supported decision
                       increment explainable count
                       retain rule/list reason when safe
```

The counter must be scoped and labelled. For example: **“37 requests blocked by this app on this page load.”** It must not imply that every tracker, iframe, redirect, `blob:` action, fingerprint signal, or background network event was observed.

### 3.4 Process and lifecycle rules

- Create one `GeckoRuntime` per process and sessions per tab, following the current GeckoView integration guidance. [2]
- Destroy/recreate a WebView after renderer loss if the fallback is active; a dead WebView cannot simply be reused. [17][27]
- Bound open tabs/processes and persist only safe metadata such as URL, title, selected tab, and navigation history where supported.
- Do not promise perfect page-state continuity after renderer or process loss.
- Test backgrounding, rotation, low memory, process kill, app relaunch, offline mode, and repeated crash pages.

---

## 4. Filter engine, list sourcing, licensing, and updates

### 4.1 Start with engine-native protection

GeckoView exposes content blocking for anti-tracking, cookies, and Safe Browsing, with settings for anti-tracking categories, ETP level, cookie behavior, bounce tracking, query-parameter stripping, tracking-database status/statistics, and Safe Browsing. Mozilla documents default and strict categories, but warns that strict/content-tracker modes may break sites. [28][29]

Therefore, the beginner implementation should:

1. enable the documented GeckoView protection categories;
2. show a simple protection status and exception control;
3. test ordinary sites and synthetic fixtures;
4. add custom lists or a custom native filter engine only after measuring the gap.

This is more achievable than trying to recreate all Brave Shields behavior immediately.

### 4.2 Initial lists

For a general-purpose browser, begin with:

- **EasyList** for common ad scripts, images, CSS, video/affiliate ads, popups, placeholders, and selected anti-adblock coverage;
- **EasyPrivacy** for analytics, telemetry, pixels/cookies, beacons, fingerprinting, email tracking, CNAME trackers, and tracking servers. [30]

The canonical production subscriptions were documented at the research snapshot as [67][68]:

- `https://easylist.to/easylist/easylist.txt`
- `https://easylist.to/easylist/easyprivacy.txt`

The snapshot headers reported four-day expiry and commit/version metadata. Treat those values as time-specific observations, not a permanent SLA. [67][68]

Do not concatenate every repository component. Start with the canonical general lists, make regional/annoyance/optional lists opt-in, and report exactly which lists are active.

### 4.3 Syntax and capability boundary

EasyList/EasyPrivacy use Adblock Plus-style text with network rules, exceptions, resource types, cosmetic selectors, and metadata. uBlock Origin supports most EasyList syntax but deliberately differs and adds extended syntax; ABP/uBO compatibility is not identical. [31][32]

Classify each rule during parsing:

- network request rule;
- exception rule;
- cosmetic selector;
- redirect/resource replacement;
- scriptlet/procedural rule;
- unsupported or parser-error rule.

Keep a **lossless source record** and a **compiled record**. Display unsupported counts and never imply an unsupported rule is active. If a declarative ruleset is used, document its limits; Chrome’s API has rule and regex constraints, and uBlock Origin Lite documents features that declarative rules cannot fully represent. [33][34]

### 4.4 `adblock-rust`: useful later, not a turnkey SDK

`adblock-rust` is an MPL-2.0 reusable filtering library, not the Brave browser or whole Shields product. It provides an `Engine`/`FilterSet`, network matching, cosmetic filtering, redirects/resources, hosts syntax, and uBlock-style extensions. [35][36]

Important engineering boundaries:

- the engine expects a caller to construct a request with URL/context/type/method/party and then apply the result;
- it does not itself hook GeckoView/WebView network events or render a blocked-resource UI;
- cosmetic filtering and scriptlets are a separate page-integration path;
- the engine is not designed for arbitrary add/remove mutation after initial creation, so update requires constructing a replacement engine and atomically swapping it;
- serialized DAT data is a cache optimization and may not deserialize across minor versions; retain source text and a rebuild path;
- the public `adblock-rust-ffi` repository documents a C/C++ wrapper with an older crate pin, not a modern Kotlin Android SDK. [35][37]

Use it only after a time-boxed bridge spike, preferably behind a `FilterEngineAdapter`. Do not claim that its presence reproduces Brave’s list pipeline, cookie policy, CNAME behavior, fingerprinting defenses, phishing service, UI, or update infrastructure.

### 4.5 Update transaction and rollback

Recommended update transaction:

1. schedule a background fetch using the documented list expiry as a hint;
2. use HTTPS, conditional requests, timeouts, backoff, and a reasonable Wi-Fi/charging policy;
3. save to a temporary file/database record;
4. validate status, size, metadata, hash/commit, parser version, and rule count bounds;
5. parse/compile off the UI thread;
6. run golden and smoke tests;
7. atomically activate the new set;
8. keep the previous active set on failure;
9. retain at least two prior snapshots and allow restoring a pinned full-SHA snapshot;
10. record source URL, license, version, modified time, commit/hash, parser version, diagnostics, and activation time.

Expired lists may continue functioning while awaiting refresh; more lists increase breakage risk, and list order/update timing can change counts. [38]

### 4.6 List licensing and attribution

EasyList’s repository states that, unless otherwise noted, its material is dual licensed **GPLv3-or-later or CC BY-SA 3.0-or-later**, with attribution to The EasyList authors and `https://easylist.to/`. It warns that externally hosted referenced files can have different conditions. [39]

Ship a **Licenses and filter sources** screen with:

- list title and author;
- source URL and homepage;
- license and attribution text;
- fetched version/commit/modified time;
- whether the copy is bundled, downloaded, modified, or compiled;
- parser/engine version;
- update timestamp and rollback controls.

This is not legal advice. Ask qualified counsel to review whether bundled or transformed list data triggers obligations, and review every optional list individually. Preserve Mozilla/GeckoView, Android Components, Rust, Chromium, and transitive dependency notices as well.

---

## 5. Features ordered by priority

### Tier 0: safety and browser fundamentals

1. Address/search entry with visible full origin.
2. Back, forward, reload, stop, progress, error pages.
3. Tabs and selected-tab state.
4. Safe scheme/host validation and external intent handling.
5. Safe Browsing enabled where provided by the selected engine; do not market it as universal threat detection. [40]
6. Cancel certificate errors; never provide a blanket “proceed anyway” control. [41]
7. HTTPS-first top-level navigation with explicit HTTP fallback.
8. No arbitrary native JavaScript bridge.
9. Mixed content disabled by default where supported.
10. Renderer/process-loss recovery and user-visible reopen/reload path.

### Tier 1: useful general browsing

1. Normal history and bookmarks.
2. Tab/session restore after process death.
3. Download confirmation, safe filename/MIME handling, progress, cancellation, open/share behavior.
4. File chooser mediated by the app.
5. Camera/microphone/location/notification prompts with exact origin/resource checks and Android runtime permission handling.
6. Find in page, share, print where supported, landscape, dark theme, large text.
7. `ACTION_VIEW` handling for `http`/`https` links and safe handoff.

### Tier 2: selected privacy protections

1. GeckoView content blocking with standard categories.
2. EasyList + EasyPrivacy pipeline after license review.
3. Per-site exception/allowlist and temporary disable/reload.
4. Explainable request counter and protection panel.
5. Third-party cookies disabled by default where exposed.
6. Per-site clear/reset of cookies/site data/permissions.
7. Disposable private tabs with no normal-history persistence.
8. Clear-on-exit for selected normal browsing data.
9. Optional query-parameter stripping/HTTPS upgrades only where tested.

### Tier 3: defer until evidence exists

- script blocking;
- cosmetic/scriptlet injection beyond engine-supported behavior;
- custom `adblock-rust` native bridge;
- advanced fingerprinting randomization/API normalization;
- CNAME uncloaking and bounce-tracking systems;
- extension store or broad remote XPI installation;
- account sync, encrypted backup, password manager, payment autofill;
- Tor integration, whole-device VPN, or network tunnel;
- DRM/enterprise/WebAuthn completeness claims.

Brave documents script blocking as an advanced, breakage-prone control, and independent research on DuckDuckGo’s Android behavior shows platform limits such as iframe JavaScript injection and incomplete HTTPS/referrer coverage. [9][42] Defer these features rather than adding a misleading toggle.

---

## 6. Privacy and security threat model

### 6.1 Assets

Protect:

- typed URLs, browsing history, bookmarks, tabs, cookies, site storage, downloads, form data, permissions, and private-session state;
- filter lists, update metadata, exception policies, and diagnostics;
- Android runtime permissions and exported intents;
- account/session secrets and authentication state;
- integrity of the engine, app updates, list updates, and download/open flows;
- user trust in the displayed origin, permission, block reason, and privacy status.

### 6.2 Adversaries and abuse cases

| STRIDE category | Browser-specific threat | Mitigations and tests |
|---|---|---|
| **Spoofing** | Phishing/IDN-like URL, fake browser permission panel, malicious deep link, forged filter update | Show origin and scheme; validate host; use Safe Browsing; keep security UI native and distinct from page content; authenticate updates; test intents and look-alike domains. |
| **Tampering** | Network modification, modified list/profile/download, hostile page attempting native bridge access | HTTPS/cleartext denial; atomic signed/app-owned data updates; no arbitrary bridge; least privilege; safe download policy; debug CA only in debug builds. [43] |
| **Repudiation** | User cannot tell why a resource was blocked or what version produced a decision | Opt-in redacted decision export with build/list/rule IDs; do not log URLs, cookies, credentials, or full history by default. |
| **Information disclosure** | History/cookies/downloads/typed URLs leaked to telemetry, other apps, logs, screenshots, or a page | Minimize/disable telemetry by default; encrypted local metadata where appropriate; non-exported components; profile separation; redaction; egress allowlist; arbitrary pages receive no native bridge. [21][44] |
| **Denial of service** | Pathological page/rules, huge list, renderer OOM/crash, reload loop, captive portal | Bound parsing/memory; timeouts; last-known-good lists; circuit breaker; tab/process cap; renderer recovery; no automatic infinite reload. [17][27] |
| **Elevation of privilege** | Unsafe file/content/custom scheme, exported component, origin-confused permission, download execution, native bridge | Disable unnecessary file/content access; reject unsafe schemes; exact origin/resource checks; Android runtime permissions only after user action; never grant all requested resources; test OWASP WebView cases. [23][44] |

### 6.3 Conservative security defaults

- **SSL:** always cancel recoverable SSL errors; do not prompt users to make the certificate decision. [41]
- **Cleartext:** disallow cleartext for app-owned endpoints and security/update traffic; a general browser may choose to offer explicit, visible HTTP fallback for legacy sites rather than silently claiming HTTPS. [45]
- **JavaScript:** enable for ordinary browsing only because many sites require it; keep automatic popup windows disabled; expose no privileged bridge to arbitrary pages. [21][22]
- **Files:** disable file-scheme cookies and unnecessary file/content access; use controlled app assets through `WebViewAssetLoader` only if WebView is used. [23]
- **Mixed content:** explicitly use the most secure mode where supported. [6]
- **Permissions:** deny by default; verify exact origin and requested resource; request Android runtime permission only after a user action; handle denial and partial grants. [46]
- **Downloads:** require explicit user action; validate destination, name, MIME, and open/share intent; do not infer safety from extension or server MIME alone. [24]
- **Bridges:** no `addJavascriptInterface`, message channel, or native messaging for arbitrary sites. [21][44]
- **Private tabs:** no sync; delete cookies/site data/history/permissions on close according to the product definition. Downloads/bookmarks may remain and must be explained; private browsing is not anonymity. [47]

### 6.4 Privacy data decisions

| Data | Default decision | Rationale and user control |
|---|---|---|
| Normal history | Store locally to provide history; offer clear-by-time/site/all | A general browser needs history, but it is sensitive. Keep local; no server sync in MVP. |
| Bookmarks | Store locally; export/import optional | Useful and low complexity; export files are user data and should be clearly labelled. |
| Normal tabs | Store safe metadata for restore | Restore URL/title/selection where possible; do not promise exact page state. |
| Private tabs | Do not store in normal history or sync; purge on close | “Private” means reduced local retention, not anonymity from sites, ISPs, administrators, or networks. [47] |
| Cookies/site data | Engine-managed; provide per-site/all clear and private separation | Do not claim perfect profile isolation unless tested and guaranteed by the chosen engine. |
| Passwords/payment data | Exclude from MVP | Avoid high-risk secret storage until there is a dedicated design and security review. |
| Filter lists | Store source metadata, compiled state, hash/version, and rollback snapshots | Needed for explainability and reproducibility; lists are not browsing history. |
| Block decisions | Keep counters and limited rule/list IDs for current page; expire detailed data | User needs feedback, but raw URL logs can reveal browsing. Redact by default. |
| Crash/ANR reports | Opt-in; redact URLs, headers, cookies, form fields, and page content | Measure stability without creating a shadow history. Android vitals thresholds are not product results. [48] |
| Network updates | Permit only documented list/security endpoints; no background browsing telemetry | Test device-level egress, not only app HTTP logs. |
| Sync/cloud backup | Exclude from MVP | More privacy, authentication, encryption, deletion, and recovery work; Firefox explicitly says Sync is not online backup. [49] |

---

## 7. Dependencies, licensing, and maintenance

### 7.1 Planned dependency inventory

- Kotlin, Android SDK, Jetpack Compose, AndroidX, Jetpack WebKit only if the fallback is compiled.
- GeckoView Stable Maven artifact and selected Android Components modules.
- Optional Rust/adblock engine only after an integration spike; do not import a legacy FFI wrapper blindly.
- EasyList/EasyPrivacy only after list licensing/attribution review.
- Android system APIs for storage, DownloadManager or equivalent, runtime permissions, notifications, intents, and lifecycle.
- Test dependencies: AndroidX test/Espresso, Macrobenchmark, local fixture server, selected WPT tests, static analysis, dependency/SBOM tooling.

Pin versions and record license, source URL, checksum, transitive dependencies, ABI implications, and update owner. Mozilla says GeckoView/Android Components/Fenix source is in Mozilla Central rather than relying on the old GitHub checkout; use the current source-of-truth and a pinned prebuilt artifact. [3][18]

### 7.2 License operations

- Preserve MPL-2.0 notices for GeckoView/Android Components and any MPL-covered files; the MPL is file-level copyleft, but exact source-offer and modification obligations must be reviewed for the shipped bundle. [50]
- Chromium’s top-level license is not a complete third-party inventory; a Chromium fork requires all enabled component notices and a review of the no-endorsement language. [8]
- EasyList/EasyPrivacy carry their own dual-license and attribution conditions; external lists may differ. [39]
- `adblock-rust` is MPL-2.0; its license does not grant Brave trademark rights. [35]
- Brave’s Terms reserve rights in marks/logos and do not grant permission to ship a confusingly branded clone. [51]
- Generate an SBOM, store exact source revisions, publish a licenses/attributions screen, and ask counsel to review the final APK/AAB, filter data, scriptlets, resources, and trademark/name selection.

### 7.3 Maintenance reality

GeckoView releases follow the Firefox ecosystem and can make breaking changes after a deprecation window; the changelog shows ongoing changes in content blocking and extensions. Plan a monthly dependency review and a tested upgrade branch, even if the student MVP has no long-term public release promise. [17][18]

---

## 8. Current competitors and lessons

| Product | Useful documented pattern | Lesson for this project | Do not copy |
|---|---|---|---|
| **Brave Android** | Shields combines ad/tracker blocking, third-party cookie controls, HTTPS upgrades, fingerprinting defenses, script blocking, site settings, data clearing, and visible per-site feedback. [9][52] | Feature families are coherent when policy, UI, lists, storage, and exceptions are designed together. | Brave name/logo, UI art, exact presentation, proprietary code, product endpoints, or an “identical Shields” claim. |
| **Firefox for Android** | GeckoView-based ETP categories, Total Cookie Protection, private browsing, extensions, HTTPS-Only, Sync and data controls. [53][54] | A browser engine and protection policy can be separated cleanly; strict protection can break sites and needs exceptions. | Mozilla/Firefox branding, assets, code outside applicable licenses, or claims that your browser inherits Firefox’s data practices. |
| **Firefox Focus** | Minimal, deliberate erase/“get in, get out” privacy flow. [55] | A one-tap disposable session is a useful MVP feature, but it is not a full browser recovery model. | Product identity or implying your app is Focus. |
| **DuckDuckGo Android** | Tracker blocking, cookie protection, HTTPS upgrades, fingerprinting/link protections, Fire Tabs, Fireproof Sites, and encrypted recovery concepts; its own documentation also describes WebView/platform limits. [42][56] | Transparent exceptions and deletion UX matter; platform limits should be documented rather than hidden. | Brand, assets, service-specific code, or claims of their exact engine/policy. |
| **Privacy Browser Android** | WebView-based general browser with explicit per-domain settings, EasyList blocking, and a minimization-oriented design. [57] | A smaller, explicit settings surface is feasible; WebView still requires careful security boundaries. | GPL code/assets unless compliance and license compatibility are deliberately handled; no “zero telemetry” claim without evidence. |

These products are comparison points, not implementation templates. Their documented data practices, code licenses, dependencies, lists, and engine details are not interchangeable.

---

## 9. Precise first vertical slice

The first vertical slice should be small enough to finish, but cross the entire product boundary:

> **A user can open the app, enter an HTTPS URL for a local test page, see it rendered in a GeckoView tab, navigate back/forward, observe a synthetic third-party tracker request blocked by the selected protection policy, open a native privacy panel showing a scoped counter and reason, temporarily disable protection for that origin, reload successfully, then close and reopen the app with the normal tab metadata restored. A private tab performs the same visit but leaves no normal-history record after close.**

### Slice implementation checklist

1. Create Android Studio project with Kotlin/Compose and Java 17 compatibility.
2. Add pinned GeckoView Stable and minimal AC session/state dependencies.
3. Create one `GeckoRuntime` and one `GeckoSession`.
4. Build one screen: address/search field, back, forward, reload, privacy button, and page surface.
5. Add a local HTTPS fixture server/debug certificate for test-only pages.
6. Implement URI/search parsing and scheme/host validation.
7. Load the fixture page and observe navigation callbacks.
8. Configure one selected GeckoView protection category.
9. Add a synthetic tracker resource and verify its decision using the engine-supported API/diagnostic path; if a custom list is not yet supported, label the result as engine-native protection rather than pretending it is EasyList coverage.
10. Store a scoped count and reason for the page load; do not log full browsing history or raw cookies.
11. Add per-origin temporary disable and reload.
12. Add normal/private session distinction and a basic normal-tab metadata store.
13. Kill/relaunch the app and restore the normal tab URL/title.
14. Close the private tab and verify no normal history/cookie record remains according to the chosen engine APIs.
15. Test TalkBack labels, rotation, background/foreground, offline/error state, and no native bridge.

### Slice exit criteria

- The fixture page loads on two devices/emulators and two Android API levels.
- Back/forward/reload works without duplicate navigation loops.
- The synthetic tracker outcome is reproducible with filtering on/off.
- Privacy status is visible, scoped, and reversible.
- Normal tab metadata restores after process death/relaunch.
- Private-tab data is not written to normal history in the tested path.
- No SSL bypass, arbitrary bridge, exported sensitive component, or release cleartext exception exists.
- A beginner can explain which decisions are GeckoView guarantees, which are app policy, and which remain unmeasured.

Do not start with a polished home page, account system, full filter catalog, or a copied Shields-like panel. This slice proves the product’s hardest integration: engine + UI + privacy decision + state + recovery.

---

## 10. Phased roadmap

Durations below are **planning assumptions**, not measured estimates or guarantees. A P0 security/privacy finding blocks the next phase.

| Phase | Duration | Work | Deliverables | Exit criteria | Tests |
|---|---:|---|---|---|---|
| **1. Product, threat model, and legal inventory** | 1 week | Define general-purpose scope, privacy promise, non-goals, abuse cases, distinct name shortlist, dependency/list license inventory. | PRD; threat model; data map; feature matrix; license register; explicit no-site-targeting/no-impersonation policy. | Written scope is site-agnostic; no Brave-copy requirement remains. | Threat-model review; dependency/SBOM scan; trademark/name review by qualified reviewer. |
| **2. GeckoView engine spike** | 2 weeks | Add GeckoView; create runtime/session; load corpus; inspect process/lifecycle; create WebView contrast adapter only if useful. | Minimal loader; pinned version; capability matrix; baseline logs/screenshots. | Corpus loads; back/forward, JS/forms, media, downloads, and process recreation are acceptable on two profiles. | Smoke navigation; process kill; crash log review; baseline screenshot/console tests. |
| **3. Native shell and intents** | 2 weeks | Compose UI, address/search, toolbar, error page, `ACTION_VIEW`, theme, accessibility foundations. | Address bar; navigation; intent filters; error/recovery UI; settings skeleton. | User can open a URL from another app, search, navigate, and recover from invalid/offline input. | Instrumented UI; intent resolution; rotation/background; accessibility scan/TalkBack smoke. |
| **4. Tabs, state, and profiles** | 3 weeks | Tab coordinator; one session/tab; history/bookmarks; normal/private boundaries; durable metadata; migrations. | Tab tray; state store; history/bookmarks; private-session store; restore flow. | No cross-profile history/cookie leakage in fixtures; deterministic restore after kill. | Reducer/property tests; database migration; cookie/storage isolation; process-death tests. |
| **5. Privacy baseline and exceptions** | 3 weeks | Enable Gecko content blocking; implement site policy/exception; HTTPS-first; cookie/storage controls; privacy panel; counters. | Protection settings; per-site policy; scoped counter; temporary disable/reload; HTTP fallback interstitial. | Every protection has a visible status and escape hatch; no arbitrary native bridge; no hard-coded site exceptions. | Filter/protection golden tests; synthetic trackers; cookie tests; permission abuse; mixed-content/HTTP tests. |
| **6. Lists and browser essentials** | 3 weeks | License-review EasyList/EasyPrivacy; parse metadata; update transaction; downloads, file chooser, share, find-in-page; decide whether custom filter bridge is deferred. | List screen; source/license metadata; atomic update/rollback; download manager; file chooser. | Failed list update retains last-known-good; downloads and forms work on representative fixtures. | List parser golden tests; corrupt/huge update tests; upload/download MIME matrix; redirects/intents; login/forms. |
| **7. Security, performance, resilience** | 4 weeks | ANR/crash triage; memory/battery profiles; secure build; egress allowlist; renderer recovery; release signing. | Security checklist; redacted opt-in diagnostics; Macrobenchmarks; signed debug/release builds; rollback plan. | Zero open P0/P1 security issues; no reproducible data leak; agreed performance targets met or re-scoped. | 8–24 hour soak; low-memory/process kill; offline/network changes; malformed URL/filter fuzzing; static/native scans. |
| **8. Beta and cross-site QA** | 3 weeks | Diverse site corpus, accessibility, localization, known-issues triage, privacy review, feedback process. | Beta APK/AAB; device/API matrix; known issues; privacy dashboard; release notes. | Proposed 95% critical-workflow completion on corpus, no site-specific tuning, all failures reproducible/classified. | WPT subset; real-site manual matrix; screenshot/DOM/network/accessibility diffs; battery/network tests; red-team review. |
| **9. Distribution and capstone** | 3 weeks release + optional 4–8 weeks engine lab | Notices/source offers, Play/sideload process, signing, clean install/update/rollback; optional toy engine lab only. | Play-ready AAB; independently signed APK; licenses; source offer; privacy policy; release notes; optional tokenizer/parser/DOM demo. | Build installs, upgrades, and rollback; notices live; user can verify signing; student can explain all engine/app boundaries. | Signature upgrade; clean install/update/uninstall; sideload stock Android; notice/link checks; toy-engine snapshot tests. |

Estimated MVP total: **22–28 weeks**, assuming the learner can work consistently, uses prebuilt artifacts, and does not fork Gecko/Chromium or build a full custom filter bridge.

---

## 11. Validation matrix and provisional targets

### 11.1 Test corpus and devices

Use a hermetic local HTTP/HTTPS fixture server and debug-only test CA. Cover:

- same/cross-origin requests, redirects, iframes, sandbox, CSP/SRI;
- cookies/SameSite, cache, storage, service workers;
- fetch/XHR/WebSocket/SSE;
- popups/new windows, downloads, upload/file chooser;
- login/form fields, media/fullscreen, permission prompts;
- malformed HTML/CSS/JS, long pages/images, slow/reset/DNS/offline/captive portal;
- custom schemes, file/content URLs, IDN-like domains;
- synthetic ads, trackers, first-party trackers, CNAME-like aliases, and anti-adblock-shaped pages.

Add a modest set of unrelated real sites only after the fixture suite is stable. Do not make one production site the acceptance test. Use CTS/device testing where applicable, Firebase Test Lab for a broader device matrix, and a pinned WPT subset for web-platform behavior. [58][59][60]

### 11.2 Validation matrix

| Area | Method | Evidence to retain | Initial gate |
|---|---|---|---|
| Navigation/origin | Unit tests + fixture pages + intent tests | URI classification, origin display, redirect traces | 100% of safety cases; no unsafe custom-scheme handoff |
| Protection | Filtering on/off, each list alone, combined lists, engine-native categories | Decision record with redacted URL hash, rule/list ID, context, result | Every displayed block has a reproducible reason or is labelled engine-native/unknown |
| Exceptions | Per-site disable, temporary page disable, reload | Before/after decision and UI state | Exception works without global disable; no hard-coded target-site rule |
| Cookies/storage | Normal/private visits; clear site/all/time range | Storage inventory before/after; history assertions | No tested private data in normal history; clearing is deterministic where API supports it |
| Permissions | Camera/mic/location/notifications, denial/partial grant, origin changes | Prompt screenshots and permission state | Deny-by-default; no origin confusion; runtime permission precedes web grant |
| Downloads/files | MIME/name/redirect/auth/cookie cases; malicious-looking filenames | Download policy logs, destination and open-intent assertions | Explicit confirmation; no automatic execution; safe storage API |
| Security | TLS errors, cleartext, file/content/custom schemes, bridges, exported components | Static scans, test results, egress capture | Zero release cleartext for app endpoints; zero arbitrary native bridge; invalid TLS cancelled |
| Lifecycle | Rotate, background, process kill, renderer/service loss, low memory | Recovery traces and user-visible state | No reload loop; safe reopen/recovery path within agreed budget |
| Accessibility | TalkBack, Switch Access, Voice Access, large text, contrast, focus | Accessibility tree snapshots/manual checklist | All browser controls have name/role/state/focus; protection UI remains understandable |
| Performance | Macrobenchmark cold/warm launch, navigation, tabs, scroll, list update | p50/p90/p95 JSON/traces, device/build/list revision | Targets below are provisional; report distributions, not one best run |
| Power/network | Fixed 30-minute feature-on/off scenario; device packet capture | CPU, wakeups, bytes, radio-active time, mAh/hour | Zero unapproved egress; feature delta compared to identical baseline |
| Release | Clean install, upgrade, rollback, sideload, Play test track | Signed artifacts, hashes, install/update logs, notices | Stable signing key path; licenses/source offer present; rollback rehearsed |

### 11.3 Provisional targets, not measured results

The following are **engineering targets/assumptions only**. No measurements were obtained by this research:

- p95 cold launch: **≤ 2.5 seconds** on a named mid-range test device;
- p95 local-fixture navigation-to-first-render: **≤ 1.5 seconds**;
- p95 protection decision overhead: **≤ 5 ms/request** where directly measurable;
- scripted 60 Hz scroll: **≤ 1% missed frames** in the chosen scenario;
- 30-minute feature-on battery median delta: **≤ 5%**, p95 **≤ 10%** versus identical feature-off baseline, reported with CPU/wakeups/network bytes;
- provisional crash-free sessions **≥ 99.9%** and ANR **< 0.1%** after the beta denominator is defined;
- renderer/process recovery to usable error/reopen UI **≤ 5 seconds** without an automatic reload loop;
- unexpected request-level false positives **≤ 0.1%** on a known-good fixture corpus and **0% critical-journey breakage**; broader corpus breakage target **≤ 1%** after corpus/denominator definition;
- **zero unapproved egress** and **zero release cleartext** for app-owned endpoints.

Android vitals publishes Play visibility thresholds of 1.09% overall crash and 0.47% overall ANR; those are not this product’s results or necessarily its SLOs. Network Inspector is incomplete for a browser because it primarily observes HttpsURLConnection/OkHttp traffic, so use device-level capture for egress validation. [48][61]

---

## 12. Sideload, Play, and release caveats

- Every installed/updated APK must be digitally signed. Keep the publisher key stable; use a separate Play upload key if applicable; publish SHA-256 fingerprints through a trusted channel. [62]
- Play App Bundles, Play App Signing, direct APK distribution, and alternative stores have different operational paths. On Android 8.0+, users must opt in to installing unknown apps for the specific source, and network providers may block unknown-source installs. [63]
- New Play submissions/updates have target API requirements that change over time. The research snapshot recorded API 36 for new apps/updates and API 35 for certain existing-app requirements; re-check the current policy immediately before release. [64]
- Complete Google Play Data Safety disclosures for the app and SDK practices. Internal-only testing has different treatment from public distribution. [65]
- Obtain product-specific policy review for cross-site ad/tracker blocking. The reviewed policy text did not expressly classify this exact browser feature; do not assume approval from a generic browser precedent.
- Do not self-update outside the permitted distribution model or download native executable code into the app. Browser JavaScript/interpreter behavior is different from downloading native code, but keep all update and code-loading paths documented and policy-reviewed. [66]
- Keep debug CA trust, verbose network logs, fixture URLs, test keys, and unsafe diagnostic switches out of release builds.
- Release a privacy policy that says exactly what is stored, what leaves the device, which update endpoints are contacted, whether crash reports are opt-in, and how users clear data.
- Before public release, complete dependency/list license audit, trademark/name clearance, accessibility review, security review, signing rehearsal, upgrade/rollback rehearsal, and incident-response contact.

---

## 13. Beginner learning sequence

Study and build in this order:

1. **Kotlin basics:** nullability, sealed classes, coroutines, flows, immutable state, testing.
2. **Android Studio/project basics:** Gradle, manifests, resources, build variants, logcat, debugger, emulator/device deployment.
3. **Compose and lifecycle:** state hoisting, navigation, accessibility semantics, configuration changes, background/foreground.
4. **Android storage and intents:** Room or a small database, scoped storage, DownloadManager, `ACTION_VIEW`, file chooser, sharing.
5. **Web fundamentals:** URI/URL, origin, cookies/SameSite, HTTP methods/status, TLS, redirects, CSP, CORS, service workers, WebSockets, iframes.
6. **GeckoView:** runtime/session/view, delegates, navigation/history, permissions, downloads, content blocking, process architecture.
7. **Browser state machines:** tabs, sessions, history, bookmarks, normal/private separation, process-death restoration.
8. **Privacy filtering:** ABP syntax basics, list metadata, exceptions, rule classes, parser/compiled cache, false positives, update rollback.
9. **Android security:** cleartext, unsafe URI/file inclusion, WebView bridges, runtime permissions, exported components, secure storage, safe downloads.
10. **Testing:** unit/property tests, instrumented UI tests, fixtures, screenshot/DOM/network diffs, Macrobenchmark, accessibility, device matrix.
11. **Release engineering:** SBOM, notices, signing, Play/sideload, privacy policy, staged rollout, crash/ANR response.
12. **Optional advanced lab:** a toy tokenizer/parser/DOM/layout engine, explicitly not a production browser and not a release dependency.

Read official docs while implementing the corresponding feature rather than trying to understand all browser internals first.

---

## 14. Failure modes and pivots

| Failure signal | Likely cause | Safe pivot |
|---|---|---|
| GeckoView cannot meet a critical media/auth flow | Engine/device/provider compatibility gap | Reduce support promise; create a reproducible case; test updated Stable; only then evaluate Chromium fork. |
| WebView fallback misses important tracker requests | Documented interception blind spots | Use GeckoView content blocking; do not market WebView counters as complete. |
| Custom filter bridge consumes the schedule | JNI/Rust/native lifecycle complexity | Ship engine-native categories plus licensed-list attribution; defer custom bridge. |
| EasyList/EasyPrivacy causes breakage | List scope, parser mismatch, false positive | Disable one list; add scoped exception; pin/revert last-known-good; do not add site-specific hacks. |
| Strict anti-tracking breaks logins/media | Engine protection trade-off | Default to conservative category; provide per-site exception and explain the trade-off. |
| Memory/renderer crashes on low-end devices | Too many tabs, heavy pages, engine pressure | Bound tabs; suspend/close background sessions; improve recovery; re-scope device range. |
| Privacy UI requires raw browsing logs | Counter/report architecture is over-detailed | Store current-page counts and rule/list IDs only; redact and expire diagnostics. |
| Permission/download surface is unsafe | Native UI is too coupled to page events | Deny by default; verify origin/resource; add explicit user gesture; delay feature. |
| Play/policy or licensing review blocks release | Unresolved distribution/data/list terms | Sideload a test build only; remove disputed data/assets; seek counsel; do not quietly ship. |
| Beginner falls behind on scope | Too many “browser” features | Freeze to vertical slice + Tier 0/Tier 2 basics; exclude sync, passwords, Tor, extensions, DRM promises. |
| Need a single-service workaround | Product direction drifting | Reject site-targeting; return to general test fixtures and cross-site policy. |

### Hard stop conditions

Stop and redesign if the implementation requires:

- a privileged native bridge accessible to arbitrary origins;
- silently proceeding through TLS errors;
- copying another browser’s name, icon, UI art, or assets;
- hard-coded selectors/rules for a named site;
- bypassing DRM, paywalls, bot detection, or access controls;
- hiding the true origin or imitating another browser’s security UI;
- claiming privacy or compatibility results that have not been measured;
- storing full browsing history/credentials in telemetry “for debugging.”

---

## 15. Explicit exclusions

This roadmap excludes:

- a Spotify-specific, YouTube-specific, or any other single-service browser/client;
- site-targeted selectors, hard-coded domains, anti-adblock evasion, paywall/DRM bypass, or service-specific user-agent spoofing;
- impersonation of Brave, Firefox, Chrome, DuckDuckGo, Privacy Browser, or any other product;
- copying names, logos, icons, screenshots, trade dress, UI art, filter-list branding, proprietary code, endpoints, or assets without applicable license/permission;
- a claim of “exactly the same as Brave,” “Brave Shields,” or equivalent parity;
- anonymous browsing, Tor, VPN, device-wide traffic filtering, or a network tunnel in the MVP;
- a new production HTML/CSS/JavaScript engine;
- a full browser extension store, password manager, payment autofill, account system, cloud sync, encrypted backup, or complete DRM/WebAuthn support claim;
- universal anti-fingerprinting, complete storage partitioning, perfect CNAME/bounce tracking protection, or complete request visibility;
- unlicensed or unreviewed third-party filter lists, scriptlets, resources, or data services.

---

## 16. Evidence gaps and decisions still required

The supplied research did not establish:

- the exact GeckoView Stable version, minimum Android API, ABI split, APK size, startup, memory, and battery results for this product;
- complete compatibility across OEMs, devices, codecs, DRM, WebAuthn, media, PWA flows, permissions, and future engine releases;
- exact per-site exception semantics and full custom filter-list coverage through the selected GeckoView artifact;
- whether the chosen release can integrate `adblock-rust` without a maintained native/JNI bridge;
- the complete license/provenance terms for every list, scriptlet, resource, dependency, and Safe Browsing provider;
- a universal false-positive rate, battery cost, latency, crash rate, or egress measurement;
- Play’s final product-specific treatment of cross-site ad/tracker blocking;
- Focus-specific retention details, DuckDuckGo’s fully documented Android renderer, or every competitor’s dependency/license inventory;
- trademark clearance for the new product name and icon.

These gaps are **work items and unknowns**, not reasons to invent claims. Resolve them with a pinned build, a named device/API matrix, a fixture corpus, legal review, and measured release reports.

---

## Conclusion

Ship a **small, transparent, site-agnostic GeckoView browser** first. Implement the browser fundamentals and selected protections that can be explained, tested, reversed, and measured. Use WebView as a contrast/fallback, not as a promise of complete browser-level filtering. Treat custom adblock-rust integration, strict anti-fingerprinting, sync, Tor, and Chromium forking as later decisions requiring evidence and capacity.

The right beginner outcome is not a clone. It is an independently designed Android browser that teaches engine embedding, lifecycle, state, web security, privacy policy, filtering, accessibility, testing, licensing, and release operations without targeting a single site or making unsupported parity claims.

---

## References

[1]: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/index.html "Mozilla GeckoView overview"
[2]: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html "GeckoView quick start"
[3]: https://searchfox.org/firefox-main/source/mobile/android/android-components/README.md "Android Components README in Mozilla Central"
[4]: https://developer.android.com/reference/android/webkit/WebView "Android WebView API reference"
[5]: https://developer.android.com/develop/ui/views/layout/webapps/webview "Build web apps in WebView"
[6]: https://developer.android.com/develop/ui/views/layout/webapps/managing-webview "Manage WebView objects"
[7]: https://chromium.googlesource.com/chromium/src/+/main/docs/android_build_instructions.md "Chromium Android build instructions"
[8]: https://chromium.googlesource.com/chromium/src/+/main/LICENSE "Chromium source license"
[9]: https://brave.com/shields/ "Brave Shields"
[10]: https://support.brave.com/hc/en-us/articles/360022973471-What-is-Shields "What is Shields?"
[11]: https://chromium.googlesource.com/chromium/src/+/HEAD/android_webview/docs/webview-providers.md "Chromium Android WebView providers"
[12]: https://developer.android.com/reference/android/webkit/WebViewClient "WebViewClient API reference"
[13]: https://developer.android.com/reference/android/webkit/ServiceWorkerController "ServiceWorkerController API reference"
[14]: https://developer.android.com/reference/android/webkit/ServiceWorkerClient "ServiceWorkerClient API reference"
[15]: https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/ContentBlocking.html "GeckoView ContentBlocking API"
[16]: https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.html "GeckoSession API"
[17]: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-architecture.html "GeckoView architecture"
[18]: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/design/breaking-changes.html "GeckoView breaking changes"
[19]: https://github.com/brave/brave-core "Brave Core repository"
[20]: https://developer.android.com/reference/android/webkit/CookieManager "CookieManager API reference"
[21]: https://developer.android.com/privacy-and-security/risks/insecure-webview-native-bridges "WebView native bridges"
[22]: https://developer.android.com/privacy-and-security/risks/cross-app-scripting "Cross-app scripting"
[23]: https://developer.android.com/privacy-and-security/risks/webview-unsafe-file-inclusion "WebView unsafe file inclusion"
[24]: https://developer.android.com/privacy-and-security/risks/unsafe-download-manager "Unsafe Download Manager"
[25]: https://developer.android.com/reference/android/webkit/WebChromeClient "WebChromeClient API reference"
[26]: https://support.mozilla.org/en-US/kb/https-only-mode-firefox-android "HTTPS-Only Mode in Firefox for Android"
[27]: https://developer.android.com/develop/ui/views/layout/webapps/handle-termination "Handle WebView termination"
[28]: https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/ContentBlocking.AntiTracking.html "GeckoView AntiTracking API"
[29]: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html "GeckoView site permissions"
[30]: https://github.com/easylist/easylist "EasyList official repository and policy"
[31]: https://help.adblockplus.org/adblock-plus-help-center/how-to-write-filters "Adblock Plus filter syntax"
[32]: https://github.com/gorhill/uBlock/wiki/static-filter-syntax "uBlock Origin static filter syntax"
[33]: https://developer.chrome.com/docs/extensions/reference/api/declarativeNetRequest "Chrome declarativeNetRequest API"
[34]: https://github.com/uBlockOrigin/uBOL-home/wiki/Frequently-asked-questions-(FAQ) "uBlock Origin Lite FAQ"
[35]: https://github.com/brave/adblock-rust "Brave adblock-rust repository"
[36]: https://docs.rs/adblock/latest/adblock/struct.Engine.html "adblock Engine API"
[37]: https://github.com/brave/adblock-rust-ffi "adblock-rust FFI repository"
[38]: https://github.com/gorhill/uBlock/wiki/Dashboard:-Filter-lists "uBlock Origin filter-list dashboard"
[39]: https://easylist.to/pages/licence.html "EasyList repository licenses"
[40]: https://developer.android.com/develop/ui/views/layout/webapps/managing-webview#safe-browsing "Android WebView Safe Browsing guidance"
[41]: https://developer.android.com/reference/android/webkit/SslErrorHandler "SslErrorHandler API reference"
[42]: https://duckduckgo.com/duckduckgo-help-pages/privacy/web-tracking-protections "DuckDuckGo Web Tracking Protections"
[43]: https://developer.android.com/privacy-and-security/security-config "Network Security Configuration"
[44]: https://mas.owasp.org/MASTG-KNOW-0018/ "OWASP MASTG WebViews"
[45]: https://developer.android.com/privacy-and-security/risks/cleartext-communications "Cleartext communications"
[46]: https://developer.android.com/guide/topics/permissions/overview "Android permissions overview"
[47]: https://support.brave.com/hc/en-us/articles/360017840332-What-is-a-Private-Window "Brave private window limitations"
[48]: https://developer.android.com/google/play/vitals "Android vitals"
[49]: https://support.mozilla.org/en-US/kb/how-set-firefox-sync-firefox-android "Firefox Sync and backup caveat"
[50]: https://www.mozilla.org/en-US/MPL/2.0/FAQ/ "Mozilla Public License FAQ"
[51]: https://brave.com/terms-of-use/ "Brave Terms of Use"
[52]: https://support.brave.app/hc/en-us/articles/360022806212-How-do-I-use-Shields-while-browsing "Using Shields while browsing"
[53]: https://support.mozilla.org/en-US/kb/enhanced-tracking-protection-firefox-android "Enhanced Tracking Protection in Firefox for Android"
[54]: https://www.firefox.com/en-US/browsers/mobile/android/ "Firefox for Android"
[55]: https://www.firefox.com/en-US/mobile/focus/ "Firefox Focus"
[56]: https://duckduckgo.com/duckduckgo-help-pages/privacy/fire-tabs "DuckDuckGo Fire Tabs and Fire Button"
[57]: https://www.stoutner.com/privacy-browser-android/ "Privacy Browser Android"
[58]: https://developer.android.com/studio/test/test-in-android-studio "Android Studio and Firebase Test Lab"
[59]: https://source.android.com/docs/compatibility/cts "Android Compatibility Test Suite"
[60]: https://web-platform-tests.org/ "web-platform-tests"
[61]: https://developer.android.com/studio/debug/network-profiler "Android Network Inspector"
[62]: https://developer.android.com/studio/publish/app-signing "Sign your app"
[63]: https://developer.android.com/distribute/marketing-tools/alternative-distribution "Alternative distribution options"
[64]: https://support.google.com/googleplay/android-developer/answer/11926878?hl=en "Google Play target API requirements"
[65]: https://support.google.com/googleplay/android-developer/answer/10787469?hl=en "Google Play Data safety"
[66]: https://support.google.com/googleplay/android-developer/answer/16559646?hl=en "Google Play Device and Network Abuse"
[67]: https://easylist.to/easylist/easylist.txt "Canonical EasyList subscription"
[68]: https://easylist.to/easylist/easyprivacy.txt "Canonical EasyPrivacy subscription"
