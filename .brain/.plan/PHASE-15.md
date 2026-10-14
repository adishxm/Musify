# Phase 15 — Obsidian Design System Foundation, Inter Typography & Vector Iconography

**Status:** PLANNED  
**Planning duration:** 2–3 days  
**Goal:** Establish the foundational layer of the "Obsidian" Design System for Musify. Replace the disparate legacy cyan/navy color palette with an uncompromising monochrome OLED token ladder, integrate the official variable font `inter_variable.ttf` (weights 100–800) with calibrated tracking and tabular digits, introduce 44 hand-crafted bespoke 24dp vector icons in `MusifyIcons.kt` (retiring cartoon emojis), build the unified primitive component suite in `Components.kt`, and update Android platform window theme styles to eliminate the white launch flash.

## Scope and tasks
1. **Typography & Font Assets:**
   - Install `inter_variable.ttf` (weights 100–800) and supporting static font faces (`inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `inter_bold.ttf`) under `app/src/main/res/font/`.
   - Update `app/src/main/java/com/musify/browser/ui/theme/Type.kt` with variable font variations, tuned tracking (`-1.3sp` to `+0.7sp`), `MusifyTypography_Overline`, `MusifyTypography_Mono`, and `MusifyTypography_Numeric` with `"tnum"`.
2. **Obsidian Color Tokens & Theming:**
   - Update `Color.kt` with the canonical Obsidian ladder (`Ink #000000`, `Surface #0A0A0A`, `SurfaceRaised #111111`, `Elevated #171717`, `ElevatedHigh #1F1F1F`, `Hairline #242424`, `HairlineStrong #343434`, `Accent #FFFFFF`, `OnAccent #0A0A0A`), Paper inverted tokens, jewel-tone security semantics (`Success #4FCB92`, `Warning #DFB356`, `Danger #E0645B`, `Private #B0A0F0`), and tint helpers (`softTint`, `mediumTint`, `strongTint`). Retain backward-compatible token aliases during transition.
   - Update `Theme.kt` with `ObsidianColorScheme`, `PaperColorScheme`, `MusifyShapes` (xs 6dp, sm 10dp, md 14dp, lg 20dp, xl 28dp, pill 50%), `MusifyMotion`, `Space` 4dp grid, and dark-first `MusifyTheme` setting light status/navigation bar glyphs.
   - Update `res/values/themes.xml` and `res/values/colors.xml` with `@color/musify_ink` window backgrounds to prevent white launch flashes.
3. **Bespoke Vector Iconography:**
   - Add `MusifyIcons.kt` containing 44 custom 24dp vector icons with 1.7f stroke, round caps/joins, and crisp alignment: `Shield`, `ShieldCheck`, `ShieldOff`, `Lock`, `LockOpen`, `Warning`, `Incognito`, `Blocked`, `ArrowLeft`, `ArrowRight`, `ChevronRight`, `ChevronDown`, `ChevronUp`, `Close`, `Plus`, `MoreVertical`, `Tabs`, `Reload`, `Stop`, `Desktop`, `Mobile`, `Globe`, `Search`, `BookmarkOutline`, `StarFilled`, `StarOutline`, `Clock`, `Download`, `Trash`, `Document`, `Link`, `ExternalLink`, `Check`, `Settings`, `Help`, `Info`, `Map`, `Location`, `Bolt`, `Flask`, `Music`, `Dot`, `Emblem`.
4. **Shared Primitives Suite (`Components.kt`):**
   - Implement `MusifyIcon`, `IconLabel`, `Hairline`, `VHairline`, `Overline`, `SectionHeader`, `MusifyCard`, `SettingRow`, `MusifyPill`, `CountBadge`, `StatRow`, and `EmptyState`.
5. **Phase 15 Test Suite:**
   - Add `Phase15DesignSystemTest.kt` verifying color tokens, font variations, vector icon integrity, and component primitives.

## Required tests (run at phase end)
- [ ] `./gradlew testDebugUnitTest` runs with 100% pass rate including `Phase15DesignSystemTest`.
- [ ] `./gradlew :app:compileDebugKotlin` completes cleanly.
- [ ] All font files and XML color resources resolve without resource compilation errors.

## Verification Gate
- [ ] Zero regression on existing browser tests.
- [ ] All 44 vector icons instantiate without null pointers or missing SVG paths.
- [ ] Comprehensive `.brain/.report/PHASE-15-REPORT.md` generated with detailed evidence.
- [ ] Branch/commit created and pushed cleanly to GitHub `origin/main`.

## Deliverables
- `app/src/main/res/font/` variable & static font assets.
- `Color.kt`, `Type.kt`, `Theme.kt`, `MusifyIcons.kt`, `Components.kt`.
- Updated `themes.xml` and `colors.xml`.
- Unit test suite `Phase15DesignSystemTest.kt`.
- `.brain/.report/PHASE-15-REPORT.md`.
