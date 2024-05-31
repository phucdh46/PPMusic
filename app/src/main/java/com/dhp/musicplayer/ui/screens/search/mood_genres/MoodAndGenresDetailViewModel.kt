package com.dhp.musicplayer.ui.screens.search.mood_genres

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.innertube.model.BrowseResult
import com.dhp.musicplayer.innertube.InnertubeApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodAndGenresDetailViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val browseId: StateFlow<String?> =
        savedStateHandle.getStateFlow(MOOD_AND_GENRES_BROWSE_ID_ARG, null)
    val params: StateFlow<String?> = savedStateHandle.getStateFlow(MOOD_AND_GENRES_PARAMS_ARG, null)

    private val _moodAndGenresUiState: MutableStateFlow<UiState<BrowseResult?>> =
        MutableStateFlow(UiState.Loading)
    val moodAndGenresUiState = _moodAndGenresUiState.asStateFlow()

    init {
        getMoodAndGenresData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMoodAndGenresData() {
        viewModelScope.launch {
            combine(params, browseId) { browseIdValue, paramsValue ->
                Pair(browseIdValue, paramsValue)
            }.flatMapLatest { (browseIdValue, paramsValue) ->
                if (browseIdValue != null && paramsValue != null) {
                    val result = InnertubeApiService(application).browse(browseIdValue, paramsValue)
                    if (result.isSuccess && result.getOrNull() != null) {
                        flowOf(UiState.Success(result.getOrNull()))
                    } else {
                        flowOf(UiState.Error)
                    }
                } else {
                    flowOf(UiState.Error)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading
            ).collect {
                _moodAndGenresUiState.value = it
            }
        }
    }

    fun refreshMoonAndGenresData() {
        getMoodAndGenresData()
    }
}