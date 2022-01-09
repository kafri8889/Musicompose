package com.anafthdev.musicompose.model

import java.io.Serializable

data class MediaPlayerState(
    var title: String,
    var album: String,
    var artist: String,
    var duration: Long,
    var currentPosition: Long,
    var albumArtPath: String,
    var isMusicPlayed: Boolean,
): Serializable {
    companion object {
        const val ACTION_PLAY = "com.anafthdev.musicompose:media:play"
        const val ACTION_PAUSE = "com.anafthdev.musicompose:media:pause"
        const val ACTION_PREVIOUS = "com.anafthdev.musicompose:media:previous"
        const val ACTION_NEXT = "com.anafthdev.musicompose:media:next"
    }
}