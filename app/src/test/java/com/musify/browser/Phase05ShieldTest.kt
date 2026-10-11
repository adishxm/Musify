package com.musify.browser

import com.musify.browser.engine.EngineAdapter
import com.musify.browser.engine.EngineNavigationState
import com.musify.browser.engine.EngineProcessState
import com.musify.browser.engine.EngineSecurityState
import com.musify.browser.engine.EngineShieldState
import com.musify.browser.engine.EngineType
import com.musify.browser.shield.LocalShieldExceptionRepository
import com.musify.browser.shield.ShieldCategories
import com.musify.browser.shield.ShieldExceptionRepository
import com.musify.browser.tabs.TabAction
import com.musify.browser.tabs.TabEnginePool
import com.musify.browser.tabs.TabSession
import com.musify.browser.tabs.TabsReducer
import com.musify.browser.tabs.TabsState
import com.musify.browser.tabs.TabsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Phase 05 automated tests covering:
 * - Engine-native content blocking counter semantics and category breakdowns.
 * - Per-origin temporary/scoped shield exceptions without global toggle side-effects.
 * - Reversibility: Exceptions can be added and cleared without persistent lock-in.
 * - No hardcoded production domain exemptions.
 * - Conservative HTTPS-First behavior: HTTP URLs trigger explicit user confirmation prompts.
 * - Resetting page shield counters upon page navigation.
 */
class Phase05ShieldTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class MockShieldEngineAdapter(val id: String = "mock-shield-adapter") : EngineAdapter {
        override val engineType: EngineType = EngineType.GECKO_VIEW
        val _nav = MutableStateFlow(EngineNavigationState())
        override val navigationState: StateFlow<EngineNavigationState> = _nav

        val _sec = MutableStateFlow(EngineSecurityState(isSecure = true, host = "example.com"))
        override val securityState: StateFlow<EngineSecurityState> = _sec

        val _shield = MutableStateFlow(EngineShieldState())
        override val shieldState: StateFlow<EngineShieldState> = _shield

        val _proc = MutableStateFlow(EngineProcessState())
        override val processState: StateFlow<EngineProcessState> = _proc

        var loadedUrl: String? = null
        var reloadCount: Int = 0

        override fun createView(context: android.content.Context): android.view.View = android.view.View(context)
        override fun loadUrl(url: String) {
            loadedUrl = url
            _nav.value = _nav.value.copy(url = url, isLoading = false)
        }
        override fun goBack(): Boolean = false
        override fun goForward(): Boolean = false
        override fun reload() {
            reloadCount++
        }
        override fun stop() {}
        override fun recoverRenderer() {}

        override fun setProtectionEnabled(enabled: Boolean) {
            _shield.value = _shield.value.copy(isProtectionEnabled = enabled)
        }

        override fun setOriginException(isException: Boolean) {
            _shield.value = _shield.value.copy(isOriginException = isException)
        }

        override fun resetPageShieldCount() {
            _shield.value = _shield.value.copy(
                blockedCount = 0,
                lastBlockedUrl = null,
                lastBlockCategory = null,
                blockedAds = 0,
                blockedSocial = 0,
                blockedCryptominers = 0,
                blockedFingerprinters = 0
            )
        }

        override fun simulateRendererLoss() {}
        override fun destroy() {}

        // Helper to simulate an engine block event
        fun simulateBlockEvent(url: String, category: String) {
            if (_shield.value.isProtectionEnabled && !_shield.value.isOriginException) {
                val current = _shield.value
                val newCount = current.blockedCount + 1
                var ads = current.blockedAds
                var social = current.blockedSocial
                var crypto = current.blockedCryptominers
                var finger = current.blockedFingerprinters

                when (category) {
                    ShieldCategories.ADS_AND_TRACKERS -> ads++
                    ShieldCategories.SOCIAL_TRACKERS -> social++
                    ShieldCategories.CRYPTOMINING -> crypto++
                    ShieldCategories.FINGERPRINTING -> finger++
                    else -> ads++
                }

                _shield.value = current.copy(
                    blockedCount = newCount,
                    lastBlockedUrl = url,
                    lastBlockCategory = category,
                    blockedAds = ads,
                    blockedSocial = social,
                    blockedCryptominers = crypto,
                    blockedFingerprinters = finger
                )
            }
        }
    }

    @Test
    fun testShieldExceptionRepository_NoHardcodedRulesAndCrudIsolation() {
        val storageFile = tempFolder.newFile("test_shield_exceptions.txt")
        val repo = LocalShieldExceptionRepository(storageFile)

        // Strict Gate: Clean repository MUST have 0 pre-populated or hardcoded exceptions
        assertEquals(0, repo.getExceptions().size)
        assertFalse(repo.isShieldDisabled("https://google.com"))
        assertFalse(repo.isShieldDisabled("https://facebook.com"))
        assertFalse(repo.isShieldDisabled("https://wikipedia.org"))

        // Add exception for a broken origin
        repo.setShieldDisabled("https://broken-legacy-site.org", true)
        assertTrue(repo.isShieldDisabled("https://broken-legacy-site.org"))

        // Verify other origins remain strictly protected (isolation)
        assertFalse(repo.isShieldDisabled("https://another-site.com"))
        assertEquals(1, repo.getExceptions().size)

        // Reversibility: clear exception for origin
        repo.setShieldDisabled("https://broken-legacy-site.org", false)
        assertFalse(repo.isShieldDisabled("https://broken-legacy-site.org"))
        assertEquals(0, repo.getExceptions().size)
    }

    @Test
    fun testShieldExceptionRepository_PersistenceAcrossInstances() {
        val storageFile = tempFolder.newFile("test_shield_persist.txt")
        val repo1 = LocalShieldExceptionRepository(storageFile)

        repo1.setShieldDisabled("https://site-a.com", true)
        repo1.setShieldDisabled("https://site-b.org", true)
        assertEquals(2, repo1.getExceptions().size)

        // Create new instance pointing to same file
        val repo2 = LocalShieldExceptionRepository(storageFile)
        assertEquals(2, repo2.getExceptions().size)
        assertTrue(repo2.isShieldDisabled("https://site-a.com"))
        assertTrue(repo2.isShieldDisabled("https://site-b.org"))

        // Clear all exceptions
        repo2.clearAllExceptions()
        assertEquals(0, repo2.getExceptions().size)

        val repo3 = LocalShieldExceptionRepository(storageFile)
        assertEquals(0, repo3.getExceptions().size)
    }

    @Test
    fun testContentBlockingCounter_DefinedSemanticsAndBreakdown() {
        val adapter = MockShieldEngineAdapter()

        assertEquals(0, adapter.shieldState.value.blockedCount)
        assertEquals(0, adapter.shieldState.value.blockedAds)
        assertEquals(0, adapter.shieldState.value.blockedSocial)
        assertEquals(0, adapter.shieldState.value.blockedCryptominers)
        assertEquals(0, adapter.shieldState.value.blockedFingerprinters)

        // Simulate 2 Ad/Tracker block events
        adapter.simulateBlockEvent("https://adservice.google.com/ads.js", ShieldCategories.ADS_AND_TRACKERS)
        adapter.simulateBlockEvent("https://analytics.yahoo.com/tag.js", ShieldCategories.ADS_AND_TRACKERS)

        // Simulate 1 Social Tracker
        adapter.simulateBlockEvent("https://connect.facebook.net/sdk.js", ShieldCategories.SOCIAL_TRACKERS)

        // Simulate 1 Cryptominer
        adapter.simulateBlockEvent("https://coinhive.com/miner.js", ShieldCategories.CRYPTOMINING)

        // Simulate 1 Fingerprinter
        adapter.simulateBlockEvent("https://fingerprintjs.com/v3.js", ShieldCategories.FINGERPRINTING)

        val state = adapter.shieldState.value
        assertEquals(5, state.blockedCount)
        assertEquals(2, state.blockedAds)
        assertEquals(1, state.blockedSocial)
        assertEquals(1, state.blockedCryptominers)
        assertEquals(1, state.blockedFingerprinters)

        // Verify last blocked decision record
        assertEquals("https://fingerprintjs.com/v3.js", state.lastBlockedUrl)
        assertEquals(ShieldCategories.FINGERPRINTING, state.lastBlockCategory)
    }

    @Test
    fun testPerOriginException_DisablesBlockingForSpecificOrigin() {
        val adapter = MockShieldEngineAdapter()

        // With protections active, events increment counter
        adapter.simulateBlockEvent("https://tracker.com/t.js", ShieldCategories.ADS_AND_TRACKERS)
        assertEquals(1, adapter.shieldState.value.blockedCount)

        // Now activate origin exception (user temporarily paused shields on this site)
        adapter.setOriginException(true)
        assertTrue(adapter.shieldState.value.isOriginException)

        // Subsequent tracking events must be ignored and not increment the blocked counter
        adapter.simulateBlockEvent("https://another-tracker.com/a.js", ShieldCategories.ADS_AND_TRACKERS)
        assertEquals(1, adapter.shieldState.value.blockedCount)

        // Re-enable shields (reversibility)
        adapter.setOriginException(false)
        assertFalse(adapter.shieldState.value.isOriginException)

        // Subsequent tracking events are blocked again
        adapter.simulateBlockEvent("https://third-tracker.com/b.js", ShieldCategories.ADS_AND_TRACKERS)
        assertEquals(2, adapter.shieldState.value.blockedCount)
    }

    @Test
    fun testResetPageShieldCount_OnNavigation() {
        val adapter = MockShieldEngineAdapter()
        adapter.simulateBlockEvent("https://tracker1.com/t.js", ShieldCategories.ADS_AND_TRACKERS)
        adapter.simulateBlockEvent("https://tracker2.com/t.js", ShieldCategories.SOCIAL_TRACKERS)
        assertEquals(2, adapter.shieldState.value.blockedCount)

        // Simulating new page load resets page-specific counters
        adapter.resetPageShieldCount()

        val resetState = adapter.shieldState.value
        assertEquals(0, resetState.blockedCount)
        assertEquals(0, resetState.blockedAds)
        assertEquals(0, resetState.blockedSocial)
        assertNull(resetState.lastBlockedUrl)
        assertNull(resetState.lastBlockCategory)
    }

    @Test
    fun testConservativeHttpsFirst_PromptOnHttpAndAllowConfirmation() {
        val adapter = MockShieldEngineAdapter()
        val pool = TabEnginePool { adapter }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

        val viewModel = TabsViewModel(
            pool = pool,
            store = null,
            historyRepository = null,
            bookmarkRepository = null,
            shieldExceptionRepository = null,
            coroutineScope = scope
        )

        // User attempts navigation to unencrypted HTTP site
        viewModel.navigateActiveTab("http://insecure-plain-text.org/login")

        // Security check: Prompt MUST be generated and engine load delayed
        val prompt = viewModel.httpFallbackPrompt.value
        assertNotNull("HTTP fallback prompt must be presented to user", prompt)
        assertEquals("http://insecure-plain-text.org/login", prompt!!.targetUrl)
        assertEquals("insecure-plain-text.org", prompt.host)
        assertNull("Unencrypted URL must not be loaded before confirmation", adapter.loadedUrl)

        // User dismisses prompt -> cancelled
        viewModel.dismissHttpFallback()
        assertNull(viewModel.httpFallbackPrompt.value)
        assertNull(adapter.loadedUrl)

        // User navigates again and confirms
        viewModel.navigateActiveTab("http://insecure-plain-text.org/login")
        viewModel.confirmHttpFallback()

        assertNull(viewModel.httpFallbackPrompt.value)
        assertEquals("http://insecure-plain-text.org/login", adapter.loadedUrl)

        // Subsequent navigation to same host proceeds directly as user explicitly whitelisted it
        adapter.loadedUrl = null
        viewModel.navigateActiveTab("http://insecure-plain-text.org/dashboard")
        assertNull(viewModel.httpFallbackPrompt.value)
        assertEquals("http://insecure-plain-text.org/dashboard", adapter.loadedUrl)
    }

    @Test
    fun testConservativeHttpsFirst_LocalhostAllowedDirectly() {
        val adapter = MockShieldEngineAdapter()
        val pool = TabEnginePool { adapter }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

        val viewModel = TabsViewModel(
            pool = pool,
            store = null,
            historyRepository = null,
            bookmarkRepository = null,
            shieldExceptionRepository = null,
            coroutineScope = scope
        )

        // Developer navigation to localhost
        viewModel.navigateActiveTab("http://localhost:3000")
        assertNull(viewModel.httpFallbackPrompt.value)
        assertEquals("http://localhost:3000", adapter.loadedUrl)
    }

    @Test
    fun testToggleShieldForCurrentOrigin_CoordinatesViewModelAndAdapter() {
        val adapter = MockShieldEngineAdapter()
        val pool = TabEnginePool { adapter }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val shieldRepo = LocalShieldExceptionRepository()

        val viewModel = TabsViewModel(
            pool = pool,
            store = null,
            historyRepository = null,
            bookmarkRepository = null,
            shieldExceptionRepository = shieldRepo,
            coroutineScope = scope
        )

        // Navigate tab to an origin
        viewModel.navigateActiveTab("https://news.ycombinator.com")
        adapter._sec.value = EngineSecurityState(isSecure = true, host = "https://news.ycombinator.com")

        assertTrue(viewModel.isShieldEnabledForCurrentTab())
        assertFalse(adapter.shieldState.value.isOriginException)

        // Toggle shields off for this origin
        val reloadedBefore = adapter.reloadCount
        val newState = viewModel.toggleShieldForCurrentOrigin()

        assertFalse("Shields should now be disabled for this origin", newState)
        assertTrue(shieldRepo.isShieldDisabled("https://news.ycombinator.com"))
        assertTrue(adapter.shieldState.value.isOriginException)
        assertEquals(reloadedBefore + 1, adapter.reloadCount)

        // Toggle shields back on (reversible)
        val enabledAgain = viewModel.toggleShieldForCurrentOrigin()
        assertTrue("Shields should now be re-enabled", enabledAgain)
        assertFalse(shieldRepo.isShieldDisabled("https://news.ycombinator.com"))
        assertFalse(adapter.shieldState.value.isOriginException)
        assertEquals(reloadedBefore + 2, adapter.reloadCount)
    }
}
