package com.dhp.musicplayer.data.repository

import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.model.BrowseResult
import com.dhp.musicplayer.data.network.innertube.model.MoodAndGenres
import com.dhp.musicplayer.data.network.innertube.model.MusicShelfRenderer
import com.dhp.musicplayer.data.network.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.NextBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.PlayerBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.SearchBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.SearchSuggestionsBody
import com.dhp.musicplayer.data.network.innertube.model.response.PlayerResponse

interface NetworkMusicRepository {
    suspend fun relatedPage(body: NextBody):  Result<Innertube.RelatedPage?>?
    suspend fun player(body: PlayerBody):   Result<PlayerResponse>?
    suspend fun albumPage(body: BrowseBody): Result<Innertube.PlaylistOrAlbumPage>?
    suspend fun playlistPage(body: BrowseBody): Result<Innertube.PlaylistOrAlbumPage>?
    suspend fun artistPage(body: BrowseBody): Result<Innertube.ArtistPage>?
    suspend fun searchSuggestions(body: SearchSuggestionsBody): Result<List<String>?>?
    suspend fun moodAndGenres(): Result<List<MoodAndGenres>>
    suspend fun browse(browseId: String, params: String?): Result<BrowseResult>
}