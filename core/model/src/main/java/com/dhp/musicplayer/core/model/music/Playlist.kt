package com.dhp.musicplayer.core.model.music

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist (
    val id: Long = 0,
    val name: String,
    val browseId: String? = null
): Parcelable