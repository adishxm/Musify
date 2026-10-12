package com.musify.browser

import com.musify.browser.release.ReleaseInfoRegistry
import com.musify.browser.storage.LocalAppPreferencesRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Phase 09 automated release readiness tests covering:
 * - App metadata, version naming, and package clearance.
 * - Trademark non-affiliation and legal provenance notices.
 * - Privacy policy statements and zero-telemetry declarations.
 * - Honest known technical limitations disclosures.
 * - App preferences & onboarding lifecycle persistence.
 * - Secure signing configuration and git exclusion of keystores.
 * - Complete data erasure and uninstall guidance.
 */
class Phase09ReleaseTest {

    private lateinit var tempDir: File
    private lateinit var prefsFile: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "musify_phase09_test_${System.nanoTime()}")
        tempDir.mkdirs()
        prefsFile = File(tempDir, "musify_preferences.json")
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    // 1. Release Metadata & Package Checks

    @Test
    fun testAppMetadata_PackageAndVersionIntegrity() {
        val meta = ReleaseInfoRegistry.METADATA

        assertEquals("Musify", meta.appName)
        assertEquals("com.musify.browser", meta.applicationId)
        assertEquals("0.9.0-beta", meta.versionName)
        assertEquals(1, meta.versionCode)
        assertEquals(35, meta.targetSdk)
        assertEquals(26, meta.minSdk)
        assertEquals("Mozilla GeckoView Stable", meta.engineName)
        assertEquals("135.0.20250130195129", meta.engineVersion)
        assertEquals("Apache-2.0", meta.license)
        assertTrue(meta.sourceRepoUrl.contains("github.com/adishxm/Musify"))
        assertTrue(meta.issueTrackerUrl.contains("github.com/adishxm/Musify/issues"))
    }

    // 2. Trademark & Independence Notice

    @Test
    fun testTrademarkAndIndependence_NoAffiliationWithBraveOrMozilla() {
        val notice = ReleaseInfoRegistry.TRADEMARK_AND_AFFILIATION_NOTICE

        assertTrue("Notice must state independent project", notice.contains("independent"))
        assertTrue("Notice must disclaim affiliation with Brave", notice.contains("Brave Software"))
        assertTrue("Notice must disclaim affiliation with Mozilla", notice.contains("Mozilla Foundation"))
        assertTrue("Notice must attribute GeckoView trademark", notice.contains("trademark of the Mozilla Foundation"))
    }

    // 3. Privacy Policy Declarations

    @Test
    fun testPrivacyPolicy_ZeroTelemetryAndLocalOnlyGuarantees() {
        val policy = ReleaseInfoRegistry.PRIVACY_POLICY
        assertTrue("Privacy policy must contain comprehensive sections", policy.size >= 5)

        val titles = policy.map { it.title }
        assertTrue(titles.any { it.contains("Zero Telemetry") })
        assertTrue(titles.any { it.contains("On-Device Data") })
        assertTrue(titles.any { it.contains("Sandboxed") })
        assertTrue(titles.any { it.contains("Deny-by-Default") })
        assertTrue(titles.any { it.contains("Private Sessions") })
        assertTrue(titles.any { it.contains("Data Erasure") })

        val telemetrySection = policy.first { it.title.contains("Zero Telemetry") }
        assertTrue(telemetrySection.details.contains("No telemetry beacons"))
    }

    // 4. Honest Limitations Disclosure

    @Test
    fun testKnownLimitations_HonestTechnicalBoundaries() {
        val limitations = ReleaseInfoRegistry.KNOWN_LIMITATIONS
        assertTrue("Known limitations must document key technical bounds", limitations.size >= 4)

        val titles = limitations.map { it.title }
        assertTrue(titles.any { it.contains("Widevine") })
        assertTrue(titles.any { it.contains("WebExtensions") })
        assertTrue(titles.any { it.contains("Cosmetic") })
        assertTrue(titles.any { it.contains("Synchronization") })

        // Check DRM note explains open-source scope
        val drmItem = limitations.first { it.title.contains("Widevine") }
        assertTrue(drmItem.workAroundOrRoadmap.contains("Widevine CDM requires proprietary binary licensing"))
    }

    // 5. Onboarding Preferences Lifecycle

    @Test
    fun testOnboardingPreferences_PersistenceAndReset() {
        val repo = LocalAppPreferencesRepository(prefsFile)

        // Fresh state: onboarding not completed
        assertFalse(repo.hasCompletedOnboarding())
        assertTrue(repo.isHttpsOnlyMode())

        // User finishes onboarding
        repo.setCompletedOnboarding(true)
        assertTrue(repo.hasCompletedOnboarding())

        // Reload from disk to verify atomic file persistence
        val reloadedRepo = LocalAppPreferencesRepository(prefsFile)
        assertTrue("Onboarding completion must persist across app reloads", reloadedRepo.hasCompletedOnboarding())

        // Reset all
        repo.resetAll()
        assertFalse("Reset must return onboarding to incomplete state", repo.hasCompletedOnboarding())
    }

    // 6. Secure Signing & Keystore Protection in Git

    @Test
    fun testKeystoreExclusion_NeverTrackedInGit() {
        val gitignoreFile = File(".gitignore")
        val content = if (gitignoreFile.exists()) gitignoreFile.readText() else File("../.gitignore").readText()

        assertTrue("Gitignore must ignore *.keystore files", content.contains("*.keystore"))
        assertTrue("Gitignore must ignore *.jks files", content.contains("*.jks"))
        assertTrue("Gitignore must ignore signing.properties", content.contains("signing.properties"))
    }

    // 7. Data Clearing & Uninstall Guidance

    @Test
    fun testDataClearingGuidance_CoversInAppAndSystemUninstall() {
        val guide = ReleaseInfoRegistry.UNINSTALL_AND_DATA_CLEAR_GUIDE

        assertTrue(guide.contains("Data & Privacy Controls"))
        assertTrue(guide.contains("Clear storage"))
        assertTrue(guide.contains("Uninstalling Musify"))
    }
}
