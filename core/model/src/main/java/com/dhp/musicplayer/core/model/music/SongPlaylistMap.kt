package com.dhp.musicplayer.core.model.music

data class SongPlaylistMap(
    val songId: String,
    val playlistId: Long,
    val position: Int
)