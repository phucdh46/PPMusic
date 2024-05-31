package com.dhp.musicplayer.ui.screens.playlist.local

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.model.PlaylistWithSongs
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.model.SongPlaylistMap
import com.dhp.musicplayer.ui.screens.playlist.navigation.LOCAL_PLAYLIST_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val savedStateHandle: SavedStateHandle,
    private val musicDao: MusicDao
    ): ViewModel() {
    val playlistId: StateFlow<Long?> = savedStateHandle.getStateFlow(LOCAL_PLAYLIST_ID_ARG, null)

    private val currentPlaylist: MutableState<PlaylistWithSongs?> = mutableStateOf(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<PlaylistWithSongs>> = playlistId.flatMapLatest { playlistId ->
        if (playlistId != null) {
            musicDao.playlistWithSongs(playlistId)
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
                musicDao.move(id, index, Int.MAX_VALUE)
                musicDao.delete(SongPlaylistMap(song.id, id, Int.MAX_VALUE))
            }
        }
    }

    fun updatePlaylist(playlistName: String) {
        currentPlaylist.value?.playlist?.let {  playlist ->
            viewModelScope.launch(Dispatchers.IO) {
                musicDao.update(playlist.copy(name = playlistName))
            }
        }
    }

    fun deletePlaylist() {
        currentPlaylist.value?.playlist?.let { playlist ->
            viewModelScope.launch(Dispatchers.IO) {
                musicDao.delete(playlist)
            }
        }
    }
}