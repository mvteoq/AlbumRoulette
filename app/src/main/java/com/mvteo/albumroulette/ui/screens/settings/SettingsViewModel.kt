package com.mvteo.albumroulette.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.mvteo.albumroulette.data.SettingsRepository
import com.mvteo.albumroulette.model.AppSettings
import com.mvteo.albumroulette.model.LanguageMode
import com.mvteo.albumroulette.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val uiState: StateFlow<AppSettings> = repository.settings
    val availableGenres: List<String> = repository.availableGenres

    fun toggleGenre(genre: String) {
        repository.toggleGenre(genre)
    }

    fun selectAllGenres() {
        repository.selectAllGenres()
    }

    fun clearGenres() {
        repository.clearGenres()
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setLanguageMode(mode: LanguageMode) {
        repository.setLanguageMode(mode)
    }
}
