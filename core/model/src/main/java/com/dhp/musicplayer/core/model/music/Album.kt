package com.dhp.musicplayer.core.model.music

data class Album(
    val id: String,
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val year: String? = null,
    val authorsText: String? = null,
)
