package com.dhp.musicplayer.api

@kotlinx.serialization.Serializable
data class SearchBody (
    val query: String,
    val params: String = "SONG",
    val type: String = "SONG"
)