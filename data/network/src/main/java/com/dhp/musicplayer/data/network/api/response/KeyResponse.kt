package com.dhp.musicplayer.data.network.api.response

import kotlinx.serialization.Serializable

@Serializable
data class KeyResponse(
    val host: String = "",
    val headerName: String = "",
    val headerKey: String = "",
    val headerMask: String = "",
    val hostPlayer: String = "",
    val hostBrowse: String = "",
    val hostNext: String = "",
    val hostSearch: String = "",
    val hostSuggestion: String = "",
    val filterSong: String = "",
    val filterVideo: String = "",
    val filterAlbum: String = "",
    val filterArtist: String = "",
    val filterCommunityPlaylist: String = "",
    val featuredPlaylist: String = "",
    val visitorData: String = "",
    val userAgentAndroid: String = "",
    val embedUrl: String = "",
)
