package com.anafthdev.musicompose.data

import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.DatabaseUtil

class MusicRepository(private val databaseUtil: DatabaseUtil): MusicRepositoryImpl {

    override fun getAllMusic(action: (List<Music>) -> Unit) {
        databaseUtil.getAllMusic(action)
    }

    override fun getMusic(audioID: Long, action: (Music) -> Unit) {
        databaseUtil.getMusic(audioID, action)
    }

    override fun update(music: Music, action: () -> Unit) {
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

}