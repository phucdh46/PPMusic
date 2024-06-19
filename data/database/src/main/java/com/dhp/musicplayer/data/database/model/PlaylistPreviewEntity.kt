package com.dhp.musicplayer.data.database.model

import androidx.room.Embedded

data class PlaylistPreviewEntity(
    @Embedded val playlist: PlaylistEntity,
    val songCount: Int
)
