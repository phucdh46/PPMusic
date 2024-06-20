package com.dhp.musicplayer.core.model.music

data class PlaylistOrAlbumPage(
    val title: String?,
    val authors: String?,
    val year: String?,
    val thumbnail: String?,
    val url: String?,
    val songsPage: List<Song>?,
    val otherVersions: List<Album>?
)