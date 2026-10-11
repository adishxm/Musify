# Filter-List Inventory & Licensing Compliance Audit

**Date:** 2026-10-12  
**Auditor:** Antigravity Engineering Agent  
**Scope:** Evaluation of external general-purpose network and ad/tracker blocking filter lists for the Musify native Android browser.

---

## 1. Candidate List Inventory & Legal Review

| List Name | Maintainer / Authors | Canonical Source URL | License | Redistribution & Modification Rules | Update Cadence | Recommendation for Musify |
|---|---|---|---|---|---|---|
| **Peter Lowe's Ad Server List** | Peter Lowe (Yoyo.org) | `https://pgl.yoyo.org/adservers/serverlist.php?hostformat=nohtml&showintro=0&mimetype=plaintext` | Permissive / Attribution (BSD-compatible) | Redistribution permitted with attribution. No copyleft virality. Safe to bundle in open-source and commercial products. | Weekly | **APPROVED FOR SEED BUNDLING** (Clean license, low overhead, high host accuracy). |
| **EasyList** | EasyList Community | `https://easylist.to/easylist/easylist.txt` | Dual-licensed: GPLv3 / CC BY-SA 3.0 | Requires CC BY-SA 3.0 attribution or GPLv3 reciprocal licensing. Contains thousands of cosmetic (`##`) and scriptlet rules that cannot be executed directly by raw network engines without DOM injection. | ~4 days | **EVALUATED & GATED**: Supported as remote subscription with honest disclosure; cosmetic rules cataloged as unsupported. |
| **EasyPrivacy** | EasyList Community | `https://easylist.to/easylist/easyprivacy.txt` | Dual-licensed: GPLv3 / CC BY-SA 3.0 | Same terms as EasyList. High tracking server coverage. | ~4 days | **EVALUATED & GATED**: Supported as remote subscription with honest disclosure. |
| **AdGuard Tracking Protection Filter** | AdGuard Team | `https://filters.adtidy.org/extension/ublock/filters/3.txt` | GNU General Public License v3.0 (GPLv3) | Requires GPLv3 compliance when bundled/distributed. | Weekly | **EVALUATED**: Available for update subscription; requires GPLv3 notice. |
| **GeckoView Native ETP (Disconnect.me)** | Disconnect, Inc. / Mozilla | Embedded inside GeckoView runtime binary | Mozilla / Disconnect Commercial Agreement | Shipped inside Mozilla's official GeckoView AAR. Zero extra licensing or bundling required by app. | Managed with engine releases | **PRIMARY ENGINE DEFENSE**: Fully active on-device via GeckoView's built-in `ContentBlocking` API (Phase 05). |

---

## 2. Engine Compatibility & Architecture Assessment

### 2.1 GeckoView Raw List Ingestion Findings
- **GeckoView API Constraint:** GeckoView 121.0.3 Stable exposes `ContentBlocking.AntiTracking.STRICT`, `CookieBehavior`, and `onContentBlocked` callbacks. It **does not** provide a native Java API to load raw ABP text filter lists (e.g. `easylist.txt`) directly into its internal network stack.
- **Engine-Native Protection:** The internal Disconnect tracking lists packaged with GeckoView provide reliable, zero-overhead tracker and fingerprinting protection at the C++ level.
- **Musify Pipeline Integration:**
  1. Musify retains **GeckoView native ETP as the primary real-time network defense**.
  2. Musify implements an **independent offline/in-memory Filter-List Compiler and Evaluator**:
     - Parses ABP-compatible network blocking (`||domain^`), exception (`@@`), and path patterns.
     - Formally catalogs and excludes cosmetic CSS element hiding (`##`) and scriptlet (`+js()`) rules with explicit counts, avoiding false claims of parity.
     - Can evaluate URLs pre-flight and export compiled rules to WebExtension / declarativeNetRequest formats or network delegates.
  3. **Bridge Decision:** Full native `adblock-rust` JNI bridge compilation is deferred to future milestones as an experimental adapter, satisfying the gate requirement: *"If bridge is not viable, keep engine-native protection and move bridge to future research."*

---

## 3. Bundling & Attribution Obligations Checklist

- [x] License and attribution for bundled Peter Lowe list embedded in app code and user-facing UI.
- [x] No proprietary or copyleft code included without corresponding notices.
- [x] Clear user-facing distinction between GeckoView built-in ETP protection and custom filter list pipeline.
- [x] Explicit rule count reporting: Network rules supported vs. cosmetic/scriptlet rules unsupported.
