package com.mvteo.albumroulette.ui.screens.roulette

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvteo.albumroulette.R
import com.mvteo.albumroulette.data.SettingsRepository
import com.mvteo.albumroulette.database.AlbumDrawException
import com.mvteo.albumroulette.database.AlbumRepository
import com.mvteo.albumroulette.database.DrawFailure
import com.mvteo.albumroulette.model.AlbumStatus
import com.mvteo.albumroulette.model.RouletteAlbum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
sealed interface RouletteUiState {
    object Initial : RouletteUiState

    object Loading : RouletteUiState

    data class Success(
        val album: RouletteAlbum,
        val isSaving: Boolean = false,
        val savedStatus: AlbumStatus? = null,
        @StringRes val saveErrorMessage: Int? = null
    ) : RouletteUiState

    data class Error(@StringRes val message: Int) : RouletteUiState
}

class RouletteViewModel(
    private val repository: AlbumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RouletteUiState>(RouletteUiState.Initial)
    val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

    fun drawAlbum() {
        val currentState = uiState.value
        if (currentState is RouletteUiState.Loading) return
        if (currentState is RouletteUiState.Success && currentState.isSaving) return

        _uiState.value = RouletteUiState.Loading
        viewModelScope.launch {
            try {
                val album = repository.drawAlbum(
                    allowedGenres = settingsRepository.settings.value.selectedGenres
                )
                _uiState.value = RouletteUiState.Success(album = album)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = RouletteUiState.Error(message = errorMessage(error))
            }
        }
    }

    fun saveAlbum(status: AlbumStatus) {
        val currentState = uiState.value as? RouletteUiState.Success ?: return
        if (currentState.isSaving || currentState.savedStatus != null) return

        _uiState.value = currentState.copy(
            isSaving = true,
            saveErrorMessage = null
        )
        viewModelScope.launch {
            try {
                repository.addAlbum(currentState.album, status)
                _uiState.value = currentState.copy(savedStatus = status)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.value = currentState.copy(
                    saveErrorMessage = R.string.error_save
                )
            }
        }
    }

    @StringRes
    private fun errorMessage(error: Exception): Int {
        return when (error) {
            is IOException -> R.string.error_network
            is HttpException -> R.string.error_server
            is AlbumDrawException -> when (error.reason) {
                DrawFailure.EMPTY_LIST -> R.string.error_empty_curated
                DrawFailure.NO_GENRES -> R.string.error_no_genres
                DrawFailure.NO_NEW_ALBUMS -> R.string.error_no_new_albums
                DrawFailure.NO_MATCH -> R.string.error_no_match
            }
            else -> R.string.error_draw
        }
    }
}
