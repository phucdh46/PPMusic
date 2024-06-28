package com.dhp.musicplayer.core.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.dhp.musicplayer.core.data.model.asExternalDownloadModel
import com.dhp.musicplayer.core.data.model.asExternalModel
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.model.music.ArtistPage
import com.dhp.musicplayer.core.model.music.MoodAndGenres
import com.dhp.musicplayer.core.model.music.MoodAndGenresDetail
import com.dhp.musicplayer.core.model.music.Music
import com.dhp.musicplayer.core.model.music.PlayerMedia
import com.dhp.musicplayer.core.model.music.PlaylistOrAlbumPage
import com.dhp.musicplayer.core.model.music.RelatedPage
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.network.innertube.Innertube
import com.dhp.musicplayer.core.network.innertube.InnertubeApiService
import com.dhp.musicplayer.core.network.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.core.network.innertube.model.bodies.NextBody
import com.dhp.musicplayer.core.network.innertube.model.bodies.PlayerBody
import com.dhp.musicplayer.core.network.innertube.model.bodies.SearchBody
import com.dhp.musicplayer.core.network.innertube.model.bodies.SearchSuggestionsBody
import com.dhp.musicplayer.core.network.innertube.utils.completed
import com.dhp.musicplayer.core.network.innertube.utils.from
import com.dhp.musicplayer.core.network.innertube.utils.fromSearch
import com.dhp.musicplayer.core.network.source.ListMusicPagingSource
import com.dhp.musicplayer.core.network.source.SearchResultPagingSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NetworkMusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkMusicRepository {

    override suspend fun relatedPage(id: String): RelatedPage? {
        return InnertubeApiService.getInstance(context).relatedPage(NextBody(videoId = id))
            ?.getOrNull()?.asExternalModel()
    }

    override suspend fun player(id: String, idDownload: Boolean): PlayerMedia? {
        val result =
            InnertubeApiService.getInstance(context).player(PlayerBody(videoId = id))?.getOrNull()
        return if (!idDownload) {
            result?.asExternalModel()
        } else {
            result?.asExternalDownloadModel()
        }
    }

    override suspend fun albumPage(browseId: String): PlaylistOrAlbumPage? {
        return InnertubeApiService.getInstance(context).albumPage(BrowseBody(browseId = browseId))
            ?.completed(context)?.getOrNull()?.asExternalModel()
    }

    override suspend fun playlistPage(browseId: String): PlaylistOrAlbumPage? {
        return InnertubeApiService.getInstance(context)
            .playlistPage(BrowseBody(browseId = browseId))?.completed(context)?.getOrNull()
            ?.asExternalModel()
    }

    override suspend fun artistPage(browseId: String): ArtistPage? {
        return InnertubeApiService.getInstance(context).artistPage(BrowseBody(browseId = browseId))
            ?.getOrNull()?.asExternalModel()
    }

    override suspend fun searchSuggestions(query: String): Result<List<String>?>? {
        return InnertubeApiService.getInstance(context)
            .searchSuggestions(SearchSuggestionsBody(input = query))
    }

    override suspend fun moodAndGenres(): List<MoodAndGenres>? {
        return InnertubeApiService.getInstance(context).moodAndGenres().getOrNull()
            ?.map { it.asExternalModel() }
    }

    override suspend fun browse(browseId: String, params: String?): MoodAndGenresDetail? {
        return InnertubeApiService.getInstance(context).browse(browseId = browseId, params = params)
            .getOrNull()?.asExternalModel()
    }

    override suspend fun getListSongs(
        browseId: String,
        params: String,
        scope: CoroutineScope
    ): Flow<PagingData<Song>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                ListMusicPagingSource(
                    browseId = browseId,
                    paramsRequest = params,
                    context = context,
                    fromMusicResponsiveListItemRenderer = Innertube.SongItem.Companion::from
                )
            }
        ).flow.map { pagingData -> pagingData.map { it.asExternalModel() } }.cachedIn(scope)
    }

    override suspend fun getListAlbums(
        browseId: String,
        params: String,
        scope: CoroutineScope
    ): Flow<PagingData<Album>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                ListMusicPagingSource(
                    browseId = browseId,
                    paramsRequest = params,
                    context = context,
                    fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                )
            }
        ).flow.map { pagingData -> pagingData.map { it.asExternalModel() } }.cachedIn(scope)
    }

    override suspend fun getSearchResult(
        query: String,
        params: String,
        scope: CoroutineScope
    ): Flow<PagingData<Music>> {
        val musicApiService = InnertubeApiService.getInstance(context)
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                SearchResultPagingSource(
                    context = context,
                    query = query,
                    paramsRequest = params,
                    fromMusicShelfRendererContent =
                    when (params) {
                        musicApiService.filterSong -> Innertube.SongItem.Companion::fromSearch
                        musicApiService.filterAlbum -> Innertube.AlbumItem.Companion::fromSearch
                        musicApiService.filterArtist -> Innertube.ArtistItem.Companion::fromSearch
                        musicApiService.filterCommunityPlaylist -> Innertube.PlaylistItem.Companion::fromSearch
                        else -> Innertube.SongItem.Companion::fromSearch
                    }
                )
            }
        ).flow.map { pagingData ->
            pagingData.map {
                when (it) {
                    is Innertube.SongItem -> it.asExternalModel()
                    is Innertube.AlbumItem -> it.asExternalModel()
                    is Innertube.ArtistItem -> it.asExternalModel()
                    is Innertube.PlaylistItem -> it.asExternalModel()
                }
            }
        }.cachedIn(scope)
    }

    override suspend fun getSearchResultAndroidAuto(query: String): List<Song>? {
        val apiService = InnertubeApiService.getInstance(context)
        return apiService.searchPage(
            body = SearchBody(
                query = query,
                params = apiService.filterSong
            ),
            fromMusicShelfRendererContent =  Innertube.SongItem.Companion::fromSearch,
        )?.getOrNull()?.items?.map { it.asExternalModel() }
    }
}