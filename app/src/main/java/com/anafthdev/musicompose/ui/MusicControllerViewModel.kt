package com.anafthdev.musicompose.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anafthdev.musicompose.data.MusicRepositoryImpl
import com.anafthdev.musicompose.model.Music

class MusicControllerViewModel(
    private val repository: MusicRepositoryImpl
): ViewModel() {

    private val _currentMusicPlayed = MutableLiveData(Music.unknown)
    val currentMusicPlayed: LiveData<Music> = _currentMusicPlayed

    private val _isMusicPlayed = MutableLiveData(false)
    val isMusicPlayed: LiveData<Boolean> = _isMusicPlayed

//    private val mediaPlayer = MediaPlayer()

    fun play(context: Context, audioID: Long) {
        repository.getMusic(audioID) { music ->
            _currentMusicPlayed.value = music
            _isMusicPlayed.value = true

            if (_isMusicPlayed.value!!) {
//                mediaPlayer.stop()
//                mediaPlayer.release()
            }

//            mediaPlayer.setDataSource(context, _currentMusicPlayed.value!!.path.toUri())
//            mediaPlayer.start()
            _isMusicPlayed.value = true
        }
    }

    fun resume() {
//        mediaPlayer.start()
        _isMusicPlayed.value = true
    }

    fun pause() {
//        mediaPlayer.pause()
        _isMusicPlayed.value = false
    }

}