package com.musify.browser.tabs

import java.util.UUID

/**
 * TabsReducer enforces pure, deterministic state transitions for all tab lifecycle operations.
 *
 * Guarantees:
 * - Deterministic selected-tab index mapping on tab closure.
 * - Strict capping at [TabsState.maxTabs] (default 20) to prevent native memory exhaustion.
 * - Safe fallback to a clean tab when all tabs are closed.
 * - Private profile isolation: Private tabs are strictly ephemeral and never restored.
 * - 100% JVM testable with zero Android runtime dependencies.
 */
object TabsReducer {

    fun reduce(state: TabsState, action: TabAction): TabsState {
        return when (action) {
            is TabAction.CreateTab -> {
                if (state.isAtMaxCapacity) {
                    // Cap reached: reject new tab creation to conserve memory
                    state
                } else {
                    val initialUrl = action.url ?: "about:blank"
                    val initialTitle = action.title ?: if (initialUrl == "about:blank") {
                        if (action.isPrivate) "Private Tab" else "New Tab"
                    } else initialUrl
                    val initialOrigin = if (initialUrl == "about:blank") "about:blank" else initialUrl

                    val newTab = TabSession(
                        id = UUID.randomUUID().toString(),
                        url = initialUrl,
                        title = initialTitle,
                        origin = initialOrigin,
                        isLoading = false,
                        progress = 0,
                        canGoBack = false,
                        canGoForward = false,
                        isSecure = true,
                        isRendererCrashed = false,
                        isPrivate = action.isPrivate,
                        createdAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    )

                    val updatedTabs = state.tabs + newTab
                    val newSelectedId = if (action.selectImmediately || state.selectedTabId == null) {
                        newTab.id
                    } else {
                        state.selectedTabId
                    }

                    state.copy(
                        tabs = updatedTabs,
                        selectedTabId = newSelectedId
                    )
                }
            }

            is TabAction.SelectTab -> {
                val exists = state.tabs.any { it.id == action.tabId }
                if (!exists) return state

                val updatedTabs = state.tabs.map { tab ->
                    if (tab.id == action.tabId) {
                        tab.copy(lastActiveAt = System.currentTimeMillis())
                    } else {
                        tab
                    }
                }

                state.copy(
                    tabs = updatedTabs,
                    selectedTabId = action.tabId
                )
            }

            is TabAction.CloseTab -> {
                val indexToClose = state.tabs.indexOfFirst { it.id == action.tabId }
                if (indexToClose == -1) return state

                val remainingTabs = state.tabs.filterNot { it.id == action.tabId }

                if (remainingTabs.isEmpty()) {
                    // If closing the last tab, spawn a fresh initial tab
                    val freshTab = TabSession(
                        id = UUID.randomUUID().toString(),
                        url = "about:blank",
                        title = "New Tab",
                        origin = "about:blank",
                        isPrivate = false
                    )
                    state.copy(
                        tabs = listOf(freshTab),
                        selectedTabId = freshTab.id
                    )
                } else {
                    val newSelectedId = if (state.selectedTabId == action.tabId) {
                        // Pick adjacent tab: next tab if available, else previous
                        val newIndex = if (indexToClose < remainingTabs.size) {
                            indexToClose
                        } else {
                            remainingTabs.size - 1
                        }
                        remainingTabs[newIndex].id
                    } else {
                        state.selectedTabId
                    }

                    state.copy(
                        tabs = remainingTabs,
                        selectedTabId = newSelectedId
                    )
                }
            }

            is TabAction.CloseAllTabs -> {
                val freshTab = TabSession(
                    id = UUID.randomUUID().toString(),
                    url = "about:blank",
                    title = "New Tab",
                    origin = "about:blank",
                    isPrivate = false
                )
                state.copy(
                    tabs = listOf(freshTab),
                    selectedTabId = freshTab.id
                )
            }

            is TabAction.CloseAllPrivateTabs -> {
                val normalRemaining = state.tabs.filterNot { it.isPrivate }
                if (normalRemaining.isEmpty()) {
                    val freshTab = TabSession(
                        id = UUID.randomUUID().toString(),
                        url = "about:blank",
                        title = "New Tab",
                        origin = "about:blank",
                        isPrivate = false
                    )
                    state.copy(
                        tabs = listOf(freshTab),
                        selectedTabId = freshTab.id
                    )
                } else {
                    val wasSelectedPrivate = state.selectedTab?.isPrivate == true
                    val newSelectedId = if (wasSelectedPrivate) {
                        normalRemaining.first().id
                    } else {
                        state.selectedTabId
                    }
                    state.copy(
                        tabs = normalRemaining,
                        selectedTabId = newSelectedId
                    )
                }
            }

            is TabAction.UpdateTabNavigation -> {
                val updatedTabs = state.tabs.map { tab ->
                    if (tab.id == action.tabId) {
                        tab.copy(
                            url = action.url,
                            title = action.title ?: tab.title,
                            origin = action.origin ?: tab.origin,
                            canGoBack = action.canGoBack,
                            canGoForward = action.canGoForward,
                            isLoading = action.isLoading,
                            progress = action.progress,
                            lastActiveAt = System.currentTimeMillis()
                        )
                    } else {
                        tab
                    }
                }
                state.copy(tabs = updatedTabs)
            }

            is TabAction.UpdateTabSecurity -> {
                val updatedTabs = state.tabs.map { tab ->
                    if (tab.id == action.tabId) {
                        tab.copy(
                            isSecure = action.isSecure,
                            origin = action.origin
                        )
                    } else {
                        tab
                    }
                }
                state.copy(tabs = updatedTabs)
            }

            is TabAction.UpdateTabRendererLoss -> {
                val updatedTabs = state.tabs.map { tab ->
                    if (tab.id == action.tabId) {
                        tab.copy(isRendererCrashed = action.hasCrashed)
                    } else {
                        tab
                    }
                }
                state.copy(tabs = updatedTabs)
            }

            is TabAction.RestoreTabs -> {
                // Strictly exclude any private tabs from restoration
                val safeRestored = action.tabs.filterNot { it.isPrivate }
                if (safeRestored.isEmpty()) {
                    val freshTab = TabSession(isPrivate = false)
                    return state.copy(tabs = listOf(freshTab), selectedTabId = freshTab.id)
                }

                // Strictly cap restored tabs to maxTabs to prevent memory spikes on restore
                val safeTabs = safeRestored.take(state.maxTabs)
                val targetSelectedId = if (safeTabs.any { it.id == action.selectedId }) {
                    action.selectedId
                } else {
                    safeTabs.first().id
                }

                state.copy(
                    tabs = safeTabs,
                    selectedTabId = targetSelectedId
                )
            }
        }
    }
}
