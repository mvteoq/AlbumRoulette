package com.mvteo.albumroulette

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mvteo.albumroulette.ui.screens.roulette.RouletteViewModel
import com.mvteo.albumroulette.ui.screens.saved.SavedViewModel
import com.mvteo.albumroulette.ui.screens.settings.SettingsViewModel

object AlbumRouletteViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = this[APPLICATION_KEY] as AlbumRouletteApplication
            RouletteViewModel(
                repository = application.albumRepository,
                settingsRepository = application.settingsRepository
            )
        }
        initializer {
            val application = this[APPLICATION_KEY] as AlbumRouletteApplication
            SavedViewModel(repository = application.albumRepository)
        }
        initializer {
            val application = this[APPLICATION_KEY] as AlbumRouletteApplication
            SettingsViewModel(repository = application.settingsRepository)
        }
    }
}
