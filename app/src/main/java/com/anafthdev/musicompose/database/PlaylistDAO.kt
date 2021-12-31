package com.anafthdev.musicompose.database

import androidx.room.*
import com.anafthdev.musicompose.model.Playlist

@Dao
interface PlaylistDAO {

    @Query("SELECT * FROM playlist_table")
    fun getAllPlaylist(): List<Playlist>

    @Update
    fun update(playlist: Playlist)

    @Delete
    fun delete(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlist: List<Playlist>)

}