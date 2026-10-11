# Musify

A native Android privacy-focused general-purpose web browser built with Kotlin, Jetpack Compose, and modern architecture.

---

## Executive Overview

Musify is an independent, native Android browser designed to deliver transparent, measurable privacy protections for arbitrary web browsing. It is engineered with safe defaults, zero telemetry, and a defensible local data lifecycle.

### Core Architectural Guarantees & Scope
- **General-Purpose Browsing:** Engineered for arbitrary web navigation, forms, media, and web applications without single-site special casing or hacks.
- **Independent Design & Identity:** Distinct UI, code, icons, assets, and product language.
- **Zero Telemetry or Credential Harvesting:** Zero behavioral analytics, ad tracking, or cloud profile synchronization. All browsing data remains strictly on-device.
- **Strict TLS Security:** Enforced certificate validation with zero silent overrides or unsafe blanket SSL proceed options.
- **Documented Request Protection:** Transparent shield counters labeled *"requests blocked by this app"*, per-site exception controls, and disposable private browsing sessions.

---

## Tech Stack & Environment

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose (Material 3, Edge-to-Edge)
- **Build System:** Gradle 8.13 with Kotlin DSL (`build.gradle.kts`)
- **Android Gradle Plugin (AGP):** 8.13.2
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 26 (Android 8.0)
- **JVM Target:** Java 17 / 21
- **Engine Roadmap:** GeckoView Stable production candidate (with isolated WebView comparison/fallback adapter)

---

## Roadmap & .brain Workspace

Project development follows the phased, evidence-based roadmap structured in the `.brain/` directory:

| Phase | Description | Status |
|---|---|---|
| **Phase 00** | Repository, project foundation & Jetpack Compose setup | **Completed** |
| **Phase 01** | Browser engine feasibility spike (GeckoView Stable) | Up Next |
| **Phase 02** | Single-tab browser shell & navigation controls | Queued |
| **Phase 03** | Multi-tab state machine & process recovery | Queued |
| **Phase 04** | History, bookmarks & disposable private sessions | Queued |
| **Phase 05** | Privacy shields & content blocking controls | Queued |
| **Phase 06** | Filter-list pipeline & licensing compliance gate | Queued |
| **Phase 07** | Browser essentials (downloads, site permissions) | Queued |
| **Phase 08** | Hardening (security audit, a11y, memory profiling) | Queued |
| **Phase 09** | Beta distribution & release readiness gate | Queued |

---

## Building and Testing

### Prerequisites
- Android Studio Ladybug / Meerkat or Android SDK (API 35 platforms, build-tools 35.0.0+)
- JDK 17 or JDK 21 (configured via `JAVA_HOME`)

### Build Commands
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

---

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.
