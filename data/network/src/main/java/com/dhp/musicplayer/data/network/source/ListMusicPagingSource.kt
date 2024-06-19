package com.dhp.musicplayer.data.network.source

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.InnertubeApiService
import com.dhp.musicplayer.data.network.innertube.model.MusicResponsiveListItemRenderer
import com.dhp.musicplayer.data.network.innertube.model.MusicTwoRowItemRenderer
import com.dhp.musicplayer.data.network.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.data.network.innertube.model.bodies.ContinuationBody

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
        val nextKey = itemPage?.getOrNull()?.continuation
        val data = itemPage?.getOrNull()?.items ?: emptyList()

        return LoadResult.Page(
            prevKey = null,
            nextKey = nextKey,
            data = data
        )
    }
}
