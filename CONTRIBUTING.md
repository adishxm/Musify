# Contributing to Musify 🎵

Thank you for your interest in contributing to **Musify**! We welcome bug reports, site compatibility feedback, documentation improvements, and pull requests from developers of all skill levels.

---

## 🧭 Code of Conduct

Musify is a welcoming, respectful, and inclusive project. We expect all contributors and participants to treat one another with kindness, constructive feedback, and mutual respect.

---

## 🐛 Reporting Issues & Site Compatibility

When reporting bugs or broken website layouts, please provide actionable, evidence-based context. Measured capability reports help us fix issues much faster than "it doesn't work."

### For General Bugs
1. **Device & OS:** Device model, Android version, architecture (ARM64, x86_64, etc.).
2. **App Version:** Musify version / commit hash.
3. **Engine Mode:** GeckoView (default) or WebView fallback.
4. **Steps to Reproduce:** Exact URLs and interaction sequence.
5. **Expected vs Actual Behavior:** Screenshots or ADB logcat snippets if available.

### For Privacy Shields Breakages
If a specific website breaks when Privacy Shields are active:
- Site URL (e.g., `https://example.com`)
- Counter state: number of blocked requests shown on the Shields badge
- Whether toggling **"Shields Active on this Site"** restores functionality
- Whether toggling **"Desktop Mode"** resolves the issue

### For Spotify Web Issues
If Spotify Web encounters playback errors on your device:
- Tap the **Shields** pill → tap **"Web Playback & Media Diagnostics"**
- Share the status of:
  - Widevine DRM hardware probe (`cenc` / L1 / L3)
  - Desktop content mode active state
  - GeckoView Media Session state

---

## 🛠️ Development Setup

### Prerequisites
- **Android Studio:** Ladybug / Meerkat (or newer)
- **Android SDK:** Platforms API 35 (Android 15), Build-Tools 35.0.0+
- **JDK:** Java 17 or Java 21 (e.g., Microsoft OpenJDK 21 LTS)
- **Git**

### Building from Source

Clone the repository:
```bash
git clone https://github.com/adishxm/Musify.git
cd Musify
```

Run all unit tests (101 unit tests across all architecture phases):
```bash
# On Linux / macOS:
./gradlew testDebugUnitTest

# On Windows (PowerShell or cmd):
.\gradlew.bat testDebugUnitTest
```

Build debug or release APKs:
```bash
# Debug APK
.\gradlew.bat assembleDebug

# Release APK
.\gradlew.bat assembleRelease
```

Install directly to an active emulator or connected USB device:
```bash
.\gradlew.bat installDebug
```

---

## 🏗️ Architecture & Guidelines

Musify follows a clean, decoupled architecture:
- **UI:** 100% Jetpack Compose with Material 3, edge-to-edge support, and custom dark neon themes.
- **Engine Layer:** Abstracted `EngineAdapter` interface. GeckoView 135 is the primary high-privacy engine; AndroidX WebKit WebView serves as a lightweight fallback.
- **Privacy & Content Blocking:** Engine-level tracking protection (ETP strict), custom offline ABP rule compiler with atomic rollback, and Android Private DNS (AdGuard companion layer).
- **Session Lifecycle:** Strictly capped 20-tab architecture with zero-leak process death recovery. Never persist passwords, form state, or DOM data to disk.
- **Security:** Strict TLS enforcement, HTTPS-first upgrades with explicit dialog warnings, and sandboxed downloads with `FileProvider` mediation.

---

## 🤝 Submitting Pull Requests

1. **Fork the repository** and create a feature branch (`git checkout -b feature/my-enhancement`).
2. **Adhere to code style**: Write idiomatic Kotlin, avoid unnecessary dependencies, and keep Compose UI components modular.
3. **Verify tests pass**: Run `.\gradlew.bat testDebugUnitTest` and ensure all 101 tests pass cleanly. Add new tests for any added logic.
4. **Commit with clear messages**: Use conventional commit prefixes (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
5. **Open a PR**: Describe your changes, rationale, and link to any relevant issues.

---

## 🌟 Good First Issues

Looking to contribute? Here are great starting points:
- Adding shortcut pills or customized search engine providers
- Improving AdBlock rule parsing edge cases
- Enhancing accessibility labels and screen reader support (`semantics`)
- Translating strings to additional languages in `res/values-*/strings.xml`

Thank you for helping make Musify the best privacy browser for music and everyday web browsing! 🚀
