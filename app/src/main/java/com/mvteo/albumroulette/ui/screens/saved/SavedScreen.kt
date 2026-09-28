package com.mvteo.albumroulette.ui.screens.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mvteo.albumroulette.R
import com.mvteo.albumroulette.model.AlbumStatus
import com.mvteo.albumroulette.model.SavedAlbum
import com.mvteo.albumroulette.ui.reusablecomponents.AlbumArtwork
import com.mvteo.albumroulette.ui.reusablecomponents.ErrorMessage
import com.mvteo.albumroulette.ui.reusablecomponents.InfoMessage
import com.mvteo.albumroulette.ui.reusablecomponents.LoadingIndicator
import com.mvteo.albumroulette.ui.reusablecomponents.albumMetadata
import com.mvteo.albumroulette.ui.reusablecomponents.albumStatusLabel
import com.mvteo.albumroulette.ui.theme.AlbumRouletteTheme

@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterUiState by viewModel.filterUiState.collectAsState()

    SavedContent(
        uiState = uiState,
        filterUiState = filterUiState,
        onSelectStatus = viewModel::selectStatus,
        onDeleteAlbum = viewModel::deleteAlbum,
        onRetry = viewModel::retryLoading,
        modifier = modifier
    )
}

@Composable
fun SavedContent(
    uiState: SavedUiState,
    filterUiState: SavedFilterUiState,
    onSelectStatus: (AlbumStatus) -> Unit,
    onDeleteAlbum: (SavedAlbum) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var albumToDelete by remember { mutableStateOf<SavedAlbum?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp)) {
            OutlinedButton(onClick = { menuExpanded = true }) {
                Text(
                    stringResource(
                        R.string.status_filter,
                        albumStatusLabel(filterUiState.selectedStatus)
                    )
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                AlbumStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(albumStatusLabel(status)) },
                        onClick = {
                            menuExpanded = false
                            onSelectStatus(status)
                        }
                    )
                }
            }
        }

        if (filterUiState.deleteErrorMessage != null) {
            Text(
                text = stringResource(filterUiState.deleteErrorMessage),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        when (uiState) {
            is SavedUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }

            is SavedUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ErrorMessage(
                        message = stringResource(uiState.message),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            is SavedUiState.Success -> {
                if (uiState.albums.isEmpty()) {
                    InfoMessage(
                        message = stringResource(R.string.empty_saved),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(items = uiState.albums, key = { it.id }) { album ->
                            SavedAlbumItem(
                                album = album,
                                isDeleting = filterUiState.deletingAlbumId == album.id,
                                canDelete = filterUiState.deletingAlbumId == null,
                                onDelete = { albumToDelete = album }
                            )
                        }
                    }
                }
            }
        }
    }

    albumToDelete?.let { album ->
        DeleteConfirmationDialog(
            album = album,
            onConfirm = {
                albumToDelete = null
                onDeleteAlbum(album)
            },
            onDismiss = { albumToDelete = null }
        )
    }
}

@Composable
private fun SavedAlbumItem(
    album: SavedAlbum,
    isDeleting: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember(album.id) { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlbumArtwork(
                    artworkUrl = album.artworkUrl,
                    description = stringResource(R.string.artwork_description, album.title),
                    modifier = Modifier.size(64.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDelete, enabled = canDelete) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = stringResource(
                                R.string.delete_album,
                                album.title
                            )
                        )
                    }
                }
            }
            if (expanded) {
                Text(
                    text = albumMetadata(
                        genre = album.genre,
                        releaseYear = album.releaseYear,
                        trackCount = album.trackCount,
                        durationMillis = album.durationMillis
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 88.dp, end = 12.dp, bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    album: SavedAlbum,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_album_title)) },
        text = { Text(stringResource(R.string.delete_album_message, album.title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SavedScreenPreview() {
    AlbumRouletteTheme {
        SavedContent(
            uiState = SavedUiState.Success(
                albums = listOf(
                    SavedAlbum(
                        id = 1,
                        artist = "Radiohead",
                        title = "OK Computer",
                        artworkUrl = "",
                        genre = "Alternative",
                        status = AlbumStatus.TO_LISTEN
                    )
                )
            ),
            filterUiState = SavedFilterUiState(),
            onSelectStatus = {},
            onDeleteAlbum = {},
            onRetry = {}
        )
    }
}
