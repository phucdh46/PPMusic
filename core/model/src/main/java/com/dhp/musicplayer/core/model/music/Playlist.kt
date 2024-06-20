package com.dhp.musicplayer.core.model.music

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist (
    val id: Long = 0,
    val name: String,
    val browseId: String = "",
    val thumbnailUrl: String? = null,
    val songCount: Int? = null,
    val channelName: String? = null,
): Parcelable, Music() {
    override val key: String
        get() = browseId
}