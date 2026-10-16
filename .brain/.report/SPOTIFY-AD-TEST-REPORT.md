# Spotify Ad Appearance - Test & Root Cause Analysis Report

**Target Origin:** `https://open.spotify.com`  
**Platform / Device:** Android Emulator (`emulator-5554`, Android 16 API 36, x86_64)  
**Active Engine:** Mozilla GeckoView 135.0  
**Installed Build:** `com.musify.browser.debug` (Musify-Universal)  
**Date:** 2026-10-16  

---

## 1. Observed Incident & Emulator State

During continuous playback testing on `open.spotify.com` in **Mobile Mode**, an in-stream audio/visual advertisement appeared:
- **Visual Presentation:** Full-screen mobile player interstitial displaying:
  - Header: `"Advertisement"`
  - Promotional Card: *"Shake up your music routine. Fill your day with good vibes with song recommendations and shared playlists, only on Premium. [ LEARN MORE ]"*
  - Track duration indicator: `0:03 / 0:00`
  - Playback controls: Skip forward and backward disabled by Spotify player.
- **Audio Experience:** Audio commercial played for several seconds before transitioning into the requested track (*DAISIES* by Justin Bieber).

---

## 2. Technical Root Cause Breakdown

1. **Absence of Background Network Interception in GeckoView:**
   - In Phase 22, network-level anti-freeze mocking (`shouldInterceptRequest`) was implemented in `WebViewAdapter.kt`.
   - However, the live universal build utilizes `GeckoEngineAdapter.kt` (Mozilla GeckoView 135).
   - GeckoView relies on its internal ETP list and registered WebExtensions. The existing `musify_adblocker` WebExtension manifest only had a content script, without a background script utilizing `browser.webRequest` with `"blocking"`.
   - Consequently, Spotify's requests to `spclient.wg.spotify.com/ad-logic/*`, `desktop-ad-service/*`, and commercial break streams were not intercepted at the network layer in GeckoView.

2. **In-Page Audio Ad Handling Gap:**
   - `spotify_adblock.js` previously only implemented CSS cosmetic hiding for banner slots and modal dialog dismissers.
   - It did not monitor Spotify's active track state machine or HTML5 `<audio>` / `<video>` elements for commercial breaks.
   - When Spotify scheduled an ad break, the media element played the commercial audio to completion while disabling the skip button.

3. **Missing Selectors for Mobile Interstitial Overlays:**
   - Spotify's mobile web player wraps the commercial view into a full-player card (`[aria-label="Advertisement"]`, track info advertiser badges, and upsell pitches) which was not fully hidden by desktop-oriented selectors.

---

## 3. Solution Strategy (Brave Shields & Spicetify Reference)

To completely eliminate Spotify ads in both Mobile and Desktop modes without causing player freezes:
1. **WebExtension Background Script (`background.js`):**
   - Register `browser.webRequest.onBeforeRequest` with `blocking` and `<all_urls>` permission.
   - Intercept Spotify ad endpoints (`spclient.wg.spotify.com/ad-logic/*`, `/ads/*`, `/desktop-ad-service/*`, `/commercial-break/*`, `audio-ak-spotify-com.akamaized.net`).
   - Return `{ redirectUrl: "data:application/json,{}" }` for JSON ad metadata to satisfy Spotify's client state machine cleanly.
   - Strictly bypass Widevine DRM license acquisition endpoints (`/widevine-license`, `/license`).
2. **Main-World Fetch/XHR Interceptor (`spotify_adblock.js`):**
   - Inject page-level hook into `window.fetch` and `XMLHttpRequest` at `document_start` to neutralize ad queries before they reach Spotify's WebPack player core.
3. **Audio Ad Auto-Advance & Instant Skip:**
   - Detect commercial breaks (`document.title.includes('Advertisement')`, `[aria-label="Advertisement"]`, etc.).
   - Instantly mute media elements, set `media.currentTime = media.duration`, and trigger player advancement.
   - Restore audio settings when genuine music resumes.
4. **Complete Cosmetic Cleanup:**
   - Hide mobile now-playing ad overlay cards, promotional banners, and upgrade prompts.
