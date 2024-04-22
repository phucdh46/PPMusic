package com.dhp.musicplayer.utils

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline

val Player.shouldBePlaying: Boolean
    get() = !(playbackState == Player.STATE_ENDED || !playWhenReady)

inline val Timeline.windows: List<Timeline.Window>
    get() = List(windowCount) {
        getWindow(it, Timeline.Window())
    }

fun Player.forceSeekToPrevious() {
    if (hasPreviousMediaItem() || currentPosition > maxSeekToPreviousPosition) {
        seekToPrevious()
    } else if (mediaItemCount > 0) {
        seekTo(mediaItemCount - 1, C.TIME_UNSET)
    }
}

fun Player.forceSeekToNext() =
    if (hasNextMediaItem()) seekToNext() else seekTo(0, C.TIME_UNSET)