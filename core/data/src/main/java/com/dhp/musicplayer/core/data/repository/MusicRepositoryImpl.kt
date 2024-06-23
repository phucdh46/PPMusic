package com.dhp.musicplayer.core.data.repository

import com.dhp.musicplayer.core.data.model.asEntity
import com.dhp.musicplayer.core.data.model.asExternalModel
import com.dhp.musicplayer.core.database.dao.FavoriteDao
import com.dhp.musicplayer.core.database.dao.PlaylistDao
import com.dhp.musicplayer.core.database.dao.SearchHistoryDao
import com.dhp.musicplayer.core.database.dao.SongDao
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.PlaylistPreview
import com.dhp.musicplayer.core.model.music.PlaylistWithSongs
import com.dhp.musicplayer.core.model.music.SearchHistory
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.model.music.SongPlaylistMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val searchHistoryDao: SearchHistoryDao
) : MusicRepository {

    override fun playlistWithSongs(id: Long): Flow<PlaylistWithSongs?> {
        return playlistDao.playlistWithSongs(id).map { it?.asExternalModel() }
    }

    override fun getAllPlaylistWithSongs(): Flow<List<PlaylistWithSongs>> {
        return playlistDao.getAllPlaylistWithSongs()
            .map { it.map { playlists -> playlists.asExternalModel() } }
    }

    override fun playlistPreviewsByDateAddedDesc(): Flow<List<PlaylistPreview>> {
        return playlistDao.playlistPreviewsByDateAddedDesc()
            .map { it.map { previews -> previews.asExternalModel() } }
    }

    override fun moveSongInPlaylist(playlistId: Long, fromPosition: Int, toPosition: Int) {
        return playlistDao.move(
            playlistId = playlistId,
            fromPosition = fromPosition,
            toPosition = toPosition
        )
    }

    override fun favorites(): Flow<List<Song>> {
        return favoriteDao.favorites().map { flow -> flow.map { it.asExternalModel() } }
    }

    override fun favorite(songId: String, likedAt: Long?): Int {
        return favoriteDao.favorite(songId, likedAt)
    }

    override fun insert(playlist: Playlist): Long {
        return playlistDao.insert(playlist.asEntity())
    }

    override fun insert(songPlaylistMap: SongPlaylistMap): Long {
        return playlistDao.insert(songPlaylistMap.asEntity())
    }

    override fun update(playlist: Playlist) {
        return playlistDao.update(playlist.asEntity())
    }

    override fun delete(playlist: Playlist) {
        return playlistDao.delete(playlist.asEntity())
    }

    override fun delete(songPlaylistMap: SongPlaylistMap) {
        return playlistDao.delete(songPlaylistMap.asEntity())
    }

    override fun getAllQueries(): Flow<List<SearchHistory>> {
        return searchHistoryDao.getAllQueries()
            .map { it.map { searchHistories -> searchHistories.asExternalModel() } }
    }

    override fun insert(searchHistory: SearchHistory) {
        searchHistoryDao.insert(searchHistory.asEntity())
    }

    override fun delete(searchHistory: SearchHistory) {
        searchHistoryDao.delete(searchHistory.asEntity())
    }

    override fun insert(song: Song): Long {
        return songDao.insert(song.asEntity())
    }

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { it.map { song -> song.asExternalModel() } }
    }

    override fun song(id: String?): Flow<Song?> {
        return songDao.song(id).map { it?.asExternalModel() }
    }

    override fun toggleLike(song: Song) {
        val isFavorite = runBlocking { isFavoriteSong(song.id).first() }
        if (favorite(
                song.id,
                if (!isFavorite) System.currentTimeMillis() else null
            ) == 0
        ) {
            insert(song.toggleLike())
        }
    }

    override fun isFavoriteSong(songId: String?): Flow<Boolean> {
        return favoriteDao.isFavoriteSong(songId)
    }

    override fun getSongsAndroidAuto(): Flow<List<Song>> {
        return getAllSongs().map { songs ->
            songs.filter { song ->
                !song.isOffline
            }
        }
    }
}