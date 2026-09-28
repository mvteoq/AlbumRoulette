package com.mvteo.albumroulette.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class ItunesSearchResponse(
    @Json(name = "results")
    val results: List<ItunesAlbum>
)
@JsonClass(generateAdapter = true)
data class ItunesAlbum(
    @Json(name = "artistName")
    val artistName: String,
    @Json(name = "collectionName")
    val collectionName: String,
    @Json(name = "artworkUrl100")
    val artworkUrl100: String? = null,
    @Json(name = "collectionId")
    val collectionId: Long? = null,
    @Json(name = "releaseDate")
    val releaseDate: String? = null,
    @Json(name = "trackCount")
    val trackCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class ItunesArtistSearchResponse(
    @Json(name = "results")
    val results: List<ItunesArtist>
)

@JsonClass(generateAdapter = true)
data class ItunesArtist(
    @Json(name = "artistId")
    val artistId: Long,
    @Json(name = "artistName")
    val artistName: String
)
@JsonClass(generateAdapter = true)
data class ItunesArtistAlbumsResponse(
    @Json(name = "results")
    val results: List<ItunesArtistAlbum>
)

@JsonClass(generateAdapter = true)
data class ItunesArtistAlbum(
    @Json(name = "wrapperType")
    val wrapperType: String? = null,
    @Json(name = "collectionType")
    val collectionType: String? = null,
    @Json(name = "artistName")
    val artistName: String? = null,
    @Json(name = "collectionName")
    val collectionName: String? = null,
    @Json(name = "artworkUrl100")
    val artworkUrl100: String? = null,
    @Json(name = "collectionId")
    val collectionId: Long? = null,
    @Json(name = "releaseDate")
    val releaseDate: String? = null,
    @Json(name = "trackCount")
    val trackCount: Int? = null
)
@JsonClass(generateAdapter = true)
data class ItunesLookupResponse(
    @Json(name = "results")
    val results: List<ItunesLookupItem>
)

@JsonClass(generateAdapter = true)
data class ItunesLookupItem(
    @Json(name = "wrapperType")
    val wrapperType: String? = null,
    @Json(name = "kind")
    val kind: String? = null,
    @Json(name = "trackTimeMillis")
    val trackTimeMillis: Long? = null
)
