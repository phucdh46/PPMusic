package com.dhp.musicplayer.feature.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.services.player.PlayerConnection
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MiniPlayerPager(
    song: Song,
    modifier: Modifier = Modifier,
    content: @Composable (Song, Int, Modifier) -> Unit,
) {
    val playerConnection: PlayerConnection = LocalPlayerConnection.current ?: return
    val windows by playerConnection.currentTimelineWindows.collectAsState()
    val playbackQueue = windows.map { it.mediaItem.toSong() }
    val playbackCurrentIndex = playerConnection.player.currentMediaItemIndex
    var lastRequestedPage by remember(playbackQueue, song) {
        mutableStateOf<Int?>(
            playbackCurrentIndex
        )
    }
    val pagerState = rememberPagerState(initialPage = playbackCurrentIndex) { playbackQueue.size }

    if (playbackCurrentIndex >= 0 && playbackQueue.isEmpty()) {
        content(song, playbackCurrentIndex, modifier)
        return
    }
    LaunchedEffect(Unit) {
        pagerState.scrollToPage(playbackCurrentIndex)
    }
    LaunchedEffect(playbackCurrentIndex, pagerState) {
        if (playbackCurrentIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(playbackCurrentIndex)
        }
        snapshotFlow { pagerState.isScrollInProgress }
            .filter { !it }
            .map { pagerState.currentPage }
            .collectLatest { page ->
                if (lastRequestedPage != page) {
                    lastRequestedPage = page
                    playerConnection.skipToQueueItem(page)
                }
            }
    }

   HorizontalPager(
        modifier = modifier,
        state = pagerState,
    ) { page ->
        val currentAudio = playbackQueue.getOrNull(page)

        val pagerMod = Modifier.graphicsLayer {
            val offset =
                ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

            alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - offset.coerceIn(0f, 1f),
            )

            lerp(
                start = 0.8f,
                stop = 1f,
                fraction = 1f - (offset / 2).coerceIn(0f, 1f),
            ).also {
                scaleX = it
                scaleY = it
            }
        }
        currentAudio?.let { content(it, page, pagerMod) }
    }
}
