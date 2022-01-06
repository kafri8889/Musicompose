package com.anafthdev.musicompose.common

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.IBinder
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.NotificationUtil
import timber.log.Timber

const val MediaPlayerService_Tag = "MediaPlayerService"

class MediaPlayerService: Service() {

    private var mediaPLayerAction: MediaPlayerManager.MediaPLayerAction? = null

    private val mBinder: IBinder = MediaPlayerServiceBinder()
    private lateinit var mediaPlayerManager: MediaPlayerManager
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaStyle: Notification.MediaStyle

    override fun onCreate() {
        super.onCreate()
        mediaPlayerManager = MediaPlayerManager.getInstance(this)
        mediaSession = MediaSession(this, "MediaPlayerSessionService")
        mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession.sessionToken)

        mediaSession.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, Music.unknown.title)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, Music.unknown.album)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, Music.unknown.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, Music.unknown.albumPath)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, Music.unknown.duration)
                .build()
        )

        startForeground(123, NotificationUtil.foregroundNotification(this))
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            MediaPlayerManager.ACTION_PLAY_PAUSE -> mediaPLayerAction?.playPause()
            MediaPlayerManager.ACTION_NEXT -> mediaPLayerAction?.next()
            MediaPlayerManager.ACTION_PREVIOUS -> mediaPLayerAction?.previous()
        }

        intent.getSerializableExtra("mediaPLayerState")?.let { newState ->
            newState as MediaPlayerManager.MediaPlayerState

            mediaSession.setPlaybackState(
                PlaybackState.Builder()
                    .setState(
                        if (newState.isMusicPlayed) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                        newState.currentPosition,
                        1f
                    )
                    .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                    .build()
            )

            mediaSession.setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, newState.title)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, newState.album)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, newState.artist)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, newState.albumArtPath)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, newState.duration)
                    .build()
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                0,
                NotificationUtil.notificationMediaPlayer(
                    applicationContext,
                    Notification.MediaStyle().setMediaSession(mediaSession.sessionToken),
                    newState
                )
            )

            Timber.i("to MediaPLayerState: $newState")
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopForeground(true)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        super.onTaskRemoved(rootIntent)
    }

    fun setMediaPlayerAction(playerAction: MediaPlayerManager.MediaPLayerAction) {
        this.mediaPLayerAction = playerAction
    }

    inner class MediaPlayerServiceBinder: Binder() {

        fun getService(): MediaPlayerService {
            return this@MediaPlayerService
        }
    }
}