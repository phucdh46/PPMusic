package com.dhp.musicplayer.core.model.music

data class RelatedPage(
    val songs: List<Song>? = null,
    val playlists: List<Playlist>? = null,
    val albums: List<Album>? = null,
    val artists: List<Artist>? = null,
)