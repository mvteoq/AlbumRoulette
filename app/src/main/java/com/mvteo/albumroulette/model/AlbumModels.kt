package com.mvteo.albumroulette.model
data class CuratedAlbum(
    val artist: String,
    val title: String,
    val genre: String
)
data class RouletteAlbum(
    val artist: String,
    val title: String,
    val artworkUrl: String,
    val genre: String,
    val releaseYear: Int? = null,
    val trackCount: Int? = null,
    val durationMillis: Long? = null
)
