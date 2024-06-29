package com.dhp.musicplayer.core.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.dhp.musicplayer.core.designsystem.component.Artwork
import com.dhp.musicplayer.core.model.music.Song
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagerThumbnail(
    songs: List<Song>,
    index: Int,
    onSwipeArtwork: (Int) -> Unit,
    modifier: Modifier = Modifier,
    imageCoverLarge: @Composable (BoxWithConstraintsScope.(size: Dp) -> Unit)? = null
) {
    val dummyPagerCount = songs.size + 2
//    val dummyPagerCount = songs.size
    val pagerState = rememberPagerState(initialPage = index + 1) { dummyPagerCount }
//    val pagerState = rememberPagerState(initialPage = index ) { dummyPagerCount }
    var lastPlayedIndex by remember { mutableIntStateOf(index) }

    LaunchedEffect(index) {
        snapshotFlow { index }.collect {
            lastPlayedIndex = it
            pagerState.animateScrollToPage(it + 1)
//            pagerState.animateScrollToPage(it )
        }
    }

    LaunchedEffect(pagerState) {
//        snapshotFlow { pagerState.currentPage }.collect {
//            if(it != index) onSwipeArtwork.invoke(it)
//        }
        snapshotFlow { pagerState.settledPage }.collect {
            if (songs.isEmpty()) return@collect

            val realIndex = when (it) {
                0 -> {
                    pagerState.scrollToPage(dummyPagerCount - 2)
                    dummyPagerCount - 3
                }

                dummyPagerCount - 1 -> {
                    pagerState.scrollToPage(1)
                    0
                }

                else -> {
                    it - 1
                }
            }

            if (realIndex != lastPlayedIndex) {
                lastPlayedIndex = realIndex
                onSwipeArtwork.invoke(realIndex)
            }
        }
    }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
    ) { dummyIndex ->
        val realIndex = when (dummyIndex) {
            0 -> dummyPagerCount - 3
            dummyPagerCount - 1 -> 0
            else -> dummyIndex - 1
        }

//        val song = songs.elementAtOrElse(realIndex) { Song.dummy() }
        val song = songs.getOrNull(realIndex)
//        val song = songs.getOrNull(dummyIndex)

        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    val offset =
                        ((pagerState.currentPage - dummyIndex) + pagerState.currentPageOffsetFraction).absoluteValue

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
                },
            colors = CardColors(
                containerColor = Color.Transparent,
                contentColor = CardDefaults.cardColors().contentColor,
                disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
                disabledContentColor = CardDefaults.cardColors().disabledContentColor,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(Modifier.fillMaxSize()) {
                Artwork(
                    modifier = Modifier.fillMaxWidth(),
                    url = song?.thumbnailUrl,
                    imageCoverLarge = imageCoverLarge
                )
            }
        }
    }
}