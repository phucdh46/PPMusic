package com.dhp.musicplayer.data.network.innertube.model.response

import com.dhp.musicplayer.data.network.innertube.model.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsResponse(
    val contents: List<Content>?
) {
    @Serializable
    data class Content(
        val searchSuggestionsSectionRenderer: SearchSuggestionsSectionRenderer?
    ) {
        @Serializable
        data class SearchSuggestionsSectionRenderer(
            val contents: List<Content>?
        ) {
            @Serializable
            data class Content(
                val searchSuggestionRenderer: SearchSuggestionRenderer?
            ) {
                @Serializable
                data class SearchSuggestionRenderer(
                    val navigationEndpoint: NavigationEndpoint?,
                )
            }
        }
    }
}