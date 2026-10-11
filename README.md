# Musify

A native Android privacy-focused general-purpose web browser built with Kotlin, Jetpack Compose, and modern architecture.

---

## Executive Overview

Musify is an independent, native Android browser designed to deliver transparent, measurable privacy protections for arbitrary web browsing. It is engineered with safe defaults, zero telemetry, and a defensible local data lifecycle.

### Core Architectural Guarantees & Scope
- **General-Purpose Browsing:** Engineered for arbitrary web navigation, forms, media, and web applications without single-site special casing or hacks.
- **Independent Design & Identity:** Distinct UI, code, icons, assets, and product language.
- **Multi-Tab Architecture & Memory Protection:** Up to 20 concurrent tabs capped deterministically to prevent out-of-memory crashes; engine sessions are lazy-allocated and destroyed upon tab closure to free native memory.
- **Process & Session Recovery:** Automatic, zero-leak session recovery that persists strictly minimal tab metadata (UUID, URL, Title, Timestamps) while completely excluding DOM contents, form entries, cookies, or secrets.
- **Zero Telemetry or Credential Harvesting:** Zero behavioral analytics, ad tracking, or cloud profile synchronization. All browsing data remains strictly on-device.
- **Strict TLS Security:** Enforced certificate validation with zero silent overrides or unsafe blanket SSL proceed options; cleartext traffic disabled globally via Android Network Security Config.
- **Verified Origin Display & Anti-Spoofing:** Omnibar displays the full, verified origin (scheme, host, port) and decodes IDN homoglyphs into ASCII Punycode (`xn--...`) to mitigate visual spoofing.
- **Privacy Shields & Content Blocking:** Engine-native GeckoView strict protection against advertising trackers, social trackers, cryptominers, and browser fingerprinters.
- **Reversible Per-Origin Exceptions:** Scoped exception management without global toggle side-effects and zero hardcoded rules for production domains.
- **Conservative HTTPS-First Behavior:** Automatic upgrade to HTTPS with explicit insecure connection warning dialog before loading unencrypted HTTP websites.
- **Filter-List Pipeline & Licensing Gate:** Memory-bounded offline rule compiler (ABP network rules, exceptions, subdomains), staged atomic activations, last-known-good rollbacks, and transparent in-app licensing/provenance screens with zero false parity claims.
- **Browser Essentials & Sandboxed Storage:** User-confirmed off-thread downloads in sandboxed app-specific storage with `FileProvider` mediation, executable threat badges, exact-origin deny-by-default site permissions, fail-closed external scheme protection, and in-page search.

---

## Tech Stack & Environment

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose (Material 3, Edge-to-Edge)
- **Build System:** Gradle 8.13 with Kotlin DSL (`build.gradle.kts`)
- **Android Gradle Plugin (AGP):** 8.13.2
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 26 (Android 8.0)
- **JVM Target:** Java 17 / 21
- **Engine Architecture:** Decoupled `EngineAdapter` interface with:
  - **GeckoView Stable** (`135.0.20250130195129`) as primary production candidate
  - **AndroidX WebKit WebView** (`1.12.1`) as bounded prototype/fallback adapter

---

## Roadmap & .brain Workspace

Project development follows the phased, evidence-based roadmap structured in the `.brain/` directory:

| Phase | Description | Status | Evidence / Report |
|---|---|---|---|
| **Phase 00** | Repository, project foundation & Jetpack Compose setup | **Completed (PASS)** | [.brain/.report/PHASE-00-REPORT.md](.brain/.report/PHASE-00-REPORT.md) |
| **Phase 01** | Browser engine feasibility spike (GeckoView Stable & WebView) | **Completed (PASS)** | [.brain/.report/PHASE-01-REPORT.md](.brain/.report/PHASE-01-REPORT.md) |
| **Phase 02** | Single-tab browser shell, safe URL parser & navigation controls | **Completed (PASS)** | [.brain/.report/PHASE-02-REPORT.md](.brain/.report/PHASE-02-REPORT.md) |
| **Phase 03** | Tabs, lifecycle & session recovery (max-tab cap, process restore) | **Completed (PASS)** | [.brain/.report/PHASE-03-REPORT.md](.brain/.report/PHASE-03-REPORT.md) |
| **Phase 04** | History, bookmarks & disposable private sessions | **Completed (PASS)** | [.brain/.report/PHASE-04-REPORT.md](.brain/.report/PHASE-04-REPORT.md) |
| **Phase 05** | Privacy shields & content blocking controls | **Completed (PASS)** | [.brain/.report/PHASE-05-REPORT.md](.brain/.report/PHASE-05-REPORT.md) |
| **Phase 06** | Filter-list pipeline & licensing compliance gate | **Completed (PASS)** | [.brain/.report/PHASE-06-REPORT.md](.brain/.report/PHASE-06-REPORT.md) |
| **Phase 07** | Browser essentials (downloads, site permissions & safety) | **Completed (PASS)** | [.brain/.report/PHASE-07-REPORT.md](.brain/.report/PHASE-07-REPORT.md) |
| **Phase 08** | Hardening (security audit, a11y, memory profiling) | Up Next | [.brain/.plan/PHASE-08.md](.brain/.plan/PHASE-08.md) |
| **Phase 09** | Beta distribution & release readiness gate | Queued | [.brain/.plan/PHASE-09.md](.brain/.plan/PHASE-09.md) |

---

## Building and Testing

### Prerequisites
- Android Studio Ladybug / Meerkat or Android SDK (API 35 platforms, build-tools 35.0.0+)
- JDK 17 or JDK 21 (Microsoft OpenJDK 21 LTS configured via `JAVA_HOME`)

### Build Commands
```bash
# Run all unit tests (54 tests across Phases 00-06)
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

---

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.
