package com.anafthdev.musicompose.data

import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist

class FakeMusicomposeRepository: MusicomposeRepositoryImpl {

    override fun getAllMusic(action: (List<Music>) -> Unit) {}

    override fun getMusic(audioID: Long, action: (Music) -> Unit) {}

    override fun updateMusic(music: Music, action: () -> Unit) {}

    override fun deleteAllMusic(action: () -> Unit) {}

    override fun deleteMusic(music: Music, action: () -> Unit) {}

    override fun insertMusic(musicList: List<Music>, action: () -> Unit) {}

    override fun insertMusic(music: Music, action: () -> Unit) {}



    override fun getAllPlaylist(action: (List<Playlist>) -> Unit) {}

    override fun getPlaylist(playlistID: Int, action: (Playlist) -> Unit) {}

    override fun updatePlaylist(playlist: Playlist, action: () -> Unit) {}

    override fun deletePlaylist(playlist: Playlist, action: () -> Unit) {}

    override fun insertPlaylist(playlist: Playlist, action: () -> Unit) {}

    override fun insertPlaylist(playlist: List<Playlist>, action: () -> Unit) {}

}