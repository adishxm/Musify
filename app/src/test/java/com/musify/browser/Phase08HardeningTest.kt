package com.musify.browser

import com.musify.browser.browser.ParsedNavigation
import com.musify.browser.browser.UrlParser
import com.musify.browser.downloads.DownloadItem
import com.musify.browser.downloads.DownloadStatus
import com.musify.browser.downloads.LocalDownloadRepository
import com.musify.browser.filterlist.FilterRuleCompiler
import com.musify.browser.permissions.LocalSitePermissionRepository
import com.musify.browser.permissions.SitePermissionDecision
import com.musify.browser.permissions.SitePermissionType
import com.musify.browser.safety.ExternalSchemeHandler
import com.musify.browser.safety.SchemeDecision
import com.musify.browser.storage.LocalBookmarkRepository
import com.musify.browser.storage.LocalHistoryRepository
import com.musify.browser.storage.TimeRange
import com.musify.browser.tabs.FileTabSessionStore
import com.musify.browser.tabs.TabsViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.EngineNavigationState
import com.musify.browser.engine.EngineProcessState
import com.musify.browser.engine.EngineSecurityState
import com.musify.browser.engine.EngineShieldState
import com.musify.browser.engine.EngineType
import com.musify.browser.tabs.TabEnginePool

/**
 * Phase08HardeningTest validates:
 * 1. Security configuration in AndroidManifest.xml and network_security_config.xml.
 * 2. ProGuard and R8 release hardening rules in proguard-rules.pro.
 * 3. STRIDE attack mitigations: dangerous schemes fail closed, benign schemes prompt, intent installers blocked.
 * 4. Origin normalization and URL parser security.
 * 5. Comprehensive Before/After browsing data deletion across history, bookmarks, tabs, downloads, and site permissions.
 * 6. Time-range selective history purging.
 * 7. Private browsing zero-disk leakage guarantee.
 * 8. Denial-of-Service protections: tab capacity cap and filter compiler line length bounding.
 * 9. Renderer process loss and recovery resilience.
 */
class Phase08HardeningTest {

    private class MockEngineAdapter(val id: String = "test-adapter") : EngineAdapter {
        override val engineType: EngineType = EngineType.GECKO_VIEW
        private val _nav = MutableStateFlow(EngineNavigationState())
        override val navigationState: StateFlow<EngineNavigationState> = _nav

        private val _sec = MutableStateFlow(EngineSecurityState(isSecure = true))
        override val securityState: StateFlow<EngineSecurityState> = _sec

        private val _shield = MutableStateFlow(EngineShieldState())
        override val shieldState: StateFlow<EngineShieldState> = _shield

        private val _proc = MutableStateFlow(EngineProcessState())
        override val processState: StateFlow<EngineProcessState> = _proc

        var isDestroyed = false
        var loadedUrl: String? = null

        override fun createView(context: android.content.Context): android.view.View = android.view.View(context)
        override fun loadUrl(url: String) {
            loadedUrl = url
            _nav.value = _nav.value.copy(url = url)
        }
        override fun goBack(): Boolean = false
        override fun goForward(): Boolean = false
        override fun reload() {}
        override fun stop() {}
        override fun recoverRenderer() {
            _proc.value = EngineProcessState(isHealthy = true, hasCrashed = false)
        }
        override fun setProtectionEnabled(enabled: Boolean) {}
        override fun simulateRendererLoss() {
            _proc.value = EngineProcessState(isHealthy = false, hasCrashed = true)
        }
        override fun destroy() {
            isDestroyed = true
        }
    }

    private fun createTestPool(): TabEnginePool = TabEnginePool { MockEngineAdapter() }

    private lateinit var tempDir: File
    private lateinit var historyFile: File
    private lateinit var bookmarksFile: File
    private lateinit var sessionFile: File
    private lateinit var downloadsFile: File
    private lateinit var sitePermissionsFile: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "musify_phase08_test_${System.nanoTime()}")
        tempDir.mkdirs()
        historyFile = File(tempDir, "history.json")
        bookmarksFile = File(tempDir, "bookmarks.json")
        sessionFile = File(tempDir, "tabs.json")
        downloadsFile = File(tempDir, "downloads.json")
        sitePermissionsFile = File(tempDir, "site_permissions.json")
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    // 1. Android Manifest & Network Security Config Hardening Tests

    @Test
    fun testManifestSecurityHardening_BackupDisabledAndCleartextForbidden() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        val manifest = if (manifestFile.exists()) manifestFile.readText() else File("app/src/main/AndroidManifest.xml").readText()

        // 1. Disallow local ADB / cloud backup extraction
        assertTrue("Manifest must disable android:allowBackup", manifest.contains("""android:allowBackup="false""""))

        // 2. Enforce strict network security configuration
        assertTrue("Manifest must declare networkSecurityConfig", manifest.contains("""android:networkSecurityConfig="@xml/network_security_config""""))

        // 3. FileProvider must NOT be exported to third-party apps
        assertTrue("FileProvider must not be exported", manifest.contains("""android:exported="false""""))
        assertTrue("FileProvider must grant URI permissions", manifest.contains("""android:grantUriPermissions="true""""))

        // 4. Verify network_security_config.xml disallows cleartext HTTP by default
        val netSecFile = File("src/main/res/xml/network_security_config.xml")
        val netSec = if (netSecFile.exists()) netSecFile.readText() else File("app/src/main/res/xml/network_security_config.xml").readText()
        assertTrue("Cleartext traffic must be explicitly disallowed", netSec.contains("""cleartextTrafficPermitted="false""""))
        assertTrue("Trust anchors must be limited to system CAs", netSec.contains("""<certificates src="system" />"""))
    }

    // 2. ProGuard & R8 Optimization & Hardening Rules

    @Test
    fun testProguardRules_GeckoAndComposeProtected() {
        val proguardFile = File("proguard-rules.pro")
        val rules = if (proguardFile.exists()) proguardFile.readText() else File("app/proguard-rules.pro").readText()

        assertTrue("ProGuard must keep GeckoView classes", rules.contains("-keep class org.mozilla.geckoview.**"))
        assertTrue("ProGuard must protect coroutines internals", rules.contains("kotlinx.coroutines"))
        assertTrue("ProGuard must preserve Compose runtime", rules.contains("androidx.compose.runtime"))
        assertTrue("ProGuard must strip debug logging in release", rules.contains("-assumenosideeffects class android.util.Log"))
    }

    // 3. STRIDE Threat Mitigation: Scheme Interception & Fail-Closed Behavior

    @Test
    fun testSchemeHardening_DangerousSchemesFailClosed() {
        val dangerousUrls = listOf(
            "javascript:alert(document.cookie)",
            "file:///system/etc/hosts",
            "content://com.android.providers.telephony",
            "data:text/html,<script>alert(1)</script>",
            "blob:https://evil.org/payload",
            "chrome://flags",
            "jar:http://evil.com/app.jar!/malware.class"
        )

        for (url in dangerousUrls) {
            val decision = ExternalSchemeHandler.evaluate(url)
            assertTrue("URL '$url' must be blocked for security", decision is SchemeDecision.Blocked)
        }
    }

    @Test
    fun testSchemeHardening_BenignExternalSchemesPromptUser() {
        val benignUrls = listOf(
            "mailto:security@musify.org" to "mailto",
            "tel:+18005550199" to "tel",
            "sms:+18005550199" to "sms",
            "geo:37.7749,-122.4194" to "geo",
            "market://details?id=com.musify.browser" to "market"
        )

        for ((url, expectedScheme) in benignUrls) {
            val decision = ExternalSchemeHandler.evaluate(url)
            assertTrue("URL '$url' must prompt user before handoff", decision is SchemeDecision.PromptExternal)
            assertEquals(expectedScheme, (decision as SchemeDecision.PromptExternal).scheme)
        }
    }

    @Test
    fun testSchemeHardening_IntentPackageInstallerBlocked() {
        val installerIntent = "intent://install#Intent;scheme=package;package=com.android.packageinstaller;end"
        val decision = ExternalSchemeHandler.evaluate(installerIntent)
        assertTrue(decision is SchemeDecision.Blocked)
        assertEquals("Automatic package installation intent blocked", (decision as SchemeDecision.Blocked).reason)
    }

    // 4. Origin Normalization & URL Parser Security

    @Test
    fun testOriginNormalization_SafeHandlingOfPortsAndPaths() {
        val parsed = UrlParser.parse("https://user:password@www.example.com:443/path/index.html?token=123")
        assertTrue(parsed is ParsedNavigation.AllowedUrl)
        val allowed = parsed as ParsedNavigation.AllowedUrl
        assertTrue(allowed.isHttps)
        assertEquals("www.example.com", allowed.host.lowercase())
    }

    // 5. Comprehensive Before/After Browsing Data Deletion

    @Test
    fun testComprehensiveDataDeletion_BeforeAndAfterEvidence() {
        val historyRepo = LocalHistoryRepository(historyFile)
        val bookmarkRepo = LocalBookmarkRepository(bookmarksFile)
        val sessionStore = FileTabSessionStore(sessionFile)
        val downloadRepo = LocalDownloadRepository(downloadsFile)
        val sitePermissionRepo = LocalSitePermissionRepository(sitePermissionsFile)

        // 1. Populate Before state
        historyRepo.addVisit("https://site1.com", "Site One")
        historyRepo.addVisit("https://site2.com", "Site Two")
        historyRepo.addVisit("https://site3.com", "Site Three")

        bookmarkRepo.addBookmark("https://site1.com", "Site One Bookmark")
        bookmarkRepo.addBookmark("https://site2.com", "Site Two Bookmark")

        downloadRepo.addDownload(
            DownloadItem(
                id = "dl_1",
                url = "https://site1.com/file1.pdf",
                origin = "https://site1.com",
                fileName = "file1.pdf",
                mimeType = "application/pdf",
                contentLength = 1024L,
                status = DownloadStatus.COMPLETED
            )
        )
        downloadRepo.addDownload(
            DownloadItem(
                id = "dl_2",
                url = "https://site2.com/file2.png",
                origin = "https://site2.com",
                fileName = "file2.png",
                mimeType = "image/png",
                contentLength = 2048L,
                status = DownloadStatus.COMPLETED
            )
        )

        sitePermissionRepo.setDecision("https://site1.com", SitePermissionType.CAMERA, SitePermissionDecision.GRANTED)
        sitePermissionRepo.setDecision("https://site2.com", SitePermissionType.GEOLOCATION, SitePermissionDecision.DENIED)

        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val viewModel = TabsViewModel(
            pool = createTestPool(),
            store = sessionStore,
            historyRepository = historyRepo,
            bookmarkRepository = bookmarkRepo,
            downloadManager = com.musify.browser.downloads.MusifyDownloadManager(
                context = null,
                repository = downloadRepo,
                scope = testScope
            ),
            sitePermissionRepository = sitePermissionRepo,
            coroutineScope = testScope
        )

        viewModel.createNewTab("https://site1.com")
        viewModel.createNewTab("https://site2.com")

        // Assert BEFORE state
        assertEquals(3, historyRepo.getHistory().size)
        assertEquals(2, bookmarkRepo.getBookmarks().size)
        assertTrue(viewModel.tabsState.value.tabCount > 1)
        assertEquals(2, downloadRepo.getAllDownloads().size)
        assertEquals(2, sitePermissionRepo.getAllPermissions().size)

        // 2. Execute Comprehensive Clear Browsing Data
        viewModel.clearBrowsingData(
            clearHistory = true,
            clearBookmarks = true,
            clearTabs = true,
            clearDownloads = true,
            clearSitePermissions = true,
            timeRange = TimeRange.ALL_TIME
        )

        // Assert AFTER state (zero remnants)
        assertEquals("History must be completely wiped", 0, historyRepo.getHistory().size)
        assertEquals("Bookmarks must be completely wiped", 0, bookmarkRepo.getBookmarks().size)
        assertEquals("Open tabs must be reset to 1 default blank tab", 1, viewModel.tabsState.value.tabCount)
        assertEquals("about:blank", viewModel.tabsState.value.selectedTab?.url)
        assertEquals("Downloads metadata must be wiped", 0, downloadRepo.getAllDownloads().size)
        assertEquals("Site permissions must be revoked", 0, sitePermissionRepo.getAllPermissions().size)
    }

    // 6. Time-Range Selective History Purge

    @Test
    fun testTimeRangeDataDeletion_SelectiveHistoryPurge() {
        val historyRepo = LocalHistoryRepository(historyFile)
        val now = System.currentTimeMillis()

        // Seed visits: 1 from 2 hours ago, 1 from 10 minutes ago
        historyRepo.seedHistoryForTesting(
            listOf(
                com.musify.browser.storage.HistoryItem("1", "https://old.org", "Old Page", now - 2 * 3600 * 1000L),
                com.musify.browser.storage.HistoryItem("2", "https://recent.org", "Recent Page", now - 10 * 60 * 1000L)
            )
        )

        assertEquals(2, historyRepo.getHistory().size)

        // Purge only last hour
        historyRepo.deleteByTimeRange(TimeRange.LAST_HOUR, now)

        val remaining = historyRepo.getHistory()
        assertEquals(1, remaining.size)
        assertEquals("https://old.org", remaining[0].url)
    }

    // 7. Private Browsing Isolation Guarantee

    @Test
    fun testPrivateMode_ZeroHistoryAndDiskPersistenceTraces() {
        val historyRepo = LocalHistoryRepository(historyFile)
        val sessionStore = FileTabSessionStore(sessionFile)
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

        val viewModel = TabsViewModel(
            pool = createTestPool(),
            store = sessionStore,
            historyRepository = historyRepo,
            coroutineScope = testScope
        )

        // Create Private Tab
        viewModel.createNewTab("https://secret.org", isPrivate = true, selectImmediately = true)
        val activeTab = viewModel.tabsState.value.selectedTab
        assertNotNull(activeTab)
        assertTrue(activeTab!!.isPrivate)

        // Verify history repo did NOT record visit
        assertEquals("Private tabs must NEVER write to history repository", 0, historyRepo.getHistory().size)

        // Verify session serialization ignores private tabs
        viewModel.persistSession()
        val reloadedStore = sessionStore.load()
        assertNotNull(reloadedStore)
        assertTrue("Saved session must not contain any private tabs", reloadedStore!!.tabs.none { it.isPrivate })
    }

    // 8. Denial-of-Service Protections: Tab Cap and Compiler Bounds

    @Test
    fun testDosProtection_MaxTabCapacityEnforced() {
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val viewModel = TabsViewModel(
            pool = createTestPool(),
            store = null,
            coroutineScope = testScope
        )

        for (i in 1..25) {
            viewModel.createNewTab("https://tab$i.org")
        }

        assertTrue("Max tab limit must cap at 20 tabs", viewModel.tabsState.value.tabCount <= 20)
        assertTrue("TabsState must flag isAtMaxCapacity", viewModel.tabsState.value.isAtMaxCapacity)
        assertFalse("Creating tab beyond max capacity must return false", viewModel.createNewTab("https://overflow.org"))
    }

    @Test
    fun testFilterRuleCompiler_MemoryBoundsAndSanitization() = kotlinx.coroutines.runBlocking {
        // Line with > 2,048 chars should be safely rejected
        val hugeLine = "||" + "a".repeat(2500) + "^"
        val input = "||tracker.com^\n$hugeLine\n||ads.com^"

        val metadata = com.musify.browser.filterlist.FilterListRegistry.PETER_LOWE_SEED
        val compiled = FilterRuleCompiler.compile(input.byteInputStream(), metadata)
        assertEquals("Valid rules must be compiled while huge lines are discarded", 2, compiled.stats.supportedNetworkRules)
        assertEquals(1, compiled.stats.oversizedLinesSkipped)
        assertTrue(compiled.evaluate("https://tracker.com") is com.musify.browser.filterlist.FilterDecision.Blocked)
        assertTrue(compiled.evaluate("https://ads.com") is com.musify.browser.filterlist.FilterDecision.Blocked)
    }

    // 9. Renderer Process Loss and Recovery

    @Test
    fun testRendererCrash_ProcessLossAndRecovery() {
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val adapter = MockEngineAdapter("test-recovery-adapter")
        val pool = TabEnginePool { adapter }
        val viewModel = TabsViewModel(
            pool = pool,
            store = null,
            coroutineScope = testScope
        )

        // Tab initially healthy
        assertFalse(viewModel.tabsState.value.selectedTab!!.isRendererCrashed)

        // Simulate crash
        adapter.simulateRendererLoss()
        assertTrue(viewModel.tabsState.value.selectedTab!!.isRendererCrashed)

        // Safe recovery call
        viewModel.recoverRenderer()
        assertFalse(viewModel.tabsState.value.selectedTab!!.isRendererCrashed)
    }
}
