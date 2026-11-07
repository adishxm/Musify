package com.musify.browser.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.musify.browser.MainActivity
import com.musify.browser.R

/**
 * MusifyMediaPlaybackService provides background media playback and OS notification integration.
 * Engineered by adishxm for background Spotify, YouTube Music, and web streaming parity.
 *
 * Keeps media session alive when the app is minimized, turned off, or in the background.
 */
class MusifyMediaPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "musify_media_playback_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_START_PLAYBACK = "com.musify.browser.media.START"
        const val ACTION_PAUSE_PLAYBACK = "com.musify.browser.media.PAUSE"
        const val ACTION_RESUME_PLAYBACK = "com.musify.browser.media.RESUME"
        const val ACTION_STOP_PLAYBACK = "com.musify.browser.media.STOP"

        const val EXTRA_TRACK_TITLE = "extra_track_title"
        const val EXTRA_ARTIST_OR_SITE = "extra_artist_or_site"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        var isServiceRunning = false
            private set

        fun startService(context: Context, title: String, site: String, isPlaying: Boolean = true) {
            val intent = Intent(context, MusifyMediaPlaybackService::class.java).apply {
                action = ACTION_START_PLAYBACK
                putExtra(EXTRA_TRACK_TITLE, title)
                putExtra(EXTRA_ARTIST_OR_SITE, site)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MusifyMediaPlaybackService::class.java).apply {
                action = ACTION_STOP_PLAYBACK
            }
            context.startService(intent)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var currentTitle: String = "Now Playing"
    private var currentSite: String = "Musify • Designed by adishxm"
    private var isPlaying: Boolean = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        isServiceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_STOP_PLAYBACK -> {
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE_PLAYBACK -> {
                isPlaying = false
                updateNotification()
            }
            ACTION_RESUME_PLAYBACK -> {
                isPlaying = true
                updateNotification()
            }
            ACTION_START_PLAYBACK, null -> {
                currentTitle = intent?.getStringExtra(EXTRA_TRACK_TITLE) ?: currentTitle
                currentSite = intent?.getStringExtra(EXTRA_ARTIST_OR_SITE) ?: currentSite
                isPlaying = intent?.getBooleanExtra(EXTRA_IS_PLAYING, true) ?: true
                val notification = buildNotification(currentTitle, currentSite, isPlaying)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        releaseWakeLock()
        isServiceRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Musify:BackgroundMediaLock"
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes timeout per lock acquisition
            }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Musify Background Audio Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Controls and media status for background web audio playback (Designed by adishxm)"
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, site: String, playing: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val togglePlayIntent = Intent(this, MusifyMediaPlaybackService::class.java).apply {
            action = if (playing) ACTION_PAUSE_PLAYBACK else ACTION_RESUME_PLAYBACK
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            togglePlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusifyMediaPlaybackService::class.java).apply {
            action = ACTION_STOP_PLAYBACK
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseTitle = if (playing) "Pause" else "Play"
        val playPauseIcon = if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(site)
            .setSubText("Musify by adishxm")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentPendingIntent)
            .setOngoing(playing)
            .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification() {
        val notification = buildNotification(currentTitle, currentSite, isPlaying)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
