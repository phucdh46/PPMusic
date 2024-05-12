package com.dhp.musicplayer.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
) : Serializable {
    data class Artist(
        val id: String?,
        val name: String,
    ) : Serializable

    data class Album(
        val id: String,
        val title: String,
    ) : Serializable

//    fun toSongEntity() = SongEntity(
//        id = id,
//        title = title,
//        duration = duration,
//        thumbnailUrl = thumbnailUrl,
//        albumId = album?.id,
//        albumName = album?.title
//    )
}