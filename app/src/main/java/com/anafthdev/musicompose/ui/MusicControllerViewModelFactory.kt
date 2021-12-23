package com.anafthdev.musicompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anafthdev.musicompose.data.MusicRepositoryImpl

class MusicControllerViewModelFactory(private val repo: MusicRepositoryImpl): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicControllerViewModel(repo) as T
    }
}