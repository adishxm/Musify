# Phase 36 — Dark Editorial Browser Suite Redesign & Unified Design System

**Status:** IN PROGRESS  
**Execution Date:** November 7, 2026  
**Goal:** Implement the complete editorial dark UI/UX design suite across Musify Browser matching the user-selected visual reference (`01_Home_Dark_Selected.png` / `media_1793966798184_24e13189.jpg`) and companion screens (`05_Browser_Page_Dark.png`, `06_Browser_Menu_Dark.png`, `07_Tabs_Normal_Dark.png`, `08_Tabs_Private_Dark.png`, `09_Shields_Sheet_Dark.png`): DM Serif Display typography, procedural vinyl hero, 2x2 Bento shortcuts, editorial floating dock, unified web toolbar, editorial more sheet, normal/private tab switcher, and shields bottom sheet.

---

## 1. Context & Design Language

The user provided the exact reference screenshot (`01_Home_Dark_Selected.png` / `media_1793966798184_24e13189.jpg`) alongside `Musify_Home_Agent_Implementation_Guide.md` and `AGENT_HANDOFF_PART_1.md`. This aesthetic establishes a calm, editorial dark browser interface:
- Deep charcoal canvases (`#09090A`), surface pills (`#18181A`), and raised cards (`#242326`).
- Warm ivory display typography with DM Serif Display Regular (`#F3EEEA`) paired with clean Inter for controls and metadata.
- Frosted floating capsule dock (`#1B1A1C` with subtle ambient glow) containing Back, Shields, Tab count badge, and More.
- Cinematic procedural vinyl record hero with metallic lathe grooves, shimmering reflections, `MUSIC + THE WEB` tag, interlocking rings, and vertical editorial copy ("A WIDER WEB FOR DEEPER LISTENING —").
- 2x2 Bento cards for Spotify, YouTube Music, YouTube, and SoundCloud.

### Strict Product Invariants:
1. **Musify is a real browser, not a native music player or social app:** Zero fake playlists, fake playback controls, fake follower counts, or fake security statistics.
2. **Media-Safe Protections:** Widevine DRM CDM license acquisition, EME, and genuine media streams (`videoplayback`, `scdn.co`, `.mpd`, `.m3u8`) must NEVER be blocked or broken.
3. **Multi-Variant APK Size Limit:** All three release variants (`Musify.apk`, `Musify-Lite.apk`, `Musify-ARM64.apk`) must remain strictly under 100 MB and verified via `apksigner`.

---

## 2. Scope & Implementation Tasks

### Task 1: Wire Editorial Home Screen as Primary Blank-Tab Surface
- In `BrowserShellScreen.kt`, when `isHome` (`url == "about:blank"` and not loading), present `MusifyHomeScreen` as the primary surface.
- Connect live state: `addressInput`, `onAddressChange`, `onSubmit`, `onNavigate`, real `tabCount`, `canGoBack`, and action sheet triggers.
- Retain search routing, HTTPS validation, external scheme handoffs, and private tab isolation.

### Task 2: Floating Editorial Web Page Toolbar (`05_Browser_Page_Dark.png`)
- Create `EditorialWebToolbar.kt` in `app/src/main/java/com/musify/browser/ui/screens/`:
  - Centered floating pill capsule (294–320 × 58 dp) above the navigation-safe inset.
  - Controls:
    1. Back button (`←`), semantically disabled when `!canGoBack`.
    2. Address/Domain capsule with HTTPS lock indicator, domain display, and tap-to-edit inline omnibox.
    3. Shield icon (`⛊`) with active shield count indicator and click trigger for Shields sheet.
    4. Tab switcher count badge (`[ N ]`) showing live tab count.
    5. More options button (`⋮`) opening the Editorial Action Menu.
- Top progress bar indicating loading state.

### Task 3: Editorial Action & More Menu Sheet (`06_Browser_Menu_Dark.png`)
- Create `EditorialActionMenuSheet.kt` in `app/src/main/java/com/musify/browser/ui/screens/`:
  - Rounded dark modal sheet with drag handle and DM Serif Display `Musify` header.
  - Grouped action grid/list preserving all existing browser features:
    - Navigation: New tab, New private tab, Bookmarks, History, Downloads.
    - Page tools: Find in page, Share page, Desktop content mode, Media diagnostics.
    - Privacy & system: Site permissions, Filter lists, Data & privacy, About & help.

### Task 4: Editorial Tab Switcher Screen (`07_Tabs_Normal_Dark.png` & `08_Tabs_Private_Dark.png`)
- Create `EditorialTabSwitcherScreen.kt` in `app/src/main/java/com/musify/browser/ui/screens/`:
  - Top header with Back button and DM Serif Display `Tabs` title.
  - Full-width Normal / Private segmented pill selector with live counts.
  - 2-column card deck with page title, domain, close `(×)` button, active tab indicator.
  - Bottom actions: `+ New tab` (or `+ New private tab`) and `Close all` (scoped strictly to active mode).

### Task 5: Editorial Shields Sheet (`09_Shields_Sheet_Dark.png`)
- Create `EditorialShieldsSheet.kt` in `app/src/main/java/com/musify/browser/ui/screens/`:
  - Bottom sheet with drag handle, Shield icon, and DM Serif Display `Privacy Shields` title.
  - Current site origin / domain.
  - Blocked requests counter card with live count.
  - Per-site shield toggle switch ("Shields for this site").
  - Filter Lists shortcut row.
  - Done button.

### Task 6: Unit Test Suite & Verification
- Create `Phase36EditorialSuiteTest.kt` validating:
  - `MusifyHomeScreen` semantics, test tags, and shortcut URLs.
  - `EditorialWebToolbar` structure, controls, and callbacks.
  - `EditorialActionMenuSheet` complete action inventory.
  - `EditorialTabSwitcherScreen` normal vs. private modes and scoped actions.
  - `EditorialShieldsSheet` live shield toggling and domain presentation.
- Run `./gradlew.bat testDebugUnitTest` ensuring 100% pass rate.

### Task 7: Release APK Compilation & Verification
- Compile `Musify.apk`, `Musify-Lite.apk`, and `Musify-ARM64.apk`.
- Verify size $< 100\text{ MB}$ and signatures with `apksigner`.

### Task 8: Live Android Emulator Testing & Documentation
- Deploy `Musify.apk` to running Android emulator `emulator-5554` (API 36).
- Capture live screenshots of Home, Web Toolbar, More Menu, Tab Switcher, and Shields Sheet.
- Document results in `.brain/.report/PHASE-36-REPORT.md`.
