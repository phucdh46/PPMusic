package com.dhp.musicplayer.feature.artist.list_songs

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.utils.from
import com.dhp.musicplayer.data.network.source.ListMusicPagingSource
import com.dhp.musicplayer.feature.artist.list_songs.navigation.LIST_SONGS_BROWSE_ID_ARG
import com.dhp.musicplayer.feature.artist.list_songs.navigation.LIST_SONGS_PARAMS_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ListSongsViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val browseId: StateFlow<String?> = savedStateHandle.getStateFlow(LIST_SONGS_BROWSE_ID_ARG, null)
    private val params: StateFlow<String?> = savedStateHandle.getStateFlow(LIST_SONGS_PARAMS_ARG, null)

    val pagingData = browseId.combine(params) { browseIdValue, paramsValue ->
        Pair(browseIdValue, paramsValue)
    }.map {(browseIdValue, paramsValue) ->
        if (browseIdValue != null && paramsValue != null) {
            val data = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { ListMusicPagingSource(browseId = browseIdValue,
                    paramsRequest = paramsValue,
                    context = application,
                    fromMusicResponsiveListItemRenderer = Innertube.SongItem.Companion::from
                ) }
            ).flow.cachedIn(viewModelScope)
            delay(500)
            UiState.Success(data)
        } else {
            UiState.Error
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )
}