package com.dhp.musicplayer.core.model.music

data class PlayerMedia (
    val id: String?,
    val status: String?,
    val url: String?,
    val urlDownload: String?,
    val expiresInSeconds: Int? = null,
)