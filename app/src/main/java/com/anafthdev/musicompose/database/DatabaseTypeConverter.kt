package com.anafthdev.musicompose.database

import androidx.room.TypeConverter
import com.anafthdev.musicompose.model.Music
import com.google.gson.Gson

object DatabaseTypeConverter {

    @TypeConverter
    fun musicListToJSON(musicList: List<Music>) = Gson().toJson(musicList)!!

    @TypeConverter
    fun musicListFromJSON(musicListJSON: String) = Gson().fromJson(musicListJSON, Array<Music>::class.java).toList()
}