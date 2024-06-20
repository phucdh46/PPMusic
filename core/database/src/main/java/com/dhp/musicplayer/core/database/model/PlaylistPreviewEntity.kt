package com.dhp.musicplayer.core.database.model

import androidx.room.Embedded

data class PlaylistPreviewEntity(
    @Embedded val playlist: PlaylistEntity,
    val songCount: Int
)
