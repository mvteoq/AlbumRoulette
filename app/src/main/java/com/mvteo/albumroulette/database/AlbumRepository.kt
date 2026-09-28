package com.mvteo.albumroulette.database

import android.util.Log
import com.mvteo.albumroulette.model.AlbumStatus
import com.mvteo.albumroulette.model.CuratedAlbum
import com.mvteo.albumroulette.model.ItunesAlbum
import com.mvteo.albumroulette.model.RouletteAlbum
import com.mvteo.albumroulette.model.SavedAlbum
import com.mvteo.albumroulette.retrofit.ItunesApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

enum class DrawFailure {
    EMPTY_LIST,
    NO_GENRES,
    NO_NEW_ALBUMS,
    NO_MATCH
}

class AlbumDrawException(val reason: DrawFailure) : IllegalStateException(reason.name)

class AlbumRepository(
    private val api: ItunesApiService,
    private val dao: SavedAlbumDao,
    private val albums: List<CuratedAlbum>
) {
    suspend fun drawAlbum(
        allowedGenres: Set<String> = albums.map { it.genre }.toSet()
    ): RouletteAlbum {
        if (albums.isEmpty()) throw AlbumDrawException(DrawFailure.EMPTY_LIST)
        if (allowedGenres.isEmpty()) throw AlbumDrawException(DrawFailure.NO_GENRES)

        val savedAlbums = dao.getAllAlbums()
        val availableAlbums = albums.distinct().filter { candidate ->
            candidate.genre in allowedGenres && savedAlbums.none { saved ->
                normalizeText(saved.artist) == normalizeText(candidate.artist) &&
                    normalizeText(saved.title) == normalizeText(candidate.title)
            }
        }
        if (availableAlbums.isEmpty()) throw AlbumDrawException(DrawFailure.NO_NEW_ALBUMS)

        val candidate = availableAlbums.random()
        val normalizedArtist = normalizeText(candidate.artist)
        val normalizedTitle = normalizeText(candidate.title)
        if (normalizedArtist.isBlank() || normalizedTitle.isBlank()) {
            throw AlbumDrawException(DrawFailure.NO_MATCH)
        }

        val query = "${candidate.artist} ${candidate.title}"
        Log.d(TAG, "Searching iTunes for curated album: $query")
        val match = findAlbum(candidate, query, normalizedArtist)
            ?: throw AlbumDrawException(DrawFailure.NO_MATCH)

        return RouletteAlbum(
            artist = candidate.artist,
            title = candidate.title,
            artworkUrl = highResolutionArtworkUrl(match.artworkUrl100.orEmpty()),
            genre = candidate.genre,
            releaseYear = match.releaseDate?.take(4)?.toIntOrNull(),
            trackCount = match.trackCount,
            durationMillis = loadCompleteAlbumDuration(
                collectionId = match.collectionId,
                expectedTrackCount = match.trackCount
            )
        )
    }

    suspend fun addAlbum(album: RouletteAlbum, status: AlbumStatus): Long {
        val savedAlbum = SavedAlbum(
            artist = album.artist,
            title = album.title,
            artworkUrl = album.artworkUrl,
            genre = album.genre,
            releaseYear = album.releaseYear,
            trackCount = album.trackCount,
            durationMillis = album.durationMillis,
            status = status
        )
        return withContext(Dispatchers.IO) {
            dao.insertAlbum(savedAlbum)
        }
    }

    fun getAlbumsByStatus(status: AlbumStatus): Flow<List<SavedAlbum>> {
        return dao.getAlbumsByStatus(status)
    }

    suspend fun deleteAlbum(album: SavedAlbum) {
        withContext(Dispatchers.IO) {
            dao.deleteAlbum(album)
        }
    }

    private suspend fun findAlbum(
        candidate: CuratedAlbum,
        query: String,
        normalizedArtist: String
    ): ItunesAlbum? {
        val directMatch = api.search(query).results.take(5).firstOrNull { result ->
            normalizeText(result.artistName).contains(normalizedArtist) &&
                albumTitleMatches(candidate.title, result.collectionName)
        }
        if (directMatch != null) return directMatch

        val artist = api.searchArtists(candidate.artist).results.firstOrNull { result ->
            normalizeText(result.artistName) == normalizedArtist
        } ?: return null

        val album = api.lookupArtistAlbums(artist.artistId).results.firstOrNull { result ->
            result.wrapperType == "collection" &&
                result.collectionType == "Album" &&
                result.artistName?.let { normalizeText(it) == normalizedArtist } == true &&
                result.collectionName?.let { albumTitleMatches(candidate.title, it) } == true
        } ?: return null

        return ItunesAlbum(
            artistName = requireNotNull(album.artistName),
            collectionName = requireNotNull(album.collectionName),
            artworkUrl100 = album.artworkUrl100,
            collectionId = album.collectionId,
            releaseDate = album.releaseDate,
            trackCount = album.trackCount
        )
    }

    private suspend fun loadCompleteAlbumDuration(
        collectionId: Long?,
        expectedTrackCount: Int?
    ): Long? {
        if (collectionId == null || expectedTrackCount == null || expectedTrackCount <= 0) {
            return null
        }

        return try {
            val tracks = api.lookup(collectionId).results.filter { it.kind == "song" }
            if (tracks.size != expectedTrackCount || tracks.any { it.trackTimeMillis == null }) {
                null
            } else {
                tracks.sumOf { requireNotNull(it.trackTimeMillis) }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
    }

    private fun normalizeText(text: String): String {
        return text
            .lowercase(Locale.ROOT)
            .replace(Regex("\\([^)]*\\)|\\[[^]]*\\]"), " ")
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun albumTitleMatches(expectedTitle: String, resultTitle: String): Boolean {
        if (normalizeText(resultTitle) != normalizeText(expectedTitle)) return false

        val expectedVariants = variantMarkers(expectedTitle)
        val resultVariants = variantMarkers(resultTitle)
        return resultVariants.all { it in expectedVariants }
    }

    private fun variantMarkers(title: String): Set<String> {
        val normalizedTitle = title
            .lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return buildSet {
            if (Regex("\\bep\\b").containsMatchIn(normalizedTitle)) add("ep")
            if (Regex("\\bsingle\\b").containsMatchIn(normalizedTitle)) add("single")
            if (Regex("\\bremix(?:es|ed)?\\b").containsMatchIn(normalizedTitle)) add("remix")
            if (Regex("\\bb sides?\\b").containsMatchIn(normalizedTitle)) add("b-side")
            if (Regex("\\blive\\b").containsMatchIn(normalizedTitle)) add("live")
            if (Regex("\\binstrumental(?:s)?\\b").containsMatchIn(normalizedTitle)) add("instrumental")
            if (Regex("\\bkaraoke\\b").containsMatchIn(normalizedTitle)) add("karaoke")
            if (Regex("\\btribute\\b").containsMatchIn(normalizedTitle)) add("tribute")
        }
    }

    private fun highResolutionArtworkUrl(url: String): String {
        return url.replace("100x100", "1000x1000")
    }

    private companion object {
        const val TAG = "AlbumRepository"
    }
}
