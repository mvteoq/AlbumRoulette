package com.mvteo.albumroulette.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mvteo.albumroulette.R
import com.mvteo.albumroulette.model.AppSettings
import com.mvteo.albumroulette.model.LanguageMode
import com.mvteo.albumroulette.model.ThemeMode
import com.mvteo.albumroulette.ui.theme.AlbumRouletteTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        genres = viewModel.availableGenres,
        onToggleGenre = viewModel::toggleGenre,
        onSelectAll = viewModel::selectAllGenres,
        onClearGenres = viewModel::clearGenres,
        onSetTheme = viewModel::setThemeMode,
        onSetLanguage = viewModel::setLanguageMode,
        modifier = modifier
    )
}

@Composable
fun SettingsContent(
    uiState: AppSettings,
    genres: List<String>,
    onToggleGenre: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearGenres: () -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetLanguage: (LanguageMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_genres),
            style = MaterialTheme.typography.titleLarge
        )
        Row {
            TextButton(onClick = onSelectAll) {
                Text(stringResource(R.string.select_all))
            }
            TextButton(onClick = onClearGenres) {
                Text(stringResource(R.string.select_none))
            }
        }
        genres.forEach { genre ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = genre in uiState.selectedGenres,
                        role = Role.Checkbox,
                        onValueChange = { onToggleGenre(genre) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = genre in uiState.selectedGenres,
                    onCheckedChange = null
                )
                Text(text = genre, modifier = Modifier.padding(start = 12.dp))
            }
        }
        if (uiState.selectedGenres.isEmpty()) {
            Text(
                text = stringResource(R.string.error_no_genres),
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Column(modifier = Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                SettingsOption(
                    text = stringResource(themeModeLabel(mode)),
                    selected = uiState.themeMode == mode,
                    onClick = { onSetTheme(mode) }
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Column(modifier = Modifier.selectableGroup()) {
            LanguageMode.entries.forEach { mode ->
                SettingsOption(
                    text = stringResource(languageModeLabel(mode)),
                    selected = uiState.languageMode == mode,
                    onClick = { onSetLanguage(mode) }
                )
            }
        }
    }
}

@Composable
private fun SettingsOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = text, modifier = Modifier.padding(start = 12.dp))
    }
}

@StringRes
private fun themeModeLabel(mode: ThemeMode): Int {
    return when (mode) {
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
        ThemeMode.SYSTEM -> R.string.theme_system
    }
}

@StringRes
private fun languageModeLabel(mode: LanguageMode): Int {
    return when (mode) {
        LanguageMode.SYSTEM -> R.string.language_system
        LanguageMode.POLISH -> R.string.language_polish
        LanguageMode.ENGLISH -> R.string.language_english
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AlbumRouletteTheme {
        SettingsContent(
            uiState = AppSettings(selectedGenres = setOf("Alternative")),
            genres = listOf("Alternative", "Rock"),
            onToggleGenre = {},
            onSelectAll = {},
            onClearGenres = {},
            onSetTheme = {},
            onSetLanguage = {}
        )
    }
}
