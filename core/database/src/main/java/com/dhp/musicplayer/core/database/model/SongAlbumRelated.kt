package com.dhp.musicplayer.core.database.model

import androidx.room.Entity

@Entity(primaryKeys = ["songId", "albumId"])
data class SongAlbumRelated(
    val songId: String,
    val albumId: String
)