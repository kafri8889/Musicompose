package com.anafthdev.musicompose.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anafthdev.musicompose.data.MusicomposeRepository
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import javax.inject.Inject

class PlaylistViewModel @Inject constructor(
    private val repository: MusicomposeRepository
): ViewModel() {

    private val _playlist = MutableLiveData(Playlist.unknown)
    val playlist: LiveData<Playlist> = _playlist

    private val _sheetStateContent = MutableLiveData(PlaylistScreenSheetStateContent.PlaylistMoreOptionSheetContent)
    val sheetStateContent: LiveData<PlaylistScreenSheetStateContent> = _sheetStateContent

    private val _deleteType = MutableLiveData(PlaylistScreenDeleteType.PLAYLIST)
    val deleteType: LiveData<PlaylistScreenDeleteType> = _deleteType

    private val _selectedMusic = MutableLiveData(Music.unknown)

    /**
     * selected music when clicked more option (MusicItem trailing icon)
     */
    val selectedMusic: LiveData<Music> = _selectedMusic

    fun getPlaylist(playlistID: Int) {
        repository.getPlaylist(playlistID) { mPlaylist ->
            _playlist.value = mPlaylist
        }
    }

    fun setSheetStateContent(sheetStateContent: PlaylistScreenSheetStateContent) {
        _sheetStateContent.value = sheetStateContent
    }

    fun setSelectedMusic(music: Music) {
        _selectedMusic.value = music
    }

    fun setDeleteType(type: PlaylistScreenDeleteType) {
        _deleteType.value = type
    }

    enum class PlaylistScreenSheetStateContent {
        DeletePlaylistSheetContent,
        PlaylistMoreOptionSheetContent,
        ChangePlaylistNameSheetContent
    }

    enum class PlaylistScreenDeleteType {
        MUSIC,
        PLAYLIST
    }

}