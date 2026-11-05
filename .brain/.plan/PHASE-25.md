# Phase 25 — SABR Protobuf Streaming Interceptor & Video Backoff Defense

**Status:** PLANNED  
**Planning duration:** 1–2 days  
**Goal:** Implement the reverse-engineered Brave SABR (Server Adaptive Bitrate) streaming protocol interceptor in Musify's WebExtension content script layer. Prevent the 4–16 second player freeze/spinner caused by YouTube's server-enforced ad slot `backoffTimeMs` delay by inspecting binary protobuf chunks, rewriting Field 4 (`0x20` varint) backoffs in-place, and triggering an inline ad-free session reload (`isInlinePlaybackNoAd = true`).

## Scope and tasks
1. **SABR Streaming Protocol Interceptor (`sabr_interceptor.js`):**
   - Intercept `window.fetch` requests matching `googlevideo.com` and `sabr=1`.
   - Protect large media chunks (>1000 bytes) by streaming them directly to the native player without buffering.
   - For small control messages carrying protobuf descriptors:
     - Scan bytes for Field 4 with wire type 0 (`tag = 0x20`):
       $$\text{tag} = (4 \ll 3) \mid 0 = 32 \ (0\text{x}20)$$
     - Parse the 7-bit varint value for `backoffTimeMs`.
     - When `backoffTimeMs > 500ms`, patch the varint in-place to random noise (50–150ms) while preserving message structure.
   - Force fresh ad-free playback session:
     - Flag `player.getVideoData().isInlinePlaybackNoAd = true`.
     - Trigger `cancelPlayback()` and `loadVideoById(vid)` on the `#movie_player` element to establish an ad-free stream without looping.

2. **Integration into Musify Content Shields:**
   - Link `sabr_interceptor.js` or integrate into `youtube_adblock.js` loaded at `document_start`.
   - Ensure compatibility with both desktop and mobile YouTube Web layouts (`m.youtube.com`, `www.youtube.com`).

3. **Unit & Verification Testing:**
   - Author `app/src/test/java/com/musify/browser/Phase25SabrInterceptorTest.kt`:
     - Test Protobuf Field 4 (`0x20`) varint decoding logic.
     - Test in-place byte patching of varints exceeding 500ms.
     - Test threshold stream bypassing for media data chunks (>1000 bytes).
     - Test `isInlinePlaybackNoAd` session reload safety logic.
   - Run `./gradlew.bat testDebugUnitTest`.

4. **Phase Completion & Deliverables:**
   - Author `.brain/.report/PHASE-25-REPORT.md`.
   - Commit and push to Git.

## Required tests (run at phase end)
- [ ] `Phase25SabrInterceptorTest` passes 100%.
- [ ] Full test suite `./gradlew.bat testDebugUnitTest` passes 100%.

## Verification Gate
- [ ] Protobuf Field 4 (`0x20`) tag parsing and in-place varint rewriting correctly verified.
- [ ] Stream teeing ensures media chunks pass with zero latency overhead.
- [ ] `.brain/.report/PHASE-25-REPORT.md` documented and committed.
