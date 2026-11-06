package com.musify.browser

import com.musify.browser.shield.ProceduralScriptletEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 33 Unit Tests: Validates Brave-aligned procedural DOM scriptlets
 * (set-constant, json-prune, abort-current-script, prevent-addEventListener)
 * for inline ad neutralization, anti-adblock defusal, and background playback preservation.
 */
class Phase33ProceduralScriptletTest {

    @Test
    fun testSetConstantScriptGeneration() {
        // Boolean true
        val scriptBool = ProceduralScriptletEngine.buildSetConstantScript("canRunAds", "true")
        assertTrue("Must target canRunAds", scriptBool.contains("'canRunAds'"))
        assertTrue("Must set value to true", scriptBool.contains("return true;"))
        assertTrue("Must be non-configurable", scriptBool.contains("configurable: false"))

        // Boolean false
        val scriptFalse = ProceduralScriptletEngine.buildSetConstantScript("isAdBlockActive", "false")
        assertTrue("Must set value to false", scriptFalse.contains("return false;"))

        // Null
        val scriptNull = ProceduralScriptletEngine.buildSetConstantScript("adblockDetector", "null")
        assertTrue("Must set value to null", scriptNull.contains("return null;"))

        // Number
        val scriptNum = ProceduralScriptletEngine.buildSetConstantScript("google_ad_status", "1")
        assertTrue("Must set value to 1", scriptNum.contains("return 1;"))

        // Nested property path
        val scriptNested = ProceduralScriptletEngine.buildSetConstantScript("google.ads.status", "0")
        assertTrue("Must resolve nested parent window.google.ads", scriptNested.contains("var target = window.google.ads;"))
        assertTrue("Must target final property status", scriptNested.contains("'status'"))

        // Empty string
        val scriptEmpty = ProceduralScriptletEngine.buildSetConstantScript("", "true")
        assertEquals("", scriptEmpty)
    }

    @Test
    fun testJsonPruneScriptGeneration() {
        val script = ProceduralScriptletEngine.buildJsonPruneScript("playerAds", "adPlacements", "adSlots")
        assertTrue("Must enforce idempotence guard", script.contains("window.__musifyJsonPruneInjected"))
        assertTrue("Must embed prune keys array", script.contains("'playerAds', 'adPlacements', 'adSlots'"))
        assertTrue("Must wrap JSON.parse", script.contains("JSON.parse = function"))
        assertTrue("Must wrap Response.prototype.json", script.contains("Response.prototype.json = function"))
        assertTrue("Must provide recursive pruning function", script.contains("function pruneObject(obj)"))
    }

    @Test
    fun testAbortCurrentScriptGeneration() {
        val script = ProceduralScriptletEngine.buildAbortCurrentScript("detectAdBlocker")
        assertTrue("Must define function that throws ReferenceError", script.contains("throw new ReferenceError"))
        assertTrue("Must include abort reason and target name", script.contains("detectAdBlocker"))
        assertTrue("Must define property on window", script.contains("Object.defineProperty(window, 'detectAdBlocker'"))
    }

    @Test
    fun testPreventEventListenerScriptGeneration() {
        val script = ProceduralScriptletEngine.buildPreventEventListenerScript("visibilitychange")
        assertTrue("Must hook EventTarget.prototype.addEventListener", script.contains("EventTarget.prototype.addEventListener = function"))
        assertTrue("Must check target event type", script.contains("type === 'visibilitychange'"))
        assertTrue("Must drop listener without calling original", script.contains("return;"))

        val emptyScript = ProceduralScriptletEngine.buildPreventEventListenerScript("   ")
        assertEquals("", emptyScript)
    }

    @Test
    fun testStandardDefuserSuiteIntegrity() {
        val suite = ProceduralScriptletEngine.buildStandardDefuserSuite()
        assertTrue("Suite must bundle canRunAds", suite.contains("'canRunAds'"))
        assertTrue("Suite must bundle isAdBlockActive", suite.contains("'isAdBlockActive'"))
        assertTrue("Suite must bundle google_ad_status", suite.contains("'google_ad_status'"))
        assertTrue("Suite must bundle JSON pruner for playerAds", suite.contains("playerAds"))
        assertTrue("Suite must bundle visibilitychange event prevention", suite.contains("'visibilitychange'"))
    }

    @Test
    fun testScriptletErrorResilienceAndSafety() {
        val setConstant = ProceduralScriptletEngine.buildSetConstantScript("safe.variable", "noopfn")
        assertTrue("Must wrap in try-catch to prevent unhandled errors on frozen globals", setConstant.contains("try {") && setConstant.contains("} catch(e) {}"))

        val abortScript = ProceduralScriptletEngine.buildAbortCurrentScript("adProbe")
        assertTrue("Must wrap in try-catch", abortScript.contains("try {") && abortScript.contains("} catch(e) {}"))
    }
}

