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
                .upsell-banner {
                    display: none !important;
                    visibility: hidden !important;
                    opacity: 0 !important;
                    pointer-events: none !important;
                    height: 0 !important;
                    width: 0 !important;
                    max-height: 0 !important;
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

    // 7. Core Loop: Process media, cards, and buttons continuously
    function tick() {
        scanExistingMedia();
        trackedMedia.forEach(media => processMediaElement(media));
        suppressAdCards();
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
