package com.dhp.musicplayer.core.model.music

data class MoodAndGenresDetail (
    val title: String?,
    val items: List<Item>,
) {
    data class Item(
        val title: String?,
        val items: List<Music>?,
    )
}