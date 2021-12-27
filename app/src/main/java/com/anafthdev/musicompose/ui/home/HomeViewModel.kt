package com.anafthdev.musicompose.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.anafthdev.musicompose.data.MusicRepositoryImpl
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.AppUtils
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.*

class HomeViewModel(private val repository: MusicRepositoryImpl): ViewModel() {

    private val _musicList = MutableLiveData<List<Music>>(emptyList())
    val musicList: LiveData<List<Music>> = _musicList

    fun getAllMusic(
        context: Context,
        sortOption: String = AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME
    ) = viewModelScope.launch {
        val collator = Collator.getInstance(context.resources.configuration.locales[0]).apply {
            strength = Collator.PRIMARY
        }

        repository.getAllMusic { list ->
            when (sortOption) {
                AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME -> {
                    Collections.sort(list, Comparator { o1, o2 ->
                        return@Comparator collator.compare(o1.title, o2.title)
                    })
                }
                AppUtils.PreferencesValue.SORT_MUSIC_BY_ARTIST_NAME -> {
                    Collections.sort(list, Comparator { o1, o2 ->
                        return@Comparator collator.compare(o1.artist, o2.artist)
                    })
                }
                AppUtils.PreferencesValue.SORT_MUSIC_BY_DATE_ADDED -> {
                    Collections.sort(list, Comparator { o1, o2 ->
                        return@Comparator collator.compare(o1.dateAdded.toString(), o2.dateAdded.toString())
                    })
                }
                else -> Collections.sort(list, Comparator { o1, o2 ->
                    return@Comparator collator.compare(o1.title, o2.title)
                })
            }

            _musicList.postValue(list)
        }
    }
}