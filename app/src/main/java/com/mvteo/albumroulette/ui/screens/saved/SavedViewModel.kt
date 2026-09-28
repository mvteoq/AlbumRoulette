package com.mvteo.albumroulette.ui.screens.saved

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvteo.albumroulette.R
import com.mvteo.albumroulette.database.AlbumRepository
import com.mvteo.albumroulette.model.AlbumStatus
import com.mvteo.albumroulette.model.SavedAlbum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SavedUiState {
    object Loading : SavedUiState

    data class Success(val albums: List<SavedAlbum>) : SavedUiState

    data class Error(@StringRes val message: Int) : SavedUiState
}

data class SavedFilterUiState(
    val selectedStatus: AlbumStatus = AlbumStatus.TO_LISTEN,
    val deletingAlbumId: Long? = null,
    @StringRes val deleteErrorMessage: Int? = null
)

class SavedViewModel(private val repository: AlbumRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<SavedUiState>(SavedUiState.Loading)
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    private val _filterUiState = MutableStateFlow(SavedFilterUiState())
    val filterUiState: StateFlow<SavedFilterUiState> = _filterUiState.asStateFlow()

    private var albumsJob: Job? = null

    init {
        observeAlbums(AlbumStatus.TO_LISTEN)
    }

    fun selectStatus(status: AlbumStatus) {
        if (filterUiState.value.selectedStatus == status && albumsJob?.isActive == true) return
        observeAlbums(status)
    }

    fun retryLoading() {
        observeAlbums(filterUiState.value.selectedStatus)
    }

    fun deleteAlbum(album: SavedAlbum) {
        if (filterUiState.value.deletingAlbumId != null) return

        _filterUiState.value = filterUiState.value.copy(
            deletingAlbumId = album.id,
            deleteErrorMessage = null
        )
        viewModelScope.launch {
            try {
                repository.deleteAlbum(album)
                _filterUiState.value = filterUiState.value.copy(deletingAlbumId = null)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _filterUiState.value = filterUiState.value.copy(
                    deletingAlbumId = null,
                    deleteErrorMessage = R.string.error_delete
                )
            }
        }
    }

    private fun observeAlbums(status: AlbumStatus) {
        albumsJob?.cancel()
        _filterUiState.value = filterUiState.value.copy(
            selectedStatus = status,
            deleteErrorMessage = null
        )
        _uiState.value = SavedUiState.Loading

        albumsJob = viewModelScope.launch {
            try {
                repository.getAlbumsByStatus(status).collect { albums ->
                    _uiState.value = SavedUiState.Success(albums = albums)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.value = SavedUiState.Error(message = R.string.error_read_saved)
            }
        }
    }
}
