package com.musify.browser

import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.EngineNavigationState
import com.musify.browser.engine.EngineProcessState
import com.musify.browser.engine.EngineSecurityState
import com.musify.browser.engine.EngineShieldState
import com.musify.browser.engine.EngineType
import com.musify.browser.storage.BookmarkItem
import com.musify.browser.storage.HistoryItem
import com.musify.browser.storage.LocalBookmarkRepository
import com.musify.browser.storage.LocalHistoryRepository
import com.musify.browser.storage.TimeRange
import com.musify.browser.tabs.FileTabSessionStore
import com.musify.browser.tabs.TabAction
import com.musify.browser.tabs.TabEnginePool
import com.musify.browser.tabs.TabSession
import com.musify.browser.tabs.TabSessionSerializer
import com.musify.browser.tabs.TabsReducer
import com.musify.browser.tabs.TabsState
import com.musify.browser.tabs.TabsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Phase 04 automated tests covering:
 * - Local History persistence, multi-visit count tracking, query search, single deletion, and time-range purging.
 * - Local Bookmarks persistence, duplicate prevention, query search, and removal.
 * - Private mode isolation: Private tabs NEVER write to history.
 * - Session store isolation: Private tabs are strictly excluded from serialization and disk storage.
 * - Ephemeral private tab cleanup: Adapters and sessions disposed upon private tab closure.
 * - Granular Clear Browsing Data coordination.
 */
class Phase04DataAndPrivateTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private open class MockEngineAdapter(val id: String = "mock-adapter") : EngineAdapter {
        override val engineType: EngineType = EngineType.GECKO_VIEW
        val _nav = MutableStateFlow(EngineNavigationState())
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
            _nav.value = _nav.value.copy(url = url, isLoading = false)
        }
        override fun goBack(): Boolean = false
        override fun goForward(): Boolean = false
        override fun reload() {}
        override fun stop() {}
        override fun recoverRenderer() {}
        override fun setProtectionEnabled(enabled: Boolean) {}
        override fun simulateRendererLoss() {}
        override fun destroy() {
            isDestroyed = true
        }
    }

    @Test
    fun testHistoryRepository_AddVisitAndCountIncrement() {
        val historyFile = tempFolder.newFile("test_history.json")
        val repo = LocalHistoryRepository(historyFile)

        assertEquals(0, repo.getHistory().size)

        repo.addVisit("https://duckduckgo.com", "DuckDuckGo")
        val history1 = repo.getHistory()
        assertEquals(1, history1.size)
        assertEquals("https://duckduckgo.com", history1[0].url)
        assertEquals("DuckDuckGo", history1[0].title)
        assertEquals(1, history1[0].visitCount)

        // Visiting again should increment visit count
        repo.addVisit("https://duckduckgo.com", "DuckDuckGo — Privacy")
        val history2 = repo.getHistory()
        assertEquals(1, history2.size)
        assertEquals(2, history2[0].visitCount)
        assertEquals("DuckDuckGo — Privacy", history2[0].title)
    }

    @Test
    fun testHistoryRepository_SearchFilter() {
        val historyFile = tempFolder.newFile("test_history_search.json")
        val repo = LocalHistoryRepository(historyFile)

        repo.addVisit("https://en.wikipedia.org", "Wikipedia")
        repo.addVisit("https://mozilla.org", "Mozilla Foundation")
        repo.addVisit("https://musify.org/privacy", "Musify Privacy Guide")

        assertEquals(3, repo.getHistory("").size)
        assertEquals(1, repo.getHistory("wiki").size)
        assertEquals("Wikipedia", repo.getHistory("wiki")[0].title)
        assertEquals(1, repo.getHistory("mozilla").size)
        assertEquals(1, repo.getHistory("privacy").size)
        assertEquals(0, repo.getHistory("nonexistent").size)
    }

    @Test
    fun testHistoryRepository_DeleteSingleItem() {
        val historyFile = tempFolder.newFile("test_history_delete.json")
        val repo = LocalHistoryRepository(historyFile)

        repo.addVisit("https://page1.com", "Page 1")
        repo.addVisit("https://page2.com", "Page 2")

        val items = repo.getHistory()
        assertEquals(2, items.size)

        val idToDelete = items.find { it.url == "https://page1.com" }!!.id
        repo.deleteItem(idToDelete)

        val remaining = repo.getHistory()
        assertEquals(1, remaining.size)
        assertEquals("https://page2.com", remaining[0].url)
    }

    @Test
    fun testHistoryRepository_DeleteByTimeRange() {
        val historyFile = tempFolder.newFile("test_history_timerange.json")
        val repo = LocalHistoryRepository(historyFile)

        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600_000L
        val twoHoursAgo = now - 7200_000L
        val threeDaysAgo = now - (3 * 86400_000L)
        val tenDaysAgo = now - (10 * 86400_000L)

        // Seed with items across past ranges
        repo.seedHistoryForTesting(listOf(
            HistoryItem(url = "https://recent.com", title = "Recent", visitTime = now - 60_000L),
            HistoryItem(url = "https://2h-ago.com", title = "2h Ago", visitTime = twoHoursAgo),
            HistoryItem(url = "https://3d-ago.com", title = "3d Ago", visitTime = threeDaysAgo),
            HistoryItem(url = "https://10d-ago.com", title = "10d Ago", visitTime = tenDaysAgo)
        ))

        assertEquals(4, repo.getHistory().size)

        // Delete LAST_HOUR -> deletes recent only
        repo.deleteByTimeRange(TimeRange.LAST_HOUR)
        var remaining = repo.getHistory()
        assertEquals(3, remaining.size)
        assertFalse(remaining.any { it.url == "https://recent.com" })

        // Delete LAST_7_DAYS -> deletes 2h ago and 3d ago, preserves 10d ago
        repo.deleteByTimeRange(TimeRange.LAST_7_DAYS)
        remaining = repo.getHistory()
        assertEquals(1, remaining.size)
        assertEquals("https://10d-ago.com", remaining[0].url)

        // Delete ALL_TIME -> deletes all
        repo.deleteByTimeRange(TimeRange.ALL_TIME)
        assertEquals(0, repo.getHistory().size)
    }

    @Test
    fun testBookmarkRepository_AddDuplicatePreventionAndSearch() {
        val bookmarkFile = tempFolder.newFile("test_bookmarks.json")
        val repo = LocalBookmarkRepository(bookmarkFile)

        assertFalse(repo.isBookmarked("https://musify.org"))

        val added1 = repo.addBookmark("https://musify.org", "Musify Browser")
        assertNotNull(added1)
        assertTrue(repo.isBookmarked("https://musify.org"))
        assertEquals(1, repo.getBookmarks().size)

        // Adding exact same URL returns existing item without creating duplicate
        val addedDuplicate = repo.addBookmark("https://musify.org", "Musify — Updated Title")
        assertNotNull(addedDuplicate)
        assertEquals(added1!!.id, addedDuplicate!!.id)
        assertEquals(1, repo.getBookmarks().size)

        // Add second bookmark
        repo.addBookmark("https://eff.org", "Electronic Frontier Foundation")
        assertEquals(2, repo.getBookmarks().size)

        // Search
        assertEquals(1, repo.getBookmarks("eff").size)
        assertEquals("Electronic Frontier Foundation", repo.getBookmarks("eff")[0].title)

        // Delete by URL
        val removed = repo.removeBookmark("https://eff.org")
        assertTrue(removed)
        assertFalse(repo.isBookmarked("https://eff.org"))
        assertEquals(1, repo.getBookmarks().size)

        // Clear All
        repo.clearAll()
        assertEquals(0, repo.getBookmarks().size)
    }

    @Test
    fun testPrivateTabIsolation_HistoryNeverRecorded() {
        val historyFile = tempFolder.newFile("test_private_history.json")
        val historyRepo = LocalHistoryRepository(historyFile)

        val adapter = MockEngineAdapter("test-private-tab-adapter")
        val pool = TabEnginePool(factory = { _ -> adapter })
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

        val viewModel = TabsViewModel(
            pool = pool,
            store = null,
            historyRepository = historyRepo,
            bookmarkRepository = null,
            coroutineScope = scope
        )

        // Create Private Tab
        val created = viewModel.createNewTab(url = "https://sensitive-site.com", isPrivate = true, selectImmediately = true)
        assertTrue(created)
        assertTrue(viewModel.tabsState.value.selectedTab?.isPrivate == true)

        // Trigger navigation in private tab
        viewModel.navigateActiveTab("https://sensitive-site.com")

        // CRITICAL PRIVACY GUARANTEE: Private tab visits MUST NEVER be written to history
        val recordedHistory = historyRepo.getHistory()
        assertEquals(0, recordedHistory.size)
    }

    @Test
    fun testPrivateTabIsolation_SessionStoreExcludesPrivateTabs() {
        val sessionFile = tempFolder.newFile("test_session_leak_check.json")
        val store = FileTabSessionStore(sessionFile)

        val stateWithMixedTabs = TabsState(
            tabs = listOf(
                TabSession(id = "tab-normal-1", url = "https://public-site.com", title = "Public Site", isPrivate = false),
                TabSession(id = "tab-private-2", url = "https://secret-site.com", title = "Secret Site", isPrivate = true),
                TabSession(id = "tab-normal-3", url = "https://news.org", title = "News", isPrivate = false)
            ),
            selectedTabId = "tab-private-2"
        )

        // Save session through store
        store.save(stateWithMixedTabs)

        // Check raw JSON file content on disk: "secret-site.com" MUST NOT appear
        val rawJson = sessionFile.readText()
        assertFalse("Private URL leaked to disk!", rawJson.contains("secret-site.com"))
        assertFalse("Private tab ID leaked to disk!", rawJson.contains("tab-private-2"))
        assertTrue("Normal URL must be persisted", rawJson.contains("public-site.com"))

        // Deserialize through store: Private tab is completely absent
        val loaded = store.load()
        assertNotNull(loaded)
        assertEquals(2, loaded!!.tabs.size)
        assertTrue(loaded.tabs.none { it.isPrivate })
        assertEquals("tab-normal-1", loaded.tabs[0].id)
        assertEquals("tab-normal-3", loaded.tabs[1].id)
    }

    @Test
    fun testCloseAllPrivateTabs_DisposesPrivateAdapters() {
        val destroyedPrivateCount = AtomicInteger(0)

        val pool = TabEnginePool(factory = { isPrivate ->
            object : MockEngineAdapter("test-adapter") {
                override fun destroy() {
                    super.destroy()
                    if (isPrivate) {
                        destroyedPrivateCount.incrementAndGet()
                    }
                }
            }
        })

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val viewModel = TabsViewModel(
            pool = pool,
            store = null,
            historyRepository = null,
            bookmarkRepository = null,
            coroutineScope = scope
        )

        // Create 1 normal tab and 2 private tabs
        viewModel.createNewTab(url = "https://normal1.com", isPrivate = false)
        viewModel.createNewTab(url = "https://private1.com", isPrivate = true)
        viewModel.createNewTab(url = "https://private2.com", isPrivate = true)

        assertEquals(2, viewModel.tabsState.value.privateTabs.size)

        // Ensure adapters for private tabs are created in the pool
        val pTabs = viewModel.tabsState.value.privateTabs
        pTabs.forEach { tab ->
            pool.getOrCreateAdapter(tab.id, isPrivate = true)
        }

        // Close all private tabs
        viewModel.closeAllPrivateTabs()

        // Verify zero private tabs remain in state
        assertEquals(0, viewModel.tabsState.value.privateTabs.size)
        assertTrue(viewModel.tabsState.value.normalTabs.isNotEmpty())

        // Verify private adapters were destroyed
        assertEquals(2, destroyedPrivateCount.get())
    }

    @Test
    fun testClearBrowsingData_Coordination() {
        val historyFile = tempFolder.newFile("test_clear_data_hist.json")
        val bookmarkFile = tempFolder.newFile("test_clear_data_bm.json")
        val histRepo = LocalHistoryRepository(historyFile)
        val bmRepo = LocalBookmarkRepository(bookmarkFile)

        histRepo.addVisit("https://test.com", "Test")
        bmRepo.addBookmark("https://bookmark.com", "Bookmark")

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val viewModel = TabsViewModel(
            pool = TabEnginePool { MockEngineAdapter() },
            store = null,
            historyRepository = histRepo,
            bookmarkRepository = bmRepo,
            coroutineScope = scope
        )

        // Clear only history
        viewModel.clearBrowsingData(clearHistory = true, clearBookmarks = false, clearTabs = false)
        assertEquals(0, histRepo.getHistory().size)
        assertEquals(1, bmRepo.getBookmarks().size)

        // Clear bookmarks
        viewModel.clearBrowsingData(clearHistory = false, clearBookmarks = true, clearTabs = false)
        assertEquals(0, bmRepo.getBookmarks().size)
    }
}
