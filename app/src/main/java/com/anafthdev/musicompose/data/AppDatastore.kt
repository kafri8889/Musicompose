package com.anafthdev.musicompose.data

import android.content.Context
import android.content.ContextWrapper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anafthdev.musicompose.utils.AppUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppDatastore(context: Context): ContextWrapper(context) {

    private val Context.datastore: DataStore<Preferences> by preferencesDataStore("app_datastore")

    private val appFirstInstall = booleanPreferencesKey(AppUtils.PreferencesKey.APP_FIRST_INSTALL)
    private val sortMusicOption = stringPreferencesKey(AppUtils.PreferencesKey.SORT_MUSIC_OPTION)

    suspend fun setAppFirstInstall(isFirstInstall: Boolean) {
        datastore.edit { preferences ->
            preferences[appFirstInstall] = isFirstInstall
        }
    }

    suspend fun setSortMusicOption(s: String) {
        datastore.edit { preferences ->
            preferences[sortMusicOption] = s
        }
    }

    val isAppFirstInstall: Flow<Boolean> = datastore.data.map { preferences ->
        preferences[appFirstInstall] ?: true
    }

    val getSortMusicOption: Flow<String> = datastore.data.map { preferences ->
        preferences[sortMusicOption] ?: AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME
    }

    companion object {
        private var INSTANCE: AppDatastore? = null

        fun getInstance(base: Context): AppDatastore {
            if (INSTANCE == null) {
                synchronized(AppDatastore::class) {
                    INSTANCE = AppDatastore(base)
                }
            }

            return INSTANCE!!
        }
    }
}