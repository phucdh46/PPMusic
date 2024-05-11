package com.dhp.musicplayer.utils

import android.content.Context
import android.content.res.Resources
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.toFormattedYear
import com.dhp.musicplayer.models.Album
import com.dhp.musicplayer.models.Music
import com.dhp.musicplayer.models.Song

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

    fun buildSortedArtistAlbums(resources: Resources, artistSongs: List<Music>?): List<Album> {

        val sortedAlbums = mutableListOf<Album>()

        artistSongs?.let {

            try {

                val groupedSongs = it.groupBy { song -> song.album }

                val iterator = groupedSongs.keys.iterator()
                while (iterator.hasNext()) {
                    val album = iterator.next()
                    val albumSongs = groupedSongs.getValue(album).toMutableList()
                    albumSongs.sortBy { song -> song.track }
                    sortedAlbums.add(
                        Album(
                            album,
                            albumSongs.first().year.toFormattedYear(resources),
                            albumSongs,
                            albumSongs.sumOf { song -> song.duration }
                        )
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            sortedAlbums.sortBy { album -> album.year }
        }

        return sortedAlbums
    }

    fun getSongCountString(context: Context, songCount: Int): String {
        val songString = if (songCount == 1) context.resources
            .getString(R.string.song) else context.resources.getString(R.string.songs)
        return "$songCount $songString"
    }

}