package com.dhp.musicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.model.Playlist
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.model.SongPlaylistMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicDao: MusicDao
) : ViewModel() {
    val playlistPreview = musicDao.playlistPreviews()

    fun createPlaylist(playlistName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(Playlist(name = playlistName))
        }
    }

    fun updatePlaylist(playlistName: String, playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.update(playlist.copy(name = playlistName))
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.delete(playlist)
        }
    }

    fun addToPlaylist(playlist: Playlist, song: Song, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(song)
            musicDao.insert(
                SongPlaylistMap(
                    song.id,
                    playlist.id,
                    position
                )
            )
        }
    }
}