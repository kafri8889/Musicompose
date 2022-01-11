package com.anafthdev.musicompose.model

import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anafthdev.musicompose.R
import kotlin.random.Random

@Entity(tableName = "playlist_table")
data class Playlist(
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "musicList") var musicList: List<Music>,
    @ColumnInfo(name = "defaultImage") @DrawableRes val defaultImage: Int? = null,
    @ColumnInfo(name = "default") var isDefault: Boolean = false,
    @PrimaryKey val id: Int = Random.nextInt()
) {
    companion object {
        val unknown = Playlist(
            name = "<unknown>",
            musicList = emptyList()
        )

        val favorite = Playlist(
            name = "Favorite",
            musicList = emptyList(),
            defaultImage = R.drawable.ic_favorite_image,
            isDefault = true,
            id = 0
        )

        val justPlayed = Playlist(
            name = "Just played",
            musicList = emptyList(),
            defaultImage = R.drawable.ic_just_played_image,
            isDefault = true,
            id = 1
        )
    }
}