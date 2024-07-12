package com.dhp.musicplayer.core.network.innertube.model

import com.dhp.musicplayer.core.network.innertube.Innertube


data class BrowseResult(
    val title: String?,
    val items: List<Item>,
) {
    data class Item(
        val title: String?,
        val items: List<Innertube.Item>?,
    )
}
