package com.anafthdev.musicompose.database

import androidx.room.*
import com.anafthdev.musicompose.model.Playlist

@Dao
interface PlaylistDAO {

    @Query("SELECT * FROM playlist_table")
    suspend fun getAllPlaylist(): List<Playlist>

    @Query("SELECT * FROM playlist_table WHERE id LIKE :playlistID")
    suspend fun getPlaylist(playlistID: Int): Playlist

    @Update
    suspend fun update(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: List<Playlist>)

}