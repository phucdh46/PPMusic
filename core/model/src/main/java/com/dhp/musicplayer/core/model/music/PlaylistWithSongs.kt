package com.dhp.musicplayer.core.model.music

data class PlaylistWithSongs(
    val playlist: Playlist,
    val songs: List<Song>
)
