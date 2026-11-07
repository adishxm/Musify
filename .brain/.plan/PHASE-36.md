# Phase 36 — Complete Universal Apple Safari UI/UX Overhaul Across All App Screens & Dialogs

**Status:** IN PROGRESS  
**Execution Date:** November 7, 2026  
**Goal:** Complete comprehensive visual, architectural, and interaction redesign of the ENTIRE Musify Android Browser inspired by Apple Safari (https://www.apple.com/in/safari/ and iOS 18 / macOS Human Interface Guidelines). Extend the Safari design system to every single screen, dialog, and control: Safari Start Page, Safari Bottom Tab Bar, Safari 3-Tab Bookmarks/Reading List/History, Safari Downloads, Safari Website Data, Safari Content Blockers, Safari Website Settings, Safari Welcome/Get Started, and Apple modal dialogs.

---

## 1. Context & User Directive

The user issued an explicit, unambiguous requirement:
> *"completely redesign my entire app ui/ux by inspiring by https://www.apple.com/in/safari/ ;; complete means in everythign i want you to design the ui is yours and you have to make it like safari okay so just did it"*

Every component, screen, sheet, dialog, and control in Musify must fully embrace Apple Safari's refined Cupertino design language:
- **Design Tokens**: Pure `#000000` dark canvas, `#1C1C1E` / `#2C2C2E` inset grouped cards, `#38383A` 0.5dp borders, `#007AFF` Safari Accent Blue, `#34C759` Safari Green, `#AF52DE` Safari Private Purple, and `#FF3B30` Safari Danger Red.
- **Continuous Curvature (Squircles)**: iOS 18 style rounded surfaces and app icon squircle tiles.
- **SF-Inspired Typography**: High-contrast, beautifully weighted type scales.
- **Apple Liquid Glass**: Frosted translucent floating capsules with specular hairline rims.

---

## 2. Scope & Implementation Plan

### Task 1: Safari Design System & Custom Vector Icons (`Color.kt`, `Type.kt`, `MusifyIcons.kt`)
- Ensure all Safari tokens are first-class primitives in `Color.kt` and `Type.kt`.
- Provide complete custom vector icons matching Apple SF Symbols:
  - `Compass` (Safari Compass emblem with gradient needle)
  - `ReadingList` (Apple Safari iconic round spectacles)
  - `BookOpen` (Apple Bookmarks open book glyph)
  - `ShareIOS` (Apple square with upward arrow)
  - `FormatAA` (Apple "aA" font formatting symbol)
  - `TabsGrid` (Apple 2-card tab switcher icon)
  - `Shield` (Apple Safari Privacy Report shield)
  - `Lock` (Apple HTTPS padlock)
  - `Trash`, `Download`, `History`, `Settings`, `Desktop`, `Close`, `Plus`, `ArrowLeft`, `Refresh`

### Task 2: Core Browser Shell (`BrowserShellScreen.kt`)
- Primary Home Surface: `SafariStartPage` (Favorites grid with 8 squircle shortcuts, Privacy Report card with live tracker stats, Reading List spectacles, Edit pill).
- Floating Web Toolbar: `SafariBottomTabBar` (floating bottom capsule with `aA` menu, lock, domain, reload, share `↑`, and `[ N ]` tab badge).
- `aA` Format & Page Settings: `SafariPageSettingsSheet`.
- Share & Action Sheet: `SafariShareSheet` (iOS 18 style sheet with quick action pills and grouped system rows).
- Privacy Report: `SafariPrivacyReportSheet` (ITP tracker breakdown, blocked percentages, and shield switches).
- Tab Switcher: `SafariTabSwitcherScreen` (2-column card deck with Normal vs Private segmented selector, `Done`, and `+` New Tab).

### Task 3: Safari Welcome & First-Run Experience (`WelcomeScreen.kt`)
- Apple-inspired clean launch surface:
  - Safari Compass emblem.
  - Large Title: "Welcome to Safari" / "Safari on Musify".
  - Subhead: "Fast. Private. Beautiful."
  - 3 feature rows with rounded squircle badge icons:
    1. Lightning-Fast Browsing & Media
    2. Intelligent Tracking Prevention (ITP)
    3. Bottom Tab Bar for One-Handed Reachability
  - Bottom: Full-width vibrant Safari Blue button: "Continue".

### Task 4: Safari Bookmarks, Reading List & History (`HistoryBookmarksScreen.kt`)
- Apple Safari's signature 3-segmented modal sheet:
  - Top Bar: "Done" button on top right, "Edit" button on top left.
  - Segmented Control with 3 tabs:
    1. Bookmarks (`BookOpen`)
    2. Reading List (`ReadingList` spectacles)
    3. History (`History` clock)
  - Inset Grouped list rows with favicon squircle, title, domain subtitle, and disclosure chevrons (`›`).
  - Bottom "Clear" button in History opening Apple's standard time range sheet: "The last hour", "Today", "Today and yesterday", "All time".

### Task 5: Safari Downloads Manager (`DownloadsScreen.kt`)
- Apple Safari Downloads popover / sheet:
  - Top Bar: "Downloads" title, "Done" button, and "Clear" button.
  - Inset grouped list of downloaded files with document/audio/video icons, file size, download timestamp.
  - Quick "Open" and "Share" actions.
  - Clean iOS empty state ("No Downloads").

### Task 6: Safari Website Data & Privacy (`DataControlScreen.kt`)
- Apple Safari Settings -> Advanced -> "Website Data":
  - Search bar ("Search website data").
  - Total storage used ("14.2 MB Website Data").
  - Inset grouped list of domains (e.g. `spotify.com - 4.8 MB`, `youtube.com - 6.2 MB`).
  - "Remove All Website Data" destructive action at the bottom.

### Task 7: Safari Website Settings & Permissions (`SitePermissionsScreen.kt`)
- Apple Safari "Settings for [domain]":
  - Inset grouped switches:
    - "Request Desktop Website"
    - "Use Content Blockers"
  - Inset grouped permission selectors:
    - Camera, Microphone, Location, Notifications.

### Task 8: Safari Content Blockers & Extensions (`FilterListScreen.kt`)
- Apple Safari Settings -> "Content Blockers":
  - Description: "Content blockers prevent trackers, cookies, ads, and other content from loading in Safari."
  - Inset grouped toggles:
    - "Peter Lowe's Ad & Tracking Server List"
    - "Brave Anti-Adblock Defusers"
    - "Brave Cookie Notice Blocker"
    - "VAST & VMAP Video Ad Interceptors"
    - "SABR Protobuf Video Stream Defuser"
  - Status card with active compiled rule metrics.

### Task 9: About Safari & System Dialogs (`ReleaseInfoScreen.kt`, `DnsSettingsDialog.kt`, `PlaybackCompatibilityDialog.kt`)
- macOS / iOS "About Safari" dialog with Compass emblem, version, WebKit / GeckoView details, and Apple-styled Privacy & Legal links.
- Secure DNS / Private Relay guidance dialog.
- Media & DRM Diagnostics dialog with Apple clean grouped styling.

### Task 10: Full Test Suite Verification, APK Compilation & Real Emulator Validation
- Create `Phase36SafariUniversalOverhaulTest.kt` verifying all new and updated Safari screens.
- Run `./gradlew.bat testDebugUnitTest` ensuring 100% pass rate across all suites.
- Assemble `Musify.apk`, `Musify-Lite.apk`, and `Musify-ARM64.apk` with verified signatures and size $< 100\text{ MB}$.
- Install and test on Android Emulator `emulator-5554` (API 36).
- Capture screenshots of Welcome, Start Page, Bottom Bar, Tab Switcher, Share Sheet, Privacy Report, Bookmarks, and Settings.
- Document in `.brain/.report/PHASE-36-REPORT.md`.
