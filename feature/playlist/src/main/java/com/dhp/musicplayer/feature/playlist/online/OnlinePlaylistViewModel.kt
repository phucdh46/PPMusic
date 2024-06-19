package com.dhp.musicplayer.feature.playlist.online

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.model.music.PlaylistDisplay
import com.dhp.musicplayer.core.ui.extensions.toSong
import com.dhp.musicplayer.data.network.innertube.model.bodies.BrowseBody
import com.dhp.musicplayer.data.network.innertube.utils.completed
import com.dhp.musicplayer.data.repository.NetworkMusicRepository
import com.dhp.musicplayer.feature.playlist.online.navigation.IS_ALBUM_ARG
import com.dhp.musicplayer.feature.playlist.online.navigation.ONLINE_PLAYLIST_ID_ARG
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
    private val networkMusicRepository: NetworkMusicRepository,
): ViewModel() {
    val browseId: StateFlow<String?> = savedStateHandle.getStateFlow(ONLINE_PLAYLIST_ID_ARG, null)
    private val isAlbum: StateFlow<Boolean> = savedStateHandle.getStateFlow(IS_ALBUM_ARG, false)

    val uiState: StateFlow<UiState<PlaylistDisplay>> =
        combine(browseId, isAlbum) { browseId, isAlbum ->
            if (browseId != null) {
                val result = withContext(Dispatchers.IO) {
                    try {
                        if (isAlbum) {
                            networkMusicRepository.albumPage(BrowseBody(browseId = browseId))?.completed(application)?.getOrNull()
                        } else {
                            networkMusicRepository.playlistPage(BrowseBody(browseId = browseId))?.completed(application)?.getOrNull()
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

