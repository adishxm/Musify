package com.musify.browser.shield

/**
 * Types of advertising and tracking SDK mocks and anti-adblock defusers.
 * Reverse-engineered directly from Brave Browser (Brave Shields component mfddibmblmbccpadfndgakiopmmhebop).
 */
enum class MockScriptType {
    GOOGLE_PUBLISHER_TAG,
    GOOGLE_ADSENSE,
    AMAZON_APSTAG,
    AMAZON_ADS,
    FUCK_ADBLOCK,
    BLOCK_ADBLOCK,
    GOOGLE_ANALYTICS,
    PREBID_ADS
}

/**
 * Encapsulates a synthetic JavaScript stub and its categorization.
 */
data class AdSdkMock(
    val type: MockScriptType,
    val scriptContent: String,
    val mimeType: String = "application/javascript",
    val isAntiAdblockDefuser: Boolean = false
)

/**
 * AdSdkMockRepository provides zero-network, synthetic JavaScript stubs for ubiquitous
 * ad server SDKs (Google Publisher Tag, AdSense, Amazon A9) and anti-adblock defusers
 * (FuckAdBlock, BlockAdBlock, Prebid ad-check bait).
 *
 * When websites probe window global variables (such as googletag.cmd, adsbygoogle, FuckAdBlock),
 * these stubs immediately satisfy callback invocations and set expectation flags without
 * executing tracking scripts or loading remote advertisements.
 */
object AdSdkMockRepository {

    // 1. Google Publisher Tag (GPT) Mock
    // Brave reverse-engineered: googletagservices_gpt.js
    val GPT_SCRIPT = """
        (function() {
            'use strict';
            const noopfn = function() {}.bind();
            const noopthisfn = function() { return this; };
            const noopnullfn = function() { return null; };
            const nooparrayfn = function() { return []; };
            const noopstrfn = function() { return ''; };

            const companionAdsService = {
                addEventListener: noopthisfn,
                enableSyncLoading: noopfn,
                setRefreshUnfilledSlots: noopfn
            };
            const contentService = {
                addEventListener: noopthisfn,
                setContent: noopfn
            };
            const PassbackSlot = function() {};
            var p = PassbackSlot.prototype;
            p.display = noopfn;
            p.get = noopnullfn;
            p.set = noopthisfn;
            p.setClickUrl = noopthisfn;
            p.setTagForChildDirectedTreatment = noopthisfn;
            p.setTargeting = noopthisfn;
            p.updateTargetingFromMap = noopthisfn;

            const pubAdsService = {
                addEventListener: noopthisfn,
                clear: noopfn,
                clearCategoryExclusions: noopthisfn,
                clearTagForChildDirectedTreatment: noopthisfn,
                clearTargeting: noopthisfn,
                collapseEmptyDivs: noopfn,
                defineOutOfPagePassback: function() { return new PassbackSlot(); },
                definePassback: function() { return new PassbackSlot(); },
                disableInitialLoad: noopfn,
                display: noopfn,
                enableAsyncRendering: noopfn,
                enableLazyLoad: noopfn,
                enableSingleRequest: noopfn,
                enableSyncRendering: noopfn,
                enableVideoAds: noopfn,
                get: noopnullfn,
                getAttributeKeys: nooparrayfn,
                getTargeting: nooparrayfn,
                getTargetingKeys: nooparrayfn,
                getSlots: nooparrayfn,
                refresh: noopfn,
                removeEventListener: noopfn,
                set: noopthisfn,
                setCategoryExclusion: noopthisfn,
                setCentering: noopfn,
                setCookieOptions: noopthisfn,
                setForceSafeFrame: noopthisfn,
                setLocation: noopthisfn,
                setPublisherProvidedId: noopthisfn,
                setPrivacySettings: noopthisfn,
                setRequestNonPersonalizedAds: noopthisfn,
                setSafeFrameConfig: noopthisfn,
                setTagForChildDirectedTreatment: noopthisfn,
                setTargeting: noopthisfn,
                setVideoContent: noopthisfn,
                updateCorrelator: noopfn
            };

            const SizeMappingBuilder = function() {};
            p = SizeMappingBuilder.prototype;
            p.addSize = noopthisfn;
            p.build = noopnullfn;

            const Slot = function() {};
            p = Slot.prototype;
            p.addService = noopthisfn;
            p.clearCategoryExclusions = noopthisfn;
            p.clearTargeting = noopthisfn;
            p.defineSizeMapping = noopthisfn;
            p.get = noopnullfn;
            p.getAdUnitPath = nooparrayfn;
            p.getAttributeKeys = nooparrayfn;
            p.getCategoryExclusions = nooparrayfn;
            p.getDomId = noopstrfn;
            p.getResponseInformation = noopnullfn;
            p.getSlotElementId = noopstrfn;
            p.getSlotId = noopthisfn;
            p.getTargeting = nooparrayfn;
            p.getTargetingKeys = nooparrayfn;
            p.set = noopthisfn;
            p.setCategoryExclusion = noopthisfn;
            p.setClickUrl = noopthisfn;
            p.setCollapseEmptyDiv = noopthisfn;
            p.setTargeting = noopthisfn;
            p.updateTargetingFromMap = noopthisfn;

            const gpt = window.googletag || {};
            const cmd = gpt.cmd || [];
            gpt.apiReady = true;
            gpt.cmd = [];
            gpt.cmd.push = function(fn) {
                if (typeof fn === 'function') {
                    try { fn(); } catch (ex) {}
                }
                return 1;
            };
            gpt.companionAds = function() { return companionAdsService; };
            gpt.content = function() { return contentService; };
            gpt.defineOutOfPageSlot = function() { return new Slot(); };
            gpt.defineSlot = function() { return new Slot(); };
            gpt.destroySlots = noopfn;
            gpt.disablePublisherConsole = noopfn;
            gpt.display = noopfn;
            gpt.enableServices = noopfn;
            gpt.getVersion = noopstrfn;
            gpt.pubads = function() { return pubAdsService; };
            gpt.pubadsReady = true;
            gpt.setAdIframeTitle = noopfn;
            gpt.sizeMapping = function() { return new SizeMappingBuilder(); };
            window.googletag = gpt;

            while (cmd.length !== 0) {
                gpt.cmd.push(cmd.shift());
            }
        })();
    """.trimIndent()

    // 2. Google AdSense (adsbygoogle) Mock
    // Brave reverse-engineered: googlesyndication_adsbygoogle.js
    val ADSBYGOOGLE_SCRIPT = """
        (function() {
            'use strict';
            self.adsbygoogle = self.adsbygoogle || {
                loaded: true,
                push: function() {}
            };
            let adCount = 1;
            const setupAd = (placeholder) => {
                if (!placeholder || placeholder.dataset.adsbygoogleStatus) return;
                const fr = document.createElement('iframe');
                fr.id = 'aswift_' + adCount;
                fr.setAttribute('name', fr.id);
                fr.style.display = 'none';
                adCount += 1;
                placeholder.dataset.adsbygoogleStatus = 'loading';
                placeholder.dataset.adStatus = 'loading';
                placeholder.appendChild(fr);
                try {
                    placeholder.dataset.adsbygoogleStatus = 'done';
                    placeholder.dataset.adStatus = 'filled';
                    fr.dataset.loadComplete = 'true';
                } catch (e) {}
            };
            const process = () => {
                if (typeof document === 'undefined' || !document.querySelectorAll) return;
                const phs = document.querySelectorAll('.adsbygoogle:not([data-ad-status][data-adsbygoogle-status])');
                for (let i = 0; i < phs.length; i++) {
                    setupAd(phs[i]);
                }
            };
            if (typeof document !== 'undefined') {
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', process, { once: true });
                } else {
                    process();
                }
            }
        })();
    """.trimIndent()

    // 3. Amazon Publisher Services (apstag) Mock
    // Brave reverse-engineered: amazon_apstag.js
    val AMAZON_APSTAG_SCRIPT = """
        (function() {
            'use strict';
            const w = window;
            const noopfn = function() {}.bind();
            const _Q = (w.apstag && w.apstag._Q) || [];
            const apstag = {
                _Q: _Q,
                fetchBids: function(cfg, callback) {
                    if (typeof callback === 'function') {
                        try { callback([]); } catch (e) {}
                    }
                },
                init: noopfn,
                setDisplayBids: noopfn,
                targetingKeys: function() { return []; }
            };
            w.apstag = apstag;
            _Q.push = function(prefix, args) {
                try {
                    if (prefix === 'f' && args) {
                        apstag.fetchBids.apply(apstag, args);
                    }
                } catch (e) {}
            };
            for (let i = 0; i < _Q.length; i++) {
                _Q.push(_Q[i]);
            }
        })();
    """.trimIndent()

    // 4. Amazon Ads (amznads) Mock
    // Brave reverse-engineered: amazon_ads.js
    val AMAZON_ADS_SCRIPT = """
        (function() {
            'use strict';
            const noopfn = function() {};
            window.amznads = window.amznads || {
                getAds: noopfn,
                setTargetingForGPT: noopfn,
                renderAd: noopfn
            };
        })();
    """.trimIndent()

    // 5. FuckAdBlock Defuser
    // Brave reverse-engineered: nofab.js
    val FUCKADBLOCK_SCRIPT = """
        (function() {
            'use strict';
            const noopfn = function() {};
            const Fab = function() {};
            Fab.prototype.check = noopfn;
            Fab.prototype.clearEvent = noopfn;
            Fab.prototype.emitEvent = noopfn;
            Fab.prototype.on = function(detected, callback) {
                if (!detected && typeof callback === 'function') {
                    try { callback(); } catch (e) {}
                }
                return this;
            };
            Fab.prototype.onDetected = function() {
                return this;
            };
            Fab.prototype.onNotDetected = function(callback) {
                if (typeof callback === 'function') {
                    try { callback(); } catch (e) {}
                }
                return this;
            };
            Fab.prototype.setOption = noopfn;
            Fab.prototype.options = { set: noopfn, get: noopfn };

            const fab = new Fab();
            const getSetFab = { get: function() { return Fab; }, set: function() {} };
            const getsetfab = { get: function() { return fab; }, set: function() {} };

            try {
                if (window.hasOwnProperty('FuckAdBlock')) { window.FuckAdBlock = Fab; }
                else { Object.defineProperty(window, 'FuckAdBlock', getSetFab); }
                if (window.hasOwnProperty('BlockAdBlock')) { window.BlockAdBlock = Fab; }
                else { Object.defineProperty(window, 'BlockAdBlock', getSetFab); }
                if (window.hasOwnProperty('SniffAdBlock')) { window.SniffAdBlock = Fab; }
                else { Object.defineProperty(window, 'SniffAdBlock', getSetFab); }
                if (window.hasOwnProperty('fuckAdBlock')) { window.fuckAdBlock = fab; }
                else { Object.defineProperty(window, 'fuckAdBlock', getsetfab); }
                if (window.hasOwnProperty('blockAdBlock')) { window.blockAdBlock = fab; }
                else { Object.defineProperty(window, 'blockAdBlock', getsetfab); }
                if (window.hasOwnProperty('sniffAdBlock')) { window.sniffAdBlock = fab; }
                else { Object.defineProperty(window, 'sniffAdBlock', getsetfab); }
            } catch (ex) {
                window.FuckAdBlock = Fab;
                window.BlockAdBlock = Fab;
                window.SniffAdBlock = Fab;
                window.fuckAdBlock = fab;
                window.blockAdBlock = fab;
                window.sniffAdBlock = fab;
            }
        })();
    """.trimIndent()

    // 6. BlockAdBlock v2 Defuser
    // Brave reverse-engineered: nobab2.js
    val BLOCKADBLOCK2_SCRIPT = """
        (function() {
            'use strict';
            window.nH7eXzOsG = 858;
        })();
    """.trimIndent()

    // 7. Google Analytics / GTM Defuser & Callback Draining
    // Brave reverse-engineered: google-analytics_analytics.js
    val GOOGLE_ANALYTICS_SCRIPT = """
        (function() {
            'use strict';
            const noopfn = function() {};
            const Tracker = function() {};
            Tracker.prototype.get = noopfn;
            Tracker.prototype.set = noopfn;
            Tracker.prototype.send = noopfn;

            const w = window;
            const gaName = w.GoogleAnalyticsObject || 'ga';
            const gaQueue = w[gaName];

            const ga = function() {
                const len = arguments.length;
                if (len === 0) return;
                const args = Array.from(arguments);
                let fn;
                let a = args[len - 1];
                if (a instanceof Object && typeof a.hitCallback === 'function') {
                    fn = a.hitCallback;
                } else if (typeof a === 'function') {
                    fn = function() { a(ga.create()); };
                } else {
                    const pos = args.indexOf('hitCallback');
                    if (pos !== -1 && typeof args[pos + 1] === 'function') {
                        fn = args[pos + 1];
                    }
                }
                if (typeof fn !== 'function') return;
                try { fn(); } catch (ex) {}
            };
            ga.create = function() { return new Tracker(); };
            ga.getByName = function() { return new Tracker(); };
            ga.getAll = function() { return [new Tracker()]; };
            ga.remove = noopfn;
            ga.loaded = true;
            w[gaName] = ga;

            const dl = w.dataLayer;
            if (dl instanceof Object) {
                if (dl.hide instanceof Object && typeof dl.hide.end === 'function') {
                    dl.hide.end();
                    dl.hide.end = noopfn;
                }
                if (typeof dl.push === 'function') {
                    const doCallback = function(item) {
                        if (!(item instanceof Object)) return;
                        if (typeof item.eventCallback !== 'function') return;
                        setTimeout(item.eventCallback, 1);
                        item.eventCallback = noopfn;
                    };
                    dl.push = new Proxy(dl.push, {
                        apply: function(target, thisArg, args) {
                            doCallback(args[0]);
                            return Reflect.apply(target, thisArg, args);
                        }
                    });
                    if (Array.isArray(dl)) {
                        const q = dl.slice();
                        for (let i = 0; i < q.length; i++) {
                            doCallback(q[i]);
                        }
                    }
                }
            }
            if (typeof gaQueue === 'function' && Array.isArray(gaQueue.q)) {
                const q = gaQueue.q.slice();
                gaQueue.q.length = 0;
                for (let i = 0; i < q.length; i++) {
                    ga.apply(null, q[i]);
                }
            }
        })();
    """.trimIndent()

    // 8. Prebid Adblock Detection Bait Defuser
    // Brave reverse-engineered: prebid-ads.js
    val PREBID_ADS_SCRIPT = """
        (function() {
            'use strict';
            window.canRunAds = true;
            window.isAdBlockActive = false;
        })();
    """.trimIndent()

    /**
     * Evaluates a URL against known ad SDK endpoints and anti-adblock detection bait files.
     * Returns an [AdSdkMock] when a match is found, or null otherwise.
     */
    fun matchMockScript(url: String): AdSdkMock? {
        if (url.isBlank()) return null
        val lower = url.lowercase()

        // 1. Google Publisher Tag (GPT)
        if (lower.contains("googletagservices.com/tag/js/gpt.js") ||
            lower.contains("securepubads.g.doubleclick.net/tag/js/gpt.js") ||
            lower.contains("googletagservices.com/gpt.js") ||
            lower.endsWith("/gpt.js") ||
            lower.contains("/gpt.js?")
        ) {
            return AdSdkMock(
                type = MockScriptType.GOOGLE_PUBLISHER_TAG,
                scriptContent = GPT_SCRIPT,
                isAntiAdblockDefuser = false
            )
        }

        // 2. Google AdSense (adsbygoogle)
        if (lower.contains("pagead2.googlesyndication.com/pagead/js/adsbygoogle.js") ||
            lower.contains("googlesyndication.com/pagead/js/adsbygoogle.js") ||
            lower.contains("/adsbygoogle.js")
        ) {
            return AdSdkMock(
                type = MockScriptType.GOOGLE_ADSENSE,
                scriptContent = ADSBYGOOGLE_SCRIPT,
                isAntiAdblockDefuser = false
            )
        }

        // 3. Amazon APSTag
        if (lower.contains("amazon-adsystem.com/aax2/apstag.js") ||
            lower.contains("/apstag.js")
        ) {
            return AdSdkMock(
                type = MockScriptType.AMAZON_APSTAG,
                scriptContent = AMAZON_APSTAG_SCRIPT,
                isAntiAdblockDefuser = false
            )
        }

        // 4. Amazon Ads (amzn_ads)
        if (lower.contains("amazon-adsystem.com/aax2/amzn_ads.js") ||
            lower.contains("/amzn_ads.js")
        ) {
            return AdSdkMock(
                type = MockScriptType.AMAZON_ADS,
                scriptContent = AMAZON_ADS_SCRIPT,
                isAntiAdblockDefuser = false
            )
        }

        // 5. FuckAdBlock Defuser
        if (lower.contains("fuckadblock") ||
            lower.contains("/nofab.js")
        ) {
            return AdSdkMock(
                type = MockScriptType.FUCK_ADBLOCK,
                scriptContent = FUCKADBLOCK_SCRIPT,
                isAntiAdblockDefuser = true
            )
        }

        // 6. BlockAdBlock v2 Defuser (ad network probe scripts)
        if (lower.contains("blockadblock") ||
            lower.contains("/nobab.js") ||
            lower.contains("/nobab2.js") ||
            lower.contains("adclixx.net") ||
            lower.contains("adnetasia.com") ||
            lower.contains("adtrackers.net") ||
            lower.contains("bannertrack.net")
        ) {
            return AdSdkMock(
                type = MockScriptType.BLOCK_ADBLOCK,
                scriptContent = BLOCKADBLOCK2_SCRIPT,
                isAntiAdblockDefuser = true
            )
        }

        // 7. Google Analytics / GTM
        if (lower.contains("google-analytics.com/analytics.js") ||
            lower.contains("google-analytics.com/ga.js") ||
            lower.contains("googletagmanager.com/gtm.js") ||
            lower.contains("googletagmanager.com/gtag/js")
        ) {
            return AdSdkMock(
                type = MockScriptType.GOOGLE_ANALYTICS,
                scriptContent = GOOGLE_ANALYTICS_SCRIPT,
                isAntiAdblockDefuser = false
            )
        }

        // 8. Prebid Adblock Detection Bait
        if (lower.contains("prebid-ads.js") ||
            lower.contains("canrunads.js") ||
            lower.contains("can-run-ads.js") ||
            lower.contains("ads-prebid.js")
        ) {
            return AdSdkMock(
                type = MockScriptType.PREBID_ADS,
                scriptContent = PREBID_ADS_SCRIPT,
                isAntiAdblockDefuser = true
            )
        }

        return null
    }
}
