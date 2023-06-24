package com.dhp.musicplayer.utils

import com.dhp.musicplayer.model.Album

object MusicUtils {

    @JvmStatic
    fun getAlbumSongs(
        artist: String?,
        album: String?,
        deviceAlbumsByArtist: MutableMap<String, List<Album>>?
    ) = try {
        getAlbumFromList(
            artist,
            album,
            deviceAlbumsByArtist
        ).first.music
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    @JvmStatic
    // Returns a pair of album and its position given a list of albums
    fun getAlbumFromList(
        artist: String?,
        album: String?,
        deviceAlbumsByArtist: MutableMap<String, List<Album>>?
    ): Pair<Album, Int> {
        val albums = deviceAlbumsByArtist?.get(artist)
        return try {
            val position = albums?.indexOfFirst { it.title == album }!!
            Pair(first = albums[position], second = position)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(first = albums?.first()!!, second = 0)
        }
    }
}