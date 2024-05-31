package com.dhp.musicplayer.paging

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.innertube.model.MusicResponsiveListItemRenderer
import com.dhp.musicplayer.innertube.model.MusicTwoRowItemRenderer
import com.dhp.musicplayer.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.innertube.model.bodies.ContinuationBody
import com.dhp.musicplayer.utils.Logg

class ListMusicPagingSource<T : Innertube.Item>(
    val browseId: String,
    private val paramsRequest: String,
    private val context: Context,
    private val fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T? = { null },
    private val fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T? = { null },

    ) : PagingSource<String, T>() {
    override fun getRefreshKey(state: PagingState<String, T>): String {
        return state.anchorPosition.toString()
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, T> {
        val currentPageNumber = params.key
        Logg.d("load: $currentPageNumber - $browseId - $paramsRequest")
        val itemPage = if (currentPageNumber == null) {
            InnertubeApiService.getInstance(context).itemsPage(
                body = BrowseBody(
                    browseId = browseId,
                    params = paramsRequest
                ),
                fromMusicResponsiveListItemRenderer = fromMusicResponsiveListItemRenderer,
                fromMusicTwoRowItemRenderer = fromMusicTwoRowItemRenderer
            )
        } else {
            InnertubeApiService.getInstance(context).itemsPage(
                body = ContinuationBody(continuation = currentPageNumber),
                fromMusicResponsiveListItemRenderer = fromMusicResponsiveListItemRenderer,
                fromMusicTwoRowItemRenderer = fromMusicTwoRowItemRenderer
            )
        }
        Logg.d("load result: ${itemPage?.getOrNull()?.items?.size} - ${itemPage?.getOrNull()?.continuation}")

        val nextKey = itemPage?.getOrNull()?.continuation
        val data = itemPage?.getOrNull()?.items ?: emptyList()

        return LoadResult.Page(
            prevKey = null,
            nextKey = nextKey,
            data = data
        )
    }
}
