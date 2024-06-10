package com.dhp.musicplayer.ui.screens.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.R
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.extensions.toSong
import com.dhp.musicplayer.model.Playlist
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.model.SongPlaylistMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val musicDao: MusicDao
) : ViewModel() {
    val playlistWithSongs = musicDao.getAllPlaylistWithSongs()

    fun likeAt(songId: String?) = musicDao.likedAt(songId)

    fun favourite(mediaItem: MediaItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val likedAt = musicDao.isFavorite(mediaItem.mediaId) != null
            if (musicDao.favorite(
                    mediaItem.mediaId,
                    if (!likedAt) System.currentTimeMillis() else null
                ) == 0
            ) {
                musicDao.insert(mediaItem.toSong().toggleLike())
            }
        }
    }

    fun insertSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(song)
        }
    }

    fun createPlaylist(playlistName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(Playlist(name = playlistName))
        }
    }

    fun updatePlaylist(playlistName: String, playlist: Playlist, onResultMessage: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.update(playlist.copy(name = playlistName))
            onResultMessage(context.getString(R.string.rename_playlist_success_message))
        }
    }

    fun deletePlaylist(playlist: Playlist, onResultMessage: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.delete(playlist)
            onResultMessage(context.getString(R.string.delete_playlist_success_message, playlist.name))
        }
    }

    fun addToPlaylist(playlist: Playlist, song: Song, position: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(song)
            val id = musicDao.insert(
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