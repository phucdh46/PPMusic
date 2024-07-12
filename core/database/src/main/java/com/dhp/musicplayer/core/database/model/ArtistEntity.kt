package com.dhp.musicplayer.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Artist")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val thumbnailUrl: String?,
    val subscribersCountText: String?,
)