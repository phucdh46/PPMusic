package com.dhp.musicplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dhp.musicplayer.data.database.model.PlaylistEntity
import com.dhp.musicplayer.data.database.model.PlaylistPreviewEntity
import com.dhp.musicplayer.data.database.model.PlaylistWithSongsEntity
import com.dhp.musicplayer.data.database.model.SongPlaylistMapEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(songPlaylistMap: SongPlaylistMapEntity): Long



    @Update
    fun update(playlist: PlaylistEntity)

    @Delete
    fun delete(playlist: PlaylistEntity)

    @Delete
    fun delete(songPlaylistMap: SongPlaylistMapEntity)

    @Query(
        """
        UPDATE SongPlaylistMap SET position = 
          CASE 
            WHEN position < :fromPosition THEN position + 1
            WHEN position > :fromPosition THEN position - 1
            ELSE :toPosition
          END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition,:toPosition) and MAX(:fromPosition,:toPosition)
    """
    )
    fun move(playlistId: Long, fromPosition: Int, toPosition: Int)


    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlistWithSongs(id: Long): Flow<PlaylistWithSongsEntity?>

    @Transaction
    @Query("SELECT * FROM Playlist")
    fun getAllPlaylistWithSongs(): Flow<List<PlaylistWithSongsEntity>>


    @Transaction
    @Query("SELECT id, name, browseId, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID DESC")
    fun playlistPreviewsByDateAddedDesc(): Flow<List<PlaylistPreviewEntity>>


}