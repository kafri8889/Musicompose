package com.anafthdev.musicompose.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import androidx.compose.material.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anafthdev.musicompose.MusicomposeApplication
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicomposeRepositoryImpl
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
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

    // in second
    private val _currentProgress = MutableLiveData(0f)
    val currentProgress: LiveData<Float> = _currentProgress

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
        MusicControllerState(
            playlistScaffoldBottomSheetState = BottomSheetScaffoldState(
                drawerState = DrawerState(DrawerValue.Closed),
                bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
                snackbarHostState = SnackbarHostState()
            ),
            musicScaffoldBottomSheetState = BottomSheetScaffoldState(
                drawerState = DrawerState(DrawerValue.Closed),
                bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
                snackbarHostState = SnackbarHostState()
            ),
            modalBottomSheetMusicInfoState = ModalBottomSheetState(
                ModalBottomSheetValue.Hidden
            ),
        )
    )

    var onNext: (Int) -> Unit = {}
    var onPrevious: (Int) -> Unit = {}

    private var lastVolumeValue = 0
    private var lastMusicPlayed = false

//    private val mediaPlayer = MediaPlayer()

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

    fun setProgress(progress: Float) {
        _currentProgress.value = progress
        _currentMusicDurationInMinute.value = TimeUnit.SECONDS.toMinutes(progress.toLong()).toInt()
        _currentMusicDurationInSecond.value = (progress % 60).toInt()
    }

    fun applyProgress() {

    }

    fun onVolumeChange() {
        val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        _isVolumeMuted.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
        _currentVolume.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        Timber.i("onVolumeChange")
    }

    fun getPlaylist() {
        repository.getAllMusic { musicList ->
            _playlist.value = musicList.shuffled()
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
     */
    fun play(audioID: Long, isPlayLastMusic: Boolean = false) {
        repository.getPlaylist(Playlist.justPlayed.id) { justPlayedPlaylist ->
            repository.getMusic(audioID) { music ->
                val mJustPlayedPlaylist = ArrayList(justPlayedPlaylist.musicList).apply {
                    if (!isPlayLastMusic) {
                        if (size > 9) {
                            removeAt(0)
                            add(music)
                        } else add(music)
                    }
                }

                repository.updatePlaylist(
                    playlist = justPlayedPlaylist.apply { musicList = mJustPlayedPlaylist },
                    action = {
                        appDatastore.setLastMusicPlayed(music.audioID) {
                            _currentMusicPlayed.value = music
                            _isMusicPlayed.value = true
                            _isMusicFavorite.value = music.isFavorite
                            _musicDurationInMinute.value = TimeUnit.MILLISECONDS.toMinutes(_currentMusicPlayed.value!!.duration).toInt()
                            _musicDurationInSecond.value = TimeUnit.MILLISECONDS.toSeconds(_currentMusicPlayed.value!!.duration).toInt() % 60

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
        play(_playlist.value!![0].audioID)
    }

    fun playLastMusic() {
        onVolumeChange()
        getPlaylist()
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
//        mediaPlayer.start()
        _isMusicPlayed.value = true
    }

    fun pause() {
//        mediaPlayer.pause()
        _isMusicPlayed.value = false
    }

    fun next() {
        var currentMusicIndex = _playlist.value!!.indexOf(_currentMusicPlayed.value!!)

        if (currentMusicIndex == (_playlist.value!!.size - 1)) {
            currentMusicIndex = 0
            play(_playlist.value!![currentMusicIndex].audioID)
        } else {
            currentMusicIndex += 1
            play(_playlist.value!![currentMusicIndex].audioID)
        }

        onNext(currentMusicIndex)
    }

    fun previous() {
        var currentMusicIndex = _playlist.value!!.indexOf(_currentMusicPlayed.value!!)

        if (currentMusicIndex == 0) {
            currentMusicIndex = _playlist.value!!.size - 1
            play(_playlist.value!![currentMusicIndex].audioID)
        } else {
            currentMusicIndex -= 1
            play(_playlist.value!![currentMusicIndex].audioID)
        }

        onPrevious(currentMusicIndex)
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

    data class MusicControllerState @OptIn(ExperimentalMaterialApi::class) constructor(
        val playlistScaffoldBottomSheetState: BottomSheetScaffoldState,
        val musicScaffoldBottomSheetState: BottomSheetScaffoldState,
        val modalBottomSheetMusicInfoState: ModalBottomSheetState,
    )

    enum class MusicPlayMode {
        REPEAT_OFF,
        REPEAT_ON,
        REPEAT_ONE,
    }
}
