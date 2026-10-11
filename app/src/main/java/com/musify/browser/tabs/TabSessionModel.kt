package com.musify.browser.tabs

import java.util.UUID

/**
 * TabSession represents the complete state of a single browser tab.
 */
data class TabSession(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "about:blank",
    val title: String = "New Tab",
    val origin: String = "about:blank",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = true,
    val isRendererCrashed: Boolean = false,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

/**
 * TabsState represents the immutable multi-tab container state.
 *
 * Hardened with maximum-tab capacity to prevent out-of-memory crashes.
 */
data class TabsState(
    val tabs: List<TabSession> = emptyList(),
    val selectedTabId: String? = null,
    val maxTabs: Int = 20
) {
    val selectedTab: TabSession?
        get() = tabs.find { it.id == selectedTabId } ?: tabs.firstOrNull()

    val tabCount: Int
        get() = tabs.size

    val isAtMaxCapacity: Boolean
        get() = tabs.size >= maxTabs

    val normalTabs: List<TabSession>
        get() = tabs.filter { !it.isPrivate }

    val privateTabs: List<TabSession>
        get() = tabs.filter { it.isPrivate }

    val hasPrivateTabs: Boolean
        get() = tabs.any { it.isPrivate }

    val isCurrentTabPrivate: Boolean
        get() = selectedTab?.isPrivate == true
}

/**
 * TabAction defines every deterministic state transition in the tabs lifecycle.
 */
sealed interface TabAction {
    data class CreateTab(
        val url: String? = null,
        val title: String? = null,
        val isPrivate: Boolean = false,
        val selectImmediately: Boolean = true
    ) : TabAction

    data class SelectTab(
        val tabId: String
    ) : TabAction

    data class CloseTab(
        val tabId: String
    ) : TabAction

    data object CloseAllTabs : TabAction

    data object CloseAllPrivateTabs : TabAction

    data class UpdateTabNavigation(
        val tabId: String,
        val url: String,
        val title: String? = null,
        val origin: String? = null,
        val canGoBack: Boolean,
        val canGoForward: Boolean,
        val isLoading: Boolean,
        val progress: Int
    ) : TabAction

    data class UpdateTabSecurity(
        val tabId: String,
        val isSecure: Boolean,
        val origin: String
    ) : TabAction

    data class UpdateTabRendererLoss(
        val tabId: String,
        val hasCrashed: Boolean
    ) : TabAction

    data class RestoreTabs(
        val tabs: List<TabSession>,
        val selectedId: String?
    ) : TabAction
}
