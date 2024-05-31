package com.dhp.musicplayer.ui.screens.search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.innertube.MoodAndGenres
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val application: Application,
) : ViewModel() {

    private val _uiStateSearchScreen: MutableStateFlow<UiState<List<MoodAndGenres>>> =
        MutableStateFlow(UiState.Loading)
    val uiStateSearchScreen: StateFlow<UiState<List<MoodAndGenres>>> = _uiStateSearchScreen

    init {
        getMoodAndGenres()
    }

    private fun getMoodAndGenres() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = InnertubeApiService.getInstance(application).moodAndGenres()
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