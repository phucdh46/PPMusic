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

    @Query("UPDATE Song SET likedAt = :likedAt WHERE id = :songId")
    fun favorite(songId: String, likedAt: Long?): Int

    @Query("SELECT CASE\n" +
            "           WHEN likedAt IS NOT NULL THEN 1\n" +
            "           ELSE 0\n" +
            "       END AS isFavorite\n" +
            "FROM Song\n" +
            "WHERE id = :songId")
    fun isFavoriteSong(songId: String?): Flow<Boolean>
}