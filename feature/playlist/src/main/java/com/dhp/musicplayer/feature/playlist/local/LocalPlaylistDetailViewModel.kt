package com.dhp.musicplayer.feature.playlist.local

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.model.music.PlaylistWithSongs
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.model.music.SongPlaylistMap
import com.dhp.musicplayer.feature.playlist.local.navigation.LOCAL_PLAYLIST_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalPlaylistDetailViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository
) : ViewModel() {
    private val playlistId: StateFlow<Long?> = savedStateHandle.getStateFlow(LOCAL_PLAYLIST_ID_ARG, null)

    private val currentPlaylist: MutableState<PlaylistWithSongs?> = mutableStateOf(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<PlaylistWithSongs>> = playlistId.flatMapLatest { playlistId ->
        if (playlistId != null) {
            musicRepository.playlistWithSongs(playlistId)
                .map { playlistEntity ->
                    if (playlistEntity != null) {
                        if (playlistEntity.songs.isEmpty()) {
                            UiState.Empty
                        } else {
                            currentPlaylist.value = playlistEntity
                            UiState.Success(playlistEntity)
                        }
                    } else {
                        UiState.Error
                    }
                }
                .catch {
                    // Handle exceptions here, emit Error state if necessary
                    emit(UiState.Error)
                }
        } else {
            flowOf(UiState.Error) // Handle case where playlistId is null
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun removeSongInPlaylist(index: Int, song: Song) {
        currentPlaylist.value?.playlist?.id?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                musicRepository.moveSongInPlaylist(id, index, Int.MAX_VALUE)
                musicRepository.delete(SongPlaylistMap(song.id, id, Int.MAX_VALUE))
            }
        }
    }

    fun updatePlaylist(playlistName: String, onResultMessage: (String) -> Unit) {
        currentPlaylist.value?.playlist?.let { playlist ->
            viewModelScope.launch(Dispatchers.IO) {
                musicRepository.update(playlist.copy(name = playlistName))
                onResultMessage(context.getString(R.string.rename_playlist_success_message))
            }
        }
    }

    fun deletePlaylist() {
        currentPlaylist.value?.playlist?.let { playlist ->
            viewModelScope.launch(Dispatchers.IO) {
                musicRepository.delete(playlist)
            }
        }
    }
}