package com.anafthdev.musicompose.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anafthdev.musicompose.data.MusicomposeRepository
import com.anafthdev.musicompose.model.Playlist
import javax.inject.Inject

class PlaylistViewModel @Inject constructor(
    private val repository: MusicomposeRepository
): ViewModel() {

    private val _playlist = MutableLiveData(Playlist.unknown)
    val playlist: LiveData<Playlist> = _playlist

    fun getPlaylist(playlistID: Int) {
        repository.getPlaylist(playlistID) { mPlaylist ->
            _playlist.value = mPlaylist
        }
    }

}