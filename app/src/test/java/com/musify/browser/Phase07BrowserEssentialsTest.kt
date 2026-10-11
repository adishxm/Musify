package com.musify.browser

import com.musify.browser.downloads.DownloadItem
import com.musify.browser.downloads.DownloadSecurityUtils
import com.musify.browser.downloads.DownloadStatus
import com.musify.browser.downloads.LocalDownloadRepository
import com.musify.browser.permissions.LocalSitePermissionRepository
import com.musify.browser.permissions.SitePermissionDecision
import com.musify.browser.permissions.SitePermissionType
import com.musify.browser.safety.ExternalSchemeHandler
import com.musify.browser.safety.FindInPageManager
import com.musify.browser.safety.SchemeDecision
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Phase07BrowserEssentialsTest validates:
 * 1. Download filename sanitization, traversal stripping, DOS name mitigation, and executable detection.
 * 2. Download repository operations, progress tracking, and atomic serialization.
 * 3. Exact-origin deny-by-default site permission storage, isolation, and revocation.
 * 4. External scheme security: dangerous internal schemes fail closed, benign external schemes prompt confirmation.
 * 5. Find-in-page lifecycle, query updates, and match cycling.
 */
class Phase07BrowserEssentialsTest {

    private lateinit var tempDir: File
    private lateinit var downloadsFile: File
    private lateinit var sitePermissionsFile: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "musify_phase07_test_${System.nanoTime()}")
        tempDir.mkdirs()
        downloadsFile = File(tempDir, "downloads.json")
        sitePermissionsFile = File(tempDir, "site_permissions.json")
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    // 1. Download Security Utils Tests

    @Test
    fun testSanitizeFileName_PathTraversalStripped() {
        assertEquals("evil.apk", DownloadSecurityUtils.sanitizeFileName("../../evil.apk", "application/vnd.android.package-archive"))
        assertEquals("report.pdf", DownloadSecurityUtils.sanitizeFileName("folder/../report.pdf", "application/pdf"))
        assertEquals("archive.zip", DownloadSecurityUtils.sanitizeFileName("..\\..\\archive.zip", "application/zip"))
    }

    @Test
    fun testSanitizeFileName_DangerousAndControlCharsStripped() {
        assertEquals("filebadchars.txt", DownloadSecurityUtils.sanitizeFileName("file:*?\"<>|badchars.txt", "text/plain"))
        assertEquals("nullbyte.png", DownloadSecurityUtils.sanitizeFileName("null\u0000byte.png", "image/png"))
    }

    @Test
    fun testSanitizeFileName_EmptyFallbackByMime() {
        assertEquals("download.pdf", DownloadSecurityUtils.sanitizeFileName(null, "application/pdf"))
        assertEquals("download.png", DownloadSecurityUtils.sanitizeFileName("", "image/png"))
        assertEquals("download.html", DownloadSecurityUtils.sanitizeFileName("   ", "text/html"))
        assertEquals("download.zip", DownloadSecurityUtils.sanitizeFileName(null, "application/zip"))
        assertEquals("download.bin", DownloadSecurityUtils.sanitizeFileName(null, null))
    }

    @Test
    fun testSanitizeFileName_DosReservedNamesNeutralized() {
        assertEquals("file_CON.txt", DownloadSecurityUtils.sanitizeFileName("CON.txt", "text/plain"))
        assertEquals("file_PRN.doc", DownloadSecurityUtils.sanitizeFileName("PRN.doc", "application/msword"))
        assertEquals("file_NUL.zip", DownloadSecurityUtils.sanitizeFileName("NUL.zip", "application/zip"))
        assertEquals("file_AUX", DownloadSecurityUtils.sanitizeFileName("AUX", null))
    }

    @Test
    fun testExecutableWarningDetection() {
        assertTrue(DownloadSecurityUtils.isExecutable("malware.apk"))
        assertTrue(DownloadSecurityUtils.isExecutable("script.sh"))
        assertTrue(DownloadSecurityUtils.isExecutable("setup.exe"))
        assertTrue(DownloadSecurityUtils.isExecutable("payload.bin"))
        assertTrue(DownloadSecurityUtils.isExecutable("run.bat"))
        assertTrue(DownloadSecurityUtils.isExecutable("exploit.cmd"))

        assertFalse(DownloadSecurityUtils.isExecutable("document.pdf"))
        assertFalse(DownloadSecurityUtils.isExecutable("photo.png"))
        assertFalse(DownloadSecurityUtils.isExecutable("readme.txt"))
        assertFalse(DownloadSecurityUtils.isExecutable("index.html"))
        assertFalse(DownloadSecurityUtils.isExecutable("bundle.zip"))
    }

    // 2. Download Repository Tests

    @Test
    fun testDownloadRepository_LifecycleAndProgress() {
        val repo = LocalDownloadRepository(downloadsFile)
        val item = DownloadItem(
            id = "dl_1",
            url = "https://example.com/file.pdf",
            origin = "https://example.com",
            fileName = "file.pdf",
            mimeType = "application/pdf",
            contentLength = 1000L,
            downloadedBytes = 0L,
            status = DownloadStatus.DOWNLOADING,
            filePath = "/downloads/file.pdf",
            createdAtEpochMs = 123456789L
        )

        repo.addDownload(item)
        assertEquals(1, repo.getAllDownloads().size)
        assertEquals(DownloadStatus.DOWNLOADING, repo.getDownload("dl_1")?.status)

        // Update progress
        repo.updateProgress("dl_1", 500L, 1000L)
        val updated = repo.getDownload("dl_1")
        assertNotNull(updated)
        assertEquals(500L, updated?.downloadedBytes)
        assertEquals(0.5f, updated?.progressPercent ?: 0f, 0.01f)

        // Complete download
        repo.updateStatus("dl_1", DownloadStatus.COMPLETED, filePath = "/downloads/file.pdf")
        assertEquals(DownloadStatus.COMPLETED, repo.getDownload("dl_1")?.status)
    }

    @Test
    fun testDownloadRepository_PersistenceAcrossReload() {
        val repo1 = LocalDownloadRepository(downloadsFile)
        repo1.addDownload(
            DownloadItem(
                id = "persist_1",
                url = "https://example.com/doc.pdf",
                origin = "https://example.com",
                fileName = "doc.pdf",
                mimeType = "application/pdf",
                contentLength = 2048L,
                status = DownloadStatus.COMPLETED,
                filePath = "/path/doc.pdf",
                createdAtEpochMs = 555L
            )
        )

        // Reload from same file
        val repo2 = LocalDownloadRepository(downloadsFile)
        assertEquals(1, repo2.getAllDownloads().size)
        val restored = repo2.getDownload("persist_1")
        assertNotNull(restored)
        assertEquals("doc.pdf", restored?.fileName)
        assertEquals("https://example.com", restored?.origin)
        assertEquals(DownloadStatus.COMPLETED, restored?.status)

        // Remove and verify
        repo2.removeDownload("persist_1")
        assertEquals(0, repo2.getAllDownloads().size)

        val repo3 = LocalDownloadRepository(downloadsFile)
        assertEquals(0, repo3.getAllDownloads().size)
    }

    // 3. Site Permission Repository Tests

    @Test
    fun testSitePermissionRepository_DenyByDefault() {
        val repo = LocalSitePermissionRepository(sitePermissionsFile)
        assertEquals(
            SitePermissionDecision.PROMPT,
            repo.getDecision("https://unseen-origin.com", SitePermissionType.CAMERA)
        )
        assertEquals(
            SitePermissionDecision.PROMPT,
            repo.getDecision("https://unseen-origin.com", SitePermissionType.GEOLOCATION)
        )
    }

    @Test
    fun testSitePermissionRepository_GrantAndDenyWithOriginIsolation() {
        val repo = LocalSitePermissionRepository(sitePermissionsFile)

        // Grant Camera to origin A
        repo.setDecision("https://trusted.org", SitePermissionType.CAMERA, SitePermissionDecision.GRANTED)
        assertEquals(
            SitePermissionDecision.GRANTED,
            repo.getDecision("https://trusted.org", SitePermissionType.CAMERA)
        )

        // Origin B remains ungranted
        assertEquals(
            SitePermissionDecision.PROMPT,
            repo.getDecision("https://untrusted.org", SitePermissionType.CAMERA)
        )

        // Other permissions for Origin A remain ungranted
        assertEquals(
            SitePermissionDecision.PROMPT,
            repo.getDecision("https://trusted.org", SitePermissionType.MICROPHONE)
        )

        // Explicit Deny
        repo.setDecision("https://sketchy.org", SitePermissionType.GEOLOCATION, SitePermissionDecision.DENIED)
        assertEquals(
            SitePermissionDecision.DENIED,
            repo.getDecision("https://sketchy.org", SitePermissionType.GEOLOCATION)
        )
    }

    @Test
    fun testSitePermissionRepository_RevocationAndDiskPersistence() {
        val repo1 = LocalSitePermissionRepository(sitePermissionsFile)
        repo1.setDecision("https://maps.example.com", SitePermissionType.GEOLOCATION, SitePermissionDecision.GRANTED)
        repo1.setDecision("https://meet.example.com", SitePermissionType.CAMERA, SitePermissionDecision.GRANTED)

        // Reload from file
        val repo2 = LocalSitePermissionRepository(sitePermissionsFile)
        assertEquals(2, repo2.getAllPermissions().size)
        assertEquals(
            SitePermissionDecision.GRANTED,
            repo2.getDecision("https://maps.example.com", SitePermissionType.GEOLOCATION)
        )

        // Revoke one
        repo2.revoke("https://maps.example.com", SitePermissionType.GEOLOCATION)
        assertEquals(
            SitePermissionDecision.PROMPT,
            repo2.getDecision("https://maps.example.com", SitePermissionType.GEOLOCATION)
        )
        assertEquals(1, repo2.getAllPermissions().size)

        // Clear all
        repo2.clearAll()
        assertEquals(0, repo2.getAllPermissions().size)

        val repo3 = LocalSitePermissionRepository(sitePermissionsFile)
        assertEquals(0, repo3.getAllPermissions().size)
    }

    // 4. External Scheme Handler Tests

    @Test
    fun testExternalScheme_WebSchemesInternal() {
        assertTrue(ExternalSchemeHandler.evaluate("https://duckduckgo.com") is SchemeDecision.InternalWeb)
        assertTrue(ExternalSchemeHandler.evaluate("http://example.org") is SchemeDecision.InternalWeb)
        assertTrue(ExternalSchemeHandler.evaluate("about:blank") is SchemeDecision.InternalWeb)
    }

    @Test
    fun testExternalScheme_DangerousSchemesBlocked() {
        assertTrue(ExternalSchemeHandler.evaluate("javascript:alert(1)") is SchemeDecision.Blocked)
        assertTrue(ExternalSchemeHandler.evaluate("file:///etc/passwd") is SchemeDecision.Blocked)
        assertTrue(ExternalSchemeHandler.evaluate("content://telephony/sim_cards") is SchemeDecision.Blocked)
        assertTrue(ExternalSchemeHandler.evaluate("chrome://flags") is SchemeDecision.Blocked)
        assertTrue(ExternalSchemeHandler.evaluate("jar:http://evil.com/archive.jar!/malware.class") is SchemeDecision.Blocked)
    }

    @Test
    fun testExternalScheme_BenignSchemesPromptUser() {
        val mailDecision = ExternalSchemeHandler.evaluate("mailto:test@musify.org")
        assertTrue(mailDecision is SchemeDecision.PromptExternal)
        assertEquals("mailto", (mailDecision as SchemeDecision.PromptExternal).scheme)

        val telDecision = ExternalSchemeHandler.evaluate("tel:+1234567890")
        assertTrue(telDecision is SchemeDecision.PromptExternal)
        assertEquals("tel", (telDecision as SchemeDecision.PromptExternal).scheme)

        val smsDecision = ExternalSchemeHandler.evaluate("sms:+1234567890")
        assertTrue(smsDecision is SchemeDecision.PromptExternal)
        assertEquals("sms", (smsDecision as SchemeDecision.PromptExternal).scheme)

        val geoDecision = ExternalSchemeHandler.evaluate("geo:37.7749,-122.4194")
        assertTrue(geoDecision is SchemeDecision.PromptExternal)
        assertEquals("geo", (geoDecision as SchemeDecision.PromptExternal).scheme)

        val marketDecision = ExternalSchemeHandler.evaluate("market://details?id=com.musify.browser")
        assertTrue(marketDecision is SchemeDecision.PromptExternal)
        assertEquals("market", (marketDecision as SchemeDecision.PromptExternal).scheme)
    }

    @Test
    fun testExternalScheme_DangerousInstallerBlocked() {
        val installerDecision = ExternalSchemeHandler.evaluate(
            "intent://downloads#Intent;scheme=package;package=com.android.packageinstaller;end"
        )
        assertTrue(installerDecision is SchemeDecision.Blocked)
        assertEquals(
            "Automatic package installation intent blocked",
            (installerDecision as SchemeDecision.Blocked).reason
        )
    }

    // 5. Find In Page Manager Tests

    @Test
    fun testFindInPageManager_StateLifecycle() {
        val manager = FindInPageManager()
        assertFalse(manager.state.value.isActive)

        manager.startFind()
        assertTrue(manager.state.value.isActive)
        assertEquals("", manager.state.value.query)

        manager.updateQuery("musify")
        assertEquals("musify", manager.state.value.query)
        assertEquals(3, manager.state.value.totalMatches)
        assertEquals("1 of 3", manager.state.value.displayMatchText)

        // Cycle next
        manager.nextMatch()
        assertEquals(1, manager.state.value.activeMatchIndex)
        assertEquals("2 of 3", manager.state.value.displayMatchText)

        manager.nextMatch()
        assertEquals(2, manager.state.value.activeMatchIndex)

        manager.nextMatch() // wrap around to 0
        assertEquals(0, manager.state.value.activeMatchIndex)

        // Cycle previous
        manager.previousMatch() // wrap around to 2
        assertEquals(2, manager.state.value.activeMatchIndex)

        // Close
        manager.close()
        assertFalse(manager.state.value.isActive)
        assertEquals("", manager.state.value.query)
        assertEquals(0, manager.state.value.totalMatches)
    }
}
