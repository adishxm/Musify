# Musify: Universal Blocker, Protected Playback, and Desktop Mode Roadmap

**Repository reviewed:** `adishxm/Musify`  
**Reviewed commit:** `e8973c15991afbcf0945de8bc4369e3dce84f4a6`  
**Date:** 2026-10-13  
**Target:** Android browser shell with general-purpose privacy protection and a reliable desktop-style web mode

## 1. What the attached failure means

The screenshot shows Spotify’s **“Playback disabled”** page. Spotify says this appears when protected content is blocked, the browser is incompatible, or the browser is in private browsing. Its current support guidance requires a supported browser and, for the protected-content error, an enabled Widevine/DRM path and allowed protected-content permissions. [1]

This is not evidence that one ad filter rule is wrong. It is a compatibility failure at the boundary between the web engine, browser profile, media/DRM capability, cookies/storage, and privacy policy. A blocker must not block the media license, media-key, audio, session, or other first-party resources required for playback.

The correct product goal is:

> **A site-agnostic browser that blocks documented third-party advertising and tracking resources while preserving required first-party application, authentication, media, and DRM flows.**

Do not implement a Spotify-only bypass, hard-coded Spotify allowlist, anti-adblock evasion, or user-agent spoof intended to defeat a service’s controls. Those approaches are fragile, difficult to test, and conflict with Musify’s existing general-purpose-browser scope. The user should be able to apply a transparent, reversible per-origin exception when a site breaks.

## 2. Repository findings

### What is already in place

- The app is a Kotlin/Jetpack Compose Android browser shell with a clean `EngineAdapter` boundary.
- GeckoView is the primary engine candidate; WebView is a fallback/prototype.
- `GeckoEngineAdapter` enables strict anti-tracking, strict ETP, Safe Browsing, and first-party cookie acceptance.
- The app has a per-origin shield exception model, a visible shield counter, renderer recovery UI, tab/session storage, and filter-list screens.
- The repository already documents an important limitation: GeckoView’s public `ContentBlocking` API is engine-native protection, not a Java API for loading arbitrary raw ABP lists. [2]
- GeckoView officially supports bundled WebExtensions, and its documentation describes extensions as the preferred way to interact with web content. [3]

### The main implementation gap

`FilterEngine.evaluate(url)` is an offline Kotlin evaluator. It is created in `MainActivity` and passed to `FilterListScreen`, but it is not passed into `TabsViewModel`, `TabEnginePool`, `EngineAdapter`, or `GeckoEngineAdapter`.

Therefore, the current flow is effectively:

```text
Downloaded/asset filter list
        -> Kotlin FilterEngine
        -> filter-list UI and unit tests

Live page request
        -> GeckoView internal ETP only
        -> ContentBlocking.Delegate counter
```

There is no production path from a live request to `FilterEngine.evaluate()`. The current counter also counts GeckoView content-blocking callbacks, not every request evaluated by Musify’s custom lists. It must be labelled accordingly until a real request-decision pipeline exists.

### Additional gaps relevant to the screenshot

1. `GeckoEngineAdapter` has no explicit media/DRM capability state, protected-content diagnostic, or playback recovery flow.
2. There is no visible distinction between **tracker blocking**, **ad/resource blocking**, and **protected media compatibility**.
3. There is no desktop-mode state or user-facing desktop/mobile content-mode control.
4. `GeckoEngineAdapter.setProtectionEnabled()` mutates shared runtime content-blocking settings. This can create cross-tab and cross-origin behavior that is difficult to reason about. Protection should be represented as a policy snapshot applied to the relevant session/origin, with explicit tests for tab isolation.
5. The shield exception currently disables protection for an origin but does not record the reason, scope, expiry, or which protection categories were relaxed.
6. The repository test command could not run in this sandbox because the Android SDK location is not configured. The Gradle wrapper itself first lacked the executable bit; after correcting that locally, Gradle failed with `SDK location not found`. This is an environment limitation, not a passing test result.

## 3. Target architecture

Use one policy pipeline with two possible enforcement backends. Do not maintain an offline compiler that is disconnected from the engine.

```text
Compose browser UI
   |
BrowserCoordinator / TabsViewModel
   |
PrivacyPolicyService
   |-- global settings
   |-- per-origin policy
   |-- desktop/mobile content mode
   |-- media compatibility policy
   |
EngineAdapter
   |-- GeckoEngineAdapter
   |     |-- Gecko content blocking / ETP
   |     |-- bundled WebExtension blocker
   |     |-- media and permission delegates
   |     |-- request/block telemetry for current page
   |-- WebViewAdapter (bounded fallback only)
         |-- WebView request hooks
         |-- ServiceWorker hook installed before first page
         |-- honest limitations; no universal-blocking claim
   |
DecisionStore
   |-- current-page counters
   |-- decision reason/category
   |-- per-origin exception and expiry
   |-- list version/checksum
```

### Recommended enforcement order

**Phase A: GeckoView native ETP.** Keep this as the baseline because it is already integrated and runs inside the engine.

**Phase B: bundled GeckoView WebExtension.** Move supported ABP-compatible network rules into an actual WebExtension using `webRequest`/blocking or the appropriate supported WebExtension API for the pinned GeckoView version. The extension should own rule matching and request decisions; the native app should own settings, origin policy, counters, list updates, and lifecycle. GeckoView supports built-in extensions under the APK’s assets directory. [3]

**Phase C: native bridge only if measured necessary.** Investigate `adblock-rust`/FFI or another maintained engine-integrated backend only if the WebExtension path cannot meet measured performance, coverage, or lifecycle requirements. Do not start with JNI. A native bridge increases memory-safety, upgrade, ABI, and release complexity.

The existing Kotlin compiler can remain as a **validator, importer, and compatibility-report tool**, but it should not be marketed as the live blocker until it is connected to an enforcement backend.

## 4. Spotify playback recovery workstream

Treat Spotify as one representative media web app in a broader compatibility corpus. The goal is not to weaken protection globally.

### 4.1 Add capability diagnostics

Add a `MediaCapabilityState` to `EngineAdapter` with at least:

- `javascriptEnabled`
- `cookiesEnabled`
- `thirdPartyCookiePolicy`
- `encryptedMediaSupported`
- `widevineDetected` or `widevineUnknown`
- `autoplayPolicy`
- `privateMode`
- `userAgentMode`
- `lastMediaFailureReason`

The UI should expose a diagnostic screen, not raw secrets or license payloads. It should report actionable states such as:

- “Protected media is unavailable in this engine/device profile.”
- “This tab is private; protected playback may be unavailable or unsupported.”
- “A privacy rule blocked a request required by the page.”
- “Playback was not blocked by Musify; the engine returned a DRM or network error.”

Widevine is the platform’s protected-media system and uses Encrypted Media Extensions in browser playback. Support depends on the device and browser/engine integration; Google documents Android and major desktop browser platforms as supported, but a product must still test the actual embedded engine/device matrix. [4]

### 4.2 Preserve required media flows

Implement request classification before enabling custom blocking:

- top-level navigation and redirects;
- first-party document, script, stylesheet, font, image, audio, and media requests;
- authentication/session endpoints;
- EME/media-key/license traffic where exposed by the engine;
- service-worker requests;
- WebSocket and fetch/XHR traffic needed by the application;
- third-party requests that are classified as advertising/tracking.

The default policy must never block by URL substring alone. A decision needs at least the top-level origin, request origin, resource type, party relationship, matched rule ID/list, and policy state. If the engine does not expose a request type or protected-media signal, classify the result as **unknown** rather than pretending it was safely blocked.

### 4.3 Recovery UX

When playback fails:

1. Show a non-alarming **Playback compatibility** panel.
2. Display whether Shields are up, the current origin, private/normal mode, desktop/mobile mode, and whether a block decision occurred.
3. Offer **Temporarily relax blocking for this origin** for one reload, or a persistent origin policy after an explicit user action.
4. Reload the page after applying the scoped policy.
5. Keep the user’s global protection settings unchanged.
6. Provide **Restore protection** and show the exception in Settings.

The panel must not silently disable all protection and must not claim that the user can bypass DRM. If Widevine/EME is unavailable, direct the user to the engine/device compatibility explanation instead of attempting to intercept or decrypt protected media.

## 5. Universal blocker implementation roadmap

### Milestone 0 — Reproducible baseline and instrumentation

**Deliverables:**

- Configure Android SDK/JDK/Gradle in CI and document local setup.
- Add a `./gradlew doctor` or equivalent diagnostic task that prints SDK, JDK, ABI, GeckoView, and WebView provider information.
- Add a real-device test matrix: at least Android 8, Android 12+, and Android 15; arm64 devices; current system WebView; GeckoView pinned version.
- Add request/decision logging in debug builds only. Redact query strings, tokens, cookies, license payloads, and credentials.
- Add a local fixture corpus for ordinary sites, synthetic ads/trackers, service workers, media, cookies, WebSockets, redirects, and anti-adblock-shaped pages.

**Exit criteria:** the same navigation produces a reproducible decision trace on a clean install and after process death. No production telemetry is introduced.

### Milestone 1 — Fix policy ownership and state isolation

**Deliverables:**

- Create `PrivacyPolicyService` and inject it into `TabsViewModel` and every engine adapter.
- Replace global mutable shield behavior with explicit `ProtectionProfile` snapshots.
- Define origin normalization as scheme + host + effective port.
- Define scopes: current reload, current tab, origin, or global.
- Add expiry for temporary exceptions.
- Separate counters into `engineNativeBlocked`, `customNetworkBlocked`, `cosmeticUnsupported`, and `unknown`.
- Add a reason code and source list to every displayed count.

**Exit criteria:** disabling protection for one origin does not alter another tab or another origin; process restoration preserves only intended policy metadata.

### Milestone 2 — Integrate the blocker into GeckoView

**Preferred path:** package a minimal WebExtension under `app/src/main/assets/` and install it once per `GeckoRuntime`.

**Deliverables:**

- WebExtension manifest with a stable ID and pinned version.
- Background/request module for supported network blocking.
- Rule compiler export format with explicit unsupported-rule reporting.
- Native-to-extension messages for global protection, origin exceptions, list updates, and current-page counters.
- Extension lifecycle handling for runtime restart and app upgrade.
- Tests for rule precedence: exception over block, first-party policy, subresource type, redirects, and unknown types.
- Compatibility adapter for GeckoView API changes; do not scatter GeckoView calls across UI code.

**Exit criteria:** a synthetic page’s blocked request is prevented before load, the counter reports the matching rule/list, and turning protection off causes the same page to load after a controlled reload.

### Milestone 3 — List update and rollback system

**Deliverables:**

- Keep Peter Lowe as the initial bundled seed if licensing and attribution remain correct.
- Treat EasyList/EasyPrivacy as opt-in subscriptions until licensing and engine coverage are verified.
- Store source URL, license, attribution, version, checksum, fetched time, supported rule classes, and unsupported rule count.
- Stage updates in a temporary file, validate bounds and syntax, compile off the main thread, then atomically activate.
- Retain last-known-good data and roll back on parse, memory, checksum, or runtime failures.
- Never execute filter-list JavaScript or scriptlets in native code.
- If cosmetic filtering is added through the WebExtension, keep it separately labelled from network blocking.

**Exit criteria:** a corrupt, oversized, or incompatible update cannot replace a working list; the UI tells the user exactly what is supported.

### Milestone 4 — Protected media and web-app compatibility gate

**Deliverables:**

- Implement the media capability state and diagnostic panel.
- Test normal and private tabs separately.
- Test cookies, storage, service workers, WebSockets, fetch/XHR, audio, autoplay, fullscreen, and encrypted media.
- Verify that the blocker never blocks the media/license/session path in the test corpus unless a rule genuinely matches it.
- Add a scoped compatibility action that relaxes only the selected origin and reloads.
- Add automated regression fixtures for “page works with protection on,” “page works after scoped exception,” and “engine lacks DRM.”

**Exit criteria:** `open.spotify.com` can be evaluated as a compatibility case on real devices. Success means the page loads and playback works where the selected engine/device supports it; it is not a promise that every Android device supports protected playback.

### Milestone 5 — WebView fallback containment

WebView must remain a fallback with an explicit capability label. Android describes WebView as a web page/client surface, not a fully developed browser, and its request hooks do not provide a universal browser firewall. [5]

If retained:

- install service-worker interception before the first page/worker;
- configure JavaScript, cookies, media, safe browsing, downloads, permissions, and renderer-loss handling explicitly;
- do not add a JavaScript bridge to arbitrary sites;
- document missed request classes and redirect limitations;
- do not report WebView counters as complete blocker coverage;
- run the same media and compatibility corpus and expose the reduced capability state.

**Exit criteria:** users can see which engine is active and which protections are available. WebView cannot silently claim GeckoView-level coverage.

## 6. Desktop web mode

“Desktop mode” should mean **request the desktop content experience**, not “pretend to be a desktop browser everywhere.” A user-agent change alone cannot create desktop viewport, pointer, keyboard, codec, DRM, or layout capabilities.

### Product behavior

Add three modes:

1. **Mobile** — default Android presentation.
2. **Desktop for this site** — persistent per-origin preference.
3. **Desktop for this tab** — temporary override.

A mode change should:

- update the engine’s supported user-agent/content-mode configuration;
- set a desktop viewport/layout behavior where the selected engine supports it;
- preserve the full verified origin in the address bar;
- reload with a clear progress state;
- retain the setting across normal sessions only when the user selected the per-site option;
- avoid applying desktop mode to private tabs after they close.

### Implementation steps

- Add `ContentMode` to tab state and persist only the chosen mode, not secrets.
- Add `setContentMode(ContentMode)` to `EngineAdapter`.
- Implement the GeckoView path first and verify the API available in the pinned version before coding against newer documentation.
- Implement WebView’s user-agent setting only as a fallback and document its limits. Android documents that WebView can use a custom user agent, but this changes identification; it does not guarantee a desktop experience. [5]
- Add a toolbar control such as **Site mode: Mobile/Desktop**, with a per-site toggle in the privacy/site-info panel.
- Add a settings page listing site-specific content-mode overrides.
- Test Spotify, Google Docs, GitHub, responsive ecommerce, file upload, video/audio, and keyboard-focused sites in both modes.

### Desktop-mode acceptance criteria

- The selected site receives the intended content-mode configuration after a fresh navigation.
- The mode is visible and reversible.
- One site’s setting does not affect another site.
- Switching mode does not turn Shields off.
- Fullscreen, audio focus, downloads, permissions, and back/forward behavior remain functional.

## 7. UI roadmap

### Toolbar

Replace the current debug-oriented emphasis with a compact browser toolbar:

- back, forward, reload/stop;
- editable address bar with verified origin and security state;
- shield icon with separate counts for native and custom blocking;
- site mode button: Mobile/Desktop;
- tab count and menu.

Keep “Plan” and “Spike” behind a developer/debug build or move them into an About/Diagnostics section. They should not compete with daily browsing controls in release builds.

### Shields panel

The panel should show:

- protection state: On / Off / Temporary exception;
- current origin and exact scope;
- **Requests blocked by Musify** with category breakdown;
- engine-native protection count;
- custom-list count with list names/versions;
- unknown/unobservable count where applicable;
- site-mode setting;
- protected-media compatibility status;
- one-reload compatibility action;
- restore-protection action;
- link to filter-list licenses and limitations.

Avoid presenting one large number as “ads blocked” when it includes trackers, fingerprinting, or engine-native anti-tracking events.

### Playback compatibility panel

Use a dedicated panel or inline recovery card with:

- “Why playback may be unavailable”;
- normal/private tab status;
- DRM/Widevine capability result;
- cookies/storage status;
- whether Musify blocked a required request;
- **Retry with current protection**;
- **Temporarily relax protection for this origin**;
- **Open engine compatibility help**.

Do not hide the failure behind a blank page or silently alter global settings.

## 8. Test plan and release gates

### Automated tests

Add unit tests for:

- origin and party classification;
- rule precedence and exceptions;
- resource-type policy;
- media/DRM allow-policy;
- desktop-mode persistence and isolation;
- temporary exception expiry;
- list checksum, bounds, rollback, and unsupported syntax;
- shield counter category separation;
- tab/process restoration.

Add instrumented tests for:

- WebExtension installation and runtime restart;
- message round-trip between native code and extension;
- live request blocked/allowed outcomes;
- service worker behavior;
- cookies and storage;
- audio, autoplay, fullscreen, and downloads;
- renderer loss and recovery;
- normal/private profile separation.

### Manual device matrix

At minimum, record results for:

- Android 8, Android 12+, Android 15;
- arm64 physical device and emulator where practical;
- current GeckoView release selected for shipping;
- current Android System WebView provider;
- Shields on/off;
- mobile/desktop mode;
- normal/private tab;
- Wi-Fi, mobile network, captive/offline transitions.

### Must-pass release gates

- No P0/P1 security or privacy issue.
- No SSL bypass, arbitrary native bridge, or hidden global exception.
- A protected-media failure identifies whether the cause is engine/device capability, policy, or network.
- A filter rule that breaks a representative web app can be disabled through a scoped, reversible policy—not by a hard-coded production-domain exception.
- List updates are licensed, attributed, bounded, validated, atomic, and rollback-safe.
- The UI never claims universal coverage for a backend that cannot observe every request.
- The repository builds and tests in CI with a documented Android SDK setup.

## 9. Suggested implementation order

1. Fix CI/local Android SDK setup and establish a truthful baseline.
2. Add `PrivacyPolicyService`, `ContentMode`, and capability models.
3. Wire policy/state into `TabsViewModel` and `EngineAdapter`.
4. Implement GeckoView WebExtension proof of concept with a tiny synthetic list.
5. Add counters and decision reasons from the real enforcement path.
6. Add media/DRM diagnostics and scoped compatibility recovery.
7. Run the device corpus before enabling larger lists.
8. Add desktop-mode UI and per-origin persistence.
9. Expand list support only after compatibility and rollback gates pass.
10. Keep WebView as an explicitly limited fallback.

## 10. Definition of done

Musify is ready for a beta when it can demonstrate, on supported devices, all of the following:

- ordinary pages load with measurable, explainable blocking;
- a synthetic tracker is blocked by the live enforcement backend, not only by an offline evaluator;
- a media web app still plays when its required first-party and protected-media requests are allowed;
- a broken site can be recovered with a scoped temporary exception and protection can be restored;
- desktop mode is visible, per-site, reversible, and independent of Shields state;
- normal and private profiles have tested, documented behavior;
- filter lists and licenses are visible and rollback-safe;
- failures are reported honestly instead of being hidden with Spotify-specific hacks.

## References

[1]: https://support.spotify.com/us/article/web-player-help/ "Spotify web player help"
[2]: https://github.com/adishxm/Musify/blob/e8973c15991afbcf0945de8bc4369e3dce84f4a6/.brain/licensing/FILTER_LIST_AUDIT.md "Musify filter-list inventory and licensing audit"
[3]: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html "GeckoView WebExtensions documentation"
[4]: https://developers.google.com/widevine/drm/overview "Google Widevine DRM overview"
[5]: https://developer.android.com/develop/ui/views/layout/webapps/webview "Android: Build web apps in WebView"
[6]: https://support.brave.com/hc/en-us/articles/360022806212-How-do-I-use-Shields-while-browsing "Brave Shields controls and site-specific behavior"
