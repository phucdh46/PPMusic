package com.dhp.musicplayer.kugou.model

import kotlinx.serialization.Serializable

@Serializable
internal class DownloadLyricsResponse(
    val content: String
)