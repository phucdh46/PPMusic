package com.dhp.musicplayer.feature.menu

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.model.music.SongPlaylistMap
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.data.repository.MusicRepository
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

    fun likeAt(songId: String?) = musicRepository.likedAt(songId)

    fun favourite(mediaItem: MediaItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val likedAt = musicRepository.isFavorite(mediaItem.mediaId) != null
            if (musicRepository.favorite(
                    mediaItem.mediaId,
                    if (!likedAt) System.currentTimeMillis() else null
                ) == 0
            ) {
                musicRepository.insert(mediaItem.toSong().toggleLike())
            }
        }
    }

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

    fun updatePlaylist(playlistName: String, playlist: Playlist, onResultMessage: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.update(playlist.copy(name = playlistName))
            onResultMessage(context.getString(R.string.rename_playlist_success_message))
        }
    }

    fun deletePlaylist(playlist: Playlist, onResultMessage: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.delete(playlist)
            onResultMessage(context.getString(R.string.delete_playlist_success_message, playlist.name))
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