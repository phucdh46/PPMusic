package com.dhp.musicplayer.innertube.model

import com.dhp.musicplayer.innertube.Innertube


data class BrowseResult(
    val title: String?,
    val items: List<Item>,
) {
    data class Item(
        val title: String?,
        val items: List<Innertube.Item>?,
    )
}
