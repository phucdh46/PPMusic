package com.dhp.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dhp.musicplayer.core.database.model.SongEntity
import com.dhp.musicplayer.core.database.model.SongWithSongRelated
import com.dhp.musicplayer.core.database.model.SongWithRelatedPage
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    fun song(songId: String?): Flow<SongEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: SongEntity): Long

    @Query("SELECT * FROM Song")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRelatedSong(songWithSongRelated: SongWithSongRelated)

    @Transaction
    @Query("SELECT * FROM Song WHERE id = :songId")
    fun getSongWithRelatedPage(songId: String): Flow<SongWithRelatedPage?>

    @Query("DELETE FROM SongWithSongRelated")
    suspend fun clearAllSongRelated()
}