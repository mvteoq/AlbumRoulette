package com.mvteo.albumroulette

import android.app.Application
import com.mvteo.albumroulette.data.AlbumDataSource
import com.mvteo.albumroulette.data.SettingsRepository
import com.mvteo.albumroulette.database.AlbumDatabase
import com.mvteo.albumroulette.database.AlbumRepository
import com.mvteo.albumroulette.retrofit.RetrofitClient

class AlbumRouletteApplication : Application() {
    lateinit var albumRepository: AlbumRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val database = AlbumDatabase.getDatabase(this)
        albumRepository = AlbumRepository(
            api = RetrofitClient.apiServiceInstance,
            dao = database.dao(),
            albums = AlbumDataSource.albums
        )
        settingsRepository = SettingsRepository(
            preferences = getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE),
            availableGenres = AlbumDataSource.albums.map { it.genre }.distinct().sorted()
        )
    }

    private companion object {
        const val SETTINGS_FILE = "album_roulette_settings"
    }
}
