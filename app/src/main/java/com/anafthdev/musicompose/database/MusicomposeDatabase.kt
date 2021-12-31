package com.anafthdev.musicompose.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist

@Database(
    entities = [
        Music::class,
        Playlist::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseTypeConverter::class)
abstract class MusicomposeDatabase: RoomDatabase() {

    abstract fun musicDAO(): MusicDAO

    abstract fun playlistDAO(): PlaylistDAO

    companion object {
        private var INSTANCE: MusicomposeDatabase? = null

        fun getInstance(context: Context): MusicomposeDatabase {
            if (INSTANCE == null) {
                synchronized(MusicomposeDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context, MusicomposeDatabase::class.java, "music.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return INSTANCE!!
        }
    }
}
