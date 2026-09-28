package com.mvteo.albumroulette.ui.screens.roulette

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mvteo.albumroulette.R
import com.mvteo.albumroulette.model.AlbumStatus
import com.mvteo.albumroulette.model.RouletteAlbum
import com.mvteo.albumroulette.ui.reusablecomponents.AlbumArtwork
import com.mvteo.albumroulette.ui.reusablecomponents.ErrorMessage
import com.mvteo.albumroulette.ui.reusablecomponents.InfoMessage
import com.mvteo.albumroulette.ui.reusablecomponents.LoadingIndicator
import com.mvteo.albumroulette.ui.reusablecomponents.albumMetadata
import com.mvteo.albumroulette.ui.reusablecomponents.albumStatusLabel
import com.mvteo.albumroulette.ui.theme.AlbumRouletteTheme

@Composable
fun RouletteScreen(
    viewModel: RouletteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    RouletteContent(
        uiState = uiState,
        onDrawAlbum = viewModel::drawAlbum,
        onSaveAlbum = viewModel::saveAlbum,
        modifier = modifier
    )
}

@Composable
fun RouletteContent(
    uiState: RouletteUiState,
    onDrawAlbum: () -> Unit,
    onSaveAlbum: (AlbumStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is RouletteUiState.Initial -> {
            RouletteMessage(
                message = stringResource(R.string.roulette_prompt),
                buttonText = stringResource(R.string.draw_album),
                onButtonClick = onDrawAlbum,
                modifier = modifier
            )
        }

        is RouletteUiState.Loading -> {
            LoadingIndicator(
                message = stringResource(R.string.drawing),
                modifier = modifier.fillMaxSize()
            )
        }

        is RouletteUiState.Error -> {
            Column(
                modifier = modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ErrorMessage(
                    message = stringResource(uiState.message),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = onDrawAlbum,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }

        is RouletteUiState.Success -> {
            AlbumDetails(
                uiState = uiState,
                onDrawAlbum = onDrawAlbum,
                onSaveAlbum = onSaveAlbum,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun RouletteMessage(
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        InfoMessage(message = message, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = onButtonClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(buttonText)
        }
    }
}

@Composable
private fun AlbumDetails(
    uiState: RouletteUiState.Success,
    onDrawAlbum: () -> Unit,
    onSaveAlbum: (AlbumStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val album = uiState.album
    val canSave = !uiState.isSaving && uiState.savedStatus == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AlbumArtwork(
            artworkUrl = album.artworkUrl,
            description = stringResource(R.string.artwork_description, album.title),
            modifier = Modifier.size(240.dp)
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = album.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = albumMetadata(
                genre = album.genre,
                releaseYear = album.releaseYear,
                trackCount = album.trackCount,
                durationMillis = album.durationMillis
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        AlbumStatus.entries.forEach { status ->
            if (status == AlbumStatus.TO_LISTEN) {
                Button(
                    onClick = { onSaveAlbum(status) },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(albumStatusLabel(status))
                }
            } else {
                OutlinedButton(
                    onClick = { onSaveAlbum(status) },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(albumStatusLabel(status))
                }
            }
        }

        if (uiState.isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Text(stringResource(R.string.saving))
        }
        if (uiState.savedStatus != null) {
            Text(
                stringResource(
                    R.string.saved_as,
                    albumStatusLabel(uiState.savedStatus)
                )
            )
        }
        if (uiState.saveErrorMessage != null) {
            Text(
                text = stringResource(uiState.saveErrorMessage),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        OutlinedButton(
            onClick = onDrawAlbum,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.draw_again))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RouletteInitialPreview() {
    AlbumRouletteTheme {
        RouletteContent(
            uiState = RouletteUiState.Initial,
            onDrawAlbum = {},
            onSaveAlbum = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RouletteSuccessPreview() {
    AlbumRouletteTheme {
        RouletteContent(
            uiState = RouletteUiState.Success(
                album = RouletteAlbum(
                    artist = "Tame Impala",
                    title = "Currents",
                    artworkUrl = "",
                    genre = "Alternative",
                    releaseYear = 2015,
                    trackCount = 13
                )
            ),
            onDrawAlbum = {},
            onSaveAlbum = {}
        )
    }
}
