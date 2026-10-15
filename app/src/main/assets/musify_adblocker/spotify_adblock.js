/**
 * Musify Browser - Brave-Aligned Spotify Web Content Shield & Ad Blocker
 *
 * Modeled on Brave Shields & adblock-rust cosmetic/scriptlet architecture:
 * 1. Declarative CSS element-hiding for all known Spotify visual ads, leaderboards,
 *    companion banners, sponsored playlists, and upsell containers.
 * 2. Non-destructive modal and promotional nag dismissal.
 * 3. App-nag suppression and intent sandboxing (spotify:, intent:, spotify.link).
 * 4. Strictly preserves HTMLMediaElement, Widevine DRM CDM, and media clock integrity
 *    (zero currentTime/playbackRate/volume tampering).
 */
(function() {
    'use strict';

    if (window.__musifySpotifyShieldActive) return;
    window.__musifySpotifyShieldActive = true;

    // Safety flag: Media tampering is strictly disabled to preserve Widevine EME & hardware audio
    window.__musifyMediaTamperingEnabled = false;

    // 1. Brave-Aligned Cosmetic Ad Hiding Stylesheet
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

    // Inject immediately at document start, fallback on DOMContentLoaded
    if (document.head || document.documentElement) {
        injectBraveAdHidingCSS();
    } else {
        document.addEventListener('DOMContentLoaded', injectBraveAdHidingCSS);
    }

    // 2. Safe Modal Dialog Dismissal (Non-destructive)
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

    // 3. Safe App-Nag Prompts Suppression
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

    // 4. Safe Deep-Link Navigation Blocker (Keeps user in Musify)
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

    // 5. Protect window.open from launching external spotify: / intent: schemes
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

    // 6. Non-Destructive Periodic Sanitation Loop
    function tick() {
        injectBraveAdHidingCSS();
        dismissAdModals();
        suppressOpenAppPrompts();
    }

    // Execute periodically and via MutationObserver
    setInterval(tick, 500);

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
