package com.dhp.musicplayer.core.model.music

data class PlaylistDisplay(
    val name: String = "",
    val thumbnailUrl: String?,
    val songs: List<Song>?,
    val year: String?,
 )
