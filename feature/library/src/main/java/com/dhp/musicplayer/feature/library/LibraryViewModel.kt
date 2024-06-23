package com.dhp.musicplayer.feature.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.model.music.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val musicRepository: MusicRepository,
) : ViewModel() {
    val playlistWithSongs = musicRepository.getAllPlaylistWithSongs()

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