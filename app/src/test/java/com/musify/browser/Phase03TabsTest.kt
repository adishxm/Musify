package com.musify.browser

import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.EngineNavigationState
import com.musify.browser.engine.EngineProcessState
import com.musify.browser.engine.EngineSecurityState
import com.musify.browser.engine.EngineShieldState
import com.musify.browser.engine.EngineType
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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 03 automated tests covering:
 * - Tab reducer state transitions and deterministic selection.
 * - Maximum tab capacity enforcement (capped at 20 tabs to protect native memory).
 * - Process recovery without duplicate tabs or navigation loops.
 * - Minimum non-sensitive metadata persistence (zero passwords, cookies, or secrets).
 * - Multi-tab engine pool lifecycle and memory cleanup on tab closure.
 * - Renderer process crash isolation.
 */
class Phase03TabsTest {

    private class MockEngineAdapter(val id: String) : EngineAdapter {
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

    @Test
    fun testTabCreationAndDeterministicSelection() {
        var state = TabsState()
        assertEquals(0, state.tabCount)

        // Create Tab 1
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://musify.org", title = "Musify"))
        assertEquals(1, state.tabCount)
        val tab1 = state.tabs.first()
        assertEquals(tab1.id, state.selectedTabId)
        assertEquals("https://musify.org", tab1.url)

        // Create Tab 2 without immediate selection
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://duckduckgo.com", selectImmediately = false))
        assertEquals(2, state.tabCount)
        assertEquals(tab1.id, state.selectedTabId) // Still tab 1

        // Explicitly select Tab 2
        val tab2 = state.tabs[1]
        state = TabsReducer.reduce(state, TabAction.SelectTab(tab2.id))
        assertEquals(tab2.id, state.selectedTabId)
    }

    @Test
    fun testMaximumTabCapacityCap() {
        var state = TabsState(maxTabs = 5)

        for (i in 1..5) {
            state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://site$i.org"))
        }
        assertEquals(5, state.tabCount)
        assertTrue(state.isAtMaxCapacity)

        // Attempting to exceed cap must be rejected
        val overCapState = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://site6.org"))
        assertEquals(5, overCapState.tabCount)
        assertFalse(overCapState.tabs.any { it.url == "https://site6.org" })
    }

    @Test
    fun testCloseSelectedTabTransitionsToAdjacentTab() {
        var state = TabsState()
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://site1.com"))
        val id1 = state.selectedTabId!!
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://site2.com"))
        val id2 = state.selectedTabId!!
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://site3.com"))
        val id3 = state.selectedTabId!!

        assertEquals(3, state.tabCount)
        assertEquals(id3, state.selectedTabId) // id3 is active at index 2

        // Close middle tab (id2) while id3 is active -> id3 remains active
        state = TabsReducer.reduce(state, TabAction.CloseTab(id2))
        assertEquals(2, state.tabCount)
        assertEquals(id3, state.selectedTabId)

        // Close currently active tab (id3 at end) -> adjacent previous (id1) becomes active
        state = TabsReducer.reduce(state, TabAction.CloseTab(id3))
        assertEquals(1, state.tabCount)
        assertEquals(id1, state.selectedTabId)

        // Close last remaining tab -> fresh clean tab generated
        state = TabsReducer.reduce(state, TabAction.CloseTab(id1))
        assertEquals(1, state.tabCount)
        assertEquals("about:blank", state.selectedTab?.url)
    }

    @Test
    fun testCloseAllTabsResetsToFreshTab() {
        var state = TabsState()
        for (i in 1..4) {
            state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://site$i.org"))
        }
        assertEquals(4, state.tabCount)

        state = TabsReducer.reduce(state, TabAction.CloseAllTabs)
        assertEquals(1, state.tabCount)
        assertEquals("about:blank", state.selectedTab?.url)
        assertNotNull(state.selectedTabId)
    }

    @Test
    fun testProcessRecoveryRestoresOnlyMinimalMetadata() {
        val original = TabsState(
            tabs = listOf(
                TabSession(id = "tab-1", url = "https://example.com/page", title = "Example"),
                TabSession(id = "tab-2", url = "https://duckduckgo.com", title = "DuckDuckGo")
            ),
            selectedTabId = "tab-2"
        )

        val json = TabSessionSerializer.serialize(original)
        assertNotNull(json)

        // Security check: Must NOT contain private data markers
        assertFalse(json.contains("password"))
        assertFalse(json.contains("cookie"))
        assertFalse(json.contains("token"))
        assertFalse(json.contains("authorization"))

        val restored = TabSessionSerializer.deserialize(json)
        assertNotNull(restored)
        assertEquals(2, restored!!.tabCount)
        assertEquals("tab-2", restored.selectedTabId)
        assertEquals("https://example.com/page", restored.tabs[0].url)
        assertEquals("Example", restored.tabs[0].title)
        assertEquals("https://duckduckgo.com", restored.tabs[1].url)
    }

    @Test
    fun testRendererCrashIsolationPerTab() {
        var state = TabsState()
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://stable.com"))
        val stableId = state.selectedTabId!!
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://crasher.com"))
        val crasherId = state.selectedTabId!!

        // Simulate renderer crash on crasher tab
        state = TabsReducer.reduce(state, TabAction.UpdateTabRendererLoss(crasherId, hasCrashed = true))

        val crasherTab = state.tabs.first { it.id == crasherId }
        val stableTab = state.tabs.first { it.id == stableId }

        assertTrue(crasherTab.isRendererCrashed)
        assertFalse(stableTab.isRendererCrashed) // Stable tab is untouched

        // Recover crashed tab
        state = TabsReducer.reduce(state, TabAction.UpdateTabRendererLoss(crasherId, hasCrashed = false))
        assertFalse(state.tabs.first { it.id == crasherId }.isRendererCrashed)
    }

    @Test
    fun testTabEnginePoolLazyAllocationAndDestruction() {
        val createdAdapters = mutableListOf<MockEngineAdapter>()
        val pool = TabEnginePool {
            val adapter = MockEngineAdapter("adapter-${createdAdapters.size}")
            createdAdapters.add(adapter)
            adapter
        }

        assertEquals(0, pool.activeCount())

        // Request adapter for tab-A
        val adapterA = pool.getOrCreateAdapter("tab-A") as MockEngineAdapter
        assertEquals(1, pool.activeCount())
        assertFalse(adapterA.isDestroyed)

        // Request again -> returns existing adapter without creating new one
        val adapterASame = pool.getOrCreateAdapter("tab-A") as MockEngineAdapter
        assertEquals(1, pool.activeCount())
        assertEquals(adapterA.id, adapterASame.id)

        // Request adapter for tab-B
        val adapterB = pool.getOrCreateAdapter("tab-B") as MockEngineAdapter
        assertEquals(2, pool.activeCount())

        // Release tab-A -> destroy() is called and active count decrements
        pool.releaseAdapter("tab-A")
        assertTrue(adapterA.isDestroyed)
        assertFalse(adapterB.isDestroyed)
        assertEquals(1, pool.activeCount())

        // Release all
        pool.releaseAll()
        assertTrue(adapterB.isDestroyed)
        assertEquals(0, pool.activeCount())
    }

    @Test
    fun testViewModelLifecycleAndSessionPersistence() = runBlocking {
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val tempFile = File.createTempFile("test_tabs_session", ".json")
        tempFile.deleteOnExit()
        val store = FileTabSessionStore(tempFile)

        val pool = TabEnginePool { MockEngineAdapter("vm-adapter") }
        val viewModel = TabsViewModel(pool = pool, store = store, coroutineScope = testScope)

        assertEquals(1, viewModel.tabsState.value.tabCount)

        // Create new tab and persist
        val created = viewModel.createNewTab("https://wikipedia.org", selectImmediately = true)
        assertTrue(created)
        assertEquals(2, viewModel.tabsState.value.tabCount)

        // Verify session was persisted to file
        val diskState = store.load()
        assertNotNull(diskState)
        assertEquals(2, diskState!!.tabCount)
        assertEquals("https://wikipedia.org", diskState.selectedTab?.url)

        // Simulate app restart / process recovery with new ViewModel instance
        val recoveredViewModel = TabsViewModel(pool = pool, store = store, coroutineScope = testScope)
        assertEquals(2, recoveredViewModel.tabsState.value.tabCount)
        assertEquals("https://wikipedia.org", recoveredViewModel.tabsState.value.selectedTab?.url)
    }
}
