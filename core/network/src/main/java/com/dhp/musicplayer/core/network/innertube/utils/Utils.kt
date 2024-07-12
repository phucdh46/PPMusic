package com.dhp.musicplayer.core.network.innertube.utils

import android.content.Context
import com.dhp.musicplayer.core.model.settings.ApiKey
import com.dhp.musicplayer.core.datastore.ApiConfigKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import com.dhp.musicplayer.core.network.innertube.Innertube
import com.dhp.musicplayer.core.network.innertube.InnertubeApiService
import com.dhp.musicplayer.core.network.innertube.model.Runs
import com.dhp.musicplayer.core.network.innertube.model.SectionListRenderer
import com.dhp.musicplayer.core.network.innertube.model.bodies.ContinuationBody
import kotlinx.serialization.json.Json

internal fun SectionListRenderer.findSectionByTitle(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
        val title = content
            .musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.title
            ?: content
                .musicShelfRenderer
                ?.title

        title
            ?.runs
            ?.firstOrNull()
            ?.text == text
    }
}

internal fun SectionListRenderer.findSectionByStrapline(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
        content
            .musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.strapline
            ?.runs
            ?.firstOrNull()
            ?.text == text
    }
}

fun List<Runs.Run>.oddElements() = filterIndexed { index, _ ->
    index % 2 == 0
}

/*suspend fun Result<Innertube.PlaylistOrAlbumPage>.completed(context: Context): Result<Innertube.PlaylistOrAlbumPage>? {
    var playlistPage = getOrNull() ?: return null

    while (playlistPage.songsPage?.continuation != null) {
        val continuation = playlistPage.songsPage?.continuation!!
//        val otherPlaylistPageResult = Innertube.playlistPage(ContinuationBody(continuation = continuation)) ?: break
        val otherPlaylistPageResult = InnertubeApiService.getInstance(context).playlistPage(
            ContinuationBody(continuation = continuation)
        ) ?: break

        if (otherPlaylistPageResult.isFailure) break

        otherPlaylistPageResult.getOrNull()?.let { otherSongsPage ->
            playlistPage = playlistPage.copy(songsPage = playlistPage.songsPage + otherSongsPage)
        }
    }

    return Result.success(playlistPage)
}*/

infix operator fun <T : Innertube.Item> Innertube.ItemsPage<T>?.plus(other: Innertube.ItemsPage<T>) =
    other.copy(
        items = (this?.items?.plus(other.items ?: emptyList())
            ?: other.items)?.distinctBy(Innertube.Item::key)
    )

fun Context.getConfig(): ApiKey {
    return try {
        val key = dataStore[ApiConfigKey] ?: return ApiKey()
        Json.decodeFromString(ApiKey.serializer(), key)
    } catch (e: Exception) {
        ApiKey()
    }
}