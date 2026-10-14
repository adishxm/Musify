# Musify real-device Spotify playback failure report

**Repository:** `https://github.com/adishxm/Musify`  
**Revision reviewed:** `7bb8a3e9c77c53ae451c591443fc7f9c2e99be8f` (`origin/main`)  
**Attached evidence:** `WhatsAppVideo2026-10-14at15.13.42.mp4`  
**Review date:** 2026-10-14

## Verdict

The attached video does **not** show a unit-test assertion failure. It shows a production runtime compatibility failure:

- Musify launches and reaches Spotify Web.
- Spotify content loads and the user can browse songs/lyrics.
- At approximately **00:14**, Spotify displays **“Playback disabled.”**
- The message says playback will not work when protected content is blocked, the browser is incompatible, or private/incognito browsing is active.
- At approximately **00:29**, the user switches to Desktop mode.
- At approximately **00:41**, browsing continues but pressing play still does not produce audio.

The current repository therefore cannot truthfully claim that Spotify Web playback is fixed or that the app is a Brave-equivalent blocker.

## Main causes found in the current repository

### 1. The Lite APK is not a valid protected-playback target

The repository ships three APK variants:

- Full universal GeckoView build, about 153 MB.
- ARM64 GeckoView build, about 84 MB.
- Lite build, about 8.4 MB, using Android System WebView.

The current `WebViewAdapter` explicitly reports:

> `Standard Android WebView does not expose full Widevine CDM to arbitrary web content without custom system integration`

and sets WebView’s `widevineStatus` to `UNSUPPORTED`.

If the video was made with `Musify-Lite.apk`, Spotify playback failure is expected with the current architecture. The Lite build must not advertise Spotify protected playback. Either disable the Spotify Web feature in Lite or label it **not supported** and direct the user to the full GeckoView build.

### 2. MediaDrm probing is not the same as browser EME playback

The latest `GeckoEngineAdapter` calls:

```kotlin
MediaDrm.isCryptoSchemeSupported(WIDEVINE_UUID)
```

That proves only that the Android device exposes the Widevine crypto scheme. It does not prove that:

- the pinned GeckoView build exposes Encrypted Media Extensions;
- the Gecko runtime can create the required media key system;
- the browser can complete Spotify’s license request;
- the audio codec and output path work;
- Spotify accepts the actual embedded browser profile;
- the current session has the required cookies and persistent storage.

The app must keep EME/Widevine state as **UNKNOWN** until a real browser capability check and a real protected-media fixture are successful.

### 3. The Spotify “adblocker” is primarily a content script, not a Brave-style network blocker

`app/src/main/assets/musify_adblocker/manifest.json` only declares a `document_end` content script for `*.spotify.com`.

It does not declare a live request-blocking implementation. It does not connect the Kotlin `FilterEngine` to GeckoView request decisions. It therefore cannot be described as a universal Brave-like blocker.

The current browser counter is still based on GeckoView content-blocking callbacks. It is not a count of every custom rule evaluated by Musify.

### 4. The Spotify script actively modifies media behavior

`spotify_adblock.js` currently:

- overrides `document.createElement`;
- tracks every audio and video element;
- scans the entire DOM repeatedly;
- runs a timer every 250 ms;
- attaches a `MutationObserver` that calls the same processing loop;
- classifies media as advertising using URL and broad page-text heuristics;
- sets `muted = true` and `volume = 0`;
- sets `playbackRate = 16.0`;
- changes `currentTime` to the media duration or `9999`;
- clicks skip/next buttons;
- hides dialogs containing terms such as “advertisement”, “spotify premium”, and “get premium”.

This is not a safe compatibility layer. It can affect legitimate Spotify media, interfere with playback state, create a high-frequency DOM workload, and make diagnosis impossible. It must be disabled while establishing a clean playback baseline.

Do not use media manipulation to bypass Spotify audio advertising or protected-content controls. A privacy browser can block documented third-party trackers and ads, but it should not claim that it can remove first-party service-controlled audio ads or bypass DRM.

### 5. “Relax Shields” does not currently relax the live GeckoView policy

`TabsViewModel.optimizePlaybackForCurrentSite()`:

- switches to Desktop mode;
- writes a site exception to the repository;
- calls `adapter.setOriginException(true)`;
- reloads the page.

In `GeckoEngineAdapter`, `setOriginException()` changes only the adapter’s state. It does not update the shared GeckoView content-blocking configuration or the WebExtension’s behavior. Therefore the UI can say that Shields were relaxed while the actual engine policy remains unchanged.

The exception system must be connected to the real blocker backend and tested with a synthetic page before it is used as Spotify recovery.

### 6. Desktop mode changes presentation, not playback capability

The current Desktop toggle changes the GeckoView user-agent mode, viewport mode, and reloads. That can change which layout Spotify serves, but it cannot provide missing EME, Widevine, cookies, codecs, audio focus, or license support.

The Desktop user-agent is also hard-coded to Firefox 135. The project must test the exact user-agent and pinned GeckoView version rather than assuming that a desktop string creates a supported desktop browser.

### 7. Strict engine settings may be involved, but have not been isolated

The full Gecko runtime uses strict anti-tracking, strict ETP, and `ACCEPT_NON_TRACKERS` cookies. That may be compatible with Spotify, but the project has not captured a live request decision proving that the media/authentication/license path is allowed.

The correct diagnosis is an A/B matrix, not a permanent Spotify allowlist:

1. protection on, filtered DNS;
2. protection off, filtered DNS;
3. protection on, non-filtering DNS;
4. protection off, non-filtering DNS;
5. normal tab and private tab;
6. mobile and desktop content modes.

## Required correction sequence

### Step 1 — Identify the exact installed build

Add the build variant, engine type, GeckoView version, Android API, ABI, and Widevine status to the diagnostics screen. Do not accept a playback report without this information.

For now:

- Full GeckoView build: candidate for Spotify testing.
- ARM64 GeckoView build: candidate for Spotify testing on arm64.
- Lite/WebView build: do not promise protected Spotify playback.

### Step 2 — Disable the Spotify media manipulation script

Create a feature flag that is **off by default** for the first compatibility pass. Remove or isolate all code that mutes, fast-forwards, seeks, clicks next, or intercepts audio/video elements.

Keep only harmless, separately tested UI handling if legally and technically appropriate. Do not hide the Spotify playback-disabled diagnostic because it is needed to identify the real cause.

### Step 3 — Prove the engine before proving the blocker

Use the full GeckoView build in a normal, non-private tab and test:

- a neutral EME/Widevine fixture;
- ordinary HTML5 audio;
- persistent storage;
- service workers;
- WebSockets;
- autoplay after a user click;
- audio focus and background playback;
- login/session persistence.

If the protected-media fixture fails, stop. The blocker is not the next problem to solve; the engine/device combination is not ready for Spotify Web playback.

### Step 4 — Add real playback telemetry

Add a redacted `PlaybackDiagnostics` model with:

- engine and version;
- content mode;
- private/normal mode;
- Widevine probe result;
- EME request result;
- autoplay result;
- cookie/storage result;
- last navigation/media error;
- whether a live blocker decision matched;
- matched rule ID and category, without tokens or license payloads;
- DNS status.

Never log cookies, access tokens, license bodies, credentials, or full query strings.

### Step 5 — Implement the blocker as an actual engine-integrated backend

The Kotlin filter evaluator is not enough. Use a supported GeckoView WebExtension request-blocking path or another maintained engine-integrated blocker.

The live decision must receive and record:

- top-level origin;
- request origin;
- resource type;
- first-party/third-party relationship;
- matched rule and list;
- exception scope;
- decision category.

Test the backend with a local synthetic page. Do not begin with Spotify-specific rules.

### Step 6 — Add a media-safe policy

Default policy:

- preserve top-level navigation;
- preserve first-party authentication and session resources;
- preserve service workers, WebSockets, fetch/XHR, audio/video, and media-key/license flows;
- block only a rule that genuinely matches a documented ad/tracker resource;
- treat unknown protected-media requests as **unknown**, not automatically blocked;
- offer a one-reload origin-scoped exception.

Do not use `host.contains("spotify")` as a universal allow or block rule.

### Step 7 — Test AdGuard DNS independently

AdGuard DNS should be an optional companion layer, not the browser blocker. Test with:

- Android Private DNS `dns.adguard-dns.com`;
- temporary non-filtering resolver `unfiltered.adguard-dns.com`;
- Android Automatic DNS;
- any installed VPN/DNS changer disabled.

If Spotify works only with non-filtering DNS, capture the exact failed DNS hostname and review it. Do not permanently allow every Spotify-related hostname without evidence.

### Step 8 — Retest Desktop mode after the engine passes

Only after the protected-media fixture works:

1. open a normal tab;
2. navigate to `https://open.spotify.com`;
3. log in if needed;
4. switch to Desktop mode;
5. reload the page;
6. click play;
7. confirm audible output;
8. repeat with protection on and off;
9. repeat with filtered and non-filtering DNS.

The acceptance result must be recorded on real hardware, not only in a mock unit test.

## Brave-like feature target

Musify cannot legally or technically be a copy-paste of the proprietary Brave application. It can implement an independent browser with similar, publicly described feature categories:

- Shields toggle per site;
- tracker/ad request blocking;
- per-site block counters tied to real decisions;
- HTTPS-first behavior;
- first- and third-party cookie controls;
- script controls with clear warnings;
- fingerprinting protection with compatibility recovery;
- private tabs with honest media limitations;
- site permissions;
- DNS protection status;
- transparent filter-list versions and licenses;
- one-tap, origin-scoped recovery when a site breaks.

The UI should not use Brave branding, copied artwork, copied source code, or claims of exact Brave parity.

## Release claims that must be corrected

Until real-device evidence exists, remove or soften claims such as:

- “Spotify Web that actually plays”;
- “Spotify Web, uninterrupted”;
- “Spotify playback verified”;
- “Widevine DRM fully resolved”;
- “101/101 tests prove Spotify playback”.

Unit tests can prove state transitions. They cannot prove that Spotify produced audio on a device.

Use this wording instead:

> Spotify Web compatibility is under device-specific validation. The full GeckoView build is the candidate for protected playback. Lite/WebView builds do not guarantee protected Spotify playback. Musify does not bypass DRM.

## Definition of done

Do not mark Spotify compatibility PASS until all of these are true:

- the tested APK variant and engine are recorded;
- Spotify loads in a normal tab;
- the protected-media fixture succeeds;
- the user can log in and retain required session state;
- pressing play produces audible Spotify audio on a physical device;
- Desktop mode works after a real reload;
- the blocker blocks a synthetic tracker through the live engine backend;
- protection on does not break the tested media/session path;
- AdGuard DNS and browser Shields counters are separate;
- a scoped exception can recover a genuinely blocked synthetic request;
- no DRM bypass, TLS interception, credential capture, or blanket Spotify exception is used;
- the report includes screenshots, logs, device/API/ABI, APK hash, and exact test steps.

## Evidence references

- Spotify’s protected-content message and supported-browser guidance: <https://support.spotify.com/us/article/web-player-help/>
- Brave Shields categories and per-site controls: <https://support.brave.com/hc/en-us/articles/360022806212-How-do-I-use-Shields-while-browsing>
- AdGuard public DNS and Android Private DNS setup: <https://adguard-dns.io/en/public-dns.html>
- GeckoView WebExtensions integration: <https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html>
