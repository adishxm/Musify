package com.musify.browser

import com.musify.browser.filterlist.FilterDecision
import com.musify.browser.filterlist.FilterListRegistry
import com.musify.browser.filterlist.FilterRuleCompiler
import com.musify.browser.shield.CompiledFilterIndex
import com.musify.browser.shield.LocalShieldExceptionRepository
import com.musify.browser.shield.UnifiedShieldAction
import com.musify.browser.shield.UnifiedShieldController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import kotlin.system.measureNanoTime

/**
 * Phase 32 Unit & Performance Benchmark Tests:
 * Validates Brave-aligned Zero-Copy Compiled Filter Engine (CompiledFilterIndex),
 * FastBloomFilter negative rejection, Domain Suffix Trie, Inverted Token Index,
 * memory ceiling (< 15 MB), and sub-millisecond per-URL evaluation (< 1.0 ms).
 */
class Phase32FilterPerformanceTest {

    private lateinit var filterIndex: CompiledFilterIndex
    private lateinit var controller: UnifiedShieldController
    private lateinit var exceptionRepo: LocalShieldExceptionRepository

    @Before
    fun setUp() {
        filterIndex = CompiledFilterIndex()
        exceptionRepo = LocalShieldExceptionRepository()
        controller = UnifiedShieldController(
            exceptionRepository = exceptionRepo,
            compiledFilterIndex = filterIndex
        )
    }

    @Test
    fun testFastBloomFilterIntegrity() {
        val bloom = CompiledFilterIndex.FastBloomFilter(bitCount = 262144) // 32 KB
        val inserted = mutableListOf<String>()

        for (i in 0 until 1000) {
            val token = "token_ad_provider_$i"
            bloom.add(token)
            inserted.add(token)
        }

        // 1. Zero false negatives guarantee
        for (token in inserted) {
            assertTrue("Bloom filter must contain all inserted elements with 0% false negative", bloom.contains(token))
        }

        // 2. Low false positive rate on non-inserted strings
        var falsePositives = 0
        val testCount = 1000
        for (i in 0 until testCount) {
            val randomToken = "clean_unrelated_content_item_$i"
            if (bloom.contains(randomToken)) {
                falsePositives++
            }
        }
        val falsePositiveRate = falsePositives.toDouble() / testCount
        assertTrue("False positive rate must be under 3% (was $falsePositiveRate)", falsePositiveRate < 0.03)
    }

    @Test
    fun testDomainTrieReversedLabelMatching() {
        val trie = CompiledFilterIndex.DomainTrie()

        trie.insert("doubleclick.net", isException = false, ruleText = "||doubleclick.net^", listId = "brave_default")
        trie.insert("analytics.google.com", isException = false, ruleText = "||analytics.google.com^", listId = "brave_default")
        trie.insert("adserver.com", isException = false, ruleText = "||adserver.com^", listId = "brave_default")
        trie.insert("sub.adserver.com", isException = true, ruleText = "@@||sub.adserver.com^", listId = "brave_default")

        // Exact match
        val match1 = trie.lookup("doubleclick.net")
        assertNotNull(match1)
        assertFalse(match1!!.isException)
        assertEquals("||doubleclick.net^", match1.ruleText)

        // Subdomain matching on parent rule
        val match2 = trie.lookup("ad.doubleclick.net")
        assertNotNull(match2)
        assertFalse(match2!!.isException)
        assertEquals("||doubleclick.net^", match2.ruleText)

        val match3 = trie.lookup("deep.nested.ad.doubleclick.net")
        assertNotNull(match3)
        assertFalse(match3!!.isException)

        // Domain exception overrides block
        val match4 = trie.lookup("sub.adserver.com")
        assertNotNull(match4)
        assertTrue("Subdomain exception must be flagged", match4!!.isException)

        // Clean domains return null
        assertNull(trie.lookup("google.com"))
        assertNull(trie.lookup("wikipedia.org"))
        assertNull(trie.lookup("open.spotify.com"))
    }

    @Test
    fun testTokenRuleIndexMatching() {
        filterIndex.addPatternRule("/adbanner/*", isException = false, ruleText = "/adbanner/*")
        filterIndex.addPatternRule("@@/adbanner/safe/*", isException = true, ruleText = "@@/adbanner/safe/*")
        filterIndex.addPatternRule("|http://evil-tracker.com/ad.js|", isException = false, ruleText = "|http://evil-tracker.com/ad.js|")
        filterIndex.addPatternRule("/analytics/tracking/*", isException = false, ruleText = "/analytics/tracking/*")

        // Block pattern
        val res1 = filterIndex.evaluate("https://example.com/adbanner/300x250.png")
        assertTrue("Pattern /adbanner/* must be blocked", res1 is FilterDecision.Blocked)

        // Exception pattern
        val res2 = filterIndex.evaluate("https://example.com/adbanner/safe/partner.png")
        assertTrue("Exception @@/adbanner/safe/* must supersede block", res2 is FilterDecision.Exception)

        // Exact URL match
        val res3 = filterIndex.evaluate("http://evil-tracker.com/ad.js")
        assertTrue("Exact URL must match", res3 is FilterDecision.Blocked)

        val res3NonMatch = filterIndex.evaluate("http://evil-tracker.com/ad.js?extra=param")
        assertFalse("Exact URL rule must not match modified URL", res3NonMatch is FilterDecision.Blocked)

        // Clean URL
        val res4 = filterIndex.evaluate("https://news.ycombinator.com/item?id=12345")
        assertTrue("Clean site must be allowed", res4 is FilterDecision.Allowed)
    }

    @Test
    fun testCompiledFilterListIntegration() = runBlocking {
        val rules = """
            ! Title: Integration Test List
            ||tracker-domain.com^
            ||sub.blocked-host.net^
            /sponsored-banner/*
            @@||safe-partner.com^
        """.trimIndent()

        val stream = ByteArrayInputStream(rules.toByteArray(Charsets.UTF_8))
        val compiledList = FilterRuleCompiler.compile(stream, FilterListRegistry.PETER_LOWE_SEED)

        // Verified through CompiledFilterList which uses CompiledFilterIndex
        val d1 = compiledList.evaluate("https://tracker-domain.com/collect")
        assertTrue(d1 is FilterDecision.Blocked)

        val d2 = compiledList.evaluate("https://sub.tracker-domain.com/collect")
        assertTrue(d2 is FilterDecision.Blocked)

        val d3 = compiledList.evaluate("https://portal.com/sponsored-banner/leaderboard.jpg")
        assertTrue(d3 is FilterDecision.Blocked)

        val d4 = compiledList.evaluate("https://safe-partner.com/article")
        assertTrue(d4 is FilterDecision.Exception)

        val d5 = compiledList.evaluate("https://musify.app/library")
        assertTrue(d5 is FilterDecision.Allowed)
    }

    @Test
    fun testSubMillisecondBatchPerformanceBenchmark() {
        // Pre-populate index with 500 representative rules
        val rules = mutableListOf<String>()
        for (i in 0 until 250) {
            rules.add("||adserver-network-$i.net^")
            rules.add("/ad-telemetry-$i/*")
        }
        filterIndex.bulkLoadRules(rules, "benchmark_list")
        assertEquals(500, filterIndex.getTotalRules())

        // Create 1,000 test URLs (500 ad/tracker URLs, 500 clean URLs)
        val testUrls = ArrayList<String>(1000)
        for (i in 0 until 500) {
            testUrls.add("https://sub.adserver-network-${i % 250}.net/serve?slot=$i")
            testUrls.add("https://clean-content-domain-$i.org/articles/guide-post-${i + 100}.html?ref=musify")
        }

        // Warm up JIT
        for (i in 0 until 100) {
            filterIndex.evaluate(testUrls[i])
        }

        // Benchmark 1,000 URL evaluations
        val totalNano = measureNanoTime {
            for (url in testUrls) {
                filterIndex.evaluate(url)
            }
        }

        val totalMs = totalNano / 1_000_000.0
        val avgMsPerUrl = totalMs / testUrls.size

        println("Phase 32 Benchmark: Evaluated ${testUrls.size} URLs in ${String.format("%.2f", totalMs)} ms (avg ${String.format("%.4f", avgMsPerUrl)} ms/URL)")

        // Verification Gate: Must execute well under 1.0 ms per URL
        assertTrue(
            "Evaluation time per URL must be < 1.0 ms (actual: $avgMsPerUrl ms)",
            avgMsPerUrl < 1.0
        )
    }

    @Test
    fun testHeapMemoryCeilingUnder15MB() {
        val largeIndex = CompiledFilterIndex()
        val rules = mutableListOf<String>()
        for (i in 0 until 5000) {
            rules.add("||ad-tracker-$i.com^")
            rules.add("/campaign-ad-$i/banner/*")
        }
        largeIndex.bulkLoadRules(rules, "large_list")

        val memoryBytes = largeIndex.estimatedMemoryBytes()
        val memoryMb = memoryBytes / (1024.0 * 1024.0)

        println("Phase 32 Memory: 10,000 rules estimated memory: ${String.format("%.2f", memoryMb)} MB")

        // Verification Gate: Memory overhead must remain strictly < 15 MB
        assertTrue(
            "Heap memory overhead must be < 15 MB (actual: $memoryMb MB)",
            memoryMb < 15.0
        )
    }

    @Test
    fun testUnifiedShieldControllerWithCompiledFilterIndex() {
        filterIndex.addDomainRule("evil-analytics.com", isException = false)
        filterIndex.addPatternRule("/tracking-pixel.gif", isException = false)

        val trackerUrl = "https://evil-analytics.com/event"
        val action1 = controller.evaluateResource(trackerUrl, "https://news.com")
        assertTrue("CompiledFilterIndex rule must block tracker in UnifiedShieldController", action1 is UnifiedShieldAction.Block)

        val pixelUrl = "https://example.com/assets/tracking-pixel.gif"
        val action2 = controller.evaluateResource(pixelUrl, "https://news.com")
        assertTrue("CompiledFilterIndex pattern must block pixel", action2 is UnifiedShieldAction.Block)

        // Invariant: Media streams and DRM licenses are NEVER blocked
        val spotifyAudio = "https://audio-fa.scdn.co/stream/12345"
        val actionAudio = controller.evaluateResource(spotifyAudio, "https://open.spotify.com")
        assertTrue("Protected media must be allowed", actionAudio is UnifiedShieldAction.Allow)

        val widevineDrm = "https://spclient.wg.spotify.com/widevine-license/v1/video/license"
        val actionDrm = controller.evaluateResource(widevineDrm, "https://open.spotify.com")
        assertTrue("Widevine DRM license must be allowed", actionDrm is UnifiedShieldAction.Allow)

        // User origin exception bypasses filter index
        exceptionRepo.setShieldDisabled("https://partner-origin.com", true)
        val actionExempt = controller.evaluateResource(trackerUrl, "https://partner-origin.com")
        assertTrue("Origin exemption must allow request", actionExempt is UnifiedShieldAction.Allow)
    }
}

