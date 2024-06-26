package com.dhp.musicplayer.core.network.source

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhp.musicplayer.core.network.innertube.Innertube
import com.dhp.musicplayer.core.network.innertube.InnertubeApiService
import com.dhp.musicplayer.core.network.innertube.model.MusicShelfRenderer
import com.dhp.musicplayer.core.network.innertube.model.bodies.ContinuationBody
import com.dhp.musicplayer.core.network.innertube.model.bodies.SearchBody

class SearchResultPagingSource<T : Innertube.Item>(
    val context: Context,
    val query: String,
    private val paramsRequest: String,
    private val fromMusicShelfRendererContent: (MusicShelfRenderer.Content) -> T?
) : PagingSource<String, T>() {
    override fun getRefreshKey(state: PagingState<String, T>): String {
        return ""
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, T> {
        val currentPageNumber = params.key
        val itemPage = if (currentPageNumber.isNullOrEmpty()) {
            InnertubeApiService.getInstance(context).searchPage(
                body = SearchBody(
//                    browseId = "VLOLAK5uy_kegDCGxnGcRoh3KOJPQtBp8_7VvTAMWDk",
//                    browseId = "MPADUCU6cE7pdJPc6DU2jSrKEsdQ",
                    query = query,
                    params = paramsRequest
                ),
                fromMusicShelfRendererContent = fromMusicShelfRendererContent,
            )
        } else {
            InnertubeApiService.getInstance(context).searchPage(
                body = ContinuationBody(continuation = currentPageNumber),
                fromMusicShelfRendererContent = fromMusicShelfRendererContent,
            )
        }
        val nextKey = itemPage?.getOrNull()?.continuation

        return if (itemPage?.isSuccess == true) {
            LoadResult.Page(
                prevKey = null,
                nextKey = nextKey,
                data = itemPage.getOrNull()?.items ?: emptyList()
            )
        } else {
            val throwable = itemPage?.exceptionOrNull() ?: NullPointerException()
            LoadResult.Error(throwable)
        }
    }
}
