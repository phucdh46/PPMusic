package com.dhp.musicplayer.feature.search.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.data.network.innertube.model.MoodAndGenres
import com.dhp.musicplayer.data.repository.NetworkMusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val application: Application,
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
            val data = result.getOrNull()
            if (result.isSuccess && data != null) {
                _uiStateSearchScreen.value = UiState.Success(data)
            } else {
                _uiStateSearchScreen.value = UiState.Error
            }
        }
    }

    fun refresh() {
        getMoodAndGenres()
    }
}