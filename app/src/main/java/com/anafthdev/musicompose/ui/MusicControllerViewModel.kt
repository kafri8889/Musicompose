package com.anafthdev.musicompose.ui

import android.content.Context
import android.media.AudioManager
import androidx.core.content.getSystemService
import androidx.lifecycle.*
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicRepositoryImpl
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.AppUtils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MusicControllerViewModel(
    private val repository: MusicRepositoryImpl,
    private val appDatastore: AppDatastore
): ViewModel() {

    private val _currentMusicPlayed = MutableLiveData(Music.unknown)
    val currentMusicPlayed: LiveData<Music> = _currentMusicPlayed

    private val _playMode = MutableLiveData(MusicPlayMode.REPEAT_ON)
    val playMode: LiveData<MusicPlayMode> = _playMode

    // in second
    private val _currentProgress = MutableLiveData(0f)
    val currentProgress: LiveData<Float> = _currentProgress

    private val _currentVolume = MutableLiveData(0)
    val currentVolume: LiveData<Int> = _currentVolume

    private val _musicDurationInMinute = MutableLiveData(0)
    val musicDurationInMinute: LiveData<Int> = _musicDurationInMinute

    private val _musicDurationInSecond = MutableLiveData(0)
    val musicDurationInSecond: LiveData<Int> = _musicDurationInSecond

    private val _currentMusicDurationInMinute = MutableLiveData(0)
    val currentMusicDurationInMinute: LiveData<Int> = _currentMusicDurationInMinute

    private val _currentMusicDurationInSecond = MutableLiveData(0)
    val currentMusicDurationInSecond: LiveData<Int> = _currentMusicDurationInSecond

    private val _isMusicPlayed = MutableLiveData(false)
    val isMusicPlayed: LiveData<Boolean> = _isMusicPlayed

    private val _isMusicFavorite = MutableLiveData(_currentMusicPlayed.value?.isFavorite ?: false)
    val isMusicFavorite: LiveData<Boolean> = _isMusicFavorite

    private val _isVolumeMuted = MutableLiveData(false)
    val isVolumeMuted: LiveData<Boolean> = _isVolumeMuted

    private var lastVolumeValue = 0
    private var lastMusicPlayed = false

//    private val mediaPlayer = MediaPlayer()

    fun setMusicFavorite(favorite: Boolean) {
        _isMusicFavorite.value = favorite
    }

    fun setPlayMode(playMode: MusicPlayMode) {
        _playMode.value = playMode
    }

    fun muteVolume(context: Context, mIsVolumeMuted: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (mIsVolumeMuted) {
            lastVolumeValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                0,
                0
            )
        } else {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                lastVolumeValue,
                0
            )
        }

        _isVolumeMuted.value = mIsVolumeMuted
    }

    fun setProgress(progress: Float) {
        _currentProgress.value = progress
        _currentMusicDurationInMinute.value = TimeUnit.SECONDS.toMinutes(progress.toLong()).toInt()
        _currentMusicDurationInSecond.value = (progress % 60).toInt()
    }

    fun applyProgress() {

    }

    fun onVolumeChange(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        _isVolumeMuted.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
        _currentVolume.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        Timber.i("onVolumeChange")
    }

    /**
     * @param context Context for MediaPlayer
     * @param audioID Music audioID
     * @param isPlayLastMusic if true, the music will be paused
     */
    fun play(context: Context, audioID: Long, isPlayLastMusic: Boolean = false) {
        repository.getMusic(audioID) { music ->
            appDatastore.setLastMusicPlayed(music.audioID) {
                _currentMusicPlayed.value?.let {

                    // Update music
                    repository.update(it.copy(isFavorite = _isMusicFavorite.value ?: false)) {
                        Timber.i("Music Updated")
                    }
                }

                _currentMusicPlayed.value = music
                _isMusicPlayed.value = true
                _isMusicFavorite.value = music.isFavorite
                _musicDurationInMinute.value = TimeUnit.MILLISECONDS.toMinutes(_currentMusicPlayed.value!!.duration).toInt()
                _musicDurationInSecond.value = TimeUnit.MILLISECONDS.toSeconds(_currentMusicPlayed.value!!.duration).toInt() % 60

                if (isPlayLastMusic) pause()
                else resume()
            }
        }
    }

    fun playLastMusic(context: Context) {
        onVolumeChange(context)
        viewModelScope.launch {
            appDatastore.getLastMusicPlayed.collect { audioID ->
                // prevent calling when audioID changes
                if (!lastMusicPlayed) {
                    withContext(Dispatchers.Main) {
                        lastMusicPlayed = true
                        play(context, audioID, isPlayLastMusic = true)
                    }
                }
            }
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

    enum class MusicPlayMode {
        REPEAT_OFF,
        REPEAT_ON,
        REPEAT_ONE,
        SHUFFLE
    }
}