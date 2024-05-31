package com.dhp.musicplayer.ui.screens.search.search_result

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.utils.fromSearch
import com.dhp.musicplayer.paging.SearchResultPagingSource
import com.dhp.musicplayer.ui.screens.search.search_result.navigation.SEARCH_RESULT_QUERY_ARG
import com.dhp.musicplayer.utils.getConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _searchFilter = MutableStateFlow("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
    val searchFilter: StateFlow<String> get() = _searchFilter

    val query: StateFlow<String?> = savedStateHandle.getStateFlow(SEARCH_RESULT_QUERY_ARG, null)
    var config = application.getConfig()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingData = combine(query, searchFilter) { query, params ->
        query to params
    }.flatMapLatest { (query, params) ->
        if (query != null) {
            Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = {
                    SearchResultPagingSource(
                        context = application,
                        query = query,
                        paramsRequest = params,
                        fromMusicShelfRendererContent = when (params) {
                            config.filterSong -> Innertube.SongItem.Companion::fromSearch
                            config.filterAlbum -> Innertube.AlbumItem.Companion::fromSearch
                            config.filterArtist -> Innertube.ArtistItem.Companion::fromSearch
                            config.filterCommunityPlaylist -> Innertube.PlaylistItem.Companion::fromSearch
                            else -> Innertube.SongItem.Companion::fromSearch
                        }
                    )
                }
            ).flow.cachedIn(viewModelScope)
        } else {
            flowOf(PagingData.empty())
        }
    }

    fun updateSearchFilter(params: String) {
        _searchFilter.value = params
    }
}