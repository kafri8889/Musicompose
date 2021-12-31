package com.anafthdev.musicompose.data

import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.utils.DatabaseUtil

class MusicomposeRepository(private val databaseUtil: DatabaseUtil): MusicomposeRepositoryImpl {

    override fun getAllMusic(action: (List<Music>) -> Unit) {
        databaseUtil.getAllMusic(action)
    }

    override fun getMusic(audioID: Long, action: (Music) -> Unit) {
        databaseUtil.getMusic(audioID, action)
    }

    override fun updateMusic(music: Music, action: () -> Unit) {
        databaseUtil.updateMusic(music, action)
    }

    override fun deleteAllMusic(action: () -> Unit) {
        databaseUtil.deleteAllMusic(action)
    }

    override fun deleteMusic(music: Music, action: () -> Unit) {
        databaseUtil.deleteAllMusic(action)
    }

    override fun insertMusic(musicList: List<Music>, action: () -> Unit) {
        databaseUtil.insertMusic(musicList, action)
    }

    override fun insertMusic(music: Music, action: () -> Unit) {
        databaseUtil.insertMusic(music, action)
    }



    override fun getAllPlaylist(action: (List<Playlist>) -> Unit) {
        databaseUtil.getAllPlaylist(action)
    }

    override fun updatePlaylist(playlist: Playlist, action: () -> Unit) {
        databaseUtil.updatePlaylist(playlist, action)
    }

    override fun deletePlaylist(playlist: Playlist, action: () -> Unit) {
        databaseUtil.deletePlaylist(playlist, action)
    }

    override fun insertPlaylist(playlist: Playlist, action: () -> Unit) {
        databaseUtil.insertPlaylist(playlist, action)
    }

    override fun insertPlaylist(playlist: List<Playlist>, action: () -> Unit) {
        databaseUtil.insertPlaylist(playlist, action)
    }

}