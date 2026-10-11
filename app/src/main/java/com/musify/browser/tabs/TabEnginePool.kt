package com.musify.browser.tabs

import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.GeckoEngineAdapter

/**
 * Factory abstraction for creating EngineAdapter instances.
 */
fun interface EngineAdapterFactory {
    fun create(): EngineAdapter
}

/**
 * TabEnginePool manages the lifecycle of web engine sessions mapped 1:1 to browser tabs.
 *
 * Guarantees:
 * - Lazy initialization: Engine adapters are created on-demand only when a tab becomes active.
 * - Resource cleanup: Calling [releaseAdapter] immediately invokes [EngineAdapter.destroy]
 *   to release GeckoSession/WebView and native resources.
 * - Caps memory footprint by evicting released adapters.
 */
class TabEnginePool(
    private val factory: EngineAdapterFactory = EngineAdapterFactory { GeckoEngineAdapter() }
) {
    private val adapters = mutableMapOf<String, EngineAdapter>()

    @Synchronized
    fun getOrCreateAdapter(tabId: String): EngineAdapter {
        return adapters.getOrPut(tabId) {
            factory.create()
        }
    }

    @Synchronized
    fun getAdapter(tabId: String): EngineAdapter? {
        return adapters[tabId]
    }

    @Synchronized
    fun releaseAdapter(tabId: String): EngineAdapter? {
        val adapter = adapters.remove(tabId)
        adapter?.destroy()
        return adapter
    }

    @Synchronized
    fun releaseAll() {
        adapters.values.forEach { it.destroy() }
        adapters.clear()
    }

    @Synchronized
    fun activeCount(): Int = adapters.size

    @Synchronized
    fun isAdapterActive(tabId: String): Boolean = adapters.containsKey(tabId)
}
