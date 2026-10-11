package com.musify.browser

import com.musify.browser.filterlist.EngineBridgeStatus
import com.musify.browser.filterlist.FetchResponse
import com.musify.browser.filterlist.FilterDecision
import com.musify.browser.filterlist.FilterEngine
import com.musify.browser.filterlist.FilterListMetadata
import com.musify.browser.filterlist.FilterListRegistry
import com.musify.browser.filterlist.FilterListStorage
import com.musify.browser.filterlist.FilterListUpdater
import com.musify.browser.filterlist.FilterRuleCompiler
import com.musify.browser.filterlist.HttpFetcher
import com.musify.browser.filterlist.UpdateResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

/**
 * Phase06FilterListTest verifies filter list parsing, licensing compliance,
 * rule bounds, staged atomic updates, last-known-good rollback, and evaluation performance.
 */
class Phase06FilterListTest {

    private lateinit var tempDir: File
    private lateinit var storage: FilterListStorage

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "musify_filter_test_${System.nanoTime()}")
        tempDir.mkdirs()
        storage = FilterListStorage(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testLicenseProvenanceAndAttribution_AuditComplete() {
        val lists = listOf(
            FilterListRegistry.PETER_LOWE_SEED,
            FilterListRegistry.EASYLIST_CANDIDATE,
            FilterListRegistry.EASYPRIVACY_CANDIDATE,
            FilterListRegistry.GECKOVIEW_BUILTIN_ETP
        )

        for (list in lists) {
            assertTrue("List ID must not be blank", list.id.isNotBlank())
            assertTrue("List name must not be blank", list.name.isNotBlank())
            assertTrue("License must be stated", list.license.isNotBlank())
            assertTrue("Attribution must be documented", list.attribution.isNotBlank())
            assertTrue("Homepage must be present", list.homepage.isNotBlank())
        }

        // Verify bundled seed list uses a permissive, non-copyleft license
        assertTrue(
            "Peter Lowe list must use permissive license for clean bundling",
            FilterListRegistry.PETER_LOWE_SEED.license.contains("Permissive", ignoreCase = true)
        )
    }

    @Test
    fun testGoldenRules_SupportedTypesAndExceptions() = runBlocking {
        val rulesContent = """
            ! Title: Golden Test List
            ! Version: 1.0.0
            ! Homepage: https://example.com/filters
            ! License: MIT
            !
            ! Network domain blocks
            ||bad-tracker.net^
            ||adserver.com^
            !
            ! Substring pattern block
            /adbanner/*
            |http://evil.com/ad.js|
            !
            ! Exceptions
            @@||allowed.com^
            @@/safe-banner/*
        """.trimIndent()

        val inputStream = ByteArrayInputStream(rulesContent.toByteArray(Charsets.UTF_8))
        val compiled = FilterRuleCompiler.compile(inputStream, FilterListRegistry.PETER_LOWE_SEED)

        assertEquals("Golden Test List", compiled.metadata.name)
        assertEquals("1.0.0", compiled.metadata.version)
        assertEquals(4, compiled.metadata.supportedNetworkRules)
        assertEquals(2, compiled.metadata.supportedExceptionRules)
        assertEquals(0, compiled.metadata.unsupportedCosmeticRules)

        // Evaluate domain blocks and subdomains
        val res1 = compiled.evaluate("https://bad-tracker.net/collect")
        assertTrue("bad-tracker.net must be blocked", res1 is FilterDecision.Blocked)

        val res2 = compiled.evaluate("https://sub.adserver.com/serve")
        assertTrue("subdomain of adserver.com must be blocked", res2 is FilterDecision.Blocked)

        // Evaluate path patterns
        val res3 = compiled.evaluate("https://news.org/adbanner/300x250.png")
        assertTrue("adbanner pattern must be blocked", res3 is FilterDecision.Blocked)

        val res4 = compiled.evaluate("http://evil.com/ad.js")
        assertTrue("exact URL must be blocked", res4 is FilterDecision.Blocked)

        // Evaluate exceptions
        val res5 = compiled.evaluate("https://allowed.com/resource")
        assertTrue("allowed.com must match exception", res5 is FilterDecision.Exception)

        val res6 = compiled.evaluate("https://site.org/safe-banner/img.png")
        assertTrue("safe-banner must match exception", res6 is FilterDecision.Exception)

        // Clean site allowed
        val res7 = compiled.evaluate("https://wikipedia.org/wiki/Main_Page")
        assertTrue("clean site must be allowed", res7 is FilterDecision.Allowed)
    }

    @Test
    fun testUnsupportedRules_CatalogedWithoutFalseParity() = runBlocking {
        val rulesWithCosmetics = """
            ! Header comment
            ##.promoted-ad
            ###banner-sponsor
            #@#.ad-allow-rule
            #?#div.sponsored-card
            example.com#+js(set, adsDisabled, true)
            site.org#+js(noeval)
            /regex-pattern-here/
            ||normal-ad.com^
        """.trimIndent()

        val inputStream = ByteArrayInputStream(rulesWithCosmetics.toByteArray(Charsets.UTF_8))
        val compiled = FilterRuleCompiler.compile(inputStream, FilterListRegistry.PETER_LOWE_SEED)

        // Rigorously catalog unsupported rules without claiming network parity
        assertEquals("Must catalog 4 cosmetic element hiding rules", 4, compiled.metadata.unsupportedCosmeticRules)
        assertEquals("Must catalog 2 scriptlet rules", 2, compiled.metadata.unsupportedScriptletRules)
        assertEquals("Must catalog 1 regex rule", 1, compiled.metadata.otherUnsupportedRules)
        assertEquals("Must keep 1 supported network rule", 1, compiled.metadata.supportedNetworkRules)
        assertEquals(1, compiled.stats.commentLines)

        // Verify normal rule works
        val blockRes = compiled.evaluate("https://normal-ad.com/script")
        assertTrue(blockRes is FilterDecision.Blocked)
    }

    @Test
    fun testMalformedAndOversizedInputs_SafelyBounded() = runBlocking {
        val normalRule = "||tracker-ok.com^"
        val oversizedLine = "||" + "a".repeat(3000) + "^" // Exceeds 2048 chars limit
        val afterRule = "||after-tracker.com^"

        val content = "$normalRule\n$oversizedLine\n$afterRule\n"
        val inputStream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        val compiled = FilterRuleCompiler.compile(inputStream, FilterListRegistry.PETER_LOWE_SEED)

        assertEquals("Oversized line must be safely skipped", 1, compiled.stats.oversizedLinesSkipped)
        assertEquals("Valid rules must still be parsed", 2, compiled.metadata.supportedNetworkRules)

        assertTrue(compiled.evaluate("https://tracker-ok.com/ping") is FilterDecision.Blocked)
        assertTrue(compiled.evaluate("https://after-tracker.com/ping") is FilterDecision.Blocked)
    }

    @Test
    fun testStagedAtomicActivation_PreservesActiveOnCorruptOrFailedUpdate() = runBlocking {
        val listId = "peter_lowe"
        val goodV1 = "||good-v1-tracker.com^\n||ad-delivery.net^\n"
        val corruptV2 = "" // Empty list -> golden check will reject

        // 1. Stage and activate good v1
        storage.stageList(listId, ByteArrayInputStream(goodV1.toByteArray(Charsets.UTF_8)))
        val res1 = storage.validateAndActivate(listId, FilterListRegistry.PETER_LOWE_SEED)
        assertTrue("Good v1 must activate", res1 is UpdateResult.Success)
        assertTrue(storage.hasActiveList(listId))

        val activeList1 = storage.loadActiveOrRollback(listId, FilterListRegistry.PETER_LOWE_SEED)
        assertNotNull(activeList1)
        assertEquals(2, activeList1?.metadata?.supportedNetworkRules)

        // 2. Stage corrupt v2
        storage.stageList(listId, ByteArrayInputStream(corruptV2.toByteArray(Charsets.UTF_8)))
        val res2 = storage.validateAndActivate(listId, FilterListRegistry.PETER_LOWE_SEED)
        assertTrue("Corrupt v2 must fail and trigger rollback", res2 is UpdateResult.Rollback)

        // 3. Verify active list is STILL valid v1 list!
        val activeAfterFailure = storage.loadActiveOrRollback(listId, FilterListRegistry.PETER_LOWE_SEED)
        assertNotNull(activeAfterFailure)
        assertEquals("Active rules must be preserved from v1", 2, activeAfterFailure?.metadata?.supportedNetworkRules)
        assertTrue(activeAfterFailure!!.evaluate("https://good-v1-tracker.com") is FilterDecision.Blocked)
    }

    @Test
    fun testUpdateRollback_ChecksumMismatchRejection() = runBlocking {
        val listId = "test_checksum"
        val content = "||ad-network.com^\n"

        storage.stageList(listId, ByteArrayInputStream(content.toByteArray(Charsets.UTF_8)))
        val result = storage.validateAndActivate(
            listId = listId,
            fallbackMetadata = FilterListRegistry.PETER_LOWE_SEED,
            expectedSha256 = "0000000000000000000000000000000000000000000000000000000000000000"
        )

        assertTrue("Hash mismatch must result in rollback", result is UpdateResult.Rollback)
        assertFalse("Staging file must not be committed to active", storage.hasActiveList(listId))
    }

    @Test
    fun testUpdater_HandlesHttpErrorAndNetworkFailure() = runBlocking {
        val listId = "http_test"
        val initialContent = "||working-rule.com^\n"

        // Activate working baseline
        storage.stageList(listId, ByteArrayInputStream(initialContent.toByteArray(Charsets.UTF_8)))
        storage.validateAndActivate(listId, FilterListRegistry.PETER_LOWE_SEED)

        // Mock fetcher that simulates HTTP 500 error
        val errorFetcher = HttpFetcher { _, _ ->
            FetchResponse(statusCode = 500, inputStream = ByteArrayInputStream(ByteArray(0)))
        }
        val updaterWithError = FilterListUpdater(storage, errorFetcher)
        val errorResult = updaterWithError.updateList(
            FilterListRegistry.PETER_LOWE_SEED.copy(id = listId)
        )
        assertTrue("HTTP 500 must return Rollback", errorResult is UpdateResult.Rollback)

        // Mock fetcher that simulates network timeout
        val timeoutFetcher = HttpFetcher { _, _ ->
            throw IOException("Connect timed out after 10000ms")
        }
        val updaterWithTimeout = FilterListUpdater(storage, timeoutFetcher)
        val timeoutResult = updaterWithTimeout.updateList(
            FilterListRegistry.PETER_LOWE_SEED.copy(id = listId)
        )
        assertTrue("Timeout must return Rollback", timeoutResult is UpdateResult.Rollback)

        // Verify active list remained intact throughout failures
        val active = storage.loadActiveOrRollback(listId, FilterListRegistry.PETER_LOWE_SEED)
        assertNotNull(active)
        assertTrue(active!!.evaluate("https://working-rule.com") is FilterDecision.Blocked)
    }

    @Test
    fun testRollbackToLastKnownGood_OnActiveCorruption() = runBlocking {
        val listId = "lkg_test"
        val v1Rules = "||v1-tracker.com^\n"
        val v2Rules = "||v2-tracker.com^\n"

        // Activate V1
        storage.stageList(listId, ByteArrayInputStream(v1Rules.toByteArray(Charsets.UTF_8)))
        storage.validateAndActivate(listId, FilterListRegistry.PETER_LOWE_SEED)

        // Activate V2 -> V1 becomes Last-Known-Good
        storage.stageList(listId, ByteArrayInputStream(v2Rules.toByteArray(Charsets.UTF_8)))
        storage.validateAndActivate(listId, FilterListRegistry.PETER_LOWE_SEED)
        assertTrue("Must have Last-Known-Good file", storage.hasLkgList(listId))

        // Corrupt active file by writing invalid raw data
        val activeFile = File(tempDir, "active_${listId}.txt")
        activeFile.writeBytes(ByteArray(10) { 0.toByte() }) // Raw null bytes

        val recovered = storage.loadActiveOrRollback(listId, FilterListRegistry.PETER_LOWE_SEED)
        assertNotNull("Must successfully recover from LKG", recovered)
        assertTrue("V1 rule must be restored from LKG", recovered!!.evaluate("https://v1-tracker.com") is FilterDecision.Blocked)
    }

    @Test
    fun testPerformanceBenchmark_LoadAndEvaluationOverhead() = runBlocking {
        // Generate a synthetic list of 1,000 domains, 50 exceptions, and 200 cosmetic rules
        val sb = StringBuilder()
        sb.append("! Title: Benchmark Test List\n")
        for (i in 1..1000) {
            sb.append("||ad-server-$i.benchmark.com^\n")
        }
        for (i in 1..50) {
            sb.append("@@||ad-server-$i.benchmark.com/allowed^\n")
        }
        for (i in 1..200) {
            sb.append("##.cosmetic-banner-$i\n")
        }

        val bytes = sb.toString().toByteArray(Charsets.UTF_8)

        // 1. Benchmark compilation time
        val compileStart = System.nanoTime()
        val compiled = FilterRuleCompiler.compile(
            ByteArrayInputStream(bytes),
            FilterListRegistry.PETER_LOWE_SEED
        )
        val compileTimeMs = (System.nanoTime() - compileStart) / 1_000_000.0

        println("BENCHMARK: Compiled 1,250 rules in ${compileTimeMs}ms")
        assertTrue("List compilation must complete under 500ms (Actual: ${compileTimeMs}ms)", compileTimeMs < 500.0)

        // 2. Benchmark evaluation latency over 5,000 lookups
        val evalStart = System.nanoTime()
        for (i in 1..5000) {
            val domainIndex = (i % 1000) + 1
            val testUrl = "https://ad-server-$domainIndex.benchmark.com/load"
            val decision = compiled.evaluate(testUrl)
            assertTrue(decision is FilterDecision.Blocked)
        }
        val totalEvalTimeMs = (System.nanoTime() - evalStart) / 1_000_000.0
        val avgLatencyMicroseconds = (totalEvalTimeMs * 1000.0) / 5000.0

        println("BENCHMARK: 5,000 evaluations completed in ${totalEvalTimeMs}ms (Avg: ${avgLatencyMicroseconds}µs per check)")
        assertTrue(
            "Average evaluation latency must be under 100µs (Actual: ${avgLatencyMicroseconds}µs)",
            avgLatencyMicroseconds < 100.0
        )
    }

    @Test
    fun testGeckoViewEngineBridgeStatus_DocumentedAndSafe() {
        // Verify engine bridge status satisfies Phase 06 gate
        val nativeStatus = EngineBridgeStatus.ENGINE_NATIVE_ACTIVE
        val offlineStatus = EngineBridgeStatus.OFFLINE_PIPELINE_ACTIVE

        assertTrue(nativeStatus.description.contains("GeckoView built-in ContentBlocking"))
        assertTrue(nativeStatus.description.contains("future research"))
        assertTrue(offlineStatus.description.contains("Musify Kotlin Filter Pipeline"))
    }
}
