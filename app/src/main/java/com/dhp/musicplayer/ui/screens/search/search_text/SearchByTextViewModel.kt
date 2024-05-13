package com.dhp.musicplayer.ui.screens.search.search_text

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.model.SearchHistory
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.repository.MusicRepository
import com.dhp.musicplayer.ui.screens.search.navigation.SEARCH_QUERY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val musicDao: MusicDao
) : ViewModel() {

    private val _searchResult: MutableStateFlow<List<Song>?> = MutableStateFlow(null)
    val searchResult = _searchResult.asStateFlow()
    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(SearchHistory(query = query, timestamp = System.currentTimeMillis()))
            val result = musicRepository.search(query = query)
            if (result.isSuccess) {
                _searchResult.value = result.getOrNull()?.result?.items?.map {
                    Song(
                        id = it.info?.endpoint?.videoId ?: " ",
                        title = it.info?.name ?: "",
                        thumbnailUrl = it.thumbnail?.url,
                        artistsText = it.authors,
                        durationText = it.durationText
                    )
                }
            }
        }
    }
}

sealed interface SearchResultUiState {
    data object Loading : SearchResultUiState

    /**
     * The state query is empty or too short. To distinguish the state between the
     * (initial state or when the search query is cleared) vs the state where no search
     * result is returned, explicitly define the empty query state.
     */
    data object Empty : SearchResultUiState

    data object Failed : SearchResultUiState

    data class Success(
        val song: List<Song> = emptyList(),
    ) : SearchResultUiState {
        fun isEmpty(): Boolean = song.isEmpty()
    }

    /**
     * A state where the search contents are not ready. This happens when the *Fts tables are not
     * populated yet.
     */
    data object SearchNotReady : SearchResultUiState
}

