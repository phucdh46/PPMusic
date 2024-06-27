package com.dhp.musicplayer.core.database.model

import androidx.room.Entity

@Entity(primaryKeys = ["songId", "relatedSongId"])
data class SongWithSongRelated(
    val songId: String,
    val relatedSongId: String
)