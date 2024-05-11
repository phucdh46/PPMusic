package com.dhp.musicplayer.models

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import javax.annotation.concurrent.Immutable

@DatabaseView("SELECT * FROM SongPlaylistMap ORDER BY position")
data class SortedSongPlaylistMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val playlistId: Long,
    val position: Int
)
