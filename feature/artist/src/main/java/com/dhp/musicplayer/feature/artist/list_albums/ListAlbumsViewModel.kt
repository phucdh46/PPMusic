package com.dhp.musicplayer.feature.artist.list_albums

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
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
    savedStateHandle: SavedStateHandle,
    private val networkMusicRepository: NetworkMusicRepository
) : ViewModel() {
    private val browseId: StateFlow<String?> =
        savedStateHandle.getStateFlow(LIST_ALBUMS_BROWSE_ID_ARG, null)
    private val params: StateFlow<String?> =
        savedStateHandle.getStateFlow(LIST_ALBUMS_PARAMS_ARG, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingData = browseId.combine(params) { browseIdValue, paramsValue ->
        Pair(browseIdValue, paramsValue)
    }.flatMapLatest { (browseIdValue, paramsValue) ->
        if (browseIdValue != null && paramsValue != null) {
            networkMusicRepository.getListAlbums(
                browseId = browseIdValue,
                params = paramsValue,
                scope = viewModelScope
            )
        } else {
            flowOf(PagingData.empty())
        }
    }
}