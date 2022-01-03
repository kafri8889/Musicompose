package com.anafthdev.musicompose.utils

import android.content.Context
import android.widget.Toast

object AppUtils {

    object PreferencesKey {
        const val SORT_MUSIC_OPTION = "sort_music_option"
        const val LAST_MUSIC_PLAYED = "last_music_played"
    }

    object PreferencesValue {
        const val SORT_MUSIC_BY_NAME = "sort_music_by_name"
        const val SORT_MUSIC_BY_ARTIST_NAME = "sort_music_by_artist_name"
        const val SORT_MUSIC_BY_DATE_ADDED = "sort_music_by_date_added"
    }

    /**
     * Checks if the specified element is contained in this collection with given [predicate].
     * @author kafri8889
     */
    fun <T> Collection<T>.containBy(predicate: (T) -> Boolean): Boolean {
        this.forEach {
            if (predicate(it)) return true
        }

        return false
    }

    /**
     * Return a item containing only elements matching the given [predicate].
     * @author kafri8889
     */
    fun <T> Collection<T>.get(predicate: (T) -> Boolean): T? {
        this.forEach {
            if (predicate(it)) return it
        }

        return null
    }

    fun Any.toast(context: Context, length: Int = Toast.LENGTH_SHORT) =
        Toast.makeText(context, this.toString(), length).show()
}