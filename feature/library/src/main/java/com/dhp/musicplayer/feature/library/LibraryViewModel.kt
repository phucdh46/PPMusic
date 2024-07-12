package com.dhp.musicplayer.feature.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.model.music.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val musicRepository: MusicRepository,
) : ViewModel() {
    val playlistWithSongs = musicRepository.getAllPlaylistWithSongs().map {
        if (it.isEmpty()) UiState.Empty
        else UiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun createPlaylist(playlistName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.insert(Playlist(name = playlistName))
        }
    }

    fun updatePlaylist(
        playlistName: String,
        playlist: Playlist,
        onResultMessage: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.update(playlist.copy(name = playlistName))
            onResultMessage(context.getString(R.string.rename_playlist_success_message))
        }
    }

    fun deletePlaylist(playlist: Playlist, onResultMessage: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.delete(playlist)
            onResultMessage(
                context.getString(
                    R.string.delete_playlist_success_message,
                    playlist.name
                )
            )
        }
    }
}