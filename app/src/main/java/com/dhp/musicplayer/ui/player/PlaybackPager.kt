package com.dhp.musicplayer.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.player.PlayerConnection
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.absoluteValue

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun PlaybackPager(
    song: Song,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    pagerState: PagerState = rememberPagerState(),
    content: @Composable (Song, Int, Modifier) -> Unit,
) {
    val playerConnection: PlayerConnection = LocalPlayerConnection.current ?: return

    val playbackQueue by playerConnection.currentQueue.collectAsStateWithLifecycle()
    val playbackCurrentIndex = playerConnection.player.currentMediaItemIndex
    var lastRequestedPage by remember(playbackQueue, song) {
        mutableStateOf<Int?>(
            playbackCurrentIndex
        )
    }

    if ( playbackCurrentIndex >= 0 && playbackQueue.isNullOrEmpty()) {
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
        count = playbackQueue?.size ?: 0,
        modifier = modifier,
        state = pagerState,
        key = { playbackQueue?.getOrNull(it) ?: it },
        verticalAlignment = verticalAlignment,
    ) { page ->
        val currentAudio = playbackQueue?.getOrNull(page)

        val pagerMod = Modifier.graphicsLayer {
            val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
            // TODO: report to upstream if can be reproduced in isolation
            if (pageOffset.isNaN()) {
                return@graphicsLayer
            }

            lerp(
                start = 0.85f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            ).also { scale ->
                scaleX = scale
                scaleY = scale
            }
            alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )
        }
        currentAudio?.let { content(it, page, pagerMod) }
    }
}
