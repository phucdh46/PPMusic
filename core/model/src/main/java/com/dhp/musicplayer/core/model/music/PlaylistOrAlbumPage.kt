package com.dhp.musicplayer.core.model.music

data class PlaylistOrAlbumPage(
    val title: String?,
    val year: String?,
    val url: String?,
    val songsPage: List<Song>?,
)