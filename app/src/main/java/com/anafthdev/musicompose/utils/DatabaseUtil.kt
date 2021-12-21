package com.anafthdev.musicompose.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.anafthdev.musicompose.database.MusicDatabase
import com.anafthdev.musicompose.model.Music
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DatabaseUtil(context: Context) {

    private val musicDao = MusicDatabase.getInstance(context).dao()
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private fun postAction(action: () -> Unit) = Handler(Looper.getMainLooper()).post { action() }

    fun getAllMusic(action: (List<Music>) -> Unit) {
        val musicList = ArrayList<Music>()
        scope.launch {
            musicList.addAll(musicDao.getAllMusic())
        }.invokeOnCompletion { postAction { action(musicList) } }
    }

    fun getMusic(audioID: Long, action: (Music) -> Unit) {
        var music: Music? = null
        scope.launch {
            music = musicDao.getMusic(audioID)
        }.invokeOnCompletion { postAction { action(music!!) } }
    }

    fun deleteAllMusic(action: () -> Unit) {
        scope.launch {
            musicDao.deleteAllMusic()
        }.invokeOnCompletion { postAction(action) }
    }

    fun deleteMusic(music: Music, action: () -> Unit) {
        scope.launch {
            musicDao.deleteMusic(music)
        }.invokeOnCompletion { postAction(action) }
    }

    fun insertMusic(musicList: List<Music>, action: () -> Unit) {
        scope.launch {
            musicDao.insertMusic(musicList)
        }.invokeOnCompletion { postAction(action) }
    }

    fun insertMusic(music: Music, action: () -> Unit) {
        scope.launch {
            musicDao.insertMusic(music)
        }.invokeOnCompletion { postAction(action) }
    }

}