package com.anafthdev.musicompose.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anafthdev.musicompose.data.MusicRepositoryImpl

class HomeViewModelFactory(private val repo: MusicRepositoryImpl): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo) as T
    }
}