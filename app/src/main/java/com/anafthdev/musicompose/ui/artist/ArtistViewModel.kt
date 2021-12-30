package com.anafthdev.musicompose.ui.artist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anafthdev.musicompose.data.MusicRepository
import com.anafthdev.musicompose.model.Music
import javax.inject.Inject

class ArtistViewModel @Inject constructor(
    private val repository: MusicRepository
): ViewModel() {

    private val _filteredMusicList = MutableLiveData(emptyList<Music>())
    val filteredMusicList: LiveData<List<Music>> = _filteredMusicList

    fun filterMusic(artist: String) {
        repository.getAllMusic { musicList ->
            _filteredMusicList.value = musicList.filter { it.artist == artist }
        }
    }
}