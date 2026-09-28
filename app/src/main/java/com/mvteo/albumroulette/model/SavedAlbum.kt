package com.mvteo.albumroulette.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlbumStatus {
    TO_LISTEN,
    KNOWN,
    NOT_INTERESTED
}

@Entity(tableName = "saved_albums")
data class SavedAlbum(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val artist: String,
    val title: String,
    val artworkUrl: String,
    val genre: String,
    val releaseYear: Int? = null,
    val trackCount: Int? = null,
    val durationMillis: Long? = null,
    val status: AlbumStatus,
    val savedAt: Long = System.currentTimeMillis()
)
