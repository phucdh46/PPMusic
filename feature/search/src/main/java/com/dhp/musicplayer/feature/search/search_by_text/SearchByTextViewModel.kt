package com.dhp.musicplayer.feature.search.search_by_text

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.SearchHistory
import com.dhp.musicplayer.feature.search.search_by_text.navigation.SEARCH_QUERY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchByTextViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val networkMusicRepository: NetworkMusicRepository,
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")

    private val _searchHistories: MutableStateFlow<List<SearchHistory>> =
        MutableStateFlow(emptyList())
    val searchHistories = _searchHistories.asStateFlow()

    val searchSuggestions = searchQuery.map { query ->
        networkMusicRepository.searchSuggestions(query = query)?.getOrNull()
    }

    init {
        getSearchHistory()
    }

    private fun getSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.getAllQueries().collect {
                _searchHistories.value = it
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    fun insertSearchHistory(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.insert(
                SearchHistory(
                    query = query,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteSearchHistory(searchHistory: SearchHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.delete(searchHistory)
        }
    }
}
