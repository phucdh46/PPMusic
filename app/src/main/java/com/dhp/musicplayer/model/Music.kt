package com.dhp.musicplayer.model
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Music(
    val artist: String? = null,
    val year: Int = 0,
    val track: Int = 0,
    val title: String? = null,
    val displayName: String? = null,
    val duration: Long = 0L,
    val album: String? = null,
    val albumId: Long? = 0L,
    val relativePath: String? = null,
    val id: Long? = 0L,
    val launchedBy: String  = "",
    val startFrom: Int = 0,
    val dateAdded: Int = 0
)
