package com.dhp.musicplayer.feature.artist.artist_detail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.data.repository.NetworkMusicRepository
import com.dhp.musicplayer.feature.artist.artist_detail.navigation.ARTIST_DETAIL_BROWSE_ID_ARG
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
    private val networkMusicRepository: NetworkMusicRepository,
) : ViewModel() {
    val browseId: StateFlow<String?> =
        savedStateHandle.getStateFlow(ARTIST_DETAIL_BROWSE_ID_ARG, null)

    val uiState: StateFlow<UiState<Innertube.ArtistPage>> =
        browseId.map { browseId ->
            if (browseId != null) {
                val result = withContext(Dispatchers.IO) {
                    try {
                        networkMusicRepository.artistPage(BrowseBody(browseId = browseId))?.getOrNull()
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

