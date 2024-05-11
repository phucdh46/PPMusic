package com.dhp.musicplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.models.Playlist
import com.dhp.musicplayer.models.PlaylistWithSongs
import com.dhp.musicplayer.models.Song
import com.dhp.musicplayer.models.SongPlaylistMap
import com.dhp.musicplayer.utils.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicDao: MusicDao
) : ViewModel() {

    private val _playlist: MutableLiveData<List<Playlist>?> = MutableLiveData()
    val playlist: LiveData<List<Playlist>?>  = _playlist

    private val _playlistWithSongs: MutableLiveData<PlaylistWithSongs?> = MutableLiveData()
    val playlistWithSongs: LiveData<PlaylistWithSongs?> = _playlistWithSongs

    init {
        getPlaylists()
    }

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
                   Log.d("getPlaylists: $it")
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

    fun addToPlaylist(playlist: Playlist, song: Song, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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