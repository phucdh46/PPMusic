package com.dhp.musicplayer.core.model.music

data class MoodAndGenres (
    val title: String,
    val items: List<Item>?,
){
    data class Item(
        val title: String,
        val stripeColor: Int?,
        val endpoint: Endpoint,
    )
}

