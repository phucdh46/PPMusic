package com.dhp.musicplayer.ui.screens.artist

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.ui.screens.artist.navigation.ARTIST_DETAIL_BROWSE_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val browseId: StateFlow<String?> =
        savedStateHandle.getStateFlow(ARTIST_DETAIL_BROWSE_ID_ARG, null)

    val uiState: StateFlow<UiState<Innertube.ArtistPage>> =
        browseId.map { browseId ->
            if (browseId != null) {
                val result = withContext(Dispatchers.IO) {
                    try {
                        InnertubeApiService.getInstance(application)
                            .artistPage(BrowseBody(browseId = browseId))?.getOrNull()
                    } catch (e: Exception) {
                        null
                    }
                }
                if (result == null) {
                    UiState.Error
                } else {
                    UiState.Success(result)
                }
            } else {
                UiState.Error
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )
}

