/**
 * Musify Browser - WebExtension Background Network Ad Blocker
 *
 * Modeled on Brave Shields webRequest blocking architecture:
 * 1. Intercepts all Spotify ad-logic and tracking network endpoints before they reach the player.
 * 2. Returns a synthetic 200 OK empty JSON `{}` response (via data: URI redirect) to satisfy
 *    Spotify's state machine and eliminate audio/video ad queues without freezing playback.
 * 3. Strictly safeguards Widevine DRM license acquisition endpoints and genuine audio/video streams.
 */

(function() {
    'use strict';

    const AD_AND_TRACKER_ENDPOINTS = [
        // Spotify ad & telemetry endpoints
        'spclient.wg.spotify.com/ad-logic',
        'spclient.wg.spotify.com/ads',
        'spclient.wg.spotify.com/v1/ads',
        'spclient.wg.spotify.com/desktop-ad-service',
        'spclient.wg.spotify.com/commercial-break',
        'spclient.wg.spotify.com/ad-break',
        'spclient.wg.spotify.com/track-playback/v1/commercial',
        'spclient.wg.spotify.com/melody',
        'spclient.wg.spotify.com/metadata/4/ad',
        'audio-ak-spotify-com.akamaized.net',
        // YouTube ad & telemetry endpoints (Brave Shields reverse-engineered)
        'youtube.com/api/stats/ads',
        'youtube.com/pagead',
        'youtube.com/ptracking',
        'youtube.com/youtubei/v1/log_event',
        'googleads.g.doubleclick.net',
        'adservice.google.com',
        'static.doubleclick.net',
        // General trackers
        'scorecardresearch.com',
        'doubleclick.net',
        'google-analytics.com'
    ];

    const MEDIA_LICENSE_SAFEGUARD = [
        'widevine',
        'widevine-license',
        '/license',
        '/drm/',
        'drm-widevine',
        '.mpd',
        '.m3u8',
        'audio-fa.scdn.co',
        'heads-fa.scdn.co',
        'video-fa.scdn.co',
        'audio-ak-p-b.spotifycdn.net',
        'cping.spotify.com',
        // YouTube genuine audio/video playback streams
        'videoplayback',
        'initplayback'
    ];

    function isSafeMediaOrLicense(url) {
        const lower = url.toLowerCase();
        return MEDIA_LICENSE_SAFEGUARD.some(term => lower.includes(term));
    }

    function isAdOrTracker(url) {
        const lower = url.toLowerCase();
        return AD_AND_TRACKER_ENDPOINTS.some(term => lower.includes(term));
    }

    if (typeof browser !== 'undefined' && browser.webRequest && browser.webRequest.onBeforeRequest) {
        browser.webRequest.onBeforeRequest.addListener(
            function(details) {
                const url = details.url;

                // Priority 1: Never intercept Widevine DRM licenses or genuine media streams
                if (isSafeMediaOrLicense(url)) {
                    return {};
                }

                // Priority 2: Intercept ad & tracker endpoints
                if (isAdOrTracker(url)) {
                    const lower = url.toLowerCase();
                    // For JSON ad-logic metadata endpoints, redirect to synthetic empty JSON {}
                    // to prevent player freeze loops
                    if (
                        lower.includes('ad-logic') ||
                        lower.includes('desktop-ad-service') ||
                        lower.includes('commercial-break') ||
                        lower.includes('ad-break') ||
                        lower.includes('v1/ads') ||
                        lower.includes('/ads/')
                    ) {
                        return {
                            redirectUrl: 'data:application/json,%7B%7D'
                        };
                    }

                    // For audio ad CDN streams or trackers, cancel request cleanly
                    return { cancel: true };
                }

                return {};
            },
            {
                urls: ["<all_urls>"]
            },
            ["blocking"]
        );
    }
})();
