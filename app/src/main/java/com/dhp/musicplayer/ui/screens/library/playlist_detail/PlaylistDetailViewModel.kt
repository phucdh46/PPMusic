package com.dhp.musicplayer.ui.screens.library.playlist_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.model.PlaylistWithSongs
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.model.SongPlaylistMap
import com.dhp.musicplayer.ui.screens.library.navigation.PLAYLIST_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
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
class PlaylistDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val musicDao: MusicDao
    ): ViewModel() {
    val playlistId: StateFlow<Long?> = savedStateHandle.getStateFlow(PLAYLIST_ID_ARG, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlaylistDetailUiState> = playlistId.flatMapLatest { playlistId ->
        if (playlistId != null) {
            musicDao.playlistWithSongs(playlistId)
                .map { playlistEntity ->
                    if (playlistEntity != null) {
                        if (playlistEntity.songs.isEmpty()) {
                            PlaylistDetailUiState.Empty
                        } else {
                            PlaylistDetailUiState.Success(playlistEntity)
                        }
                    } else {
                        PlaylistDetailUiState.Error
                    }
                }
                .catch {
                    // Handle exceptions here, emit Error state if necessary
                    emit(PlaylistDetailUiState.Error)
                }
        } else {
            flowOf(PlaylistDetailUiState.Error) // Handle case where playlistId is null
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlaylistDetailUiState.Loading,
    )

    fun move(playlistId: Long, index: Int, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.move(playlistId, index, Int.MAX_VALUE)
            musicDao.delete(SongPlaylistMap(song.id, playlistId, Int.MAX_VALUE))
        }
    }

    sealed interface PlaylistDetailUiState {
        data object Loading : PlaylistDetailUiState
        data object Empty : PlaylistDetailUiState
        data object Error : PlaylistDetailUiState
        data class Success(val playlistWithSongs: PlaylistWithSongs?) : PlaylistDetailUiState
    }
}