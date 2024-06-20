package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.settings.ApiKey
import com.dhp.musicplayer.core.network.api.response.KeyResponse

fun KeyResponse.asExternalModel(): ApiKey {
    return ApiKey(
        host = host,
        headerName = headerName,
        headerKey = headerKey,
        headerMask = headerMask,
        hostPlayer = hostPlayer,
        hostBrowse = hostBrowse,
        hostNext = hostNext,
        hostSearch = hostSearch,
        hostSuggestion = hostSuggestion,
        filterSong = filterSong,
        filterVideo = filterVideo,
        filterAlbum = filterAlbum,
        filterArtist = filterArtist,
        filterCommunityPlaylist = filterCommunityPlaylist,
        featuredPlaylist = featuredPlaylist,
        visitorData = visitorData,
        userAgentAndroid = userAgentAndroid,
        embedUrl = embedUrl,
    )
}