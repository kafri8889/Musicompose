package com.anafthdev.musicompose.data

import com.anafthdev.musicompose.model.Music

class FakeMusicRepository: MusicRepositoryImpl {

    override fun getAllMusic(action: (List<Music>) -> Unit) {}

    override fun getMusic(audioID: Long, action: (Music) -> Unit) {}

    override fun deleteAllMusic(action: () -> Unit) {}

    override fun deleteMusic(music: Music, action: () -> Unit) {}

    override fun insertMusic(musicList: List<Music>, action: () -> Unit) {}

    override fun insertMusic(music: Music, action: () -> Unit) {}

}