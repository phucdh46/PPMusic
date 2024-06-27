package com.dhp.musicplayer.core.database.model

import androidx.room.Entity

@Entity(primaryKeys = ["songId", "artistId"])
data class SongArtistRelated(
    val songId: String,
    val artistId: String
)