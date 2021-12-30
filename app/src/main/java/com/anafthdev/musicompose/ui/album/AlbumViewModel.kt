package com.anafthdev.musicompose.ui.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anafthdev.musicompose.data.MusicRepository
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.AppUtils.get
import javax.inject.Inject

class AlbumViewModel @Inject constructor(
    private val repository: MusicRepository
): ViewModel() {

    private val _artist = MutableLiveData(Music.unknown.artist)
    val artist: LiveData<String> = _artist

    private val _album = MutableLiveData(Music.unknown.album)
    val album: LiveData<String> = _album

    private val _filteredMusicList = MutableLiveData(emptyList<Music>())
    val filteredMusicList: LiveData<List<Music>> = _filteredMusicList

    fun get(albumID: String) {
        repository.getAllMusic { musicList ->
            val music = musicList.get { it.albumID == albumID } ?: Music.unknown

            _artist.value = music.artist
            _album.value = music.album
            _filteredMusicList.value = musicList.filter { it.albumID == albumID }
        }
    }
}