package com.dhp.musicplayer.data.network.innertube

import android.annotation.SuppressLint
import android.util.Log
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.data.network.innertube.model.BrowseResult
import com.dhp.musicplayer.data.network.innertube.model.Context
import com.dhp.musicplayer.data.network.innertube.model.Context.Companion.clientDefaultAndroid
import com.dhp.musicplayer.data.network.innertube.model.Context.Companion.clientDefaultWeb
import com.dhp.musicplayer.data.network.innertube.model.GridRenderer
import com.dhp.musicplayer.data.network.innertube.model.MoodAndGenres
import com.dhp.musicplayer.data.network.innertube.model.MusicCarouselShelfRenderer
import com.dhp.musicplayer.data.network.innertube.model.MusicResponsiveListItemRenderer
import com.dhp.musicplayer.data.network.innertube.model.MusicShelfRenderer
import com.dhp.musicplayer.data.network.innertube.model.MusicTwoRowItemRenderer
import com.dhp.musicplayer.data.network.innertube.model.NavigationEndpoint
import com.dhp.musicplayer.data.network.innertube.model.Runs
import com.dhp.musicplayer.data.network.innertube.model.SectionListRenderer
import com.dhp.musicplayer.data.network.innertube.model.Thumbnail
import com.dhp.musicplayer.data.network.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.ContinuationBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.NextBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.PlayerBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.SearchBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.SearchSuggestionsBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.runCatchingNonCancellable
import com.dhp.musicplayer.data.network.innertube.model.response.BrowseResponse
import com.dhp.musicplayer.data.network.innertube.model.response.ContinuationResponse
import com.dhp.musicplayer.data.network.innertube.model.response.NextResponse
import com.dhp.musicplayer.data.network.innertube.model.response.PlayerResponse
import com.dhp.musicplayer.data.network.innertube.model.response.SearchResponse
import com.dhp.musicplayer.data.network.innertube.model.response.SearchSuggestionsResponse
import com.dhp.musicplayer.data.network.innertube.utils.findSectionByStrapline
import com.dhp.musicplayer.data.network.innertube.utils.findSectionByTitle
import com.dhp.musicplayer.data.network.innertube.utils.from
import com.dhp.musicplayer.data.network.innertube.utils.getConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.compression.ContentEncoder
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.brotli.dec.BrotliInputStream

internal object BrotliEncoder : ContentEncoder {
    override val name: String = "br"

    override fun CoroutineScope.encode(source: ByteReadChannel) =
        error("BrotliOutputStream not available (https://github.com/google/brotli/issues/715)")

    override fun CoroutineScope.decode(source: ByteReadChannel): ByteReadChannel =
        BrotliInputStream(source.toInputStream()).toByteReadChannel()
}

fun ContentEncoding.Config.brotli(quality: Float? = null) {
    customEncoder(BrotliEncoder, quality)
}

class InnertubeApiService(val context: android.content.Context) {
    private var config = context.getConfig()
    init {
        Logg.d("InnertubeApiService: $config")

    }
    private var client = HttpClient(OkHttp) {
        BrowserUserAgent()

        expectSuccess = true

        install(ContentNegotiation) {
            @OptIn(ExperimentalSerializationApi::class)
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            brotli()
        }

        defaultRequest {
            url(scheme = "https", host = config.host) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                headers.append(config.headerName, config.headerKey)
                parameters.append("prettyPrint", "false")
            }
        }
    }

    private val player = config.hostPlayer
    private val next = config.hostNext
    private val browse = config.hostBrowse
    private val search = config.hostSearch
    private val searchSuggestions = config.hostSuggestion

    val filterSong: String = config.filterSong
    val filterVideo: String = config.filterVideo
    val filterAlbum: String = config.filterAlbum
    val filterArtist: String = config.filterArtist
    val filterCommunityPlaylist: String = config.filterCommunityPlaylist
    val featuredPlaylist: String = config.featuredPlaylist

    private fun HttpRequestBuilder.mask(value: String = "*") =
        header(config.headerMask, value)

    val musicResponsiveListItemRendererMask =
        "musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint)"
    val musicTwoRowItemRendererMask =
        "musicTwoRowItemRenderer(thumbnailRenderer,title,subtitle,navigationEndpoint)"
    val playlistPanelVideoRendererMask =
        "playlistPanelVideoRenderer(title,navigationEndpoint,longBylineText,shortBylineText,thumbnail,lengthText)"

    suspend fun relatedPage(body: NextBody) = runCatchingNonCancellable {
        val nextResponse = client.post(next) {
            setBody(
                body.copy(
                    context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
                )
            )
            mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer(endpoint,title)")
        }.body<NextResponse>()

        val browseId = nextResponse
            .contents
            ?.singleColumnMusicWatchNextResultsRenderer
            ?.tabbedRenderer
            ?.watchNextTabbedResultsRenderer
            ?.tabs
            ?.getOrNull(2)
            ?.tabRenderer
            ?.endpoint
            ?.browseEndpoint
            ?.browseId
            ?: return@runCatchingNonCancellable null

        val response = client.post(browse) {
            setBody(BrowseBody(browseId = browseId).copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
            mask("contents.sectionListRenderer.contents.musicCarouselShelfRenderer(header.musicCarouselShelfBasicHeaderRenderer(title,strapline),contents($musicResponsiveListItemRendererMask,$musicTwoRowItemRendererMask))")
        }.body<BrowseResponse>()

        val sectionListRenderer = response
            .contents
            ?.sectionListRenderer

        Innertube.RelatedPage(
            songs = sectionListRenderer
                ?.findSectionByTitle("You might also like")
                ?.musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicResponsiveListItemRenderer)
                ?.mapNotNull(Innertube.SongItem::from),
            playlists = sectionListRenderer
                ?.findSectionByTitle("Recommended playlists")
                ?.musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.PlaylistItem::from)
                ?.sortedByDescending { it.channel?.name == "YouTube Music" },
            albums = sectionListRenderer
                ?.findSectionByStrapline("MORE FROM")
                ?.musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from),
            artists = sectionListRenderer
                ?.findSectionByTitle("Similar artists")
                ?.musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.ArtistItem::from),
        )
    }

    suspend fun player(body: PlayerBody) = runCatchingNonCancellable {
        val response = client.post(player) {
            setBody(
                body.copy(
                    context = Context(
                        client = clientDefaultAndroid.copy(
                            visitorData = config.visitorData
                        )
                    )
                )
            )
            mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,streamingData.expiresInSeconds,videoDetails.videoId")
        }.body<PlayerResponse>()

        if (response.playabilityStatus?.status == "OK") {
            Log.d("DHPP", "playabilityStatus: $response")
            response

        } else {
            @Serializable
            data class AudioStream(
                val url: String,
                val bitrate: Long
            )

            @Serializable
            data class PipedResponse(
                val audioStreams: List<AudioStream>
            )

            val safePlayerResponse = client.post(player) {
                setBody(
                    body.copy(
                        context = Context.DefaultAgeRestrictionBypass.copy(
                            thirdParty = Context.ThirdParty(
                                embedUrl = "${config.embedUrl}${body.videoId}"
                            )
                        ),
                    )
                )
                mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
            }.body<PlayerResponse>()

            if (safePlayerResponse.playabilityStatus?.status != "OK") {
                Log.d("DHPP", "safePlayerResponse: $response")
                return@runCatchingNonCancellable response
            }

            val audioStreams =
                client.get("https://watchapi.whatever.social/streams/${body.videoId}") {
                    contentType(ContentType.Application.Json)
                }.body<PipedResponse>().audioStreams
            Log.d("DHPP", "audioStreams: $audioStreams")

            safePlayerResponse.copy(
                streamingData = safePlayerResponse.streamingData?.copy(
                    adaptiveFormats = safePlayerResponse.streamingData.adaptiveFormats?.map { adaptiveFormat ->
                        adaptiveFormat.copy(
                            url = audioStreams.find { it.bitrate == adaptiveFormat.bitrate }?.url
                        )
                    }
                )
            )
        }
    }

    suspend fun albumPage(body: BrowseBody): Result<Innertube.PlaylistOrAlbumPage>? {
        return playlistPage(body.copy(
            context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
        ))?.map { album ->
            album.url?.let { Url(it).parameters["list"] }?.let { playlistId ->
                playlistPage(BrowseBody(browseId = "VL$playlistId").copy(
                        context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
                        ))?.getOrNull()?.let { playlist ->
                    album.copy(songsPage = playlist.songsPage)
                }
            } ?: album
        }?.map { album ->
            val albumInfo = Innertube.Info(
                name = album.title,
                endpoint = NavigationEndpoint.Endpoint.Browse(
                    browseId = body.browseId,
                    params = body.params
                )
            )

            album.copy(
                songsPage = album.songsPage?.copy(
                    items = album.songsPage.items?.map { song ->
                        song.copy(
                            authors = song.authors ?: album.authors,
                            album = albumInfo,
                            thumbnail = album.thumbnail
                        )
                    }
                )
            )
        }
    }

    suspend fun playlistPage(body: BrowseBody) = runCatchingNonCancellable {
        val response = client.post(browse) {
            setBody( body.copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
            mask("contents.singleColumnBrowseResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents(musicPlaylistShelfRenderer(continuations,contents.$musicResponsiveListItemRendererMask),musicCarouselShelfRenderer.contents.$musicTwoRowItemRendererMask),header.musicDetailHeaderRenderer(title,subtitle,thumbnail),microformat")
        }.body<BrowseResponse>()

        val musicDetailHeaderRenderer = response
            .header
            ?.musicDetailHeaderRenderer

        val sectionListRendererContents = response
            .contents
            ?.singleColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents

        val musicShelfRenderer = sectionListRendererContents
            ?.firstOrNull()
            ?.musicShelfRenderer

        val musicCarouselShelfRenderer = sectionListRendererContents
            ?.getOrNull(1)
            ?.musicCarouselShelfRenderer

        Innertube.PlaylistOrAlbumPage(
            title = musicDetailHeaderRenderer
                ?.title
                ?.text,
            thumbnail = musicDetailHeaderRenderer
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.firstOrNull(),
            authors = musicDetailHeaderRenderer
                ?.subtitle
                ?.splitBySeparator()
                ?.getOrNull(1)
                ?.map(Innertube::Info),
            year = musicDetailHeaderRenderer
                ?.subtitle
                ?.splitBySeparator()
                ?.getOrNull(2)
                ?.firstOrNull()
                ?.text,
            url = response
                .microformat
                ?.microformatDataRenderer
                ?.urlCanonical,
            songsPage = musicShelfRenderer
                ?.toSongsPage(),
            otherVersions = musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from)
        )
    }

    suspend fun playlistPage(body: ContinuationBody) = runCatchingNonCancellable {
        val response = client.post(browse) {
            setBody( body.copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
            mask("continuationContents.musicPlaylistShelfContinuation(continuations,contents.$musicResponsiveListItemRendererMask)")
        }.body<ContinuationResponse>()

        response
            .continuationContents
            ?.musicShelfContinuation
            ?.toSongsPage()
    }

    private fun MusicShelfRenderer?.toSongsPage() =
        Innertube.ItemsPage(
            items = this
                ?.contents
                ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                ?.mapNotNull(Innertube.SongItem::from),
            continuation = this
                ?.continuations
                ?.firstOrNull()
                ?.nextContinuationData
                ?.continuation
        )

    suspend fun artistPage(body: BrowseBody): Result<Innertube.ArtistPage>? =
        runCatchingNonCancellable {
            val response = client.post(browse) {
                setBody( body.copy(
                    context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
                ))
                mask("contents,header")
            }.body<BrowseResponse>()

            fun findSectionByTitle(text: String): SectionListRenderer.Content? {
                return response
                    .contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.get(0)
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.findSectionByTitle(text)
            }

            val songsSection = findSectionByTitle("Songs")?.musicShelfRenderer
            val albumsSection = findSectionByTitle("Albums")?.musicCarouselShelfRenderer
            val singlesSection = findSectionByTitle("Singles")?.musicCarouselShelfRenderer

            Innertube.ArtistPage(
                name = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.title
                    ?.text,
                description = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.description
                    ?.text,
                thumbnail = (response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.foregroundThumbnail
                    ?: response
                        .header
                        ?.musicImmersiveHeaderRenderer
                        ?.thumbnail)
                    ?.musicThumbnailRenderer
                    ?.thumbnail
                    ?.thumbnails
                    ?.getOrNull(0),
                shuffleEndpoint = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.playButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.watchEndpoint,
                radioEndpoint = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.startRadioButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.watchEndpoint,
                songs = songsSection
                    ?.contents
                    ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                    ?.mapNotNull(Innertube.SongItem::from),
                songsEndpoint = songsSection
                    ?.bottomEndpoint
                    ?.browseEndpoint,
                albums = albumsSection
                    ?.contents
                    ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                    ?.mapNotNull(Innertube.AlbumItem::from),
                albumsEndpoint = albumsSection
                    ?.header
                    ?.musicCarouselShelfBasicHeaderRenderer
                    ?.moreContentButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.browseEndpoint,
                singles = singlesSection
                    ?.contents
                    ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                    ?.mapNotNull(Innertube.AlbumItem::from),
                singlesEndpoint = singlesSection
                    ?.header
                    ?.musicCarouselShelfBasicHeaderRenderer
                    ?.moreContentButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.browseEndpoint,
            )
        }

    suspend fun <T : Innertube.Item> itemsPage(
        body: BrowseBody,
        fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T? = { null },
        fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T? = { null },
    ) = runCatchingNonCancellable {
        val response = client.post(browse) {
            setBody(body.copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
//        mask("contents.singleColumnBrowseResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents(musicPlaylistShelfRenderer(continuations,contents.$musicResponsiveListItemRendererMask),gridRenderer(continuations,items.$musicTwoRowItemRendererMask))")
        }.body<BrowseResponse>()

        val sectionListRendererContent = response
            .contents
            ?.singleColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()

        itemsPageFromMusicShelRendererOrGridRenderer(
            musicShelfRenderer = sectionListRendererContent
                ?.musicShelfRenderer,
            gridRenderer = sectionListRendererContent
                ?.gridRenderer,
            fromMusicResponsiveListItemRenderer = fromMusicResponsiveListItemRenderer,
            fromMusicTwoRowItemRenderer = fromMusicTwoRowItemRenderer,
        )
    }

    suspend fun <T : Innertube.Item> itemsPage(
        body: ContinuationBody,
        fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T? = { null },
        fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T? = { null },
    ) = runCatchingNonCancellable {
        val response = client.post(browse) {
            setBody(body.copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
//        mask("contents.singleColumnBrowseResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents(musicPlaylistShelfRenderer(continuations,contents.$musicResponsiveListItemRendererMask),gridRenderer(continuations,items.$musicTwoRowItemRendererMask))")
        }.body<ContinuationResponse>()

        itemsPageFromMusicShelRendererOrGridRenderer(
            musicShelfRenderer = response
                .continuationContents
                ?.musicShelfContinuation,
            gridRenderer = null,
            fromMusicResponsiveListItemRenderer = fromMusicResponsiveListItemRenderer,
            fromMusicTwoRowItemRenderer = fromMusicTwoRowItemRenderer,
        )
    }

    private fun <T : Innertube.Item> itemsPageFromMusicShelRendererOrGridRenderer(
        musicShelfRenderer: MusicShelfRenderer?,
        gridRenderer: GridRenderer?,
        fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T?,
        fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T?,
    ): Innertube.ItemsPage<T>? {
        return if (musicShelfRenderer != null) {
            Innertube.ItemsPage(
                continuation = musicShelfRenderer
                    .continuations
                    ?.firstOrNull()
                    ?.nextContinuationData
                    ?.continuation,
                items = musicShelfRenderer
                    .contents
                    ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                    ?.mapNotNull(fromMusicResponsiveListItemRenderer)
            )
        } else if (gridRenderer != null) {
            Innertube.ItemsPage(
                continuation = null,
                items = gridRenderer
                    .items
                    ?.mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
                    ?.mapNotNull(fromMusicTwoRowItemRenderer)
            )
        } else {
            null
        }
    }

    suspend fun <T : Innertube.Item> searchPage(
        body: SearchBody,
        fromMusicShelfRendererContent: (MusicShelfRenderer.Content) -> T?
    ) = runCatchingNonCancellable {
        val response = client.post(search) {
            setBody(body.copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
            mask("contents.tabbedSearchResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents.musicShelfRenderer(continuations,contents.$musicResponsiveListItemRendererMask)")
        }.body<SearchResponse>()

        response
            .contents
            ?.tabbedSearchResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.lastOrNull()
            ?.musicShelfRenderer
            ?.toItemsPage(fromMusicShelfRendererContent)
    }

    suspend fun <T : Innertube.Item> searchPage(
        body: ContinuationBody,
        fromMusicShelfRendererContent: (MusicShelfRenderer.Content) -> T?
    ) = runCatchingNonCancellable {
        val response = client.post(search) {
            setBody(body.copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
            mask("continuationContents.musicShelfContinuation(continuations,contents.$musicResponsiveListItemRendererMask)")
        }.body<ContinuationResponse>()

        response
            .continuationContents
            ?.musicShelfContinuation
            ?.toItemsPage(fromMusicShelfRendererContent)
    }

    private fun <T : Innertube.Item> MusicShelfRenderer?.toItemsPage(mapper: (MusicShelfRenderer.Content) -> T?) =
        Innertube.ItemsPage(
            items = this
                ?.contents
                ?.mapNotNull(mapper),
            continuation = this
                ?.continuations
                ?.firstOrNull()
                ?.nextContinuationData
                ?.continuation
        )

    suspend fun searchSuggestions(body: SearchSuggestionsBody) = runCatchingNonCancellable {
        val response = client.post(searchSuggestions) {
            setBody(body.copy(
                context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
            ))
            mask("contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query")
        }.body<SearchSuggestionsResponse>()

        response
            .contents
            ?.firstOrNull()
            ?.searchSuggestionsSectionRenderer
            ?.contents
            ?.mapNotNull { content ->
                content
                    .searchSuggestionRenderer
                    ?.navigationEndpoint
                    ?.searchEndpoint
                    ?.query
            }
    }

    suspend fun moodAndGenres(): Result<List<MoodAndGenres>> = runCatching {
        val response = client.post(browse) {
            setBody(BrowseBody(browseId = "FEmusic_moods_and_genres").copy(
                    context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
                    ))
        }
            .body<BrowseResponse>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents!!
            .mapNotNull(MoodAndGenres.Companion::fromSectionListRendererContent)
    }

    suspend fun browse(browseId: String, params: String?): Result<BrowseResult> = runCatching {
        val response = client.post(browse) {
            setBody(BrowseBody(browseId = browseId, params = params).copy(
                    context = Context(client = clientDefaultWeb.copy(visitorData = config.visitorData))
                    ))
        }.body<BrowseResponse>()
        BrowseResult(
            title = response.header?.musicHeaderRenderer?.title?.runs?.firstOrNull()?.text,
            items = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.mapNotNull { content ->
                when {
                    content.gridRenderer != null -> {
                        BrowseResult.Item(
                            title = content.gridRenderer.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text,
                            items = content.gridRenderer.items
                                ?.mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
                                ?.mapNotNull(Innertube.RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                        )
                    }

                    content.musicCarouselShelfRenderer != null -> {
                        BrowseResult.Item(
                            title = content.musicCarouselShelfRenderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text,
                            items = content.musicCarouselShelfRenderer.contents
                                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                                ?.mapNotNull(Innertube.RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                        )
                    }

                    else -> null
                }
            }.orEmpty()
        )
    }

    suspend fun nextPage(body: NextBody): Result<Innertube.NextPage>? =
        runCatchingNonCancellable {
            val response = client.post(next) {
                setBody(body)
                mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer.content.musicQueueRenderer.content.playlistPanelRenderer(continuations,contents(automixPreviewVideoRenderer,$playlistPanelVideoRendererMask))")
            }.body<NextResponse>()

            val tabs = response
                .contents
                ?.singleColumnMusicWatchNextResultsRenderer
                ?.tabbedRenderer
                ?.watchNextTabbedResultsRenderer
                ?.tabs

            val playlistPanelRenderer = tabs
                ?.getOrNull(0)
                ?.tabRenderer
                ?.content
                ?.musicQueueRenderer
                ?.content
                ?.playlistPanelRenderer

            if (body.playlistId == null) {
                val endpoint = playlistPanelRenderer
                    ?.contents
                    ?.lastOrNull()
                    ?.automixPreviewVideoRenderer
                    ?.content
                    ?.automixPlaylistVideoRenderer
                    ?.navigationEndpoint
                    ?.watchPlaylistEndpoint

                if (endpoint != null) {
                    return nextPage(
                        body.copy(
                            playlistId = endpoint.playlistId,
                            params = endpoint.params
                        )
                    )
                }
            }

            Innertube.NextPage(
                playlistId = body.playlistId,
                playlistSetVideoId = body.playlistSetVideoId,
                params = body.params,
                itemsPage = playlistPanelRenderer
                    ?.toSongsPage()
            )
        }

    suspend fun nextPage(body: ContinuationBody) = runCatchingNonCancellable {
        val response = client.post(next) {
            setBody(body)
            mask("continuationContents.playlistPanelContinuation(continuations,contents.$playlistPanelVideoRendererMask)")
        }.body<ContinuationResponse>()

        response
            .continuationContents
            ?.playlistPanelContinuation
            ?.toSongsPage()
    }

    private fun NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer?.toSongsPage() =
        Innertube.ItemsPage(
            items = this
                ?.contents
                ?.mapNotNull(NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer.Content::playlistPanelVideoRenderer)
                ?.mapNotNull(Innertube.SongItem::from),
            continuation = this
                ?.continuations
                ?.firstOrNull()
                ?.nextContinuationData
                ?.continuation
        )


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: InnertubeApiService? = null

        fun getInstance(context: android.content.Context): InnertubeApiService {

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: InnertubeApiService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

object Innertube {

    data class Info<T : NavigationEndpoint.Endpoint>(
        val name: String?,
        val endpoint: T?
    ) {
        @Suppress("UNCHECKED_CAST")
        constructor(run: Runs.Run) : this(
            name = run.text,
            endpoint = run.navigationEndpoint?.endpoint as T?
        )
    }

    sealed class Item {
        abstract val thumbnail: Thumbnail?
        abstract val key: String
    }

    data class RelatedPage(
        val songs: List<SongItem>? = null,
        val playlists: List<PlaylistItem>? = null,
        val albums: List<AlbumItem>? = null,
        val artists: List<ArtistItem>? = null,
    ) {
        companion object {
            fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): Item? {
                return when {
                    renderer.isAlbum -> AlbumItem.from(renderer)
                    renderer.isPlaylist -> PlaylistItem.from(renderer)
                    renderer.isArtist -> ArtistItem.from(renderer)
                    else -> null
                }
            }

        }
    }

    data class SongItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val album: Info<NavigationEndpoint.Endpoint.Browse>?,
        val durationText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.videoId!!
//        override val key get() = "r7Rn4ryE_w8"

//        fun asMusic() : Music {
//            return Music(
//                title = info?.name,
//                artist = authors?.joinToString("") { it.name ?: "" }
//            )
//        }

        companion object
    }

    data class PlaylistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val channel: Info<NavigationEndpoint.Endpoint.Browse>?,
        val songCount: Int?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!

        companion object
    }

    data class AlbumItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!

        companion object
    }

    data class ArtistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val subscribersCountText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!

        companion object
    }

    data class PlaylistOrAlbumPage(
        val title: String?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        val thumbnail: Thumbnail?,
        val url: String?,
        val songsPage: ItemsPage<SongItem>?,
        val otherVersions: List<AlbumItem>?
    )

    data class ItemsPage<T : Item>(
        val items: List<T>?,
        val continuation: String?
    )

    data class ArtistPage(
        val name: String?,
        val description: String?,
        val thumbnail: Thumbnail?,
        val shuffleEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val radioEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val songs: List<SongItem>?,
        val songsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val albums: List<AlbumItem>?,
        val albumsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val singles: List<AlbumItem>?,
        val singlesEndpoint: NavigationEndpoint.Endpoint.Browse?,
    )

    data class NextPage(
        val itemsPage: ItemsPage<SongItem>?,
        val playlistId: String?,
        val params: String? = null,
        val playlistSetVideoId: String? = null
    )
}