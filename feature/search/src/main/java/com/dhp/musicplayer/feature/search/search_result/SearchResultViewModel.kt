package com.dhp.musicplayer.feature.search.search_result

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.Music
import com.dhp.musicplayer.feature.search.search_result.navigation.SEARCH_RESULT_QUERY_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val networkMusicRepository: NetworkMusicRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _searchFilter = MutableStateFlow("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
    val searchFilter: StateFlow<String> get() = _searchFilter

    val query: StateFlow<String?> = savedStateHandle.getStateFlow(SEARCH_RESULT_QUERY_ARG, null)

    private val filterMapSearchResult = mutableStateMapOf<String?, Flow<PagingData<Music>>?>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingData = combine(query, searchFilter) { query, params ->
        query to params
    }.flatMapLatest { (query, params) ->
        if (query != null) {
            if (filterMapSearchResult[params] == null) {
                filterMapSearchResult[params] =
                    networkMusicRepository.getSearchResult(query, params, viewModelScope)
            }
            filterMapSearchResult[params] ?: flowOf(PagingData.empty())
        } else {
            flowOf(PagingData.empty())
        }
    }

    fun resetSearchFilter() {
        filterMapSearchResult[searchFilter.value] = null
    }

    fun updateSearchFilter(params: String) {
        _searchFilter.value = params
    }

    fun onErrorInitPage() {
        filterMapSearchResult[_searchFilter.value] = null
    }
}