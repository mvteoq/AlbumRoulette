package com.mvteo.albumroulette

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mvteo.albumroulette.model.LanguageMode
import com.mvteo.albumroulette.model.ThemeMode
import com.mvteo.albumroulette.ui.screens.roulette.RouletteScreen
import com.mvteo.albumroulette.ui.screens.roulette.RouletteViewModel
import com.mvteo.albumroulette.ui.screens.saved.SavedScreen
import com.mvteo.albumroulette.ui.screens.saved.SavedViewModel
import com.mvteo.albumroulette.ui.screens.settings.SettingsScreen
import com.mvteo.albumroulette.ui.screens.settings.SettingsViewModel
import com.mvteo.albumroulette.ui.theme.AlbumRouletteTheme
import java.util.Locale

enum class AlbumRouletteDestinations(
    @StringRes val title: Int,
    @DrawableRes val icon: Int
) {
    ROULETTE(R.string.nav_roulette, R.drawable.ic_home),
    SAVED(R.string.nav_saved, R.drawable.ic_saved),
    SETTINGS(R.string.nav_settings, R.drawable.ic_settings)
}

@Composable
fun AlbumRouletteApp(
    navController: NavHostController = rememberNavController(),
    rouletteViewModel: RouletteViewModel = viewModel(factory = AlbumRouletteViewModelProvider.Factory),
    savedViewModel: SavedViewModel = viewModel(factory = AlbumRouletteViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AlbumRouletteViewModelProvider.Factory)
) {
    val settings by settingsViewModel.uiState.collectAsState()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    LocalizedApp(languageMode = settings.languageMode) {
        AlbumRouletteTheme(darkTheme = darkTheme) {
            AlbumRouletteContent(
                navController = navController,
                rouletteViewModel = rouletteViewModel,
                savedViewModel = savedViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

@Composable
fun LocalizedApp(
    languageMode: LanguageMode,
    content: @Composable () -> Unit
) {
    if (languageMode == LanguageMode.SYSTEM) {
        content()
        return
    }

    val context = LocalContext.current
    val currentConfiguration = LocalConfiguration.current
    val localizedContext = remember(context, currentConfiguration, languageMode) {
        val locale = when (languageMode) {
            LanguageMode.POLISH -> Locale.forLanguageTag("pl")
            LanguageMode.ENGLISH -> Locale.forLanguageTag("en")
            LanguageMode.SYSTEM -> error("System language uses the original context")
        }
        val configuration = Configuration(currentConfiguration).apply { setLocale(locale) }
        context.createConfigurationContext(configuration)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        content = content
    )
}

@Composable
private fun AlbumRouletteContent(
    navController: NavHostController,
    rouletteViewModel: RouletteViewModel,
    savedViewModel: SavedViewModel,
    settingsViewModel: SettingsViewModel
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
        ?: AlbumRouletteDestinations.ROULETTE.name

    Scaffold(
        bottomBar = {
            NavigationBar {
                AlbumRouletteDestinations.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.name,
                        onClick = {
                            if (currentRoute != destination.name) {
                                navController.navigate(destination.name) {
                                    popUpTo(AlbumRouletteDestinations.ROULETTE.name)
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(destination.title)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AlbumRouletteDestinations.ROULETTE.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AlbumRouletteDestinations.ROULETTE.name) {
                RouletteScreen(viewModel = rouletteViewModel)
            }
            composable(AlbumRouletteDestinations.SAVED.name) {
                SavedScreen(viewModel = savedViewModel)
            }
            composable(AlbumRouletteDestinations.SETTINGS.name) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
