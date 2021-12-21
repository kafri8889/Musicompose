package com.anafthdev.musicompose.ui.scan_music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anafthdev.musicompose.data.MusicRepositoryImpl

class ScanMusicViewModelFactory(private val repo: MusicRepositoryImpl): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScanMusicViewModel(repo) as T
    }
}