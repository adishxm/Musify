/**
 * Musify Procedural DOM Scriptlets & Anti-Adblock Defusers
 * Reverse-engineered from Brave Browser's resources.json
 */
(function() {
    'use strict';
    if (window.__musifyProceduralDefusersInjected) return;
    window.__musifyProceduralDefusersInjected = true;

    // 1. set-constant defusers for common anti-adblock variables
    function setConstant(target, prop, value) {
        try {
            if (!target) return;
            Object.defineProperty(target, prop, {
                get: function() { return value; },
                set: function() {},
                configurable: false,
                enumerable: true
            });
        } catch(e) {}
    }

    setConstant(window, 'canRunAds', true);
    setConstant(window, 'isAdBlockActive', false);
    setConstant(window, 'google_ad_status', 1);

    // 2. json-prune for media and news ad payloads
    var pruneKeys = ['playerAds', 'adPlacements', 'adSlots', 'adPods', 'advertisement'];
    function pruneObject(obj) {
        if (!obj || typeof obj !== 'object') return;
        if (Array.isArray(obj)) {
            for (var i = 0; i < obj.length; i++) pruneObject(obj[i]);
            return;
        }
        for (var k = 0; k < pruneKeys.length; k++) {
            var key = pruneKeys[k];
            if (key in obj) {
                try { delete obj[key]; } catch(e) { obj[key] = undefined; }
            }
        }
        for (var prop in obj) {
            if (Object.prototype.hasOwnProperty.call(obj, prop) && typeof obj[prop] === 'object') {
                pruneObject(obj[prop]);
            }
        }
    }

    try {
        var origParse = JSON.parse;
        JSON.parse = function(text, reviver) {
            var result = origParse.call(this, text, reviver);
            pruneObject(result);
            return result;
        };
    } catch(e) {}

    try {
        if (window.Response && Response.prototype && Response.prototype.json) {
            var origJson = Response.prototype.json;
            Response.prototype.json = function() {
                return origJson.apply(this, arguments).then(function(res) {
                    pruneObject(res);
                    return res;
                });
            };
        }
    } catch(e) {}

    // 3. prevent-addEventListener on visibilitychange to preserve media background playback
    try {
        var origAEL = EventTarget.prototype.addEventListener;
        EventTarget.prototype.addEventListener = function(type, listener, options) {
            if (type === 'visibilitychange') {
                return;
            }
            return origAEL.apply(this, arguments);
        };
    } catch(e) {}
})();

