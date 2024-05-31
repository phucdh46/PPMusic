package com.dhp.musicplayer.model.display

import com.dhp.musicplayer.model.Song

data class PlaylistDisplay(
    val name: String = "",
    val thumbnailUrl: String?,
    val songs: List<Song>?
)
