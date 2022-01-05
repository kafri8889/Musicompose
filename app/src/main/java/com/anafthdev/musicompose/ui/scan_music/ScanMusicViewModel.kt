package com.anafthdev.musicompose.ui.scan_music

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anafthdev.musicompose.data.MusicomposeRepositoryImpl
import com.anafthdev.musicompose.utils.MusicUtil

class ScanMusicViewModel(
    private val repository: MusicomposeRepositoryImpl
): ViewModel() {

    private var _scannedMusicInPercent = MutableLiveData(0)
    val scannedMusicInPercent: LiveData<Int> = _scannedMusicInPercent

    fun scanLocalSong(context: Context, onComplete: () -> Unit) {
        val totalMusic = MusicUtil.getMusicCount(context)
        val musicList = MusicUtil.getMusic(
            context = context,
            scannedMusicCount = { scannedMusicCount ->
                // ((scanned / total) * 100%) * 100
                val percent = (((scannedMusicCount / totalMusic) * (100/100)) * 100)
                _scannedMusicInPercent.value = percent
            },
        )

        repository.deleteAllMusic {
            repository.insertMusic(musicList) {
                onComplete()
            }
        }
    }
}
