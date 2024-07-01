package com.dhp.musicplayer.data.network.innertube.utils

import com.dhp.musicplayer.data.network.innertube.Innertube
import com.dhp.musicplayer.data.network.innertube.model.MusicResponsiveListItemRenderer
import com.dhp.musicplayer.data.network.innertube.model.NavigationEndpoint
import com.dhp.musicplayer.data.network.innertube.model.Runs


fun Innertube.SongItem.Companion.from(renderer: MusicResponsiveListItemRenderer): Innertube.SongItem? {
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
        thumbnail = renderer
            .thumbnail
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull()
    ).takeIf { it.info?.endpoint?.videoId != null }
}
