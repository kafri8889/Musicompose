package com.anafthdev.musicompose.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.anafthdev.musicompose.data.MusicRepository
import com.anafthdev.musicompose.data.MusicRepositoryImpl
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.AppUtils.toast
import com.anafthdev.musicompose.utils.MusicManager

class HomeViewModel(private val repository: MusicRepositoryImpl): ViewModel() {

    private val _musicList = MutableLiveData<List<Music>>(emptyList())
    val musicList: LiveData<List<Music>> = _musicList

    fun getAllMusic() {
        repository.getAllMusic { list ->
            _musicList.value = list
        }
    }

}