package com.anafthdev.musicompose.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_table")
data class Music(
	@PrimaryKey val audioID: Long,
	@ColumnInfo(name = "displayName") val displayName: String,
	@ColumnInfo(name = "title") val title: String,
	@ColumnInfo(name = "artist") val artist: String,
	@ColumnInfo(name = "album") val album: String,
	@ColumnInfo(name = "duration") val duration: Long,
	@ColumnInfo(name = "albumPath") val albumPath: String?,
	@ColumnInfo(name = "path") val path: String,
	@ColumnInfo(name = "dateAdded") val dateAdded: Long,
) {
	companion object {

	}
}
