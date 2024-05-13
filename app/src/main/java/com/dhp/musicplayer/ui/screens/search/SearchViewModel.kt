package com.dhp.musicplayer.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.model.SearchHistory
import com.dhp.musicplayer.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicDao: MusicDao
) : ViewModel() {
    private val _searchHistories: MutableStateFlow<List<SearchHistory>> =
        MutableStateFlow(emptyList())
    val searchHistories = _searchHistories.asStateFlow()

    private val _songs: MutableStateFlow<List<Song>> = MutableStateFlow(emptyList())
    val songs = _songs.asStateFlow()

    init {
        getSearchHistory()
        getSongs()
    }

    private fun getSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.getAllQueries().collect {
                _searchHistories.value = it
            }
        }
    }

    private fun getSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.getAllSongs().collect {
                _songs.value = it
            }
        }
    }

    fun deleteSearchHistory(searchHistory: SearchHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.delete(searchHistory)
        }
    }

}