package com.dhp.musicplayer.core.network.innertube.utils

import com.dhp.musicplayer.core.network.innertube.Innertube
import com.dhp.musicplayer.core.network.innertube.model.MusicResponsiveListItemRenderer
import com.dhp.musicplayer.core.network.innertube.model.NavigationEndpoint
import com.dhp.musicplayer.core.network.innertube.model.Runs
import com.dhp.musicplayer.core.network.innertube.model.Thumbnail


fun Innertube.SongItem.Companion.from(
    renderer: MusicResponsiveListItemRenderer,
    thumbnailUrl: String? = null
): Innertube.SongItem? {
    val thumbnail = renderer
        .thumbnail
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.firstOrNull()
    val result = if (thumbnail == null && thumbnailUrl != null) Thumbnail(
        url = thumbnailUrl,
        height = null,
        width = null
    ) else thumbnail
    return Innertube.SongItem(
        info = renderer
            .flexColumns
            .getOrNull(0)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.getOrNull(0)
            ?.let(Innertube::Info),
        authors = renderer
            .flexColumns
            .getOrNull(1)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.map<Runs.Run, Innertube.Info<NavigationEndpoint.Endpoint.Browse>>(Innertube::Info)
            ?.takeIf(List<Any>::isNotEmpty),
        durationText = renderer
            .fixedColumns
            ?.getOrNull(0)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.getOrNull(0)
            ?.text,
        album = renderer
            .flexColumns
            .getOrNull(2)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.firstOrNull()
            ?.let(Innertube::Info),
        thumbnail = result
    ).takeIf { it.info?.endpoint?.videoId != null }
}
