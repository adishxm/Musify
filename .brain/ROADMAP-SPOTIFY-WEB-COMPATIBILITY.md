# Musify Spotify Web Compatibility Roadmap

**Repository:** `https://github.com/adishxm/Musify`  
**Latest main reviewed:** `255404f9ee5c1c79a3d8b305ed08778005833059`  
**Current feature branch reviewed:** `phase/10-desktop-mode-and-protected-media-playback`  
**Target:** Musify should load `https://open.spotify.com` in mobile and desktop content modes, support legitimate web playback where the Android device/engine supports protected media, and provide Brave-like privacy feature categories without copying Brave’s product, code, or branding.

## 1. The current problem is not solved yet

The current Phase 10 report claims that Spotify playback is resolved, but the repository evidence does not support that claim.

The latest source has these problems:

1. `MediaCapabilityState` initializes `isWidevineSupported = true` and `isEncryptedMediaSupported = true`. Those values are not measured from the device or from a real EME playback attempt.
2. Phase 10 tests use `MockEngineAdapterWithMedia`. They verify state transitions, serialization, and a simulated reload. They do not click Spotify’s play button on a physical Android device and verify audible playback.
3. The current GeckoView blocker still uses GeckoView’s engine-native content-blocking callback. The Kotlin `FilterEngine` remains an offline evaluator and is not a live request-enforcement backend.
4. The WebView fallback uses hostname substring checks such as `spotify.com`, `scdn.co`, and `spotifycdn.com`. That is not a universal blocker and is not sufficient evidence that GeckoView media requests are preserved.
5. `setProtectionEnabled()` changes shared runtime settings. A change intended for one page can affect other sessions unless policy state is isolated.
6. A desktop user-agent and viewport can change the page layout, but they cannot create Widevine support, codecs, browser permissions, authentication state, or a working media-license flow.
7. The current source contains a release limitation saying proprietary Widevine may not be available, while Phase 10 reports Widevine playback as fully resolved. Those claims must be reconciled with a real device test.

**Conclusion:** Phase 10 should be reclassified as **implementation-in-progress / unverified on real devices**, not as a completed Spotify playback solution.

Spotify’s own documentation says the web player depends on a supported browser and protected-content/Widevine support. Its Android list includes Chrome 87+, Samsung Internet 17+, and Firefox 115+, while desktop support covers current Chrome, Firefox, Edge, Opera, and Safari versions. [1]

## 2. Product target and boundaries

### Product target

Musify should provide:

- mobile and desktop content modes for arbitrary websites;
- a reliable browser shell around one tested production engine;
- browser-level tracker and advertising request protection;
- optional AdGuard DNS as a separate domain-level network layer;
- clear per-site Shields controls and counters;
- protected-media diagnostics;
- normal-tab web playback when the device and engine support it;
- a reversible compatibility action for sites that break under privacy protection.

### Important non-goals

Musify must not:

- decrypt, capture, or bypass DRM;
- claim that every Android device supports Spotify Web Player;
- silently disable all privacy protection for Spotify;
- use a permanent Spotify-specific allowlist without evidence of the exact failed request and its role;
- use anti-adblock evasion or service impersonation;
- promise that DNS filtering removes first-party audio advertising controlled by the service;
- claim exact Brave parity or use Brave’s name, UI artwork, proprietary code, or branding.

The safe target is **Brave-like feature categories implemented independently**: tracker/ad blocking, HTTPS-first behavior, cookie controls, fingerprinting controls, per-site exceptions, and transparent counters. Brave itself separates site-specific protections and notes that blocking scripts or fingerprinting can break websites; Musify needs the same kind of reversible recovery UX. [2]

## 3. Required architecture

```text
Compose UI
  |
BrowserCoordinator / TabsViewModel
  |
PrivacyPolicyService
  |-- global Shields profile
  |-- per-origin policy
  |-- temporary compatibility exception
  |-- content mode: MOBILE / DESKTOP
  |-- media compatibility policy
  |-- DNS status
  |
EngineAdapter
  |-- GeckoView production adapter
  |     |-- engine-native ETP
  |     |-- bundled WebExtension blocker
  |     |-- media/permission delegates
  |     |-- navigation and process recovery
  |-- WebView fallback adapter
        |-- documented limited interception only
        |-- no universal blocker claim
  |
PrivacyDecisionStore
  |-- decision category and rule ID
  |-- current-page counters
  |-- list version/checksum
  |-- exception scope and expiry
  |
Optional DNS layer
  |-- Android Private DNS guidance
  |-- user-provided AdGuard DNS hostname
  |-- later: separate user-approved VPN DNS mode
```

### Enforcement separation

The app must distinguish three different mechanisms:

1. **GeckoView ETP:** engine-native anti-tracking behavior.
2. **Live browser blocker:** a bundled GeckoView WebExtension or another engine-integrated backend that evaluates supported filter rules during requests.
3. **AdGuard DNS:** domain-level resolution filtering, optionally configured through Android Private DNS.

Do not merge their counters. A DNS resolver generally cannot tell Musify which page caused a lookup. Display it as **DNS protection status**, not as a page-specific Shields count.

## 4. Phase roadmap

### Phase 00 — Evidence reset and truthful baseline

**Duration:** 2–3 days

**Purpose:** stop treating mock tests and hard-coded capability fields as proof of Spotify playback.

**Tasks:**

- Mark Phase 10’s Spotify playback gate as unverified until real-device evidence exists.
- Add a debug diagnostic showing engine type, GeckoView version, Android API, WebView provider, normal/private mode, content mode, and current URL.
- Replace default `isWidevineSupported = true` with `UNKNOWN` until the capability is actually measured.
- Add a build/CI check that rejects hard-coded “Spotify solved” or “Widevine supported” claims without a test evidence reference.
- Record screenshots, logcat output, and a short reproduction video for the current failure.

**Exit gate:** a clean install can reproduce the failure and identify whether it occurs in GeckoView or WebView.

### Phase 01 — Android and engine capability audit

**Duration:** 1 week

**Purpose:** determine whether the selected embedded engine can support Spotify Web Player on the target devices.

**Tasks:**

- Test GeckoView on at least two physical Android devices and one emulator.
- Record Android API, vendor, system WebView provider, ABI, GeckoView artifact, and available media capabilities.
- Verify whether EME is exposed to the page and whether the device has a usable Widevine CDM.
- Test normal and private sessions separately.
- Verify cookies, persistent storage, service workers, WebSockets, audio focus, autoplay, and fullscreen.
- Test a neutral protected-media fixture before testing Spotify.

**Exit gate:** choose one of these outcomes explicitly:

- **Supported:** GeckoView can play the neutral protected-media fixture and Spotify can proceed to actual playback testing.
- **Partially supported:** ordinary HTML5 audio works, but protected media is unavailable; Musify must show a limitation and cannot claim Spotify Web playback.
- **Unsupported:** use a different supported browser engine or reduce the product promise. Do not attempt DRM bypass.

### Phase 02 — Engine adapter and policy isolation

**Duration:** 1 week

**Purpose:** prevent a global protection toggle from causing cross-tab or cross-site failures.

**Tasks:**

- Add an immutable `ProtectionProfile` passed to each session.
- Remove shared-runtime mutation as the only implementation of per-origin exceptions.
- Define policy scopes: global, origin, tab, and one-reload temporary exception.
- Normalize origins to scheme, host, and effective port.
- Persist only the selected content mode and intended exception metadata.
- Add tab-isolation tests for two origins and two tabs.

**Exit gate:** changing protection for `open.spotify.com` cannot change protection for another tab or origin.

### Phase 03 — Mobile content mode

**Duration:** 3–5 days

**Purpose:** make the mobile experience stable before adding desktop presentation.

**Tasks:**

- Keep `MOBILE` as the default mode.
- Use engine-supported mobile user-agent and viewport behavior.
- Confirm that the mobile layout is reported honestly as mobile; do not claim it is a supported Spotify Web Player path if Spotify directs the user to the native app.
- Add a visible **Mobile** state in the toolbar.
- Ensure mode changes reload the current page only after the user requests them.

**Exit gate:** ordinary mobile sites, login forms, audio fixtures, downloads, and navigation work in mobile mode.

### Phase 04 — Desktop content mode

**Duration:** 1 week

**Purpose:** request a desktop web presentation without pretending that UA spoofing solves media support.

**Tasks:**

- Keep per-tab and per-origin `MOBILE`/`DESKTOP` state.
- Use GeckoView’s supported desktop user-agent and viewport settings for the pinned version.
- Avoid manually inventing a browser UA unless compatibility tests prove it is needed; a stale Firefox version can make a site reject the client.
- Reload after switching modes.
- Add a toolbar control and a site-settings control.
- Make desktop mode independent from Shields state.
- Test responsive layout, keyboard focus, fullscreen, downloads, forms, and audio in both modes.

**Exit gate:** desktop mode changes the served layout and viewport, is reversible, is isolated per origin, and does not claim playback success until the media gate passes.

### Phase 05 — Protected-media capability and permission handling

**Duration:** 1–2 weeks

**Purpose:** replace hard-coded media capability claims with measured state.

**Tasks:**

- Add `UNKNOWN`, `SUPPORTED`, `UNSUPPORTED`, and `FAILED` states for EME/Widevine.
- Implement the minimum required GeckoView media permission delegates using the pinned artifact’s actual API.
- Do not automatically grant unrelated camera, microphone, location, or sensor permissions.
- Handle autoplay as a user gesture policy, not as a blanket guarantee.
- Preserve first-party cookies/storage needed for login and session state.
- Add a protected-media fixture and record whether the media key system initializes.
- Report the actual failure reason when available.

**Exit gate:** a real device can prove either successful protected playback or a clear unsupported state. A mock `true` value is not acceptable evidence.

### Phase 06 — AdGuard DNS companion layer

**Duration:** 3–5 days for system Private DNS guidance; 2–4 weeks for an optional VPN DNS mode

**Purpose:** add the user-requested AdGuard DNS blocker without confusing it with browser request filtering.

**First implementation: Android Private DNS guidance**

- Add a DNS Settings screen.
- Open `Settings.ACTION_PRIVATE_DNS_SETTINGS` when supported.
- Show that the setting affects the whole device, not only Musify.
- Document AdGuard’s public hostname `dns.adguard-dns.com`.
- Provide troubleshooting guidance using `unfiltered.adguard-dns.com`.
- Detect and display likely VPN/DNS-changer override states where possible.
- Never silently change the user’s system DNS.

AdGuard documents `dns.adguard-dns.com` for filtered DNS and `unfiltered.adguard-dns.com` for troubleshooting. [3] Android Private DNS uses DNS-over-TLS on Android 9 and newer, but VPN or DNS-changer software can override that path. [4]

**Later implementation: user-approved local DNS VPN**

Only add a `VpnService` DNS layer if per-app DNS filtering is a hard requirement. It requires user consent, has one-active-VPN conflicts, and still cannot remove in-page ads or inspect HTTPS paths without TLS interception. It must not be used as a shortcut for the missing live GeckoView blocker.

**Exit gate:** DNS status is visible, separate from browser Shields counts, and Spotify testing can determine whether a DNS failure is involved.

### Phase 07 — Live blocker backend

**Duration:** 2–4 weeks

**Purpose:** make the blocker actually work in the browser engine.

**Preferred backend:** bundled GeckoView WebExtension.

**Tasks:**

- Add a stable extension manifest under `app/src/main/assets/`.
- Install it once per GeckoRuntime using GeckoView’s built-in extension mechanism.
- Export supported ABP network rules from the Kotlin compiler into the extension format.
- Implement request matching with top-level origin, request origin, resource type, party relationship, rule ID, and list ID.
- Add native-to-extension messages for enable/disable, origin exceptions, list updates, and counters.
- Handle extension installation after process death and app upgrade.
- Add a synthetic page with known tracker requests and assert that the live backend blocks them.
- Keep cosmetic and scriptlet rules separate from network blocking until they have a tested browser implementation.

GeckoView documents bundled WebExtensions as the preferred way for an embedding application to interact with web content. [5]

**Exit gate:** a live synthetic page proves that requests are blocked before loading, the matching rule is reported, and a scoped exception reloads the page successfully.

### Phase 08 — Media-safe blocker policy

**Duration:** 1–2 weeks

**Purpose:** ensure the blocker does not break legitimate application and media flows.

**Tasks:**

- Define resource categories: document, script, XHR/fetch, WebSocket, image, font, audio, video, media key/license, service worker, and unknown.
- Default to allowing top-level first-party application resources.
- Treat media key/license requests as protected compatibility flows; never block them by hostname substring.
- Preserve authentication/session storage.
- Log only redacted debug metadata.
- Add a temporary one-reload compatibility action.
- Do not add a permanent `spotify.com`/`scdn.co` bypass without a recorded, reviewed request reason.

**Exit gate:** tracker fixtures are blocked while the protected-media fixture continues to play. If a rule breaks playback, the UI identifies the rule and the user can recover with a scoped exception.

### Phase 09 — Spotify Web Player compatibility suite

**Duration:** 1–2 weeks plus real-device retesting

**Purpose:** test the requested site without claiming that all Spotify or all Android configurations are supported.

**Test matrix:**

- `open.spotify.com` normal tab, mobile mode;
- `open.spotify.com` normal tab, desktop mode;
- protection on;
- protection off;
- AdGuard filtered DNS;
- non-filtering DNS troubleshooting mode;
- private tab;
- logged-in and logged-out state;
- Wi-Fi and mobile data;
- two Android device/API profiles;
- GeckoView and fallback engine if fallback is shipped.

**Record for every run:**

- page loaded or failed;
- login completed or failed;
- play button response;
- audible playback started or not;
- EME/Widevine state;
- audio focus/autoplay state;
- exact blocker decision, if any;
- DNS resolution failure, if any;
- screenshot and redacted log.

**Exit gate:** the product may claim **verified Spotify Web playback on tested device profiles** only when the play button produces audio in a normal tab and the evidence is stored. Otherwise the UI must report the exact limitation.

### Phase 10 — Brave-like Shields UI, independently designed

**Duration:** 1–2 weeks

**Purpose:** make protection understandable and reversible.

**Toolbar:**

- verified origin;
- Shields state;
- browser-blocked count with source/category;
- DNS status separately;
- Mobile/Desktop mode;
- playback compatibility button;
- reload/stop and tab controls.

**Shields panel:**

- protection on/off for the current origin;
- trackers/ads blocked;
- engine-native anti-tracking events;
- custom blocker events;
- unsupported rule count;
- current list versions;
- cookie policy;
- fingerprinting policy;
- HTTPS status;
- one-reload compatibility exception;
- restore protection.

**Playback panel:**

- normal/private mode;
- content mode;
- EME/Widevine measured status;
- cookie/storage status;
- autoplay state;
- whether a live rule blocked a request;
- DNS status;
- retry and scoped recovery actions.

Never show a large number as “ads blocked” if it includes engine callbacks, DNS lookups, trackers, or unknown events.

### Phase 11 — AdGuard/list update and licensing system

**Duration:** 1–2 weeks

**Tasks:**

- Keep source, license, attribution, version, checksum, and supported-rule counts for every list.
- Use staged atomic updates and last-known-good rollback.
- Bound file size, line size, rule count, and compile memory.
- Make EasyList/EasyPrivacy opt-in until the distribution and license obligations are reviewed.
- Clearly distinguish AdGuard DNS’s server-side lists from Musify’s browser lists.
- Provide an allowlist/exception UI that is scoped and reversible.

**Exit gate:** a corrupt or incompatible list cannot replace the active list, and the UI accurately reports which layer made each decision.

### Phase 12 — Real-device, network, and regression hardening

**Duration:** 2–3 weeks

**Tasks:**

- Android 8, Android 12+, and Android 15 device profiles where supported.
- Process death and renderer recovery during playback.
- Network switching, captive portal, offline mode, DNS failure, and VPN conflict tests.
- Cookie clearing and re-login tests.
- Rotation/background/audio-focus tests.
- Service worker and WebSocket tests.
- Memory, battery, startup, and thermal measurements.
- Accessibility and TalkBack testing for every recovery action.

**Exit gate:** no P0/P1 security issue, no silent SSL bypass, no credential leakage, no uncontrolled native bridge, and no data leak from private mode.

### Phase 13 — Release and evidence gate

**Duration:** 1–2 weeks

**Tasks:**

- Add a real device test report to `.brain/.report/`.
- Replace Phase 10’s current PASS wording if real-device evidence is absent.
- Build signed APK/AAB from a clean checkout.
- Publish dependency/license notices.
- Add a known-limitations page that says exactly which devices support protected playback.
- Do not advertise “Spotify ad-free” or “works everywhere.”
- Advertise only verified device/engine combinations.

**Release gate:** a new user can understand whether Musify is using mobile or desktop content mode, browser Shields, AdGuard DNS, and protected-media support—and can recover a broken page without guessing.

### Phase 14 — Optional per-app DNS VPN mode

**Status:** later / optional

Only pursue this after the browser blocker and playback suite are stable. Implementing a VPN to provide per-app AdGuard DNS requires:

- explicit user consent;
- one-active-VPN conflict handling;
- DNS-only routing without loops;
- reconnect and kill-switch behavior;
- clear privacy documentation;
- a separate diagnostic from browser Shields.

Do not add TLS interception in this phase.

## 5. Immediate next engineering tasks

Do these in order:

1. Reclassify Phase 10 from **PASS** to **unverified** until physical-device playback evidence exists.
2. Replace hard-coded media capability booleans with an `UNKNOWN` state and actual diagnostics.
3. Add a real Android device test for GeckoView before changing more UI.
4. Test Spotify with protection on/off and filtered/non-filtering DNS to identify the failing layer.
5. Implement the GeckoView WebExtension proof of concept with one synthetic tracker rule.
6. Add the live decision counter and rule ID.
7. Add protected-media-safe request classification.
8. Add the AdGuard DNS settings screen and Private DNS guidance.
9. Re-test Spotify in normal Desktop mode, then normal Mobile mode.
10. Only after playback works, expand the filter lists and polish the Brave-like Shields UI.

## 6. Definition of done for the requested app

Musify is ready for the requested beta when all statements below are true on declared test devices:

- `open.spotify.com` loads in a normal tab in Mobile mode and Desktop mode, subject to Spotify’s supported-browser and device requirements.
- In Desktop mode, Spotify’s play button produces audio in the tested normal-tab configuration.
- The app reports whether EME/Widevine is measured as supported, unsupported, or unknown.
- The live blocker blocks a synthetic ad/tracker request through the actual engine backend.
- The blocker does not block the tested media/authentication/session path.
- AdGuard DNS can be configured through system Private DNS guidance without silently changing Android settings.
- DNS filtering and browser Shields are shown as separate layers.
- A broken page can be recovered with a temporary, origin-scoped exception.
- The app has independent mobile/desktop state per site or tab.
- Tests use real devices and real playback evidence, not only mock engines.
- The app does not bypass DRM or claim universal Spotify compatibility beyond the tested matrix.

## References

[1]: https://support.spotify.com/us/article/web-player-help/ "Spotify web player help"
[2]: https://support.brave.com/hc/en-us/articles/360022806212-How-do-I-use-Shields-while-browsing "Brave Shields controls and per-site behavior"
[3]: https://adguard-dns.io/en/public-dns.html "AdGuard public DNS servers and Android Private DNS setup"
[4]: https://developers.google.com/speed/public-dns/docs/using "Android Private DNS and DNS-over-TLS guidance"
[5]: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html "GeckoView WebExtensions documentation"
