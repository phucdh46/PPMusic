package com.dhp.musicplayer.data.repository

import android.content.Context
import com.dhp.musicplayer.data.datastore.ConfigApiKey
import com.dhp.musicplayer.data.datastore.dataStore
import com.dhp.musicplayer.data.datastore.get
import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.InnertubeApiService
import com.dhp.musicplayer.data.network.innertube.model.BrowseResult
import com.dhp.musicplayer.data.network.innertube.model.MoodAndGenres
import com.dhp.musicplayer.data.network.innertube.model.MusicShelfRenderer
import com.dhp.musicplayer.data.network.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.NextBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.PlayerBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.SearchBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.SearchSuggestionsBody
import com.dhp.musicplayer.data.network.innertube.model.response.PlayerResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkMusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): NetworkMusicRepository {

    override suspend fun relatedPage(body: NextBody): Result<Innertube.RelatedPage?>? {
        return InnertubeApiService.getInstance(context).relatedPage(body)
    }

    override suspend fun player(body: PlayerBody): Result<PlayerResponse>? {
        return InnertubeApiService.getInstance(context).player(body)
    }

    override suspend fun albumPage(body: BrowseBody): Result<Innertube.PlaylistOrAlbumPage>? {
        return InnertubeApiService.getInstance(context).albumPage(body)
    }

    override suspend fun playlistPage(body: BrowseBody): Result<Innertube.PlaylistOrAlbumPage>? {
        return InnertubeApiService.getInstance(context).playlistPage(body)
    }

    override suspend fun artistPage(body: BrowseBody): Result<Innertube.ArtistPage>? {
        return InnertubeApiService.getInstance(context).artistPage(body)
    }

    override suspend fun searchSuggestions(body: SearchSuggestionsBody): Result<List<String>?>? {
        return InnertubeApiService.getInstance(context).searchSuggestions(body)
    }

    override suspend fun moodAndGenres(): Result<List<MoodAndGenres>> {
        return InnertubeApiService.getInstance(context).moodAndGenres()
    }

    override suspend fun browse(browseId: String, params: String?): Result<BrowseResult> {
        return InnertubeApiService.getInstance(context).browse(browseId = browseId, params = params)
    }
}