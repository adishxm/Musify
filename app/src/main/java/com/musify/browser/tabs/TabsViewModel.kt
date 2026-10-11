package com.musify.browser.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.browser.browser.ParsedNavigation
import com.musify.browser.browser.UrlParser
import com.musify.browser.engine.EngineAdapter
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
 * TabsViewModel manages multi-tab state, tab-to-engine session mappings,
 * session persistence for process recovery, and active tab navigation.
 */
class TabsViewModel(
    val pool: TabEnginePool = TabEnginePool(),
    val store: TabSessionStore? = null,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: runCatching { viewModelScope }.getOrElse {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    private val _tabsState = MutableStateFlow(TabsState())
    val tabsState: StateFlow<TabsState> = _tabsState.asStateFlow()

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
                TabAction.CreateTab(url = "about:blank", title = "New Tab", selectImmediately = true)
            )
        }

        bindActiveTabEngine()
        val activeTab = _tabsState.value.selectedTab
        if (activeTab != null && activeTab.url.isNotBlank() && activeTab.url != "about:blank") {
            getActiveEngineAdapter()?.loadUrl(activeTab.url)
        }
    }

    fun getActiveEngineAdapter(): EngineAdapter? {
        val selectedId = _tabsState.value.selectedTabId ?: return null
        return pool.getOrCreateAdapter(selectedId)
    }

    fun selectTab(tabId: String) {
        val current = _tabsState.value.selectedTabId
        if (current == tabId) return

        _tabsState.value = TabsReducer.reduce(_tabsState.value, TabAction.SelectTab(tabId))
        bindActiveTabEngine()
        persistSession()
    }

    fun createNewTab(url: String? = null, selectImmediately: Boolean = true): Boolean {
        if (_tabsState.value.isAtMaxCapacity) {
            return false // Capped at max tabs
        }

        val previousSelected = _tabsState.value.selectedTabId
        _tabsState.value = TabsReducer.reduce(
            _tabsState.value,
            TabAction.CreateTab(url = url, selectImmediately = selectImmediately)
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

    fun navigateActiveTab(rawInput: String) {
        val activeTab = _tabsState.value.selectedTab ?: return
        val adapter = getActiveEngineAdapter() ?: return

        when (val result = UrlParser.parse(rawInput)) {
            is ParsedNavigation.AllowedUrl -> {
                adapter.loadUrl(result.url)
            }
            is ParsedNavigation.Search -> {
                adapter.loadUrl(result.searchUrl)
            }
            is ParsedNavigation.Rejected -> {
                // Keep engine on current page, notify error
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
        val activeId = _tabsState.value.selectedTabId ?: return
        val adapter = pool.getOrCreateAdapter(activeId)

        activeObserverJob = scope.launchInTabScope(adapter, activeId)
    }

    private fun CoroutineScope.launchInTabScope(adapter: EngineAdapter, activeId: String): Job {
        val parentJob = Job()
        val combinedScope = CoroutineScope(coroutineContext + parentJob)

        adapter.navigationState.onEach { nav ->
            val currentTab = _tabsState.value.tabs.find { it.id == activeId }
            val effectiveUrl = if (nav.url == "about:blank" && currentTab != null && currentTab.url != "about:blank") {
                currentTab.url
            } else {
                nav.url
            }
            _tabsState.value = TabsReducer.reduce(
                _tabsState.value,
                TabAction.UpdateTabNavigation(
                    tabId = activeId,
                    url = effectiveUrl,
                    title = if (effectiveUrl.startsWith("http")) effectiveUrl else currentTab?.title,
                    canGoBack = nav.canGoBack,
                    canGoForward = nav.canGoForward,
                    isLoading = nav.isLoading,
                    progress = nav.progress
                )
            )
        }.launchIn(combinedScope)

        adapter.securityState.onEach { sec ->
            _tabsState.value = TabsReducer.reduce(
                _tabsState.value,
                TabAction.UpdateTabSecurity(
                    tabId = activeId,
                    isSecure = sec.isSecure,
                    origin = sec.host ?: _tabsState.value.selectedTab?.origin ?: "unknown"
                )
            )
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
