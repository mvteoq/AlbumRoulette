package com.mvteo.albumroulette.model

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class LanguageMode {
    SYSTEM,
    POLISH,
    ENGLISH
}
data class AppSettings(
    val selectedGenres: Set<String>,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.SYSTEM
)
