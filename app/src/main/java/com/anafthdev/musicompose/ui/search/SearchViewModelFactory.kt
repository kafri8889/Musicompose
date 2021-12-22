package com.anafthdev.musicompose.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anafthdev.musicompose.data.MusicRepository

class SearchViewModelFactory(private val repo: MusicRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(repo) as T
    }
}