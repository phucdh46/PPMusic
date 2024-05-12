package com.dhp.musicplayer.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dhp.musicplayer.enums.PlaylistSortBy
import com.dhp.musicplayer.enums.SortOrder
import com.dhp.musicplayer.model.Playlist
import com.dhp.musicplayer.model.PlaylistPreview
import com.dhp.musicplayer.model.PlaylistWithSongs
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.model.SongPlaylistMap
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlistWithSongs(id: Long): LiveData<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM Playlist")
    fun playlists(): Flow<List<Playlist>?>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY name ASC")
    fun playlistPreviewsByNameAsc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID ASC")
    fun playlistPreviewsByDateAddedAsc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY songCount ASC")
    fun playlistPreviewsByDateSongCountAsc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY name DESC")
    fun playlistPreviewsByNameDesc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID DESC")
    fun playlistPreviewsByDateAddedDesc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query("SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY songCount DESC")
    fun playlistPreviewsByDateSongCountDesc(): Flow<List<PlaylistPreview>>

    fun playlistPreviews(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ): Flow<List<PlaylistPreview>> {
        return when (sortBy) {
            PlaylistSortBy.Name -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByNameAsc()
                SortOrder.Descending -> playlistPreviewsByNameDesc()
            }
            PlaylistSortBy.SongCount -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByDateSongCountAsc()
                SortOrder.Descending -> playlistPreviewsByDateSongCountDesc()
            }
            PlaylistSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByDateAddedAsc()
                SortOrder.Descending -> playlistPreviewsByDateAddedDesc()
            }
        }
    }

    @Query("""
        UPDATE SongPlaylistMap SET position = 
          CASE 
            WHEN position < :fromPosition THEN position + 1
            WHEN position > :fromPosition THEN position - 1
            ELSE :toPosition
          END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition,:toPosition) and MAX(:fromPosition,:toPosition)
    """)
    fun move(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query("DELETE FROM SongPlaylistMap WHERE playlistId = :id")
    fun clearPlaylist(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(songPlaylistMap: SongPlaylistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSongPlaylistMaps(songPlaylistMaps: List<SongPlaylistMap>)

    @Update
    fun update(playlist: Playlist)

    @Delete
    fun delete(playlist: Playlist)

    @Delete
    fun delete(songPlaylistMap: SongPlaylistMap)

}