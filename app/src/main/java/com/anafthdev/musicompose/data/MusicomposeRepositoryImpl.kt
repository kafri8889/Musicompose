package com.anafthdev.musicompose.data

import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist

interface MusicomposeRepositoryImpl {

    fun getAllMusic(action: (List<Music>) -> Unit)

    fun getMusic(audioID: Long, action: (Music) -> Unit)

    fun updateMusic(music: Music, action: () -> Unit)

    fun deleteAllMusic(action: () -> Unit)

    fun deleteMusic(music: Music, action: () -> Unit)

    fun insertMusic(musicList: List<Music>, action: () -> Unit)

    fun insertMusic(music: Music, action: () -> Unit)



    fun getAllPlaylist(action: (List<Playlist>) -> Unit)

    fun getPlaylist(playlistID: Int, action: (Playlist) -> Unit)

    fun updatePlaylist(playlist: Playlist, action: () -> Unit)

    fun deletePlaylist(playlist: Playlist, action: () -> Unit)

    fun insertPlaylist(playlist: Playlist, action: () -> Unit)

    fun insertPlaylist(playlist: List<Playlist>, action: () -> Unit)

}