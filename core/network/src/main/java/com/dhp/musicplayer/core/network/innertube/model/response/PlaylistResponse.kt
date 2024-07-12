package com.dhp.musicplayer.core.network.innertube.model.response

import com.dhp.musicplayer.core.network.innertube.model.Runs
import com.dhp.musicplayer.core.network.innertube.model.SectionListRenderer
import com.dhp.musicplayer.core.network.innertube.model.ThumbnailRenderer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistResponse(
    @SerialName("contents") val contents: Contents?,
)

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

@Serializable
data class TabRenderers(
    @SerialName("tabRenderer") val tabRenderer: TabRenderer?,
) {
    @Serializable
    data class TabRenderer(
        @SerialName("content") val content: Content?,
    ) {
        @Serializable
        data class Content(
            @SerialName("sectionListRenderer") val sectionListRenderer: SectionListRenderer?
        ) {
            @Serializable
            data class SectionListRenderer(
                @SerialName("contents") val contents: List<MusicResponsiveHeaderRenderers>?
            ) {
                @Serializable
                data class MusicResponsiveHeaderRenderers(
                    @SerialName("musicResponsiveHeaderRenderer") val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer?
                ) {
                    @Serializable
                    data class MusicResponsiveHeaderRenderer(
                        @SerialName("title") val title: Runs?,
                        @SerialName("subtitle") val subtitle: Runs?,
                        @SerialName("secondSubtitle") val secondSubtitle: Runs?,
                        @SerialName("straplineTextOne") val straplineTextOne: Runs?,
                        @SerialName("thumbnail") val thumbnail: ThumbnailRenderer?,
                    )
                }
            }
        }
    }
}
