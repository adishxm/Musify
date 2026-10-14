/**
 * Musify Browser - Spotify Web Ad Suppressor & Fast-Forward Engine
 *
 * Mutes and skips audio/video ads, auto-advances playback to songs,
 * and clears visual overlay interstitial cards and upsell banners.
 */
(function() {
    'use strict';

    if (window.__musifySpotifyShieldActive) return;
    window.__musifySpotifyShieldActive = true;

    // 1. Inject CSS for permanent ad suppression
    function injectAdHidingCSS() {
        try {
            if (document.getElementById('musify-ad-styles')) return;
            const style = document.createElement('style');
            style.id = 'musify-ad-styles';
            style.textContent = `
                [data-testid="ad-companion-banner"],
                [data-testid="ad-iframe"],
                [data-testid="interstitial-ad"],
                [aria-label="Advertisement"],
                .Root__ads-container,
                div[data-test="upsell-container"],
                #ad-leaderboard,
                #leaderboard,
                .main-leaderboard-container,
                .desktop-interstitial,
                .upsell-banner,
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

    if (document.head || document.documentElement) {
        injectAdHidingCSS();
    } else {
        document.addEventListener('DOMContentLoaded', injectAdHidingCSS);
    }

    // 2. Track & intercept HTMLMediaElements (audio and video)
    const trackedMedia = new Set();
    try {
        const origCreateElement = document.createElement;
        document.createElement = function(tagName, options) {
            const el = origCreateElement.call(document, tagName, options);
            try {
                if (el && tagName && (tagName.toLowerCase() === 'audio' || tagName.toLowerCase() === 'video')) {
                    trackedMedia.add(el);
                    attachMediaListeners(el);
                }
            } catch (_) {}
            return el;
        };
    } catch (_) {}

    function attachMediaListeners(media) {
        ['play', 'playing', 'timeupdate', 'loadedmetadata'].forEach(evt => {
            media.addEventListener(evt, () => processMediaElement(media), true);
        });
    }

    // Find any existing media elements
    function scanExistingMedia() {
        document.querySelectorAll('audio, video').forEach(el => {
            if (!trackedMedia.has(el)) {
                trackedMedia.add(el);
                attachMediaListeners(el);
            }
        });
    }

    // 3. Ad Detection Logic
    function isAdPlaying(media) {
        // A. Check source URL
        const src = (media && media.src) ? media.src.toLowerCase() : "";
        if (src.includes("audio-fa.scdn.co") || src.includes("heads-fa.scdn.co") || src.includes("video-fa.scdn.co") || src.includes("/mp3-ad/")) {
            return true;
        }

        // B. Check standard ad DOM attributes
        if (document.querySelector('[data-testid="advertisement-label"], [aria-label="Advertisement"], [data-testid="ad-companion-banner"], [data-testid="ad-iframe"]')) {
            return true;
        }

        // C. Check Now Playing widget text
        const nowPlaying = document.querySelector('[data-testid="now-playing-widget"]');
        if (nowPlaying) {
            const txt = (nowPlaying.innerText || "").toLowerCase();
            if (txt.includes("advertisement") || txt.includes("spotify premium")) {
                return true;
            }
        }

        // D. Check for screen headers explicitly stating "Advertisement"
        const headings = document.querySelectorAll('h1, h2, h3, header, span, div');
        for (let i = 0; i < headings.length; i++) {
            const el = headings[i];
            if (el.childNodes.length === 1 && (el.textContent || "").trim().toLowerCase() === "advertisement") {
                return true;
            }
        }

        return false;
    }

    // 4. Click next/skip button
    function triggerSkip() {
        const skipButtons = [
            '[data-testid="control-button-skip-forward"]',
            'button[aria-label="Next"]',
            'button[title="Next"]',
            'button[aria-label="Skip forward"]',
            '[data-testid="skip-ad-button"]'
        ];
        for (const selector of skipButtons) {
            const btn = document.querySelector(selector);
            if (btn && !btn.disabled) {
                try {
                    btn.click();
                    return true;
                } catch (_) {}
            }
        }
        return false;
    }

    // 5. Media evaluation: mute, speed up 16x, jump to duration
    function processMediaElement(media) {
        try {
            const adActive = isAdPlaying(media);
            if (adActive) {
                // Immediately silence ad audio
                media.muted = true;
                media.volume = 0;
                media.playbackRate = 16.0;

                // Jump directly to track end
                if (media.duration && !isNaN(media.duration) && isFinite(media.duration) && media.duration > 0) {
                    media.currentTime = media.duration;
                } else {
                    media.currentTime = 9999;
                }

                // Advance track
                triggerSkip();
            } else {
                // Restore regular song playback settings
                if (media.playbackRate > 1.0) {
                    media.playbackRate = 1.0;
                }
                if (media.muted) {
                    media.muted = false;
                    media.volume = 1.0;
                }
            }
        } catch (_) {}
    }

    // 6. Suppress Fullscreen Interstitial Card & Modals
    function suppressAdCards() {
        // Dismiss promo dialogs / modals
        const modals = document.querySelectorAll('[data-testid="modal-container"], div[role="dialog"]');
        modals.forEach(modal => {
            const text = (modal.innerText || "").toLowerCase();
            if (text.includes("advertisement") || text.includes("spotify premium") || text.includes("get premium") || text.includes("explore even more music")) {
                const closeBtn = modal.querySelector('button[aria-label="Close"], button[aria-label="Dismiss"], button[title="Close"]');
                if (closeBtn) {
                    try { closeBtn.click(); } catch (_) {}
                } else {
                    modal.style.setProperty('display', 'none', 'important');
                }
            }
        });

        // Hide full-screen / in-page interstitial overlay card (as seen in screenshot)
        const allTextNodes = document.querySelectorAll('h1, h2, h3, p, span, div');
        allTextNodes.forEach(el => {
            if (el.childNodes.length === 1 && (el.textContent || "").trim().toLowerCase() === "advertisement") {
                let cur = el;
                for (let depth = 0; depth < 6; depth++) {
                    if (!cur || cur === document.body || cur === document.documentElement) break;
                    const cardText = (cur.innerText || "").toLowerCase();
                    if (cardText.includes("explore even more music") || cardText.includes("without audio ads") || cardText.includes("learn more")) {
                        cur.style.setProperty('display', 'none', 'important');
                        break;
                    }
                    cur = cur.parentElement;
                }
            }
        });
    }

    // 7. Suppress "Open App" / "Open in Spotify" Buttons and Dialogs
    function suppressOpenAppPrompts() {
        try {
            // A. Hide any buttons or links whose text or aria is specifically "Open App", "Install App", or "Get the app"
            const clickables = document.querySelectorAll('button, a, div[role="button"]');
            for (let i = 0; i < clickables.length; i++) {
                const el = clickables[i];
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

            // B. Auto-dismiss modal dialogs prompting "Open in App" or "Install"
            const dialogs = document.querySelectorAll('div[role="dialog"], [data-testid="modal-container"]');
            dialogs.forEach(dialog => {
                const txt = (dialog.innerText || '').toLowerCase();
                if (txt.includes('open app') || txt.includes('switch to the app') || txt.includes('listen in the spotify app') || txt.includes('install app')) {
                    const closeBtn = dialog.querySelector('button[aria-label="Close"], button[aria-label="Dismiss"], button[title="Close"]');
                    if (closeBtn) {
                        try { closeBtn.click(); } catch (_) {}
                    } else {
                        dialog.style.setProperty('display', 'none', 'important');
                    }
                }
            });
        } catch (_) {}
    }

        // 8. Prevent Link Clicks / Deep Links to External Spotify App or Downloads
        try {
            document.addEventListener('click', function(e) {
                try {
                    const target = e.target && e.target.closest ? e.target.closest('a, button, div[role="button"]') : null;
                    if (!target) return;
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

        // 9. Protect window.open from launching spotify: / intent: schemes
        try {
            const origWindowOpen = window.open;
            window.open = function(url, ...args) {
                if (typeof url === 'string') {
                    const low = url.toLowerCase();
                    if (low.startsWith('spotify:') || low.startsWith('intent:') || low.includes('spotify.link') || low.includes('play.google.com/store/apps/details?id=com.spotify.music')) {
                        return null;
                    }
                }
                return origWindowOpen ? origWindowOpen.call(window, url, ...args) : null;
            };
        } catch (_) {}

        // 10. Core Loop: Process media, cards, open-app elements continuously
        function tick() {
            scanExistingMedia();
            trackedMedia.forEach(media => processMediaElement(media));
            suppressAdCards();
            suppressOpenAppPrompts();
        }

    // Execute immediately and periodically
    setInterval(tick, 250);

    // Also observe DOM changes
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
