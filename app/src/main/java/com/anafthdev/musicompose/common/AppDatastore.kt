package com.anafthdev.musicompose.common

import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.utils.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppDatastore(context: Context): ContextWrapper(context) {

    private val Context.datastore: DataStore<Preferences> by preferencesDataStore("app_datastore")

    private val appFirstInstall = booleanPreferencesKey(AppUtils.PreferencesKey.APP_FIRST_INSTALL)
    private val sortMusicOption = stringPreferencesKey(AppUtils.PreferencesKey.SORT_MUSIC_OPTION)
    private val lastMusicPlayed = longPreferencesKey(AppUtils.PreferencesKey.LAST_MUSIC_PLAYED)

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private fun postAction(action: () -> Unit) = Handler(Looper.getMainLooper()).post { action() }

    suspend fun setAppFirstInstall(isFirstInstall: Boolean, action: () -> Unit) {
        scope.launch {
            datastore.edit { preferences ->
                preferences[appFirstInstall] = isFirstInstall
            }
        }.invokeOnCompletion { postAction(action) }
    }

    fun setSortMusicOption(option: String, action: () -> Unit) {
        scope.launch {
            datastore.edit { preferences ->
                preferences[sortMusicOption] = option
            }
        }.invokeOnCompletion { postAction(action) }
    }

    fun setLastMusicPlayed(audioID: Long, action: () -> Unit) {
        scope.launch {
            datastore.edit { preferences ->
                preferences[lastMusicPlayed] = audioID
            }
        }.invokeOnCompletion { postAction(action) }
    }



    val isAppFirstInstall: Flow<Boolean> = datastore.data.map { preferences ->
        preferences[appFirstInstall] ?: true
    }

    val getSortMusicOption: Flow<String> = datastore.data.map { preferences ->
        preferences[sortMusicOption] ?: AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME
    }

    val getLastMusicPlayed: Flow<Long> = datastore.data.map { preferences ->
        preferences[lastMusicPlayed] ?: Music.unknown.audioID
    }

    companion object {
        private var INSTANCE: AppDatastore? = null

        fun getInstance(base: Context): AppDatastore {
            if (INSTANCE == null) {
                synchronized(AppDatastore::class.java) {
                    INSTANCE = AppDatastore(base)
                }
            }

            return INSTANCE!!
        }
    }

}