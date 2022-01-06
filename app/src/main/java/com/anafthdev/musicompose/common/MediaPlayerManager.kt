package com.anafthdev.musicompose.common

import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.NotificationUtil
import timber.log.Timber
import java.io.Serializable

class MediaPlayerManager(
    context: Context,
): ContextWrapper(context) {

    private val mediaSession = MediaSession(this, "MediaPlayerService")

    private val mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession.sessionToken)

    val token = mediaSession.sessionToken
    val state = MediaPlayerState(
        title = Music.unknown.title,
        album = Music.unknown.album,
        artist = Music.unknown.artist,
        duration = Music.unknown.duration,
        albumArtPath = Music.unknown.albumPath,
        currentPosition = 0,
        isMusicPlayed = false
    )

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
    ): Serializable

    interface MediaPLayerAction {

        fun playPause()

        fun next()

        fun previous()

    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.anafthdev.musicompose:media:play_pause"
        const val ACTION_PREVIOUS = "com.anafthdev.musicompose:media:previous"
        const val ACTION_NEXT = "com.anafthdev.musicompose:media:next"

        private var INSTANCE: MediaPlayerManager? = null

        fun getInstance(
            base: Context,
        ): MediaPlayerManager {
            if (INSTANCE == null) {
                synchronized(MediaPlayerManager::class.java) {
                    INSTANCE = MediaPlayerManager(base)
                }
            }

            return INSTANCE!!
        }
    }
}