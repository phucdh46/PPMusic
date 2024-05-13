package com.dhp.musicplayer.ui.screens.library

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.model.Playlist
import com.dhp.musicplayer.model.PlaylistWithSongs
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.model.SongPlaylistMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicDao: MusicDao
): ViewModel() {

    private val _playlist: MutableLiveData<List<Playlist>?> = MutableLiveData()
    val playlist: LiveData<List<Playlist>?> = _playlist

    private val _playlistWithSongs: MutableLiveData<PlaylistWithSongs?> = MutableLiveData()
    val playlistWithSongs: LiveData<PlaylistWithSongs?> = _playlistWithSongs

    init {
        getPlaylists()
    }

    val playlistPreview = musicDao.playlistPreviews()
    fun getPlaylistWithSongs(id: Long) = musicDao.playlistWithSongs(id)

    fun renameRoomPlaylist(playlist: Playlist, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newPlaylist = playlist.copy(name = name)
            musicDao.update(newPlaylist)
        }
    }

    fun playlistWithSongs(id: Long) = musicDao.playlistWithSongs(id)

    fun addSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(song)
        }
    }

    private fun getPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.playlists().collect {
                _playlist.postValue(it)
            }
        }
    }

    fun createAndAddToPlaylist(playlistName: String, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDao.insert(song)
            val id = musicDao.insert(Playlist(name = playlistName))
            musicDao.insert(
                SongPlaylistMap(
                    song.id,
                    id,
                    0
                )
            )
        }
    }

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
        Log.d("DHP","addToPlaylist: $position - ${song.id} - ${playlist.id}")
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