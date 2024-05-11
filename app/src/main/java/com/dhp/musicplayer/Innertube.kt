package com.dhp.musicplayer

import com.dhp.musicplayer.models.Music
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CoroutineScope
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

object Innertube {

    val client = HttpClient(OkHttp) {
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
            url(scheme = "https", host = Constants.HOST) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                headers.append(Constants.HEADER_NAME, Constants.HEADER_KEY)
                parameters.append("prettyPrint", "false")
            }
        }
    }

//    internal const val player = Constants.HOST_PLAYER
    internal  val player = Constants.HOST_PLAYER
    internal fun HttpRequestBuilder.mask(value: String = "*") =
        header(Constants.HEADER_MASK, value)

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
    )

    data class SongItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val album: Info<NavigationEndpoint.Endpoint.Browse>?,
        val durationText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.videoId!!
//        override val key get() = "r7Rn4ryE_w8"

        fun asMusic() : Music {
            return Music(
                title = info?.name,
                artist = authors?.joinToString("") { it.name ?: "" }
            )
        }

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
}