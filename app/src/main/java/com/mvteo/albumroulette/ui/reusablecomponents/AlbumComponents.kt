package com.mvteo.albumroulette.ui.reusablecomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mvteo.albumroulette.R
import com.mvteo.albumroulette.model.AlbumStatus

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        if (message != null) {
            Text(text = message, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun InfoMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AlbumArtwork(
    artworkUrl: String,
    description: String,
    modifier: Modifier = Modifier
) {
    var artworkFailed by remember(artworkUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (artworkUrl.isBlank() || artworkFailed) {
            Text(
                text = stringResource(R.string.no_artwork),
                textAlign = TextAlign.Center
            )
        } else {
            AsyncImage(
                model = artworkUrl,
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onError = { artworkFailed = true }
            )
        }
    }
}

@Composable
fun albumStatusLabel(status: AlbumStatus): String {
    return stringResource(
        when (status) {
            AlbumStatus.TO_LISTEN -> R.string.status_to_listen
            AlbumStatus.KNOWN -> R.string.status_known
            AlbumStatus.NOT_INTERESTED -> R.string.status_not_interested
        }
    )
}

@Composable
fun albumMetadata(
    genre: String,
    releaseYear: Int?,
    trackCount: Int?,
    durationMillis: Long?
): String {
    val metadata = mutableListOf(genre)
    if (releaseYear != null) metadata.add(releaseYear.toString())
    if (trackCount != null) {
        metadata.add(pluralStringResource(R.plurals.track_count, trackCount, trackCount))
    }
    if (durationMillis != null) {
        metadata.add(stringResource(R.string.duration_minutes, durationMillis / 60_000L))
    }
    return metadata.joinToString(" • ")
}
