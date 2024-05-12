package com.dhp.musicplayer.model

class PlaylistWithSongsPreview(val playlistWithSongs: PlaylistWithSongs) {

    val playlist: Playlist get() = playlistWithSongs.playlist
    val songs: List<Song> get() = playlistWithSongs.songs

    override fun equals(other: Any?): Boolean {
        println("Glide equals $this $other")
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaylistWithSongsPreview
        if (other.playlist.id != playlist.id) return false
        if (other.songs.size != songs.size) return false
        return true
    }

    override fun hashCode(): Int {
        var result = playlist.id.hashCode()
        result = 31 * result + playlistWithSongs.songs.size
        println("Glide $result")
        return result
    }
}