package com.musify.browser.tabs

import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.GeckoEngineAdapter
import com.musify.browser.engine.WebViewAdapter

/**
 * Factory abstraction for creating EngineAdapter instances with profile awareness.
 */
fun interface EngineAdapterFactory {
    fun create(isPrivate: Boolean): EngineAdapter
}

/**
 * TabEnginePool manages the lifecycle of web engine sessions mapped 1:1 to browser tabs.
 *
 * Guarantees:
 * - Profile isolation: Normal tabs run in persistent profiles; private tabs run in ephemeral private sessions.
 * - Lazy initialization: Engine adapters are created on-demand only when a tab becomes active.
 * - Resource cleanup: Calling [releaseAdapter] immediately invokes [EngineAdapter.destroy]
 *   to release GeckoSession/WebView and native resources.
 * - Caps memory footprint by evicting released adapters.
 */
class TabEnginePool(
    private val factory: EngineAdapterFactory = EngineAdapterFactory { isPrivate ->
        if (isGeckoAvailable()) {
            GeckoEngineAdapter(isPrivate)
        } else {
            WebViewAdapter(isPrivate)
        }
    }
) {
    constructor(simpleFactory: () -> EngineAdapter) : this(EngineAdapterFactory { _ -> simpleFactory() })

    companion object {
        @Volatile
        private var geckoAvailable: Boolean? = null

        fun isGeckoAvailable(): Boolean {
            if (geckoAvailable != null) return geckoAvailable!!
            geckoAvailable = try {
                System.loadLibrary("xul")
                true
            } catch (_: Throwable) {
                false
            }
            return geckoAvailable!!
        }

        fun setGeckoAvailableForTesting(available: Boolean?) {
            geckoAvailable = available
        }
    }

    private val adapters = mutableMapOf<String, EngineAdapter>()

    @Synchronized
    fun getOrCreateAdapter(tabId: String, isPrivate: Boolean = false): EngineAdapter {
        return adapters.getOrPut(tabId) {
            factory.create(isPrivate)
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
    fun releasePrivateAdapters(privateTabIds: Set<String>) {
        privateTabIds.forEach { releaseAdapter(it) }
    }

    @Synchronized
    fun activeCount(): Int = adapters.size

    @Synchronized
    fun isAdapterActive(tabId: String): Boolean = adapters.containsKey(tabId)
}
