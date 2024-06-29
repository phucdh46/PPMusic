package com.dhp.musicplayer.core.network.innertube.model.response

import com.dhp.musicplayer.core.network.innertube.model.SectionListRenderer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumResponse(
    @SerialName("contents") val contents: Contents?,
) {
    @Serializable
    data class Contents(
        @SerialName("twoColumnBrowseResultsRenderer") val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer?
    ) {
        @Serializable
        data class TwoColumnBrowseResultsRenderer(
            @SerialName("secondaryContents") val secondaryContents: SecondaryContents?,
            @SerialName("tabs") val tabs: List<TabRenderers>
        ) {
            @Serializable
            data class SecondaryContents(
                @SerialName("sectionListRenderer") val sectionListRenderer: SectionListRenderer?
            )
        }
    }
}
