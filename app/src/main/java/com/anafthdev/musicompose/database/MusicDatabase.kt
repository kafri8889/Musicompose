package com.anafthdev.musicompose.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.anafthdev.musicompose.model.Music

@Database(
    entities = [Music::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase: RoomDatabase() {

    abstract fun dao(): MusicDAO

    companion object {
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            if (INSTANCE == null) {
                synchronized(MusicDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context, MusicDatabase::class.java, "music.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return INSTANCE!!
        }
    }
}