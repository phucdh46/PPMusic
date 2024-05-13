package com.dhp.musicplayer.api.reponse

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val items: List<SearchItem>?,

    )
@Serializable
data class SearchItem(
    val info: Info?,
    val authors: String?,
    val album: String?,
    val durationText: String?,
    val thumbnail: Thumbnail?,
)
@Serializable
data class Thumbnail(
    val url: String?,
)
@Serializable
data class Info(
    val name: String?,
    val endpoint: Endpoit?,
)

@Serializable
data class Endpoit(
    val videoId: String?,
)