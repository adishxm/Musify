package com.musify.browser.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.browser.browser.ParsedNavigation
import com.musify.browser.browser.UrlParser
import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.EngineShieldState
import com.musify.browser.shield.ShieldExceptionRepository
import com.musify.browser.storage.BookmarkRepository
import com.musify.browser.storage.HistoryRepository
import com.musify.browser.storage.TimeRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * State representing a prompt for explicit HTTP fallback confirmation.
 */
data class HttpFallbackPrompt(
    val targetUrl: String,
    val host: String
)

/**
 * TabsViewModel coordinates multi-tab lifecycle, ephemeral private sessions,
 * history & bookmark persistence, shield exceptions, and active tab navigation.
 */
class TabsViewModel(
    val pool: TabEnginePool = TabEnginePool(),
    val store: TabSessionStore? = null,
    val historyRepository: HistoryRepository? = null,
    val bookmarkRepository: BookmarkRepository? = null,
    val shieldExceptionRepository: ShieldExceptionRepository? = null,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: runCatching { viewModelScope }.getOrElse {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    private val _tabsState = MutableStateFlow(TabsState())
    val tabsState: StateFlow<TabsState> = _tabsState.asStateFlow()

    private val _activeShieldState = MutableStateFlow(EngineShieldState())
    val activeShieldState: StateFlow<EngineShieldState> = _activeShieldState.asStateFlow()

    private val _httpFallbackPrompt = MutableStateFlow<HttpFallbackPrompt?>(null)
    val httpFallbackPrompt: StateFlow<HttpFallbackPrompt?> = _httpFallbackPrompt.asStateFlow()

    private val allowedHttpOrigins = mutableSetOf("localhost", "127.0.0.1")

    private var activeObserverJob: Job? = null

    init {
        restoreOrInitialize()
    }

    private fun restoreOrInitialize() {
        val restoredState = store?.load()
        if (restoredState != null && restoredState.tabs.isNotEmpty()) {
            _tabsState.value = TabsReducer.reduce(
                _tabsState.value,
                TabAction.RestoreTabs(restoredState.tabs, restoredState.selectedTabId)
            )
        } else {
            // Initial default tab
            _tabsState.value = TabsReducer.reduce(
                _tabsState.value,
                TabAction.CreateTab(url = "about:blank", title = "New Tab", isPrivate = false, selectImmediately = true)
            )
        }

        bindActiveTabEngine()
        val activeTab = _tabsState.value.selectedTab
        if (activeTab != null && activeTab.url.isNotBlank() && activeTab.url != "about:blank") {
            getActiveEngineAdapter()?.loadUrl(activeTab.url)
        }
    }

    fun getActiveEngineAdapter(): EngineAdapter? {
        val selectedTab = _tabsState.value.selectedTab ?: return null
        return pool.getOrCreateAdapter(selectedTab.id, isPrivate = selectedTab.isPrivate)
    }

    fun selectTab(tabId: String) {
        val current = _tabsState.value.selectedTabId
        if (current == tabId) return

        _tabsState.value = TabsReducer.reduce(_tabsState.value, TabAction.SelectTab(tabId))
        bindActiveTabEngine()
        persistSession()
    }

    fun createNewTab(
        url: String? = null,
        isPrivate: Boolean = false,
        selectImmediately: Boolean = true
    ): Boolean {
        if (_tabsState.value.isAtMaxCapacity) {
            return false // Capped at max tabs
        }

        val previousSelected = _tabsState.value.selectedTabId
        _tabsState.value = TabsReducer.reduce(
            _tabsState.value,
            TabAction.CreateTab(
                url = url,
                isPrivate = isPrivate,
                selectImmediately = selectImmediately
            )
        )

        val newSelected = _tabsState.value.selectedTabId
        if (newSelected != previousSelected) {
            bindActiveTabEngine()
            if (!url.isNullOrBlank() && url != "about:blank") {
                getActiveEngineAdapter()?.loadUrl(url)
            }
        }

        persistSession()
        return true
    }

    fun closeTab(tabId: String) {
        val wasSelected = _tabsState.value.selectedTabId == tabId
        pool.releaseAdapter(tabId)

        _tabsState.value = TabsReducer.reduce(_tabsState.value, TabAction.CloseTab(tabId))

        if (wasSelected) {
            bindActiveTabEngine()
        }

        persistSession()
    }

    fun closeAllTabs() {
        pool.releaseAll()
        _tabsState.value = TabsReducer.reduce(_tabsState.value, TabAction.CloseAllTabs)
        bindActiveTabEngine()
        persistSession()
    }

    fun closeAllPrivateTabs() {
        val privateIds = _tabsState.value.privateTabs.map { it.id }.toSet()
        pool.releasePrivateAdapters(privateIds)
        _tabsState.value = TabsReducer.reduce(_tabsState.value, TabAction.CloseAllPrivateTabs)
        bindActiveTabEngine()
        persistSession()
    }

    fun navigateActiveTab(rawInput: String) {
        val activeTab = _tabsState.value.selectedTab ?: return
        val adapter = getActiveEngineAdapter() ?: return

        when (val result = UrlParser.parse(rawInput)) {
            is ParsedNavigation.AllowedUrl -> {
                // Conservative HTTPS-first check: if unencrypted HTTP, prompt explicit fallback
                if (!result.isHttps && !allowedHttpOrigins.contains(result.host.lowercase())) {
                    _httpFallbackPrompt.value = HttpFallbackPrompt(result.url, result.host)
                    return
                }
                adapter.loadUrl(result.url)
            }
            is ParsedNavigation.Search -> {
                adapter.loadUrl(result.searchUrl)
            }
            is ParsedNavigation.Rejected -> {
                // Keep engine on current page, record error state
                _tabsState.value = TabsReducer.reduce(
                    _tabsState.value,
                    TabAction.UpdateTabNavigation(
                        tabId = activeTab.id,
                        url = activeTab.url,
                        title = "Blocked Navigation: ${result.reason}",
                        canGoBack = activeTab.canGoBack,
                        canGoForward = activeTab.canGoForward,
                        isLoading = false,
                        progress = 0
                    )
                )
            }
        }

        persistSession()
    }

    fun confirmHttpFallback() {
        val prompt = _httpFallbackPrompt.value ?: return
        allowedHttpOrigins.add(prompt.host.lowercase())
        _httpFallbackPrompt.value = null
        getActiveEngineAdapter()?.loadUrl(prompt.targetUrl)
        persistSession()
    }

    fun dismissHttpFallback() {
        _httpFallbackPrompt.value = null
    }

    fun isShieldEnabledForCurrentTab(): Boolean {
        val tab = _tabsState.value.selectedTab ?: return true
        val origin = tab.origin
        if (origin.isBlank() || origin == "unknown" || origin == "musify://home") return true
        return shieldExceptionRepository?.isShieldDisabled(origin) != true
    }

    fun toggleShieldForCurrentOrigin(): Boolean {
        val tab = _tabsState.value.selectedTab ?: return true
        val origin = tab.origin
        if (origin.isBlank() || origin == "unknown" || origin == "musify://home") return true
        val repo = shieldExceptionRepository ?: return true
        val wasDisabled = repo.isShieldDisabled(origin)
        val nowDisabled = !wasDisabled
        repo.setShieldDisabled(origin, nowDisabled)
        getActiveEngineAdapter()?.setOriginException(nowDisabled)
        getActiveEngineAdapter()?.reload()
        return !nowDisabled
    }

    fun isCurrentTabBookmarked(): Boolean {
        val url = _tabsState.value.selectedTab?.url ?: return false
        return bookmarkRepository?.isBookmarked(url) == true
    }

    fun toggleBookmarkCurrentTab(): Boolean {
        val tab = _tabsState.value.selectedTab ?: return false
        if (tab.url.isBlank() || tab.url == "about:blank") return false
        val repo = bookmarkRepository ?: return false
        return if (repo.isBookmarked(tab.url)) {
            repo.removeBookmark(tab.url)
            false
        } else {
            repo.addBookmark(tab.url, tab.title)
            true
        }
    }

    fun clearBrowsingData(
        clearHistory: Boolean = true,
        clearBookmarks: Boolean = false,
        clearTabs: Boolean = false,
        timeRange: TimeRange = TimeRange.ALL_TIME
    ) {
        if (clearHistory) {
            historyRepository?.deleteByTimeRange(timeRange)
        }
        if (clearBookmarks) {
            bookmarkRepository?.clearAll()
        }
        if (clearTabs) {
            closeAllTabs()
        }
    }

    fun goBack(): Boolean {
        return getActiveEngineAdapter()?.goBack() ?: false
    }

    fun goForward(): Boolean {
        return getActiveEngineAdapter()?.goForward() ?: false
    }

    fun reload() {
        getActiveEngineAdapter()?.reload()
    }

    fun stop() {
        getActiveEngineAdapter()?.stop()
    }

    fun recoverRenderer() {
        val activeId = _tabsState.value.selectedTabId ?: return
        getActiveEngineAdapter()?.recoverRenderer()
        _tabsState.value = TabsReducer.reduce(
            _tabsState.value,
            TabAction.UpdateTabRendererLoss(activeId, hasCrashed = false)
        )
    }

    private fun bindActiveTabEngine() {
        activeObserverJob?.cancel()
        val activeTab = _tabsState.value.selectedTab ?: return
        val adapter = pool.getOrCreateAdapter(activeTab.id, isPrivate = activeTab.isPrivate)

        // Sync shield exception for this origin
        val isException = shieldExceptionRepository?.isShieldDisabled(activeTab.origin) == true
        adapter.setOriginException(isException)

        activeObserverJob = scope.launchInTabScope(adapter, activeTab.id, activeTab.isPrivate)
    }

    private fun CoroutineScope.launchInTabScope(
        adapter: EngineAdapter,
        activeId: String,
        isPrivate: Boolean
    ): Job {
        val parentJob = Job()
        val combinedScope = CoroutineScope(coroutineContext + parentJob)

        adapter.navigationState.onEach { nav ->
            val currentTab = _tabsState.value.tabs.find { it.id == activeId }
            val effectiveUrl = if (nav.url == "about:blank" && currentTab != null && currentTab.url != "about:blank") {
                currentTab.url
            } else {
                nav.url
            }

            val title = if (effectiveUrl.startsWith("http")) effectiveUrl else currentTab?.title

            _tabsState.value = TabsReducer.reduce(
                _tabsState.value,
                TabAction.UpdateTabNavigation(
                    tabId = activeId,
                    url = effectiveUrl,
                    title = title,
                    canGoBack = nav.canGoBack,
                    canGoForward = nav.canGoForward,
                    isLoading = nav.isLoading,
                    progress = nav.progress
                )
            )

            // Strict Privacy Rule: NEVER record private browsing history
            if (!isPrivate && !nav.isLoading && effectiveUrl.startsWith("http")) {
                historyRepository?.addVisit(effectiveUrl, title ?: effectiveUrl)
            }
        }.launchIn(combinedScope)

        adapter.securityState.onEach { sec ->
            val origin = sec.host ?: _tabsState.value.selectedTab?.origin ?: "unknown"
            _tabsState.value = TabsReducer.reduce(
                _tabsState.value,
                TabAction.UpdateTabSecurity(
                    tabId = activeId,
                    isSecure = sec.isSecure,
                    origin = origin
                )
            )

            // Update shield exception state when origin changes
            val isException = shieldExceptionRepository?.isShieldDisabled(origin) == true
            adapter.setOriginException(isException)
        }.launchIn(combinedScope)

        adapter.shieldState.onEach { shield ->
            _activeShieldState.value = shield
        }.launchIn(combinedScope)

        adapter.processState.onEach { proc ->
            if (proc.hasCrashed) {
                _tabsState.value = TabsReducer.reduce(
                    _tabsState.value,
                    TabAction.UpdateTabRendererLoss(
                        tabId = activeId,
                        hasCrashed = true
                    )
                )
            }
        }.launchIn(combinedScope)

        return parentJob
    }

    fun persistSession() {
        store?.save(_tabsState.value)
    }

    override fun onCleared() {
        super.onCleared()
        activeObserverJob?.cancel()
        persistSession()
    }
}
