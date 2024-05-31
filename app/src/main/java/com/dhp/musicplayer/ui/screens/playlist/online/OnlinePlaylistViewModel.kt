package com.dhp.musicplayer.ui.screens.playlist.online

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.innertube.utils.completed
import com.dhp.musicplayer.model.display.PlaylistDisplay
import com.dhp.musicplayer.ui.screens.playlist.navigation.ONLINE_PLAYLIST_ID_ARG
import com.dhp.musicplayer.ui.screens.playlist.navigation.IS_ALBUM_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {
    val browseId: StateFlow<String?> = savedStateHandle.getStateFlow(ONLINE_PLAYLIST_ID_ARG, null)
    private val isAlbum: StateFlow<Boolean> = savedStateHandle.getStateFlow(IS_ALBUM_ARG, false)

    val uiState: StateFlow<UiState<PlaylistDisplay>> =
        combine(browseId, isAlbum) { browseId, isAlbum ->
            if (browseId != null) {
                    val result = withContext(Dispatchers.IO) {
                        try {
                            if (isAlbum) {
                                InnertubeApiService.getInstance(application).albumPage(BrowseBody(browseId = browseId))?.completed(application)?.getOrNull()
                            } else {
                                InnertubeApiService.getInstance(application).playlistPage(BrowseBody(browseId = browseId))?.completed(application)?.getOrNull()
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (result == null) {
                        UiState.Error
                    } else {
                        UiState.Success(
                            PlaylistDisplay(
                                name = result.title.orEmpty(),
                                thumbnailUrl = result.thumbnail?.url,
                                songs = result.songsPage?.items?.map { it.toSong() } ?: emptyList()
                            )
                        )
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

