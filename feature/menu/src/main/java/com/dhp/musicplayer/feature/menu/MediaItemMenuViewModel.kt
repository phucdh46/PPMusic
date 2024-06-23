package com.dhp.musicplayer.feature.menu

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.model.music.SongPlaylistMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaItemMenuViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val musicRepository: MusicRepository,
) : ViewModel() {
    val playlistWithSongs = musicRepository.getAllPlaylistWithSongs()

    fun isFavoriteSong(songId: String?) = musicRepository.isFavoriteSong(songId)

    fun insertSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.insert(song)
        }
    }

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

    fun addToPlaylist(playlist: Playlist, song: Song, position: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.insert(song)
            val id = musicRepository.insert(
                SongPlaylistMap(
                    song.id,
                    playlist.id,
                    position
                )
            )
            onResult(id != -1L)
        }
    }
}