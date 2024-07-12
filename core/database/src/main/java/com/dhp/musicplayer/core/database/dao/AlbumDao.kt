package com.dhp.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhp.musicplayer.core.database.model.AlbumEntity
import com.dhp.musicplayer.core.database.model.SongAlbumRelated

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongAlbumRelated(crossRef: SongAlbumRelated)

    @Query("DELETE FROM SongAlbumRelated")
    suspend fun clearSongAlbumRelated()
}