package com.dhp.musicplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dhp.musicplayer.data.database.model.SongEntity
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

}