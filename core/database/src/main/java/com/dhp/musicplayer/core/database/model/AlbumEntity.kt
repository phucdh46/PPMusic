package com.dhp.musicplayer.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Album")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val year: String? = null,
    val authorsText: String? = null,
)