package com.dhp.musicplayer.core.domain.repository

import androidx.paging.PagingData
import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.model.music.Artist
import com.dhp.musicplayer.core.model.music.ArtistPage
import com.dhp.musicplayer.core.model.music.MoodAndGenres
import com.dhp.musicplayer.core.model.music.MoodAndGenresDetail
import com.dhp.musicplayer.core.model.music.Music
import com.dhp.musicplayer.core.model.music.PlayerMedia
import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.PlaylistOrAlbumPage
import com.dhp.musicplayer.core.model.music.RelatedPage
import com.dhp.musicplayer.core.model.music.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface NetworkMusicRepository {
    suspend fun relatedPage(id: String): RelatedPage?
    suspend fun player(id: String, idDownload: Boolean = false):  PlayerMedia?
    suspend fun albumPage(browseId: String): PlaylistOrAlbumPage?
    suspend fun playlistPage(browseId: String): PlaylistOrAlbumPage?
    suspend fun artistPage(browseId: String): ArtistPage?
    suspend fun searchSuggestions(query: String): Result<List<String>?>?
    suspend fun moodAndGenres(): List<MoodAndGenres>?
    suspend fun browse(browseId: String, params: String?): MoodAndGenresDetail?
    suspend fun getListSongs(browseId: String, params: String, scope: CoroutineScope): Flow<PagingData<Song>>
    suspend fun getListAlbums(browseId: String, params: String, scope: CoroutineScope): Flow<PagingData<Album>>
    suspend fun getSearchResult(query: String, params: String, scope: CoroutineScope): Flow<PagingData<Music>>
    suspend fun getSearchResultAndroidAuto(query: String): List<Song>?
}