package com.dhp.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.dhp.musicplayer.core.database.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    fun favorites(): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND id = :songId")
    fun isFavorite(songId: String?): SongEntity?

    @Query("SELECT likedAt FROM Song WHERE id = :songId")
    fun likedAt(songId: String?): Flow<Long?>

    @Query("UPDATE Song SET likedAt = :likedAt WHERE id = :songId")
    fun favorite(songId: String, likedAt: Long?): Int
}