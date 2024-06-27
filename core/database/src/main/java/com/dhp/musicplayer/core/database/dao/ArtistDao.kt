package com.dhp.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhp.musicplayer.core.database.model.ArtistEntity
import com.dhp.musicplayer.core.database.model.SongArtistRelated

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artist: ArtistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongArtistRelated(crossRef: SongArtistRelated)

    @Query("DELETE FROM SongArtistRelated")
    suspend fun clearSongArtistRelated()
}