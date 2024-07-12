package com.dhp.musicplayer.core.network.kugou.model

import kotlinx.serialization.Serializable

@Serializable
internal class DownloadLyricsResponse(
    val content: String
)