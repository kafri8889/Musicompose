package com.anafthdev.musicompose.utils

import android.content.Context
import android.widget.Toast

object AppUtils {

    object PreferencesKey {
        const val APP_FIRST_INSTALL = "app_first_install"
        const val SORT_MUSIC_OPTION = "sort_music_option"
    }

    object PreferencesValue {
        const val SORT_MUSIC_BY_NAME = "sort_music_by_name"
        const val SORT_MUSIC_BY_ARTIST_NAME = "sort_music_by_artist_name"
        const val SORT_MUSIC_BY_DATE_ADDED = "sort_music_by_date_added"
    }

    /**
     * Returns a list element from given collection
     * @author kafri8889
     */
    fun <T, U> Collection<T>.getBy(selector: (T) -> U): List<U> {
        val result = ArrayList<U>()
        for (v in this) { result.add(selector(v)) }
        return result
    }

    fun Any.toast(context: Context, length: Int = Toast.LENGTH_SHORT) =
        Toast.makeText(context, this.toString(), length).show()
}