package com.dhp.musicplayer.model

import androidx.room.Embedded

data class PlaylistPreview(
    @Embedded val playlist: Playlist,
    val songCount: Int
)
