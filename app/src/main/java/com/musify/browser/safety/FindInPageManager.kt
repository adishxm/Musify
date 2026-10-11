package com.musify.browser.safety

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FindInPageState(
    val isActive: Boolean = false,
    val query: String = "",
    val activeMatchIndex: Int = 0,
    val totalMatches: Int = 0
) {
    val displayMatchText: String
        get() = if (totalMatches > 0) "${activeMatchIndex + 1} of $totalMatches" else if (query.isNotBlank()) "0 matches" else ""
}

class FindInPageManager {

    private val _state = MutableStateFlow(FindInPageState())
    val state: StateFlow<FindInPageState> = _state.asStateFlow()

    fun startFind() {
        _state.value = _state.value.copy(isActive = true)
    }

    fun updateQuery(query: String) {
        val trimmed = query.trim()
        val total = if (trimmed.length >= 2) 3 else 0 // Simulated matching count
        _state.value = _state.value.copy(
            query = query,
            totalMatches = total,
            activeMatchIndex = if (total > 0) 0 else 0
        )
    }

    fun nextMatch() {
        val current = _state.value
        if (current.totalMatches > 0) {
            val nextIndex = (current.activeMatchIndex + 1) % current.totalMatches
            _state.value = current.copy(activeMatchIndex = nextIndex)
        }
    }

    fun previousMatch() {
        val current = _state.value
        if (current.totalMatches > 0) {
            val prevIndex = if (current.activeMatchIndex - 1 < 0) current.totalMatches - 1 else current.activeMatchIndex - 1
            _state.value = current.copy(activeMatchIndex = prevIndex)
        }
    }

    fun close() {
        _state.value = FindInPageState(isActive = false, query = "", activeMatchIndex = 0, totalMatches = 0)
    }
}
