package com.musify.browser.shield

/**
 * AnnoyanceFilterRepository provides cosmetic CSS and procedural unfreezing scripts
 * to defuse intrusive GDPR cookie consent dialogs, modal backdrops, and mobile "Open in App" prompts.
 *
 * Reverse-engineered from official Brave Browser components:
 * - Cookie Notice Blocker (`cdbbhgbmjhfnhnmgeddbliobbofkgdhe` / list.txt)
 * - Mobile App Promo Blocker (`bfpgedeaaibpoidldhjcknekahbikncb` / list.txt)
 */
object AnnoyanceFilterRepository {

    // 1. High-impact Cookie Consent Dialog Selectors (OneTrust, Cookiebot, TrustArc, Didomi, Quantcast, Usercentrics)
    val COOKIE_NOTICE_SELECTORS = listOf(
        "#onetrust-consent-sdk",
        "#onetrust-banner-sdk",
        "#onetrust-pc-sdk",
        ".onetrust-pc-dark-filter",
        "#CookiebotWidget",
        "#CookiebotDialog",
        "#cookiebanner",
        "#didomi-host",
        "#didomi-popup",
        ".didomi-popup-container",
        "#truste-consent-track",
        ".truste_box_overlay",
        ".truste_overlay",
        "#consent_blackbar",
        ".qc-cmp2-container",
        ".qc-cmp-ui-container",
        ".qc-cmp2-ui",
        "#usercentrics-root",
        ".cmplz-cookiebanner",
        ".cmplz-soft-cookiewall",
        "[id*='cookie-banner' i]",
        "[class*='cookie-banner' i]",
        "[id*='cookie-consent' i]",
        "[class*='cookie-consent' i]",
        "[id*='cookie-notice' i]",
        "[class*='cookie-notice' i]",
        "[id*='cookie-law' i]",
        "[class*='cookie-law' i]",
        "[id*='cookie-dialog' i]",
        "[class*='cookie-dialog' i]",
        "[id*='cookiebanner' i]",
        "[class*='cookiebanner' i]",
        "[id*='consent-banner' i]",
        "[class*='consent-banner' i]",
        "[id*='consent-modal' i]",
        "[class*='consent-modal' i]",
        "[aria-label*='cookie consent' i]",
        "[aria-label*='cookie notice' i]",
        "[data-testid='cookie-policy-banner']"
    )

    // 2. High-impact Mobile "Open in App" and Smart Banner Promos
    val MOBILE_PROMO_SELECTORS = listOf(
        ".smartbanner",
        "[class*='smart-banner' i]",
        "[class*='smartbanner' i]",
        "[id*='smartbanner' i]",
        ".app-banner",
        ".open-in-app",
        "[data-testid='app-upsell']",
        ".reddit-app-button",
        "[aria-label='Open in app' i]",
        "[aria-label='Use app' i]",
        ".branch-banner",
        ".app-download-banner",
        ".mobile-app-prompt",
        "[class*='app-promo' i]",
        "[class*='app-upsell' i]",
        "[id*='app-banner' i]",
        "[id*='app-promo' i]",
        "reddit-pwa-bottom-sheet",
        "shreddit-async-loader[bundlename='bottom_sheet']",
        ".XPromoPopup",
        "shreddit-experience-tree[banner-type='bottom_sheet']",
        "[data-testid='BottomBar']",
        "[data-testid='sheetDialog']"
    )

    // 3. Known network-level annoyance rule substrings
    private val ANNOYANCE_URL_SUBSTRINGS = listOf(
        "/smart-app-banner",
        "/mobile-download-banner",
        "/mobile-welcome-overlay",
        "-cookie-banner",
        "-cookie-consent",
        "-cookie-dialog",
        "-cookie-popup",
        "-cookie-notice",
        "/cookie-law-popup",
        "onetrust.com/consent",
        "cookiebot.com/uc.js",
        "consent.trustarc.com"
    )

    /**
     * Checks if a network request targets known cookie banner or mobile app nag scripts.
     */
    fun isAnnoyanceRequest(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase()
        return ANNOYANCE_URL_SUBSTRINGS.any { lower.contains(it) }
    }

    /**
     * Generates a combined CSS stylesheet string hiding cookie and/or mobile promo elements,
     * and resetting body scroll locks caused by modal dialogs.
     */
    fun getCombinedCss(blockCookies: Boolean = true, blockPromos: Boolean = true): String {
        val rules = mutableListOf<String>()

        if (blockCookies) {
            val cookieSelector = COOKIE_NOTICE_SELECTORS.joinToString(", ")
            rules.add("$cookieSelector { display: none !important; visibility: hidden !important; opacity: 0 !important; pointer-events: none !important; }")
            // Un-freeze page body scroll locks applied by consent walls without collapsing SPA viewport containers
            rules.add("body.modal-open, body.cookie-modal-open, body.has-modal, body[class*='modal' i], body[class*='cookie' i] { overflow: auto !important; position: static !important; }")
            rules.add(".modal-backdrop, .fade.show:not(.dropdown-menu) { display: none !important; }")
        }

        if (blockPromos) {
            val promoSelector = MOBILE_PROMO_SELECTORS.joinToString(", ")
            rules.add("$promoSelector { display: none !important; visibility: hidden !important; opacity: 0 !important; pointer-events: none !important; }")
        }

        return rules.joinToString("\n")
    }

    /**
     * Generates a safe client-side JavaScript snippet to inject into web pages.
     * Uses a MutationObserver to suppress newly inserted modal dialogs dynamically.
     */
    fun getInjectionScript(blockCookies: Boolean = true, blockPromos: Boolean = true): String {
        val css = getCombinedCss(blockCookies, blockPromos).replace("'", "\\'").replace("\n", " ")
        return """
            (function() {
                'use strict';
                if (window.__musifyAnnoyanceShieldInjected) return;
                window.__musifyAnnoyanceShieldInjected = true;

                function applyStyle() {
                    var style = document.getElementById('musify-annoyance-style');
                    if (!style) {
                        style = document.createElement('style');
                        style.id = 'musify-annoyance-style';
                        style.textContent = '$css';
                        (document.head || document.documentElement).appendChild(style);
                    }
                }

                function unfreezeScroll() {
                    try {
                        var hasConsentModal = document.querySelector('.modal-backdrop, #onetrust-consent-sdk, #CookiebotDialog, #didomi-popup, [id*="cookie" i], [class*="cookie" i]');
                        if (hasConsentModal && document.body) {
                            var overflow = window.getComputedStyle(document.body).overflow;
                            if (overflow === 'hidden') {
                                document.body.style.setProperty('overflow', 'auto', 'important');
                            }
                        }
                    } catch (e) {}
                }

                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', function() {
                        applyStyle();
                        unfreezeScroll();
                    }, { once: true });
                } else {
                    applyStyle();
                    unfreezeScroll();
                }

                try {
                    var debounceTimer = null;
                    var observer = new MutationObserver(function() {
                        if (debounceTimer) return;
                        debounceTimer = setTimeout(function() {
                            debounceTimer = null;
                            unfreezeScroll();
                        }, 250);
                    });
                    observer.observe(document.body || document.documentElement || document, {
                        childList: true,
                        subtree: true
                    });
                    setTimeout(function() {
                        if (observer) observer.disconnect();
                    }, 10000);
                } catch (e) {}
            })();
        """.trimIndent()
    }
}

