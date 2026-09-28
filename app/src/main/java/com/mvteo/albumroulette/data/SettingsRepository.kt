package com.mvteo.albumroulette.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.mvteo.albumroulette.model.AppSettings
import com.mvteo.albumroulette.model.LanguageMode
import com.mvteo.albumroulette.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class SettingsRepository(
    private val preferences: SharedPreferences,
    val availableGenres: List<String>
) {
    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun readSettings(): AppSettings {
        val allGenres = availableGenres.toSet()
        val storedGenres = preferences.getStringSet(SELECTED_GENRES, null)?.toSet()
        val selectedGenres = storedGenres?.intersect(allGenres) ?: allGenres

        if (storedGenres != selectedGenres) {
            preferences.edit {
                putStringSet(SELECTED_GENRES, selectedGenres)
            }
        }

        val storedTheme = preferences.getString(THEME_MODE, ThemeMode.SYSTEM.name)
        val themeMode = ThemeMode.entries.firstOrNull { it.name == storedTheme }
            ?: ThemeMode.SYSTEM
        val storedLanguage = preferences.getString(LANGUAGE_MODE, LanguageMode.SYSTEM.name)
        val languageMode = LanguageMode.entries.firstOrNull { it.name == storedLanguage }
            ?: LanguageMode.SYSTEM

        return AppSettings(
            selectedGenres = selectedGenres,
            themeMode = themeMode,
            languageMode = languageMode
        )
    }

    fun toggleGenre(genre: String) {
        if (genre !in availableGenres) return

        val selectedGenres = settings.value.selectedGenres
        if (genre in selectedGenres) {
            setGenres(selectedGenres - genre)
        } else {
            setGenres(selectedGenres + genre)
        }
    }

    fun selectAllGenres() {
        setGenres(availableGenres.toSet())
    }

    fun clearGenres() {
        setGenres(emptySet())
    }

    private fun setGenres(genres: Set<String>) {
        preferences.edit { putStringSet(SELECTED_GENRES, genres) }
        _settings.value = settings.value.copy(selectedGenres = genres)
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.edit { putString(THEME_MODE, mode.name) }
        _settings.value = settings.value.copy(themeMode = mode)
    }

    fun setLanguageMode(mode: LanguageMode) {
        preferences.edit { putString(LANGUAGE_MODE, mode.name) }
        _settings.value = settings.value.copy(languageMode = mode)
    }

    private companion object {
        const val SELECTED_GENRES = "selected_genres"
        const val THEME_MODE = "theme_mode"
        const val LANGUAGE_MODE = "language_mode"
    }
}
