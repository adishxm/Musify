/**
 * Musify Browser - WebExtension Content Script for Cookie Notice & Mobile Annoyance Shielding
 *
 * Reverse-engineered from Brave Browser:
 * - Cookie Notice Blocker (`cdbbhgbmjhfnhnmgeddbliobbofkgdhe`)
 * - Mobile App Promo Blocker (`bfpgedeaaibpoidldhjcknekahbikncb`)
 */

(function() {
    'use strict';
    if (window.__musifyAnnoyanceShieldInjected) return;
    window.__musifyAnnoyanceShieldInjected = true;

    const COOKIE_SELECTORS = [
        '#onetrust-consent-sdk',
        '#onetrust-banner-sdk',
        '#onetrust-pc-sdk',
        '.onetrust-pc-dark-filter',
        '#CookiebotWidget',
        '#CookiebotDialog',
        '#cookiebanner',
        '#didomi-host',
        '#didomi-popup',
        '.didomi-popup-container',
        '#truste-consent-track',
        '.truste_box_overlay',
        '.truste_overlay',
        '#consent_blackbar',
        '.qc-cmp2-container',
        '.qc-cmp-ui-container',
        '.qc-cmp2-ui',
        '#usercentrics-root',
        '.cmplz-cookiebanner',
        '.cmplz-soft-cookiewall',
        '[id*="cookie-banner" i]',
        '[class*="cookie-banner" i]',
        '[id*="cookie-consent" i]',
        '[class*="cookie-consent" i]',
        '[id*="cookie-notice" i]',
        '[class*="cookie-notice" i]',
        '[id*="cookie-law" i]',
        '[class*="cookie-law" i]',
        '[id*="cookie-dialog" i]',
        '[class*="cookie-dialog" i]',
        '[id*="cookiebanner" i]',
        '[class*="cookiebanner" i]',
        '[id*="consent-banner" i]',
        '[class*="consent-banner" i]',
        '[id*="consent-modal" i]',
        '[class*="consent-modal" i]',
        '[aria-label*="cookie consent" i]',
        '[aria-label*="cookie notice" i]',
        '[data-testid="cookie-policy-banner"]'
    ];

    const PROMO_SELECTORS = [
        '.smartbanner',
        '[class*="smart-banner" i]',
        '[class*="smartbanner" i]',
        '[id*="smartbanner" i]',
        '.app-banner',
        '.open-in-app',
        '[data-testid="app-upsell"]',
        '.reddit-app-button',
        '[aria-label="Open in app" i]',
        '[aria-label="Use app" i]',
        '.branch-banner',
        '.app-download-banner',
        '.mobile-app-prompt',
        '[class*="app-promo" i]',
        '[class*="app-upsell" i]',
        '[id*="app-banner" i]',
        '[id*="app-promo" i]',
        'reddit-pwa-bottom-sheet',
        'shreddit-async-loader[bundlename="bottom_sheet"]',
        '.XPromoPopup',
        'shreddit-experience-tree[banner-type="bottom_sheet"]',
        '[data-testid="BottomBar"]',
        '[data-testid="sheetDialog"]'
    ];

    const COMBINED_CSS = COOKIE_SELECTORS.concat(PROMO_SELECTORS).join(', ') +
        ' { display: none !important; visibility: hidden !important; opacity: 0 !important; pointer-events: none !important; }\n' +
        'html, body { overflow: auto !important; position: static !important; }\n' +
        '.modal-backdrop, .fade.show:not(.dropdown-menu) { display: none !important; }';

    function injectCss() {
        if (!document.getElementById('musify-annoyance-style')) {
            const style = document.createElement('style');
            style.id = 'musify-annoyance-style';
            style.textContent = COMBINED_CSS;
            (document.head || document.documentElement).appendChild(style);
        }
    }

    function unfreeze() {
        try {
            if (document.body && window.getComputedStyle(document.body).overflow === 'hidden') {
                document.body.style.setProperty('overflow', 'auto', 'important');
            }
            if (document.documentElement && window.getComputedStyle(document.documentElement).overflow === 'hidden') {
                document.documentElement.style.setProperty('overflow', 'auto', 'important');
            }
        } catch (e) {}
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            injectCss();
            unfreeze();
        }, { once: true });
    } else {
        injectCss();
        unfreeze();
    }

    try {
        const observer = new MutationObserver(() => {
            injectCss();
            unfreeze();
        });
        observer.observe(document.documentElement || document, { childList: true, subtree: true });
        setTimeout(() => { if (observer) observer.disconnect(); }, 15000);
    } catch (e) {}
})();

