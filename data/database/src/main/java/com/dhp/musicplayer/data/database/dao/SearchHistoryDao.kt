package com.dhp.musicplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhp.musicplayer.data.database.model.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistory WHERE query LIKE :query ORDER BY timestamp DESC")
    fun queries(query: String): Flow<List<SearchHistoryEntity>>

    @Query("SELECT * FROM SearchHistory ORDER BY timestamp DESC")
    fun getAllQueries(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchQuery: SearchHistoryEntity)

    @Delete
    fun delete(searchQuery: SearchHistoryEntity)

}