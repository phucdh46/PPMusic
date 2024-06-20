package com.dhp.musicplayer.feature.search.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.MoodAndGenres
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val networkMusicRepository: NetworkMusicRepository,
) : ViewModel() {

    private val _uiStateSearchScreen: MutableStateFlow<UiState<List<MoodAndGenres>>> =
        MutableStateFlow(UiState.Loading)
    val uiStateSearchScreen: StateFlow<UiState<List<MoodAndGenres>>> = _uiStateSearchScreen

    init {
        getMoodAndGenres()
    }

    private fun getMoodAndGenres() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = networkMusicRepository.moodAndGenres()
            if (result != null) {
                _uiStateSearchScreen.value = UiState.Success(result)
            } else {
                _uiStateSearchScreen.value = UiState.Error
            }
        }
    }

    fun refresh() {
        getMoodAndGenres()
    }
}