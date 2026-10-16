/**
 * Musify Browser - Brave-Aligned Spotify Web Content Shield & Ad Bypass Engine
 *
 * Modeled on Brave Shields, uBlock Origin & Spicetify adblock architecture:
 * 1. Main-World fetch & XMLHttpRequest interception: neutralizes Spotify ad-logic and
 *    commercial break queries with synthetic empty JSON responses to prevent ad queueing
 *    and eliminate playback freeze loops.
 * 2. Non-destructive Commercial Break Advancer: detects ad break overlays and automatically
 *    triggers player progression without modifying HTMLMediaElement clock or muting properties.
 * 3. Complete cosmetic element-hiding for all desktop and mobile ad cards, banners, and overlays.
 * 4. Deep-link sandboxing suppressing external app-nag intent schemes.
 * 5. Strictly preserves Widevine DRM CDM license acquisition and genuine media streams
 *    (zero currentTime, playbackRate, or volume tampering).
 */
(function() {
    'use strict';

    if (window.__musifySpotifyShieldActive) return;
    window.__musifySpotifyShieldActive = true;

    // Safety flag: Media tampering is strictly disabled to preserve Widevine EME & hardware audio
    window.__musifyMediaTamperingEnabled = false;

    // 1. Inject Main-World Fetch & XHR Interceptor
    function injectMainWorldHooks() {
        try {
            if (document.getElementById('musify-main-world-hooks')) return;
            const script = document.createElement('script');
            script.id = 'musify-main-world-hooks';
            script.textContent = `(${function() {
                // Hook window.fetch
                const origFetch = window.fetch;
                if (origFetch) {
                    window.fetch = async function(resource, init) {
                        try {
                            const url = (typeof resource === 'string' ? resource : (resource && resource.url) || '').toString().toLowerCase();
                            // Safeguard: Never intercept Widevine DRM licenses or real media streams
                            if (url.includes('widevine') || url.includes('/license') || url.includes('.mpd') || url.includes('.m3u8')) {
                                return origFetch.apply(this, arguments);
                            }
                            if (
                                url.includes('spclient.wg.spotify.com/ad-logic') ||
                                url.includes('spclient.wg.spotify.com/ads') ||
                                url.includes('spclient.wg.spotify.com/v1/ads') ||
                                url.includes('spclient.wg.spotify.com/desktop-ad-service') ||
                                url.includes('spclient.wg.spotify.com/commercial-break') ||
                                url.includes('spclient.wg.spotify.com/ad-break') ||
                                url.includes('spclient.wg.spotify.com/track-playback/v1/commercial') ||
                                url.includes('spclient.wg.spotify.com/melody') ||
                                url.includes('doubleclick.net') ||
                                url.includes('scorecardresearch.com')
                            ) {
                                return new Response('{}', {
                                    status: 200,
                                    headers: { 'Content-Type': 'application/json' }
                                });
                            }
                        } catch (_) {}
                        return origFetch.apply(this, arguments);
                    };
                }

                // Hook XMLHttpRequest
                const origOpen = XMLHttpRequest.prototype.open;
                const origSend = XMLHttpRequest.prototype.send;
                XMLHttpRequest.prototype.open = function(method, url) {
                    try {
                        this.__musifyUrl = (url || '').toString().toLowerCase();
                    } catch (_) {}
                    return origOpen.apply(this, arguments);
                };
                XMLHttpRequest.prototype.send = function() {
                    try {
                        const url = this.__musifyUrl || '';
                        if (
                            !url.includes('widevine') && !url.includes('/license') && (
                                url.includes('ad-logic') ||
                                url.includes('/ads/') ||
                                url.includes('desktop-ad-service') ||
                                url.includes('commercial-break') ||
                                url.includes('ad-break')
                            )
                        ) {
                            Object.defineProperty(this, 'status', { value: 200, writable: false });
                            Object.defineProperty(this, 'statusText', { value: 'OK', writable: false });
                            Object.defineProperty(this, 'responseText', { value: '{}', writable: false });
                            Object.defineProperty(this, 'response', { value: '{}', writable: false });
                            Object.defineProperty(this, 'readyState', { value: 4, writable: false });
                            this.dispatchEvent(new Event('readystatechange'));
                            this.dispatchEvent(new Event('load'));
                            return;
                        }
                    } catch (_) {}
                    return origSend.apply(this, arguments);
                };
            }})();`;
            const target = document.head || document.documentElement;
            if (target) {
                target.appendChild(script);
            }
        } catch (_) {}
    }

    // 2. Brave-Aligned Cosmetic Ad Hiding Stylesheet
    function injectBraveAdHidingCSS() {
        try {
            if (document.getElementById('musify-brave-ad-styles')) return;
            const style = document.createElement('style');
            style.id = 'musify-brave-ad-styles';
            style.textContent = `
                /* Visual advertisement containers & companion banners */
                [data-testid="ad-companion-banner"],
                [data-testid="ad-iframe"],
                [data-testid="interstitial-ad"],
                [aria-label="Advertisement"],
                .Root__ads-container,
                div[data-testid="advertisement-row"],
                aside[aria-label="Upgrade to Premium"],
                [class*="AdsContainer"],
                [aria-label="Sponsored"],
                div[data-test="upsell-container"],
                #ad-leaderboard,
                #leaderboard,
                .main-leaderboard-container,
                .desktop-interstitial,
                .upsell-banner,
                [data-testid="track-info-advertiser"],
                .advertisement,
                .sponsored-playlist,
                div[data-testid="top-banner-ad"],
                div[data-testid="in-feed-ad"],
                div[data-testid="tracklist-row-ad"],
                div[data-testid="billboard-ad"],
                div[data-testid="leaderboard-ad"],
                div[data-testid="mpu-ad"],
                div[data-testid="sponsored-header"],
                div[data-testid="sponsored-card"],
                div[data-testid="sponsored-title"],
                div[data-testid="sponsored-recommendation"],
                div[data-testid="now-playing-widget"] [aria-label*="Advertisement"],
                section[aria-label="Sponsored Content"],

                /* "Open App" and "Install App" smart banners & buttons */
                [data-testid="open-app-button"],
                [data-testid="install-app-button"],
                [data-testid="smart-banner-pitch"],
                [data-testid="banner-open-button"],
                [data-testid="mobile-app-banner"],
                [data-testid="smart-banner"],
                [data-testid*="open-app"],
                [data-testid*="install-app"],
                [aria-label="Open App"],
                [aria-label="Open in app"],
                [aria-label="Open in Spotify"],
                [aria-label="Install App"],
                [aria-label*="Install the app"],
                [aria-label*="Install Spotify"],
                a[href^="spotify:"],
                a[href*="spotify.link"],
                a[href*="spotify.com/download"] {
                    display: none !important;
                    visibility: hidden !important;
                    opacity: 0 !important;
                    pointer-events: none !important;
                    height: 0 !important;
                    width: 0 !important;
                    max-height: 0 !important;
                    overflow: hidden !important;
                }
            `;
            const target = document.head || document.documentElement;
            if (target) {
                target.appendChild(style);
            }
        } catch (_) {}
    }

    // 3. Non-Destructive Commercial Break Auto-Advancer (Preserves Widevine EME)
    function advanceCommercialBreaks() {
        try {
            const title = (document.title || '').toLowerCase();
            const isTitleAd = title.includes('advertisement') || title === 'spotify';

            const advertiserEl = document.querySelector(
                '[data-testid="track-info-advertiser"], ' +
                '[data-testid="now-playing-widget"] [aria-label*="Advertisement"], ' +
                '[aria-label="Advertisement"], ' +
                '.advertisement'
            );

            const nowPlayingText = (
                document.querySelector('[data-testid="context-item-info-title"]')?.textContent ||
                document.querySelector('[data-testid="now-playing-widget"]')?.textContent ||
                document.querySelector('.now-playing')?.textContent ||
                ''
            ).toLowerCase();

            const isNowPlayingAd = nowPlayingText.includes('advertisement');
            const isAdActive = isTitleAd || Boolean(advertiserEl) || isNowPlayingAd;

            if (isAdActive) {
                // Safely click next/skip if control button exists
                const skipBtn = document.querySelector(
                    'button[data-testid="control-button-skip-forward"], ' +
                    'button[aria-label="Next"], ' +
                    'button[aria-label="Skip forward"]'
                );
                if (skipBtn && !skipBtn.disabled) {
                    try { skipBtn.click(); } catch (_) {}
                }
            }
        } catch (_) {}
    }

    // 4. Safe Modal Dialog Dismissal (Non-destructive)
    function dismissAdModals() {
        try {
            const modals = document.querySelectorAll('[data-testid="modal-container"], div[role="dialog"]');
            modals.forEach(modal => {
                const text = (modal.innerText || '').toLowerCase();
                if (
                    text.includes('advertisement') ||
                    text.includes('spotify premium') ||
                    text.includes('get premium') ||
                    text.includes('open app') ||
                    text.includes('switch to the app') ||
                    text.includes('listen in the spotify app') ||
                    text.includes('install app')
                ) {
                    const closeBtn = modal.querySelector('button[aria-label="Close"], button[aria-label="Dismiss"], button[title="Close"]');
                    if (closeBtn) {
                        try { closeBtn.click(); } catch (_) {}
                    } else {
                        modal.style.setProperty('display', 'none', 'important');
                    }
                }
            });
        } catch (_) {}
    }

    // 5. Safe App-Nag Prompts Suppression
    function suppressOpenAppPrompts() {
        try {
            const clickables = document.querySelectorAll('button, a, div[role="button"]');
            for (let i = 0; i < clickables.length; i++) {
                const el = clickables[i];
                // Never touch playback controls
                const testId = el.getAttribute('data-testid') || '';
                if (testId.includes('control') || testId.includes('play') || testId.includes('pause') || testId.includes('skip')) {
                    continue;
                }

                const text = (el.textContent || '').trim().toLowerCase();
                const aria = (el.getAttribute('aria-label') || '').toLowerCase();
                if (
                    text === 'open app' ||
                    text === 'open in app' ||
                    text === 'open in spotify' ||
                    text === 'install app' ||
                    text === 'install spotify' ||
                    text === 'get the app' ||
                    text === 'get app' ||
                    text === 'switch to the app' ||
                    aria === 'open app' ||
                    aria === 'open in app' ||
                    aria === 'install app' ||
                    aria.includes('install app')
                ) {
                    el.style.setProperty('display', 'none', 'important');
                    el.style.setProperty('visibility', 'hidden', 'important');
                    el.style.setProperty('pointer-events', 'none', 'important');
                }
            }
        } catch (_) {}
    }

    // 6. Safe Deep-Link Navigation Blocker (Keeps user in Musify)
    try {
        document.addEventListener('click', function(e) {
            try {
                const target = e.target && e.target.closest ? e.target.closest('a, button, div[role="button"]') : null;
                if (!target) return;

                // Protect audio playback controls from being intercepted
                const testId = target.getAttribute('data-testid') || '';
                if (testId.includes('control') || testId.includes('play') || testId.includes('pause') || testId.includes('skip')) {
                    return;
                }

                const href = (target.getAttribute('href') || target.href || '').toString().toLowerCase();
                const text = (target.textContent || '').trim().toLowerCase();
                const aria = (target.getAttribute('aria-label') || '').toLowerCase();

                if (
                    href.startsWith('spotify:') ||
                    href.startsWith('intent:') ||
                    href.includes('spotify.link') ||
                    href.includes('play.google.com/store/apps/details?id=com.spotify.music') ||
                    href.includes('spotify.com/download') ||
                    text === 'open app' ||
                    text === 'open in app' ||
                    text === 'open in spotify' ||
                    text === 'install app' ||
                    text === 'install spotify' ||
                    text === 'get the app' ||
                    text === 'get app' ||
                    aria.includes('open app') ||
                    aria.includes('open in app') ||
                    aria.includes('install app')
                ) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    return false;
                }
            } catch (_) {}
        }, true);
    } catch (_) {}

    // 7. Protect window.open from launching external spotify: / intent: schemes
    try {
        const origWindowOpen = window.open;
        window.open = function(url, ...args) {
            if (typeof url === 'string') {
                const low = url.toLowerCase();
                if (
                    low.startsWith('spotify:') ||
                    low.startsWith('intent:') ||
                    low.includes('spotify.link') ||
                    low.includes('play.google.com/store/apps/details?id=com.spotify.music')
                ) {
                    return null;
                }
            }
            return origWindowOpen ? origWindowOpen.call(window, url, ...args) : null;
        };
    } catch (_) {}

    // 8. Execution loop & Lifecycle hooks
    function tick() {
        injectMainWorldHooks();
        injectBraveAdHidingCSS();
        advanceCommercialBreaks();
        dismissAdModals();
        suppressOpenAppPrompts();
    }

    // Run immediately
    tick();

    // Fast-cycle polling for ad transitions
    setInterval(tick, 300);

    const observer = new MutationObserver(() => {
        tick();
    });

    const initObserver = () => {
        const root = document.body || document.documentElement;
        if (root) {
            observer.observe(root, { childList: true, subtree: true });
        }
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initObserver);
    } else {
        initObserver();
    }
})();
