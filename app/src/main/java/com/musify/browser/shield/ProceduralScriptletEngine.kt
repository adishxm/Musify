package com.musify.browser.shield

/**
 * ProceduralScriptletEngine synthesizes Brave-aligned procedural DOM scriptlets
 * executed in the page's execution context.
 *
 * Reverse-engineered from Brave Browser's `resources.json`:
 * - `set-constant.js`: Freezes properties with Object.defineProperty
 * - `json-prune.js`: Prunes blacklisted ad/telemetry keys from JSON.parse and Response.json()
 * - `abort-current-script.js`: Neutralizes inline ad and anti-adblock scripts
 * - `prevent-addEventListener.js`: Prevents websites from halting playback on visibilitychange/blur
 */
object ProceduralScriptletEngine {

    /**
     * Builds a JavaScript snippet that defines a non-configurable, non-writable property
     * on `window` or a nested object hierarchy.
     */
    fun buildSetConstantScript(propertyPath: String, rawValue: String): String {
        val cleanPath = propertyPath.trim()
        val parts = cleanPath.split('.').filter { it.isNotBlank() }
        if (parts.isEmpty()) return ""

        val jsValue = when (rawValue.trim().lowercase()) {
            "true" -> "true"
            "false" -> "false"
            "null" -> "null"
            "undefined" -> "undefined"
            "''", "\"\"", "emptyarr", "[]" -> "[]"
            "emptyobj", "{}" -> "({})"
            "noopfn" -> "(function(){})"
            "noopthis" -> "(function(){ return this; })"
            "noopnull" -> "(function(){ return null; })"
            "nooparray" -> "(function(){ return []; })"
            else -> {
                val num = rawValue.trim().toDoubleOrNull()
                if (num != null) rawValue.trim() else "'${rawValue.replace("'", "\\'")}'"
            }
        }

        val prop = parts.last()
        val parentPath = if (parts.size > 1) {
            "window." + parts.subList(0, parts.size - 1).joinToString(".")
        } else {
            "window"
        }

        return """
            (function() {
                try {
                    var target = $parentPath;
                    if (!target) return;
                    Object.defineProperty(target, '$prop', {
                        get: function() { return $jsValue; },
                        set: function() {},
                        configurable: false,
                        enumerable: true
                    });
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Builds a JavaScript snippet that wraps `JSON.parse` and `Response.prototype.json`
     * to prune ad keys from fetched objects.
     */
    fun buildJsonPruneScript(vararg paths: String): String {
        val pathArrayJs = paths.filter { it.isNotBlank() }
            .joinToString(", ") { "'${it.replace("'", "\\'").trim()}'" }

        return """
            (function() {
                if (window.__musifyJsonPruneInjected) return;
                window.__musifyJsonPruneInjected = true;

                var pruneKeys = [$pathArrayJs];
                function pruneObject(obj) {
                    var depth = arguments[1] || 0;
                    if (!obj || typeof obj !== 'object' || depth > 8) return;
                    if (Array.isArray(obj)) {
                        for (var i = 0; i < obj.length; i++) {
                            pruneObject(obj[i], depth + 1);
                        }
                        return;
                    }
                    for (var k = 0; k < pruneKeys.length; k++) {
                        var key = pruneKeys[k];
                        if (key in obj) {
                            try { delete obj[key]; } catch(e) { obj[key] = undefined; }
                        }
                    }
                    for (var prop in obj) {
                        if (Object.prototype.hasOwnProperty.call(obj, prop) && typeof obj[prop] === 'object' && obj[prop] !== null) {
                            pruneObject(obj[prop], depth + 1);
                        }
                    }
                }

                try {
                    var origParse = JSON.parse;
                    JSON.parse = function(text, reviver) {
                        var result = origParse.call(this, text, reviver);
                        try { pruneObject(result, 0); } catch(e) {}
                        return result;
                    };
                } catch(e) {}

                try {
                    if (window.Response && Response.prototype && Response.prototype.json) {
                        var origJson = Response.prototype.json;
                        Response.prototype.json = function() {
                            return origJson.apply(this, arguments).then(function(res) {
                                try { pruneObject(res, 0); } catch(e) {}
                                return res;
                            });
                        };
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Builds a scriptlet that neutralizes callers of anti-adblock functions by throwing
     * an error to abort execution of the calling script tag.
     */
    fun buildAbortCurrentScript(functionOrPropName: String): String {
        val clean = functionOrPropName.trim()
        if (clean.isBlank()) return ""

        return """
            (function() {
                try {
                    var fn = function() {
                        throw new ReferenceError('Musify: Aborting script for anti-adblock defense: $clean');
                    };
                    Object.defineProperty(window, '$clean', {
                        get: function() { return fn; },
                        set: function() {},
                        configurable: false
                    });
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Builds a scriptlet that suppresses unwanted EventListeners (e.g. visibilitychange
     * or blur to prevent video pause loops when tabbing away).
     */
    fun buildPreventEventListenerScript(targetEventType: String): String {
        val cleanType = targetEventType.trim()
        if (cleanType.isBlank()) return ""

        return """
            (function() {
                try {
                    var origAddEventListener = EventTarget.prototype.addEventListener;
                    EventTarget.prototype.addEventListener = function(type, listener, options) {
                        if (type === '$cleanType') {
                            return;
                        }
                        return origAddEventListener.apply(this, arguments);
                    };
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Bundles Brave's core standard anti-adblock defusers and YouTube/Spotify JSON pruners
     * into a single compact bootstrap script injected at document_start.
     */
    fun buildStandardDefuserSuite(): String {
        val setCanRunAds = buildSetConstantScript("canRunAds", "true")
        val setIsAdBlockActive = buildSetConstantScript("isAdBlockActive", "false")
        val setGoogleAdStatus = buildSetConstantScript("google_ad_status", "1")
        val jsonPruner = buildJsonPruneScript("playerAds", "adPlacements", "adSlots", "adPods", "advertisement")
        val preventVisibility = buildPreventEventListenerScript("visibilitychange")

        return listOf(
            setCanRunAds,
            setIsAdBlockActive,
            setGoogleAdStatus,
            jsonPruner,
            preventVisibility
        ).joinToString("\n")
    }
}

