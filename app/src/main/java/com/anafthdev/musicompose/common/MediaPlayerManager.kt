package com.anafthdev.musicompose.common

import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import com.anafthdev.musicompose.utils.NotificationUtil

class MediaPlayerManager(
    context: Context,
    private val state: MediaPlayerState
): ContextWrapper(context) {

    private val notificationUtil = NotificationUtil(this)

    private val mediaSession = MediaSession(this, "MediaPlayerService")

    private val mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession.sessionToken)

    private var playbackState: PlaybackState = PlaybackState.Builder()
        .setState(
            if (state.isMusicPlayed) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
            state.currentPosition,
            1f
        )
        .build()

    init {
        applyMetaData()
        applyPlaybackState()
    }

    fun mediaNotification(): Notification {
        return notificationUtil.notificationMediaPlayer(mediaStyle)
    }

    private fun applyPlaybackState() {
        playbackState = PlaybackState.Builder()
            .setState(
                if (state.isMusicPlayed) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                state.currentPosition,
                1f
            )
            .setActions(PlaybackState.ACTION_PLAY_PAUSE)
            .build()

        mediaSession.setPlaybackState(playbackState)
    }

    private fun applyMetaData() {
        mediaSession.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, state.title)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, state.album)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, state.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, state.albumArtPath)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, state.duration)
                .build()
        )
    }

    fun updateState(mState: MediaPlayerState) {
        state.title = mState.title
        state.album = mState.album
        state.artist = mState.artist
        state.albumArtPath = mState.albumArtPath
        state.duration = mState.duration

        applyMetaData()
        applyPlaybackState()
    }

    data class MediaPlayerState(
        var title: String,
        var album: String,
        var artist: String,
        var duration: Long,
        var currentPosition: Long,
        var albumArtPath: String,
        var isMusicPlayed: Boolean,
    )

    companion object {
        const val ACTION_PLAY_PAUSE = "com.anafthdev.musicompose:media:play_pause"
        const val ACTION_PREVIOUS = "com.anafthdev.musicompose:media:previous"
        const val ACTION_NEXT = "com.anafthdev.musicompose:media:next"

        private var INSTANCE: MediaPlayerManager? = null

        fun getInstance(
            base: Context,
            state: MediaPlayerState
        ): MediaPlayerManager {
            if (INSTANCE == null) {
                synchronized(MediaPlayerManager::class.java) {
                    INSTANCE = MediaPlayerManager(base, state)
                }
            }

            return INSTANCE!!
        }
    }
}