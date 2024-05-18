package com.dhp.musicplayer.extensions

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import com.dhp.musicplayer.model.MediaMetadata

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

val Player.currentMetadata: MediaMetadata?
    get() = currentMediaItem?.metadata

fun Player.playQueue(mediaItem: MediaItem) {
    val index = currentTimeline.windows.find { it.mediaItem.mediaId == mediaItem.mediaId }?.firstPeriodIndex ?: 0
    seekToDefaultPosition(index)
    playWhenReady = true
    prepare()
}

val Player.currentWindow: Timeline.Window?
    get() = if (mediaItemCount == 0) null else currentTimeline.getWindow(currentMediaItemIndex, Timeline.Window())

fun Player.forcePlay(mediaItem: MediaItem) {
    setMediaItem(mediaItem, true)
    playWhenReady = true
    prepare()
}

fun Player.addNext(mediaItem: MediaItem) {
    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
        forcePlay(mediaItem)
    } else {
        addMediaItem(currentMediaItemIndex + 1, mediaItem)
    }
}

fun Player.enqueue(mediaItem: MediaItem) {
    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
        forcePlay(mediaItem)
    } else {
        addMediaItem(mediaItemCount, mediaItem)
    }
}

val Player.mediaItems: List<MediaItem>
    get() = object : AbstractList<MediaItem>() {
        override val size: Int
            get() = mediaItemCount

        override fun get(index: Int): MediaItem = getMediaItemAt(index)
    }

val Timeline.mediaItems: List<MediaItem>
    get() = List(windowCount) {
        getWindow(it, Timeline.Window()).mediaItem
    }

