package com.dhp.musicplayer.data.network.innertube.model.response

import com.dhp.musicplayer.data.network.innertube.model.Tabs
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val contents: Contents?,
) {
    @Serializable
    data class Contents(
        val tabbedSearchResultsRenderer: Tabs?
    )
}
