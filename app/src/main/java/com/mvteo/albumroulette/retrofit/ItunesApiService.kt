package com.mvteo.albumroulette.retrofit

import com.mvteo.albumroulette.model.ItunesArtistAlbumsResponse
import com.mvteo.albumroulette.model.ItunesArtistSearchResponse
import com.mvteo.albumroulette.model.ItunesLookupResponse
import com.mvteo.albumroulette.model.ItunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query
interface ItunesApiService {
    @GET("search?country=US&media=music&entity=album&limit=5")
    suspend fun search(@Query("term") term: String): ItunesSearchResponse

    @GET("search?country=US&media=music&entity=musicArtist&limit=10")
    suspend fun searchArtists(@Query("term") term: String): ItunesArtistSearchResponse

    @GET("lookup?country=US&entity=album&limit=200")
    suspend fun lookupArtistAlbums(@Query("id") artistId: Long): ItunesArtistAlbumsResponse

    @GET("lookup?country=US&entity=song")
    suspend fun lookup(@Query("id") collectionId: Long): ItunesLookupResponse
}
