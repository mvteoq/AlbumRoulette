package com.mvteo.albumroulette.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mvteo.albumroulette.model.AlbumStatus
import com.mvteo.albumroulette.model.SavedAlbum
import kotlinx.coroutines.flow.Flow
@Dao
interface SavedAlbumDao {
    @Query("SELECT * FROM saved_albums")
    suspend fun getAllAlbums(): List<SavedAlbum>

    @Query("SELECT * FROM saved_albums WHERE status = :status ORDER BY savedAt DESC, id DESC")
    fun getAlbumsByStatus(status: AlbumStatus): Flow<List<SavedAlbum>>

    @Insert
    suspend fun insertAlbum(album: SavedAlbum): Long

    @Delete
    suspend fun deleteAlbum(album: SavedAlbum)
}
