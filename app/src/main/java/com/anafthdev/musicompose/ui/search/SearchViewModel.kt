package com.anafthdev.musicompose.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anafthdev.musicompose.data.MusicomposeRepositoryImpl
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class SearchViewModel(
    private val repository: MusicomposeRepositoryImpl
): ViewModel() {

    private val _filteredMusic = MutableLiveData<List<Music>>()
    val filteredMusic: LiveData<List<Music>> = _filteredMusic

    private val _filteredArtist = MutableLiveData<List<Music>>()
    val filteredArtist: LiveData<List<Music>> = _filteredArtist

    private val _filteredAlbum = MutableLiveData<List<Music>>()
    val filteredAlbum: LiveData<List<Music>> = _filteredAlbum

    private val _playlist = MutableLiveData(Playlist.unknown)
    val playlist: LiveData<Playlist> = _playlist

    fun getPlaylist(playlistID: Int, action: () -> Unit = {}) {
        repository.getPlaylist(playlistID) { mPlaylist ->
            _playlist.value = mPlaylist
            action()
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist) {
            _playlist.value = playlist
        }
    }

    fun filter(s: String) {
        repository.getAllMusic { musicList ->
            typeValues.forEach { type ->
                if (s.isBlank()) {
                    when (type) {
                        TYPE_MUSIC -> _filteredMusic.value = emptyList()
                        TYPE_ARTIST -> _filteredArtist.value = emptyList()
                        TYPE_ALBUM -> _filteredAlbum.value = emptyList()
                        else -> _filteredMusic.value = emptyList()
                    }
                } else {
                    val filteredList = ArrayList<Music>()

                    for (music in musicList) {
                        when (type) {
                            TYPE_MUSIC -> {
                                val containTitle = music.title
                                    .lowercase(Locale.getDefault())
                                    .contains(s, true)

                                val containArtist = music.artist
                                    .lowercase(Locale.getDefault())
                                    .contains(s, true)

                                if (containTitle or containArtist) {
                                    filteredList.add(music)
                                }
                            }
                            TYPE_ARTIST -> {
                                if (music.artist
                                        .lowercase(Locale.getDefault())
                                        .contains(s, true)) {
                                    filteredList.add(music)
                                }
                            }
                            TYPE_ALBUM -> {
                                if (music.album
                                        .lowercase(Locale.getDefault())
                                        .contains(s, true)) {
                                    filteredList.add(music)
                                }
                            }
                            else -> {
                                if (
                                    music.title
                                        .lowercase(Locale.getDefault())
                                        .contains(s, true)
                                ) {
                                    filteredList.add(music)
                                }
                            }
                        }
                    }

                    when (type) {
                        TYPE_MUSIC -> _filteredMusic.value = filteredList
                        TYPE_ARTIST -> _filteredArtist.value = filteredList.distinctBy { it.artist }
                        TYPE_ALBUM -> _filteredAlbum.value = filteredList
                        else -> _filteredMusic.value = filteredList
                    }
                }
            }

            Timber.i("filtered music: ${filteredMusic.value}")
            Timber.i("filtered artist: ${filteredArtist.value}")
            Timber.i("filtered album: ${filteredAlbum.value}")
        }
    }

    companion object {
        private const val TYPE_MUSIC = "filter_music"
        private const val TYPE_ARTIST = "filter_artist"
        private const val TYPE_ALBUM = "filter_album"

        private val typeValues = listOf(
            TYPE_MUSIC,
            TYPE_ARTIST,
            TYPE_ALBUM
        )
    }
}