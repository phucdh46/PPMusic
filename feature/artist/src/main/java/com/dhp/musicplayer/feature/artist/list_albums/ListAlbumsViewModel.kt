package com.dhp.musicplayer.feature.artist.list_albums

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.utils.from
import com.dhp.musicplayer.data.network.source.ListMusicPagingSource
import com.dhp.musicplayer.feature.artist.list_albums.navigation.LIST_ALBUMS_BROWSE_ID_ARG
import com.dhp.musicplayer.feature.artist.list_albums.navigation.LIST_ALBUMS_PARAMS_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject


@HiltViewModel
class ListAlbumsViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val browseId: StateFlow<String?> =
        savedStateHandle.getStateFlow(LIST_ALBUMS_BROWSE_ID_ARG, null)
    private val params: StateFlow<String?> =
        savedStateHandle.getStateFlow(LIST_ALBUMS_PARAMS_ARG, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingData = browseId.combine(params) { browseIdValue, paramsValue ->
        Pair(browseIdValue, paramsValue)
    }.flatMapLatest { (browseIdValue, paramsValue) ->
        if (browseIdValue != null && paramsValue != null) {
            Pager(
                config = PagingConfig(pageSize = 20),
                initialKey = null,
                pagingSourceFactory = {
                    ListMusicPagingSource(
                        browseId = browseIdValue,
                        paramsRequest = paramsValue,
                        context = application,
                        fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                    )
                }
            ).flow.cachedIn(viewModelScope)
        } else {
            flowOf(PagingData.empty())
        }
    }
}