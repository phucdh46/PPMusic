package com.dhp.musicplayer.core.model.music

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

data class ArtistPage (
    val name: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val shuffleEndpoint: RadioEndpoint?,
    val radioEndpoint: RadioEndpoint?,
    val songs: List<Song>?,
    val songsEndpoint: Endpoint?,
    val albums: List<Album>?,
    val albumsEndpoint: Endpoint?,
    val singles: List<Album>?,
    val singlesEndpoint: Endpoint?,
)

data class Endpoint (
    val browseId: String?,
    val params: String?
)

@Serializable
@Parcelize
data class RadioEndpoint (
    val videoId: String?,
    val playlistId: String? = null,
    val params: String? = null,
    val playlistSetVideoId: String? = null,
): Parcelable