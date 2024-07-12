package com.dhp.musicplayer.feature.artist.artist_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.ArtistPage
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
    savedStateHandle: SavedStateHandle,
    private val networkMusicRepository: NetworkMusicRepository,
) : ViewModel() {
    private val browseId: StateFlow<String?> =
        savedStateHandle.getStateFlow(ARTIST_DETAIL_BROWSE_ID_ARG, null)

    val uiState: StateFlow<UiState<ArtistPage>> =
        browseId.map { browseId ->
            if (browseId != null) {
                val result = withContext(Dispatchers.IO) {
                    try {
                        networkMusicRepository.artistPage(browseId = browseId)
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

