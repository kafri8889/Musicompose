package com.anafthdev.musicompose.data

import com.anafthdev.musicompose.model.Music

interface MusicRepositoryImpl {

    fun getAllMusic(action: (List<Music>) -> Unit)

    fun getMusic(audioID: Long, action: (Music) -> Unit)

    fun update(music: Music, action: () -> Unit)

    fun deleteAllMusic(action: () -> Unit)

    fun deleteMusic(music: Music, action: () -> Unit)

    fun insertMusic(musicList: List<Music>, action: () -> Unit)

    fun insertMusic(music: Music, action: () -> Unit)

}