package com.mvteo.albumroulette.retrofit

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://itunes.apple.com/"

    val apiServiceInstance: ItunesApiService by lazy {
        getClient().create(ItunesApiService::class.java)
    }

    private fun getClient(): Retrofit {
        val moshi = Moshi.Builder().build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
