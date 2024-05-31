package com.dhp.musicplayer.innertube.utils

import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.model.MusicShelfRenderer
import com.dhp.musicplayer.innertube.model.NavigationEndpoint

fun Innertube.SongItem.Companion.fromSearch(content: MusicShelfRenderer.Content): Innertube.SongItem? {
    val (mainRuns, otherRuns) = content.runs

    // Possible configurations:
    // "song" • author(s) • album • duration
    // "song" • author(s) • duration
    // author(s) • album • duration
    // author(s) • duration

    val album: Innertube.Info<NavigationEndpoint.Endpoint.Browse>? =
        otherRuns
//        .getOrNull(otherRuns.lastIndex - 1)
//        secondaryLine
            .getOrNull(1)
            ?.firstOrNull()
            ?.takeIf { run ->
                run
                    .navigationEndpoint
                    ?.browseEndpoint
                    ?.type == "MUSIC_PAGE_TYPE_ALBUM"
            }
            ?.let(Innertube::Info)

    return Innertube.SongItem(
        info = mainRuns
            .firstOrNull()
            ?.let(Innertube::Info),
        authors = otherRuns
//                secondaryLine
            .firstOrNull()?.oddElements()
//            .getOrNull(otherRuns.lastIndex - if (album == null) 1 else 2)
            ?.map(Innertube::Info),
        album = album,
        durationText = //secondaryLine.lastOrNull()?.firstOrNull()?.text,
        otherRuns
            .lastOrNull()
            ?.firstOrNull()?.text ,
        thumbnail = content
            .thumbnail
    ).takeIf { it.info?.endpoint?.videoId != null }
}

//fun Innertube.VideoItem.Companion.from(content: MusicShelfRenderer.Content): Innertube.VideoItem? {
//    val (mainRuns, otherRuns) = content.runs
//
//    return Innertube.VideoItem(
//        info = mainRuns
//            .firstOrNull()
//            ?.let(Innertube::Info),
//        authors = otherRuns
//            .getOrNull(otherRuns.lastIndex - 2)
//            ?.map(Innertube::Info),
//        viewsText = otherRuns
//            .getOrNull(otherRuns.lastIndex - 1)
//            ?.firstOrNull()
//            ?.text,
//        durationText = otherRuns
//            .getOrNull(otherRuns.lastIndex)
//            ?.firstOrNull()
//            ?.text,
//        thumbnail = content
//            .thumbnail
//    ).takeIf { it.info?.endpoint?.videoId != null }
//}

fun Innertube.AlbumItem.Companion.fromSearch(content: MusicShelfRenderer.Content): Innertube.AlbumItem? {
    val (mainRuns, otherRuns) = content.runs

    return Innertube.AlbumItem(
        info = Innertube.Info(
            name = mainRuns
                .firstOrNull()
                ?.text,
            endpoint = content
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
        ),
        authors = otherRuns
            .getOrNull(otherRuns.lastIndex - 1)
            ?.map(Innertube::Info),
        year = otherRuns
            .getOrNull(otherRuns.lastIndex)
            ?.firstOrNull()
            ?.text,
        thumbnail = content
            .thumbnail
    ).takeIf { it.info?.endpoint?.browseId != null }
}

fun Innertube.ArtistItem.Companion.fromSearch(content: MusicShelfRenderer.Content): Innertube.ArtistItem? {
    val (mainRuns, otherRuns) = content.runs

    return Innertube.ArtistItem(
        info = Innertube.Info(
            name = mainRuns
                .firstOrNull()
                ?.text,
            endpoint = content
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
        ),
        subscribersCountText = otherRuns
            .lastOrNull()
            ?.last()
            ?.text,
        thumbnail = content
            .thumbnail
    ).takeIf { it.info?.endpoint?.browseId != null }
}

fun Innertube.PlaylistItem.Companion.fromSearch(content: MusicShelfRenderer.Content): Innertube.PlaylistItem? {
    val (mainRuns, otherRuns) = content.runs

    return Innertube.PlaylistItem(
        info = Innertube.Info(
            name = mainRuns
                .firstOrNull()
                ?.text,
            endpoint = content
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
        ),
        channel = otherRuns
            .firstOrNull()
            ?.firstOrNull()
            ?.let(Innertube::Info),
        songCount = otherRuns
            .lastOrNull()
            ?.firstOrNull()
            ?.text
            ?.split(' ')
            ?.firstOrNull()
            ?.toIntOrNull(),
        thumbnail = content
            .thumbnail
    ).takeIf { it.info?.endpoint?.browseId != null }
}
