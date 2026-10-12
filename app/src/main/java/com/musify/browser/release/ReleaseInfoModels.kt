package com.musify.browser.release

data class AppReleaseMetadata(
    val appName: String = "Musify",
    val applicationId: String = "com.musify.browser",
    val versionCode: Int = 1,
    val versionName: String = "0.9.0-beta",
    val targetSdk: Int = 35,
    val minSdk: Int = 26,
    val engineName: String = "Mozilla GeckoView Stable",
    val engineVersion: String = "135.0.20250130195129",
    val license: String = "Apache-2.0",
    val sourceRepoUrl: String = "https://github.com/adishxm/Musify",
    val issueTrackerUrl: String = "https://github.com/adishxm/Musify/issues"
)

data class PrivacyPolicySection(
    val title: String,
    val summary: String,
    val details: String
)

data class KnownLimitation(
    val title: String,
    val impact: String,
    val workAroundOrRoadmap: String
)

object ReleaseInfoRegistry {

    val METADATA = AppReleaseMetadata()

    val TRADEMARK_AND_AFFILIATION_NOTICE = """
        Musify is an independent native Android browser project. 
        It is not affiliated with, endorsed by, or sponsored by Brave Software or the Mozilla Foundation. 
        GeckoView is an open-source web engine and a registered trademark of the Mozilla Foundation.
    """.trimIndent()

    val PRIVACY_POLICY: List<PrivacyPolicySection> = listOf(
        PrivacyPolicySection(
            title = "Zero Telemetry & Beaconing",
            summary = "No analytics, tracking, or cloud crash collectors are bundled.",
            details = "Musify does not collect your IP address, device identifier, browsing history, search queries, or form inputs. No telemetry beacons are sent to Musify or third parties."
        ),
        PrivacyPolicySection(
            title = "Strict On-Device Data Lifecycle",
            summary = "All browsing data remains exclusively on your physical device.",
            details = "History, bookmarks, open tabs, and site permissions are saved in private application storage. No cloud profile synchronization or external backup extraction is permitted."
        ),
        PrivacyPolicySection(
            title = "Sandboxed File Downloads",
            summary = "Downloads are isolated in app storage with FileProvider mediation.",
            details = "Downloaded files are stored in sandboxed storage (filesDir/downloads) and are never exposed to broad external storage scanners without explicit user sharing via Android FileProvider."
        ),
        PrivacyPolicySection(
            title = "Deny-by-Default Hardware Permissions",
            summary = "Sensors and media devices are blocked until explicitly authorized.",
            details = "Websites requesting Geolocation, Camera, Microphone, or Notifications are blocked by default. Access is granted strictly on a per-origin basis and can be revoked at any time."
        ),
        PrivacyPolicySection(
            title = "Disposable Ephemeral Private Sessions",
            summary = "Private browsing tabs leave zero persistent traces on disk.",
            details = "Private tabs run in isolated sessions. Cookies, cache, local storage, and history visited in private mode are discarded immediately when private tabs are closed."
        ),
        PrivacyPolicySection(
            title = "Data Erasure & Uninstallation",
            summary = "Total removal is guaranteed by in-app controls and system uninstall.",
            details = "You can purge any or all browsing data via 'Data & Privacy Controls'. Uninstalling Musify triggers complete, permanent erasure of all local data by the Android operating system."
        )
    )

    val KNOWN_LIMITATIONS: List<KnownLimitation> = listOf(
        KnownLimitation(
            title = "Proprietary Commercial DRM (Widevine)",
            impact = "Certain subscription streaming websites (e.g., Netflix, Spotify Web) may fail to play protected video or fallback to standard resolution.",
            workAroundOrRoadmap = "Widevine CDM requires proprietary binary licensing not bundled with open-source distributions. General web media and HTML5 audio/video function normally."
        ),
        KnownLimitation(
            title = "WebExtensions / Add-ons UI",
            impact = "Third-party browser extensions cannot be installed through an in-app marketplace UI.",
            workAroundOrRoadmap = "Underlying GeckoView engine supports WebExtensions; a safe extension management interface is scheduled for a future release cycle."
        ),
        KnownLimitation(
            title = "Cosmetic Element Hiding Filters",
            impact = "Ad blocking engine performs strict network-level blocking (Peter Lowe & EasyList syntax); cosmetic CSS element placeholders may occasionally remain visible on page layouts.",
            workAroundOrRoadmap = "Network requests and trackers are actively blocked before loading. DOM CSS injection rules are planned for subsequent minor releases."
        ),
        KnownLimitation(
            title = "Cross-Device Cloud Synchronization",
            impact = "Tabs, history, and bookmarks cannot be synchronized between multiple devices.",
            workAroundOrRoadmap = "Intentionally excluded by design to prevent cloud profile aggregation and server-side credential retention."
        )
    )

    val UNINSTALL_AND_DATA_CLEAR_GUIDE = """
        • In-App Selective or Full Purge:
          Navigate to Overflow Menu (⋮) -> '🛡 Data & Privacy Controls'. Select History, Bookmarks, Open Tabs, Downloads, or Site Permissions, choose a time range, and confirm permanent erasure.

        • Android System Storage Wipe:
          Open Android Settings -> Apps -> Musify -> Storage & cache -> 'Clear storage'. This immediately resets the app to its factory state.

        • Complete Application Uninstall:
          Uninstalling Musify via the Android launcher completely and irreversibly removes all sandboxed databases, downloaded files, and configuration from the device.
    """.trimIndent()
}
