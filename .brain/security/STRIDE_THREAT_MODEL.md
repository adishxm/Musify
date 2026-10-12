# Musify STRIDE Security & Privacy Threat Model

**Status:** APPROVED  
**Scope:** Musify Native Android Privacy Browser  
**Framework:** Microsoft STRIDE Threat Modeling  
**Date:** 2026-10-12  
**Auditor:** Antigravity Automated Verification Agent  

---

## 1. Executive Summary

Musify is an independent native Android privacy-focused web browser built with Kotlin, Jetpack Compose, and GeckoView Stable. This STRIDE analysis documents the threat surface, attack vectors, defensive mitigations, and automated verification tests across the six STRIDE categories: **Spoofing**, **Tampering**, **Repudiation**, **Information Disclosure**, **Denial of Service**, and **Elevation of Privilege**.

---

## 2. Threat Matrix

| STRIDE Category | Threat Description | Attack Vector | Implemented Mitigation | Verification Test |
|---|---|---|---|---|
| **Spoofing** | Origin Spoofing / Homograph Attack | Malicious domain using lookalike Unicode chars or sneaky subdomains to imitate trusted sites. | Exact-origin extraction via `java.net.URI`, lowercase normalization, and scheme/host separation in Omnibar chrome. | `Phase02SingleTabTest`, `Phase08HardeningTest` |
| **Spoofing** | Scheme Hijacking / Intent Impersonation | Web page triggers `intent://` or custom schemes to impersonate system components or redirect to malicious apps. | `ExternalSchemeHandler.evaluate()` intercepts non-web schemes; package installer intents blocked; benign schemes require user confirmation. | `Phase07BrowserEssentialsTest`, `Phase08HardeningTest` |
| **Tampering** | Filter List Poisoning / Corrupt Update | Attacker modifies filter list feed or injects invalid syntax to break adblocking or crash the app. | Memory-bounded compilation (max 15MB, 2,048 chars/line), syntax validation, staged atomic activation (`.tmp` -> active), and Last-Known-Good rollback. | `Phase06FilterListTest` |
| **Tampering** | Download Path Traversal | Content-Disposition or download URL with `../../` attempts to overwrite arbitrary app files or system binaries. | `DownloadSecurityUtils.sanitizeFileName()` strips traversal sequences (`/`, `\`, `..`), control characters, null bytes, and DOS reserved names (`CON`, `PRN`). | `Phase07BrowserEssentialsTest` |
| **Tampering** | Session File Corruption | Corrupted or truncated JSON session state causes app startup crash. | Safe JSON serialization with try/catch fallback; automatic creation of fresh blank tab session if disk file is unreadable. | `Phase03TabsTest` |
| **Repudiation** | Silent Permission Exploitation | Site accesses location or camera without user knowledge or record. | Deny-by-default architecture; explicit permission prompt displaying exact requesting origin; persistent audit record in `SitePermissionsScreen`. | `Phase07BrowserEssentialsTest` |
| **Information Disclosure** | Network Sniffing / TLS Downgrade | Network attacker intercepts unencrypted HTTP traffic or forces TLS downgrade. | Conservative HTTPS-first navigation with blocking warning dialog; `network_security_config.xml` blocks cleartext traffic; system CA validation. | `Phase02SingleTabTest`, `Phase08HardeningTest` |
| **Information Disclosure** | Telemetry & Background Beaconing | Third-party SDKs, analytics, or crash reporters leaking URLs, device IDs, or browsing habits. | Zero telemetry architecture: no Firebase, no Google Analytics, no third-party network SDKs; all browsing state is strictly local. | Codebase Audit, `Phase08HardeningTest` |
| **Information Disclosure** | Private Session Disk Leakage | Private browsing tabs persisting URLs or form data to SQLite or JSON on disk. | Private tabs run with `isPrivate = true`; completely excluded from history recording and tab session disk persistence; memory discarded on tab close. | `Phase04DataAndPrivateTest`, `Phase08HardeningTest` |
| **Information Disclosure** | ADB Backup Data Extraction | Attacker connects device over USB and runs `adb backup` to extract saved session, history, and bookmarks. | `android:allowBackup="false"` in `AndroidManifest.xml` prevents local extraction via ADB or cloud device backup. | `Phase08HardeningTest` |
| **Denial of Service** | Tab Exhaustion / Out of Memory | Malicious page opens infinite popups or user opens dozens of heavy tabs until OOM crash. | Strict 20-tab maximum cap (`maxTabs = 20`); excess tabs rejected; engine adapters released when tabs are closed. | `Phase03TabsTest`, `Phase08HardeningTest` |
| **Denial of Service** | Renderer Crash / Web Process Loss | Heavy WebAssembly or GPU exploit crashes the GeckoView child content process. | Isolated multi-process architecture; browser chrome survives renderer loss; displays interactive "Recover Tab" overlay to reload without losing session. | `Phase03TabsTest`, `Phase08HardeningTest` |
| **Elevation of Privilege** | Unprompted Binary Execution | Malicious website downloads `.apk`, `.exe`, or `.sh` and tricks the browser into executing it. | Downloads stored in sandboxed app storage; zero broad storage permissions; executable warning badge; opening mediated strictly through `FileProvider`. | `Phase07BrowserEssentialsTest` |
| **Elevation of Privilege** | Malicious Internal Scheme Access | Website attempts navigation to `file:///`, `content://`, `javascript:`, or `chrome://` to access device files. | `ExternalSchemeHandler` fails closed immediately on all dangerous internal schemes, blocking navigation before the engine loads the URI. | `Phase07BrowserEssentialsTest`, `Phase08HardeningTest` |
| **Elevation of Privilege** | Component Hijacking via Exported Activities | Other malicious apps send crafted intents to unexported internal components. | Only `MainActivity` is exported for `ACTION_MAIN` and `ACTION_VIEW` (`http`/`https`); inputs sanitized with `UrlParser`; FileProvider is unexported (`exported="false"`). | `Phase08HardeningTest` |

---

## 3. Defense-in-Depth Architecture

1. **Sandboxed Storage:**
   - App files directory (`context.filesDir`) stores history, bookmarks, tabs, filter lists, downloads, and site permissions.
   - `android:allowBackup="false"` ensures local sandboxed data cannot be extracted via cloud or USB backup.
2. **Mediation via Android FileProvider:**
   - Authority `${applicationId}.fileprovider` with `grantUriPermissions="true"` and `exported="false"`.
   - File sharing uses short-lived, permission-scoped `content://` URIs instead of dangerous raw `file://` URIs.
3. **Data Erasure Assurance:**
   - Comprehensive browsing data deletion removes history, bookmarks, tab sessions, downloads metadata, and site permissions atomically.
   - Time-range filtering (`TimeRange.LAST_HOUR`, `TimeRange.LAST_24_HOURS`, `TimeRange.LAST_7_DAYS`, `TimeRange.ALL_TIME`) accurately targets visit timestamps.
4. **Offline Rule Compilation:**
   - ABP rule parsing runs off-thread without executing JavaScript or dynamic code.
   - Cosmetic (`##`) and scriptlet (`+js()`) rules are filtered and logged honestly without DOM injection.

---

## 4. Conclusion & Sign-Off

All identified threats across STRIDE categories have implemented architectural mitigations and automated verification test cases. Musify satisfies all security, privacy, and integrity requirements for Phase 08.
