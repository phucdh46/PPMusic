package com.dhp.musicplayer.model

import androidx.media3.common.MediaItem
import java.io.Serializable

data class PersistQueue(
    val items: List<Song>,
    val mediaItemIndex: Int,
    val position: Long,
) : Serializable
