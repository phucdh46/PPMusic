package com.dhp.musicplayer.core.domain.repository

import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.PlaylistPreview
import com.dhp.musicplayer.core.model.music.PlaylistWithSongs
import com.dhp.musicplayer.core.model.music.SearchHistory
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.model.music.SongPlaylistMap
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun playlistWithSongs(id: Long): Flow<PlaylistWithSongs?>

    fun getAllPlaylistWithSongs(): Flow<List<PlaylistWithSongs>>

    fun playlistPreviewsByDateAddedDesc(): Flow<List<PlaylistPreview>>
    fun moveSongInPlaylist(playlistId: Long, fromPosition: Int, toPosition: Int)

    fun favorites(): Flow<List<Song>>
    fun favorite(songId: String, likedAt: Long?): Int

    fun insert(playlist: Playlist): Long
    fun insert(songPlaylistMap: SongPlaylistMap): Long
    fun update(playlist: Playlist)
    fun delete(playlist: Playlist)
    fun delete(songPlaylistMap: SongPlaylistMap)

    fun getAllQueries(): Flow<List<SearchHistory>>
    fun insert(searchHistory: SearchHistory)
    fun delete(searchHistory: SearchHistory)

    fun insert(song: Song): Long
    fun getAllSongs(): Flow<List<Song>>
    fun song(id: String?): Flow<Song?>

    fun toggleLike(song: Song)
    fun isFavoriteSong(songId: String?): Flow<Boolean>

    fun getSongsAndroidAuto(): Flow<List<Song>>
}