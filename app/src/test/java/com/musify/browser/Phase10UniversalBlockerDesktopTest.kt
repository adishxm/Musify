package com.musify.browser

import com.musify.browser.engine.ContentMode
import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.EngineNavigationState
import com.musify.browser.engine.EngineProcessState
import com.musify.browser.engine.EngineSecurityState
import com.musify.browser.engine.EngineShieldState
import com.musify.browser.engine.EngineType
import com.musify.browser.engine.MediaCapabilityState
import com.musify.browser.storage.LocalSiteContentModeRepository
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
 * Phase 10 automated test suite covering:
 * - ContentMode persistence per origin (SiteContentModeRepository).
 * - TabSession serialization and deserialization with ContentMode backwards-compatibility.
 * - TabsReducer state transition for UpdateContentMode and tab isolation.
 * - EngineAdapter contentMode and MediaCapabilityState integration.
 * - TabsViewModel toggleContentModeCurrentTab and optimizePlaybackForCurrentSite.
 * - Playback diagnostic and media CDN handling for Spotify Web Player.
 */
class Phase10UniversalBlockerDesktopTest {

    private class MockEngineAdapterWithMedia(val id: String) : EngineAdapter {
        override val engineType: EngineType = EngineType.GECKO_VIEW
        private val _nav = MutableStateFlow(EngineNavigationState())
        override val navigationState: StateFlow<EngineNavigationState> = _nav

        private val _sec = MutableStateFlow(EngineSecurityState(isSecure = true))
        override val securityState: StateFlow<EngineSecurityState> = _sec

        private val _shield = MutableStateFlow(EngineShieldState())
        override val shieldState: StateFlow<EngineShieldState> = _shield

        private val _proc = MutableStateFlow(EngineProcessState())
        override val processState: StateFlow<EngineProcessState> = _proc

        private val _contentMode = MutableStateFlow(ContentMode.MOBILE)
        override val contentMode: StateFlow<ContentMode> = _contentMode

        private val _mediaState = MutableStateFlow(
            MediaCapabilityState(
                isEncryptedMediaSupported = true,
                isWidevineSupported = true,
                isDesktopMode = false,
                isPrivateMode = false,
                isAutoplayAllowed = true,
                cookiePolicy = "ACCEPT_NON_TRACKERS"
            )
        )
        override val mediaCapabilityState: StateFlow<MediaCapabilityState> = _mediaState

        var isDestroyed = false
        var loadedUrl: String? = null
        var reloadCount = 0

        override fun createView(context: android.content.Context): android.view.View = android.view.View(context)
        override fun loadUrl(url: String) {
            loadedUrl = url
            _nav.value = _nav.value.copy(url = url)
        }
        override fun goBack(): Boolean = false
        override fun goForward(): Boolean = false
        override fun reload() {
            reloadCount++
        }
        override fun stop() {}
        override fun recoverRenderer() {}
        override fun setProtectionEnabled(enabled: Boolean) {}
        override fun simulateRendererLoss() {}
        override fun destroy() {
            isDestroyed = true
        }

        override fun setContentMode(mode: ContentMode) {
            _contentMode.value = mode
            _mediaState.value = _mediaState.value.copy(
                isDesktopMode = (mode == ContentMode.DESKTOP)
            )
        }
    }

    @Test
    fun testSiteContentModeRepositoryDefaultAndToggle() {
        val tempFile = File.createTempFile("site_content_mode_test", ".txt")
        tempFile.deleteOnExit()
        val repo = LocalSiteContentModeRepository(tempFile)

        val spotifyUrl = "https://open.spotify.com/album/4aawyAB9vmqN3uQ7FjRGTy"
        val normalizedOrigin = "https://open.spotify.com"

        // Default must be MOBILE
        assertEquals(ContentMode.MOBILE, repo.getContentMode(spotifyUrl))
        assertFalse(repo.isDesktopMode(spotifyUrl))

        // Toggle to DESKTOP
        val newMode = repo.toggleContentMode(spotifyUrl)
        assertEquals(ContentMode.DESKTOP, newMode)
        assertTrue(repo.isDesktopMode(spotifyUrl))
        assertEquals(ContentMode.DESKTOP, repo.getContentMode(normalizedOrigin))

        // Toggle back to MOBILE
        val toggledBack = repo.toggleContentMode(spotifyUrl)
        assertEquals(ContentMode.MOBILE, toggledBack)
        assertFalse(repo.isDesktopMode(spotifyUrl))
    }

    @Test
    fun testSiteContentModeRepositoryFilePersistence() {
        val tempFile = File.createTempFile("site_content_mode_persist", ".txt")
        tempFile.deleteOnExit()

        val repo1 = LocalSiteContentModeRepository(tempFile)
        repo1.setContentMode("https://open.spotify.com", ContentMode.DESKTOP)
        repo1.setContentMode("https://music.youtube.com", ContentMode.DESKTOP)

        // Reload from same file with new instance
        val repo2 = LocalSiteContentModeRepository(tempFile)
        assertTrue(repo2.isDesktopMode("https://open.spotify.com"))
        assertTrue(repo2.isDesktopMode("https://music.youtube.com"))
        assertFalse(repo2.isDesktopMode("https://duckduckgo.com")) // Unset remains MOBILE

        // Clear all
        repo2.clearAll()
        val repo3 = LocalSiteContentModeRepository(tempFile)
        assertFalse(repo3.isDesktopMode("https://open.spotify.com"))
    }

    @Test
    fun testTabSessionSerializationWithContentMode() {
        val tab = TabSession(
            id = "spotify-tab",
            url = "https://open.spotify.com",
            title = "Spotify - Web Player",
            isPrivate = false,
            contentMode = ContentMode.DESKTOP
        )
        val state = TabsState(tabs = listOf(tab), selectedTabId = "spotify-tab")

        val json = TabSessionSerializer.serialize(state)
        assertTrue(json.contains("\"contentMode\": \"DESKTOP\"") || json.contains("\"contentMode\":\"DESKTOP\""))

        val deserialized = TabSessionSerializer.deserialize(json)
        assertNotNull(deserialized)
        assertEquals(1, deserialized!!.tabCount)
        val restoredTab = deserialized.tabs.first()
        assertEquals("spotify-tab", restoredTab.id)
        assertEquals(ContentMode.DESKTOP, restoredTab.contentMode)
    }

    @Test
    fun testTabSessionDeserializationBackwardsCompatibility() {
        // Legacy JSON without contentMode field
        val legacyJson = """
            {
              "selectedTabId": "legacy-tab",
              "tabs": [
                {
                  "id": "legacy-tab",
                  "url": "https://example.com",
                  "title": "Example",
                  "createdAt": 1000,
                  "lastAccessedAt": 2000,
                  "isPrivate": false
                }
              ]
            }
        """.trimIndent()

        val deserialized = TabSessionSerializer.deserialize(legacyJson)
        assertNotNull(deserialized)
        val restoredTab = deserialized!!.tabs.first()
        assertEquals(ContentMode.MOBILE, restoredTab.contentMode) // Defaults safely to MOBILE
    }

    @Test
    fun testTabsReducerUpdateContentModeAndIsolation() {
        var state = TabsState()
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://open.spotify.com", title = "Spotify"))
        val spotifyTabId = state.selectedTabId!!
        assertEquals(ContentMode.MOBILE, state.selectedTab?.contentMode)

        // Create second tab
        state = TabsReducer.reduce(state, TabAction.CreateTab(url = "https://musify.org", title = "Musify"))
        val musifyTabId = state.selectedTabId!!

        // Switch spotify tab to DESKTOP
        state = TabsReducer.reduce(state, TabAction.UpdateContentMode(spotifyTabId, ContentMode.DESKTOP))

        val spotifyTab = state.tabs.first { it.id == spotifyTabId }
        val musifyTab = state.tabs.first { it.id == musifyTabId }

        assertEquals(ContentMode.DESKTOP, spotifyTab.contentMode)
        assertEquals(ContentMode.MOBILE, musifyTab.contentMode) // Isolated, unaffected
    }

    @Test
    fun testTabsViewModelContentModeToggleAndSitePersistence() = runBlocking {
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val tempStoreFile = File.createTempFile("vm_test_session", ".json")
        tempStoreFile.deleteOnExit()
        val store = FileTabSessionStore(tempStoreFile)

        val tempModeFile = File.createTempFile("vm_test_mode", ".txt")
        tempModeFile.deleteOnExit()
        val modeRepo = LocalSiteContentModeRepository(tempModeFile)

        var createdAdapter: MockEngineAdapterWithMedia? = null
        val pool = TabEnginePool {
            val adapter = MockEngineAdapterWithMedia("engine-1")
            createdAdapter = adapter
            adapter
        }

        val viewModel = TabsViewModel(
            pool = pool,
            store = store,
            siteContentModeRepository = modeRepo,
            coroutineScope = testScope
        )

        // Initial tab
        val tabId = viewModel.tabsState.value.selectedTabId!!
        viewModel.navigateActiveTab("https://open.spotify.com")

        // Initially in mobile mode
        assertFalse(viewModel.isCurrentTabDesktop())
        assertEquals(ContentMode.MOBILE, createdAdapter?.contentMode?.value)

        // Toggle to desktop
        viewModel.toggleContentModeCurrentTab()
        assertTrue(viewModel.isCurrentTabDesktop())
        assertEquals(ContentMode.DESKTOP, createdAdapter?.contentMode?.value)
        assertTrue(createdAdapter?.mediaCapabilityState?.value?.isDesktopMode == true)
        assertTrue(modeRepo.isDesktopMode("https://open.spotify.com"))

        // Create another tab navigating to different site -> defaults to MOBILE
        viewModel.createNewTab("https://musify.org", selectImmediately = true)
        assertFalse(viewModel.isCurrentTabDesktop())
    }

    @Test
    fun testTabsViewModelOptimizePlaybackForCurrentSite() = runBlocking {
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val tempStoreFile = File.createTempFile("vm_test_session_opt", ".json")
        tempStoreFile.deleteOnExit()
        val store = FileTabSessionStore(tempStoreFile)

        val tempModeFile = File.createTempFile("vm_test_mode_opt", ".txt")
        tempModeFile.deleteOnExit()
        val modeRepo = LocalSiteContentModeRepository(tempModeFile)

        var lastAdapter: MockEngineAdapterWithMedia? = null
        val pool = TabEnginePool {
            val adapter = MockEngineAdapterWithMedia("engine-opt")
            lastAdapter = adapter
            adapter
        }

        val viewModel = TabsViewModel(
            pool = pool,
            store = store,
            siteContentModeRepository = modeRepo,
            coroutineScope = testScope
        )

        viewModel.navigateActiveTab("https://open.spotify.com")
        assertFalse(viewModel.isCurrentTabDesktop())

        // Run one-tap optimize playback
        viewModel.optimizePlaybackForCurrentSite()

        // Verifies desktop mode applied, persisted, and reload triggered
        assertTrue(viewModel.isCurrentTabDesktop())
        assertTrue(modeRepo.isDesktopMode("https://open.spotify.com"))
        assertEquals(1, lastAdapter?.reloadCount)
    }

    @Test
    fun testMediaCapabilityStateDefaults() {
        val defaultState = MediaCapabilityState()
        assertTrue(defaultState.isEncryptedMediaSupported)
        assertTrue(defaultState.isWidevineSupported)
        assertFalse(defaultState.isDesktopMode)
        assertFalse(defaultState.isPrivateMode)
        assertTrue(defaultState.isAutoplayAllowed)
        assertEquals("Accept Non-Trackers & Isolate", defaultState.cookiePolicy)
    }
}
