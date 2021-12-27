package com.anafthdev.musicompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicRepositoryImpl

class MusicControllerViewModelFactory(
    private val repo: MusicRepositoryImpl,
    private val datastore: AppDatastore
    ): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicControllerViewModel(repo, datastore) as T
    }
}