package com.dhp.musicplayer.core.network.innertube.model.response

import com.dhp.musicplayer.core.network.innertube.model.Tabs
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