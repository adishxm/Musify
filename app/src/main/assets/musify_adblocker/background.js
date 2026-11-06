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
        'google-analytics.com',
        // Ad SDKs & Anti-Adblock Defusers (Brave Shields reverse-engineered)
        'googletagservices.com',
        'pagead2.googlesyndication.com',
        'amazon-adsystem.com',
        'fuckadblock',
        'blockadblock',
        'prebid-ads.js',
        'canrunads.js',
        // VAST & VMAP video ad providers
        'pubads.g.doubleclick.net/gampad/ads',
        'securepubads.g.doubleclick.net/gampad/ads',
        'imasdk.googleapis.com',
        'innovid.com/v1/endpoints/vast',
        'springserve.com/vast',
        'spotxchange.com/vast',
        'tremorhub.com/live/vast'
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
                    // For DoubleClick instream ad status, return window.google_ad_status = 1;
                    if (lower.includes('doubleclick.net/instream/ad_status') || lower.includes('google_ad_status.js')) {
                        return {
                            redirectUrl: 'data:application/javascript,window.google_ad_status%20%3D%201%3B'
                        };
                    }

                    // For VAST/VMAP video advertising tags, return synthetic empty XML (Brave noop-vast3 / noop-vmap1)
                    if (lower.includes('/vmap') || lower.includes('output=vmap')) {
                        return {
                            redirectUrl: 'data:text/xml,%3Cvmap%3AVMAP%20xmlns%3Avmap%3D%22http%3A%2F%2Fwww.iab.net%2Fvideosuite%2Fvmap%22%20version%3D%221.0%22%3E%3C%2Fvmap%3AVMAP%3E'
                        };
                    }
                    if (lower.includes('/vast') || lower.includes('output=vast') || lower.includes('output=xml_vast') || lower.includes('format=vast') || lower.includes('env=vp')) {
                        return {
                            redirectUrl: 'data:text/xml,%3CVAST%20version%3D%223.0%22%3E%3C%2FVAST%3E'
                        };
                    }

                    // For Google Publisher Tag (GPT), redirect to synthetic stub that executes cmd.push callbacks
                    if (lower.includes('gpt.js') || lower.includes('googletagservices.com')) {
                        return {
                            redirectUrl: 'data:application/javascript,(function()%7Bwindow.googletag%3Dwindow.googletag%7C%7C%7B%7D%3Bgoogletag.cmd%3Dgoogletag.cmd%7C%7C%5B%5D%3Bgoogletag.cmd.push%3Dfunction(fn)%7Bif(typeof%20fn%3D%3D%3D%27function%27)try%7Bfn()%7Dcatch(e)%7B%7Dreturn%201%3B%7D%3Bgoogletag.pubadsReady%3Dtrue%3Bgoogletag.display%3Dfunction()%7B%7D%3Bgoogletag.enableServices%3Dfunction()%7B%7D%3Bgoogletag.defineSlot%3Dfunction()%7Breturn%7BaddService%3Afunction()%7Breturn%20this%7D%2CsetTargeting%3Afunction()%7Breturn%20this%7D%7D%7D%3B%7D)()'
                        };
                    }

                    // For Google AdSense, redirect to synthetic loaded object
                    if (lower.includes('adsbygoogle')) {
                        return {
                            redirectUrl: 'data:application/javascript,(function()%7Bself.adsbygoogle%3Dself.adsbygoogle%7C%7C%7Bloaded%3Atrue%2Cpush%3Afunction()%7B%7D%7D%3B%7D)()'
                        };
                    }

                    // For FuckAdBlock / BlockAdBlock, redirect to defuser
                    if (lower.includes('fuckadblock') || lower.includes('nofab.js')) {
                        return {
                            redirectUrl: 'data:application/javascript,(function()%7Bvar%20f%3Dfunction()%7B%7D%3Bf.prototype%3D%7Bon%3Afunction(d%2Ccb)%7Bif(!d%26%26cb)cb()%3Breturn%20this%3B%7D%2ConDetected%3Afunction()%7Breturn%20this%3B%7D%2ConNotDetected%3Afunction(cb)%7Bif(cb)cb()%3Breturn%20this%3B%7D%2Ccheck%3Afunction()%7Breturn%20false%3B%7D%7D%3Bwindow.FuckAdBlock%3Df%3Bwindow.BlockAdBlock%3Df%3Bwindow.fuckAdBlock%3Dnew%20f()%3Bwindow.blockAdBlock%3Dnew%20f()%3B%7D)()'
                        };
                    }

                    // For BlockAdBlock v2 honeypot
                    if (lower.includes('blockadblock') || lower.includes('nobab2.js') || lower.includes('nobab.js')) {
                        return {
                            redirectUrl: 'data:application/javascript,window.nH7eXzOsG%20%3D%20858%3B'
                        };
                    }

                    // For Prebid / CanRunAds bait
                    if (lower.includes('prebid-ads.js') || lower.includes('canrunads.js') || lower.includes('can-run-ads.js')) {
                        return {
                            redirectUrl: 'data:application/javascript,window.canRunAds%20%3D%20true%3Bwindow.isAdBlockActive%20%3D%20false%3B'
                        };
                    }

                    // For Amazon APSTag
                    if (lower.includes('apstag.js')) {
                        return {
                            redirectUrl: 'data:application/javascript,(function()%7Bwindow.apstag%3D%7BfetchBids%3Afunction(c%2Ccb)%7Bif(typeof%20cb%3D%3D%3D%27function%27)cb(%5B%5D)%3B%7D%2Cinit%3Afunction()%7B%7D%2CsetDisplayBids%3Afunction()%7B%7D%2CtargetingKeys%3Afunction()%7Breturn%5B%5D%3B%7D%7D%3B%7D)()'
                        };
                    }

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
