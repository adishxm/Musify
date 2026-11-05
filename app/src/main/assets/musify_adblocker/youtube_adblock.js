/**
 * Musify Browser - Brave-Aligned YouTube & YouTube Music Ad Shield & Background Playback Engine
 *
 * Reverse-Engineered from Brave Browser Shields (brave-yt-sabr-fix, brave-video-bg-play, json-prune, set):
 * 1. Main-World Initial Player Response Sanitization:
 *    - Traps and strips `ytInitialPlayerResponse.playerAds`, `adPlacements`, and `adSlots`.
 *    - Strips `playerResponse.adPlacements`, `playerResponse.playerAds`, and `adSlots`.
 * 2. Main-World Fetch & XHR Interception:
 *    - Sanitizes `/youtubei/v1/player`, `/player`, and `reel_watch_sequence` responses to eliminate ad queues.
 * 3. Element Hiding Cosmetic CSS:
 *    - Injects Brave cosmetic filter selectors hiding video overlay ads, promo slots, and Premium nag dialogs.
 * 4. Background Playback Preservation & Anti-Pause:
 *    - Spoofs Page Visibility API (`hidden = false`, `visibilityState = 'visible'`).
 *    - Suppresses `visibilitychange` bubbling to prevent background video pauses.
 *    - Periodically updates `window._lact` (user activity timestamp) every 5 minutes to prevent idle pause dialogs.
 * 5. Media-Safe Invariant:
 *    - Strictly preserves genuine audio/video streams (`googlevideo.com/videoplayback`).
 */

(function() {
    'use strict';

    if (window.__musifyYouTubeShieldActive) return;
    window.__musifyYouTubeShieldActive = true;

    // 1. Inject Main-World Hooks for Player Response Sanitization & Visibility Spoofing
    function injectMainWorldHooks() {
        try {
            if (document.getElementById('musify-yt-main-world-hooks')) return;
            const script = document.createElement('script');
            script.id = 'musify-yt-main-world-hooks';
            script.textContent = `(${function() {
                // A. Background Playback Spoofing (Brave brave-video-bg-play.js)
                try {
                    Object.defineProperties(document, {
                        'hidden': { value: false, writable: false },
                        'visibilityState': { value: 'visible', writable: false }
                    });
                    window.addEventListener('visibilitychange', function(e) {
                        e.stopImmediatePropagation();
                    }, true);

                    // User activity tracking keep-alive (_lact)
                    setInterval(function() {
                        try {
                            if (typeof window !== 'undefined') {
                                window._lact = Date.now();
                            }
                        } catch (_) {}
                    }, 5 * 60 * 1000);
                } catch (_) {}

                // B. Initial Player Response Property Trap (Brave set.js)
                function sanitizePlayerResponse(obj) {
                    if (!obj || typeof obj !== 'object') return obj;
                    try {
                        if (obj.playerAds) delete obj.playerAds;
                        if (obj.adPlacements) delete obj.adPlacements;
                        if (obj.adSlots) delete obj.adSlots;
                        if (obj.adBreakHeartbeatParams) delete obj.adBreakHeartbeatParams;
                        if (obj.playerResponse) {
                            sanitizePlayerResponse(obj.playerResponse);
                        }
                    } catch (_) {}
                    return obj;
                }

                try {
                    let _ytInitialPlayerResponse = window.ytInitialPlayerResponse;
                    Object.defineProperty(window, 'ytInitialPlayerResponse', {
                        configurable: true,
                        enumerable: true,
                        get: function() {
                            return _ytInitialPlayerResponse;
                        },
                        set: function(val) {
                            _ytInitialPlayerResponse = sanitizePlayerResponse(val);
                        }
                    });
                    if (window.ytInitialPlayerResponse) {
                        sanitizePlayerResponse(window.ytInitialPlayerResponse);
                    }
                } catch (_) {}

                // C. Hook JSON.parse for YouTube player payloads (Brave json-prune.js)
                const origJsonParse = JSON.parse;
                JSON.parse = function(text, reviver) {
                    const parsed = origJsonParse.apply(this, arguments);
                    if (parsed && typeof parsed === 'object') {
                        if (parsed.adPlacements || parsed.playerAds || parsed.adSlots) {
                            sanitizePlayerResponse(parsed);
                        }
                    }
                    return parsed;
                };

                // D. Hook window.fetch for player and watch requests
                const origFetch = window.fetch;
                if (origFetch) {
                    window.fetch = async function(resource, init) {
                        const url = (typeof resource === 'string' ? resource : (resource && resource.url) || '').toString();
                        const isPlayerReq = url.includes('/youtubei/v1/player') || url.includes('/player?') || url.includes('/reel_watch_sequence');
                        
                        const response = await origFetch.apply(this, arguments);
                        if (isPlayerReq && response.ok) {
                            try {
                                const clone = response.clone();
                                const data = await clone.json();
                                sanitizePlayerResponse(data);
                                return new Response(JSON.stringify(data), {
                                    status: response.status,
                                    statusText: response.statusText,
                                    headers: response.headers
                                });
                            } catch (_) {}
                        }
                        return response;
                    };
                }

                // E. Hook XMLHttpRequest for player queries
                const origOpen = XMLHttpRequest.prototype.open;
                const origSend = XMLHttpRequest.prototype.send;
                XMLHttpRequest.prototype.open = function(method, url) {
                    this.__musifyYtUrl = (url || '').toString();
                    return origOpen.apply(this, arguments);
                };
                XMLHttpRequest.prototype.send = function() {
                    const xhr = this;
                    const url = xhr.__musifyYtUrl || '';
                    if (url.includes('/youtubei/v1/player') || url.includes('/player?') || url.includes('/reel_watch_sequence')) {
                        xhr.addEventListener('readystatechange', function() {
                            if (xhr.readyState === 4 && xhr.status === 200) {
                                try {
                                    const parsed = origJsonParse(xhr.responseText);
                                    sanitizePlayerResponse(parsed);
                                    Object.defineProperty(xhr, 'responseText', {
                                        configurable: true,
                                        value: JSON.stringify(parsed)
                                    });
                                    Object.defineProperty(xhr, 'response', {
                                        configurable: true,
                                        value: JSON.stringify(parsed)
                                    });
                                } catch (_) {}
                            }
                        });
                    }
                    return origSend.apply(this, arguments);
                };
            }})();`;
            (document.head || document.documentElement).appendChild(script);
            script.remove();
        } catch (_) {}
    }

    // 2. Inject Cosmetic Element-Hiding CSS (Brave Default & First-Party Filter Lists)
    function injectCosmeticStyles() {
        try {
            if (document.getElementById('musify-yt-cosmetic-shield')) return;
            const style = document.createElement('style');
            style.id = 'musify-yt-cosmetic-shield';
            style.textContent = `
                /* Video Player Ad Elements */
                .video-ads,
                .ytp-ad-module,
                .ytp-ad-overlay-container,
                .ytp-ad-player-overlay,
                .ytp-ad-player-overlay-flyout-cta,
                .ytp-ad-action-interstitial,
                .ytp-ad-message-container,
                .ytp-ad-preview-container,
                .ytp-ad-skip-button-container,
                .ytp-suggested-action-badge[aria-label*="Ad"],
                /* YouTube Feed & Layout Ads */
                ytd-ad-slot-renderer,
                ytd-in-feed-ad-layout-renderer,
                ytd-banner-promo-renderer,
                ytd-statement-banner-renderer,
                ytd-promoted-sparkles-web-renderer,
                ytd-promoted-video-renderer,
                ytd-player-legacy-desktop-watch-ads-renderer,
                /* Mobile Web YouTube Ads */
                .ad-container,
                .ad-display,
                ytm-promoted-sparkles-web-renderer,
                ytm-companion-ad-renderer,
                /* YouTube Music Ads & Upgrade Banners */
                ytmusic-popup-container:has(#options.ytd-checkbox-survey-renderer),
                ytmusic-mealbar-promo-renderer,
                ytmusic-banner-promo-renderer,
                tp-yt-paper-dialog.ytmusic-popup-container {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0 !important;
                    width: 0 !important;
                    pointer-events: none !important;
                }
            `;
            (document.head || document.documentElement).appendChild(style);
        } catch (_) {}
    }

    // 3. Auto-Dismiss Nag & Survey Dialogs
    function setupNagDismissal() {
        const observer = new MutationObserver(function() {
            try {
                // Auto-click Skip Ad button if it appears
                const skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .videoAdUiSkipButton');
                if (skipBtn) {
                    skipBtn.click();
                }

                // Dismiss "Try YouTube Music / Premium" nag dialogs
                const dialogs = document.querySelectorAll('tp-yt-paper-dialog, ytmusic-popup-container');
                dialogs.forEach(function(dialog) {
                    const text = dialog.textContent || '';
                    if (
                        text.includes('Premium') ||
                        text.includes('without the ads') ||
                        text.includes('family plan') ||
                        text.includes('Free trial') ||
                        text.includes('Get background play')
                    ) {
                        const dismissBtn = dialog.querySelector('button[aria-label="No thanks"], button[aria-label="Dismiss"], yt-button-renderer#dismiss-button');
                        if (dismissBtn) {
                            dismissBtn.click();
                        } else {
                            dialog.remove();
                        }
                    }
                });
            } catch (_) {}
        });

        observer.observe(document.documentElement, {
            childList: true,
            subtree: true
        });
    }

    // Initialize immediately at document_start
    injectMainWorldHooks();
    injectCosmeticStyles();
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            injectCosmeticStyles();
            setupNagDismissal();
        });
    } else {
        setupNagDismissal();
    }
})();
