package com.anafthdev.musicompose.ui

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.material.*
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anafthdev.musicompose.MusicomposeApplication
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.common.MediaPlayerService
import com.anafthdev.musicompose.data.MusicomposeRepositoryImpl
import com.anafthdev.musicompose.model.MediaPlayerState
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.MusicControllerState
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.utils.AppUtils.containBy
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.move
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class MusicControllerViewModel @Inject constructor(
    private val application: MusicomposeApplication,
    private val repository: MusicomposeRepositoryImpl,
    private val appDatastore: AppDatastore
): ViewModel() {

    private val _currentMusicPlayed = MutableLiveData(Music.unknown)
    val currentMusicPlayed: LiveData<Music> = _currentMusicPlayed

    private val _playlist = MutableLiveData(emptyList<Music>())
    val playlist: LiveData<List<Music>> = _playlist

    private val _playMode = MutableLiveData(MusicPlayMode.REPEAT_ON)
    val playMode: LiveData<MusicPlayMode> = _playMode

    private val _musicDurationInMinute = MutableLiveData(0)
    val musicDurationInMinute: LiveData<Int> = _musicDurationInMinute

    private val _musicDurationInSecond = MutableLiveData(0)
    val musicDurationInSecond: LiveData<Int> = _musicDurationInSecond

    // in millisecond
    private val _currentProgress = MutableLiveData(0L)
    val currentProgress: LiveData<Long> = _currentProgress

    private val _currentVolume = MutableLiveData(0)
    val currentVolume: LiveData<Int> = _currentVolume

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

    private val _isMiniMusicPlayerHidden = MutableLiveData(false)
    val isMiniMusicPlayerHidden: LiveData<Boolean> = _isMiniMusicPlayerHidden

    @OptIn(ExperimentalMaterialApi::class)
    val musicControllerState: LiveData<MusicControllerState> = MutableLiveData(
        MusicControllerState.initial
    )

    var onNext: (Int) -> Unit = {}
    var onPrevious: (Int) -> Unit = {}

    private var lastVolumeValue = 0
    private var lastMusicPlayed = false

    private val serviceIntent = Intent(application, MediaPlayerService::class.java).apply {
        putExtra("mediaPLayerState", mediaPlayerState)
    }

    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mediaHandler = Handler(Looper.getMainLooper())
    private var mediaRunnable = Runnable {}
    private val exoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    when (_playMode.value!!) {
                        MusicPlayMode.REPEAT_OFF -> {
                            this@MusicControllerViewModel.next()
                            Handler(Looper.getMainLooper()).postDelayed({
                                this@MusicControllerViewModel.pause()
                            }, 3000)
                        }
                        MusicPlayMode.REPEAT_ON -> {
                            this@MusicControllerViewModel.next()
                        }
                        MusicPlayMode.REPEAT_ONE -> {
                            play(
                                audioID = _playlist.value!![currentMusicPlayedIndexInPlaylist()].audioID,
                                shufflePlaylist = false
                            )
                        }
                    }
                }
            }
        })
    }

    private val mediaPlayerState = MediaPlayerState(
        title = _currentMusicPlayed.value!!.title,
        album = _currentMusicPlayed.value!!.album,
        artist = _currentMusicPlayed.value!!.artist,
        duration = _currentMusicPlayed.value!!.duration,
        albumArtPath = _currentMusicPlayed.value!!.albumPath,
        currentPosition = _currentProgress.value!!,
        isMusicPlayed = _isMusicPlayed.value!!
    )

    fun hideMiniMusicPlayer() {
        viewModelScope.launch {
            _isMiniMusicPlayerHidden.postValue(true)
        }
    }

    fun showMiniMusicPlayer() {
        viewModelScope.launch {
            _isMiniMusicPlayerHidden.postValue(false)
        }
    }

    fun setMusicFavorite(favorite: Boolean) {
        if (_currentMusicPlayed.value!!.audioID != Music.unknown.audioID) {

            // Update music
            repository.updateMusic(_currentMusicPlayed.value!!.copy(isFavorite = favorite)) {

                // Update playlist
                repository.getAllMusic { mMusicList ->
                    val filteredMusicList = mMusicList.filter { it.isFavorite }
                    val favoritePlaylist = Playlist.favorite.copy(
                        name = application.getString(R.string.favorite),
                        musicList = filteredMusicList
                    )

                    repository.updatePlaylist(favoritePlaylist) {
                        _isMusicFavorite.value = favorite
                        Timber.i("Playlist \"${favoritePlaylist.name}\" Updated")
                    }
                }

                Timber.i("Music Updated")
            }
        }
    }

    fun setPlayMode(playMode: MusicPlayMode) {
        _playMode.value = playMode
    }

    fun muteVolume(mIsVolumeMuted: Boolean) {
        val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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
                if (lastVolumeValue == 0) 1 else lastVolumeValue,
                0
            )
        }

        _isVolumeMuted.value = mIsVolumeMuted
    }

    private fun setProgress(progress: Long) {
        mediaPlayerState.apply {
            currentPosition = progress
        }

        _currentProgress.value = progress
        _currentMusicDurationInMinute.value = TimeUnit.MILLISECONDS.toMinutes(progress).toInt()
        _currentMusicDurationInSecond.value = (progress / 1000 % 60).toInt()

        serviceIntent.putExtra("mediaPLayerState", mediaPlayerState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(serviceIntent)
        } else application.startService(serviceIntent)
    }

    fun applyProgress(progressInMs: Long) {
        exoPlayer.seekTo(progressInMs)
        setProgress(progressInMs)
    }

    fun onVolumeChange() {
        val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        _isVolumeMuted.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
        _currentVolume.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        Timber.i("onVolumeChange")
    }

    /**
     * get all music and shuffle
     */
    fun getPlaylist() {
        repository.getAllMusic { musicList ->
            _playlist.value = musicList.toMutableList().apply {
                remove(_currentMusicPlayed.value)
                shuffle()
                add(0, _currentMusicPlayed.value!!)
            }
        }
    }

    fun onPlaylistReordered(oldPos: ItemPosition, newPos: ItemPosition) {

        // Drag and drop list
        _playlist.value?.let { mPlaylist ->
            _playlist.value = ArrayList(mPlaylist).apply { move(oldPos.index, newPos.index) }
        }
    }

    fun currentMusicPlayedIndexInPlaylist(): Int = _playlist.value!!.indexOf(_currentMusicPlayed.value!!)

    /**
     * @param audioID Music audioID
     * @param isPlayLastMusic if true, the music will be paused
     * @param shufflePlaylist if true, call getPlaylist() function, if false, the playlist will not be created
     */
    fun play(audioID: Long, isPlayLastMusic: Boolean = false, shufflePlaylist: Boolean = true) {
        mediaHandler = Handler(Looper.getMainLooper())

        repository.getPlaylist(Playlist.justPlayed.id) { justPlayedPlaylist ->
            repository.getMusic(audioID) { music ->
                val mJustPlayedPlaylist = ArrayList(justPlayedPlaylist.musicList).apply {
                    if (!isPlayLastMusic) {
                        val containInPlaylist = containBy { it.audioID == music.audioID }
                        if (size >= 10) {
                            // if size == 10, delete first index
                            if (!containInPlaylist) removeAt(0)
                        }

                        // if the music is already in the playlist,
                        // remove it from the playlist
                        if (containInPlaylist) remove(music)

                        add(music)
                    }
                }

                repository.updatePlaylist(
                    playlist = justPlayedPlaylist.apply { musicList = mJustPlayedPlaylist },
                    action = {
                        appDatastore.setLastMusicPlayed(music.audioID) {
                            exoPlayer.setMediaItem(MediaItem.fromUri(music.path.toUri()))
                            exoPlayer.prepare()
                            _currentMusicPlayed.value = music
                            _currentProgress.value = 0L
                            _isMusicPlayed.value = true
                            _isMusicFavorite.value = music.isFavorite
                            _musicDurationInMinute.value = TimeUnit.MILLISECONDS.toMinutes(_currentMusicPlayed.value!!.duration).toInt()
                            _musicDurationInSecond.value = TimeUnit.MILLISECONDS.toSeconds(_currentMusicPlayed.value!!.duration).toInt() % 60

                            mediaRunnable = Runnable {
                                setProgress(
                                    if (exoPlayer.duration != -1L) exoPlayer.currentPosition else 0L
                                )

                                mediaHandler.postDelayed(mediaRunnable, 1000)
                            }

                            mediaHandler.post(mediaRunnable)

                            mediaPlayerState.apply {
                                title = _currentMusicPlayed.value!!.title
                                album = _currentMusicPlayed.value!!.album
                                artist = _currentMusicPlayed.value!!.artist
                                duration = _currentMusicPlayed.value!!.duration
                                currentPosition = _currentProgress.value!!
                                albumArtPath = _currentMusicPlayed.value!!.albumPath
                            }

                            if (shufflePlaylist) getPlaylist()

                            if (isPlayLastMusic) pause()
                            else resume()
                        }
                    }
                )

            }
        }
    }

    fun playAll(musicList: List<Music>) {
        _playlist.value = musicList
        play(_playlist.value!![0].audioID, shufflePlaylist = false)
        Timber.i("current playlist: ${_playlist.value}")
    }

    fun playLastMusic() {
        onVolumeChange()
        viewModelScope.launch {
            appDatastore.getLastMusicPlayed.collect { audioID ->
                // prevent calling when audioID changes
                if (!lastMusicPlayed) {
                    withContext(Dispatchers.Main) {
                        lastMusicPlayed = true
                        play(audioID, isPlayLastMusic = true)
                    }
                }
            }
        }
    }

    fun resume() {
        when {
            !exoPlayer.isPlaying -> {
                exoPlayer.play()
                _isMusicPlayed.value = true
            }
            else -> _isMusicPlayed.value = true
        }

        mediaPlayerState.apply {
            isMusicPlayed = _isMusicPlayed.value!!
        }

        serviceIntent.putExtra("mediaPLayerState", mediaPlayerState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(serviceIntent)
        } else application.startService(serviceIntent)
    }

    fun pause() {
        when {
            exoPlayer.isPlaying -> {
                exoPlayer.pause()
                _isMusicPlayed.value = false
            }
            else -> _isMusicPlayed.value = false
        }

        mediaPlayerState.apply {
            isMusicPlayed = _isMusicPlayed.value!!
        }

        serviceIntent.putExtra("mediaPLayerState", mediaPlayerState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(serviceIntent)
        } else application.startService(serviceIntent)
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.release()
        notificationManager.cancelAll()
    }

    fun next(): Int {
        var currentMusicIndex = currentMusicPlayedIndexInPlaylist()

        if (currentMusicIndex == (_playlist.value!!.size - 1)) {
            currentMusicIndex = 0
            play(_playlist.value!![currentMusicIndex].audioID, shufflePlaylist = false)
        } else {
            currentMusicIndex += 1
            play(_playlist.value!![currentMusicIndex].audioID, shufflePlaylist = false)
        }

        onNext(currentMusicIndex)

        return currentMusicIndex
    }

    fun previous(): Int {
        var currentMusicIndex = currentMusicPlayedIndexInPlaylist()

        if (currentMusicIndex == 0) {
            currentMusicIndex = _playlist.value!!.size - 1
            play(_playlist.value!![currentMusicIndex].audioID, shufflePlaylist = false)
        } else {
            currentMusicIndex -= 1
            play(_playlist.value!![currentMusicIndex].audioID, shufflePlaylist = false)
        }

        onPrevious(currentMusicIndex)

        return currentMusicIndex
    }

    fun getAllPlaylist(action: (List<Playlist>) -> Unit) {
        repository.getAllPlaylist(action)
    }

    fun newPlaylist(playlist: Playlist, action: () -> Unit = {}) {
        repository.insertPlaylist(playlist, action)
    }

    fun updatePlaylist(playlist: Playlist, action: () -> Unit = {}) {
        repository.updatePlaylist(playlist, action)
    }

    fun deletePlaylist(playlist: Playlist, action: () -> Unit = {}) {
        repository.deletePlaylist(playlist, action)
    }

    fun deleteMusicFromPlaylist(music: Music, playlist: Playlist, action: () -> Unit = {}) {
        repository.updatePlaylist(
            playlist = playlist.apply {
                musicList = ArrayList(musicList).apply {
                    remove(music)
                }
            },
            action = action
        )
    }

    enum class MusicPlayMode {
        REPEAT_OFF,
        REPEAT_ON,
        REPEAT_ONE,
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
