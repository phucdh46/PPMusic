package com.dhp.musicplayer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView("SELECT * FROM SongPlaylistMap ORDER BY position")
data class SortedSongPlaylistMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val playlistId: Long,
    val position: Int
)
