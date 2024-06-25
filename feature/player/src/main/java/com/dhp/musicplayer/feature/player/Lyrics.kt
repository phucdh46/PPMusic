package com.dhp.musicplayer.feature.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.extensions.verticalFadingEdge
import com.dhp.musicplayer.core.network.kugou.KuGou
import com.dhp.musicplayer.core.network.kugou.findCurrentLineIndex
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.items.TextPlaceholder
import com.dhp.musicplayer.feature.player.utils.SynchronizedLyrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
//    onDismiss: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    durationProvider: () -> Long,
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var lyrics by remember {
        mutableStateOf<String?>(null)
    }

    AnimatedVisibility(
        modifier = modifier.fillMaxSize(),
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val density = LocalDensity.current
        val player = LocalPlayerConnection.current?.player ?: return@AnimatedVisibility

        var isError by remember(mediaId) {
            mutableStateOf(false)
        }

        var isScrolling by rememberSaveable {
            mutableStateOf(false)
        }
        var isSeeking by remember {
            mutableStateOf(false)
        }

        var currentLineIndex by remember {
            mutableIntStateOf(-1)
        }

        LaunchedEffect(key1 = mediaId) {
            withContext(Dispatchers.IO) {
                val mediaMetadata = mediaMetadataProvider()
                var duration = withContext(Dispatchers.Main) {
                    durationProvider()
                }
                while (duration == C.TIME_UNSET) {
                    delay(100)
                    duration = withContext(Dispatchers.Main) {
                        durationProvider()
                    }
                }
                KuGou.lyrics(
                    artist = mediaMetadata.artist?.toString() ?: "",
                    title = mediaMetadata.title?.toString() ?: "",
                    duration = duration / 1000
                )?.onSuccess { syncedLyrics ->
                    lyrics = syncedLyrics?.value ?: ""
                }?.onFailure {
                    isError = true
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
//                .pointerInput(Unit) {
//                    detectTapGestures(
//                        onTap = { onDismiss() }
//                    )
//                }
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = isError && lyrics == null,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.lyric_get_data_error),
                    style = typography.titleMedium.copy(color = MaterialTheme.colorScheme.errorContainer),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = lyrics?.let(String::isEmpty) ?: false,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.lyric_get_data_not_available),
                    style = typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }


            val text = lyrics
            if (text != null) {
                val synchronizedLyrics = remember(text) {
                    SynchronizedLyrics(KuGou.Lyrics(text).sentences) {
                        player.currentPosition + 50
                    }
                }

                val lazyListState = rememberLazyListState(
                    synchronizedLyrics.index,
                    with(density) { size.roundToPx() } / 6)

                val sliderPosition = sliderPositionProvider()
                LaunchedEffect(lyrics, sliderPosition) {
//            if (lyrics.isNullOrEmpty() || !lyrics?.startsWith("[")) {
                    if (lyrics.isNullOrEmpty()) {
                        currentLineIndex = -1
                        return@LaunchedEffect
                    }

                    while (isActive) {
                        delay(50)
                        isSeeking = sliderPosition != null
                        currentLineIndex = findCurrentLineIndex(
                            synchronizedLyrics.sentences,
                            player.currentPosition
                        )
                    }
                }

                LaunchedEffect(currentLineIndex, isScrolling) {
                    Logg.d("synchronizedLyrics: $currentLineIndex - $isScrolling - $isSeeking")
                    if (currentLineIndex != -1) {
                        if (!isScrolling) {
                            val center = with(density) { size.roundToPx() } / 6
                            if (isSeeking) {
                                lazyListState.scrollToItem(
                                    currentLineIndex,
                                    center
                                )
                            } else {
                                lazyListState.animateScrollToItem(
                                    currentLineIndex,
                                    center
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(vertical = size / 2),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .verticalFadingEdge()
                        .nestedScroll(remember {
                            object : NestedScrollConnection {
                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    if (!isScrolling) isScrolling = true
                                    return super.onPostScroll(consumed, available, source)
                                }

                                override suspend fun onPostFling(
                                    consumed: Velocity,
                                    available: Velocity
                                ): Velocity {
                                    if (!isScrolling) isScrolling = true
                                    return super.onPostFling(consumed, available)
                                }
                            }
                        })
                ) {
                    itemsIndexed(items = synchronizedLyrics.sentences) { index, sentence ->
                        Text(
                            text = sentence.second,
                            textAlign = TextAlign.Center,
                            style = typography.titleLarge.copy(
                                color = (
                                        when {
                                            index < currentLineIndex -> {
                                                MaterialTheme.colorScheme.primaryContainer
                                            }

                                            index == currentLineIndex -> {
                                                MaterialTheme.colorScheme.primary
                                            }

                                            else -> {
                                                MaterialTheme.colorScheme.onBackground
                                            }
                                        }
                                        )
                            ),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                .clickable {
                                    player.seekTo(sentence.first)
                                    isScrolling = false
                                }

                        )
                    }
                }

                AnimatedVisibility(
                    visible = (isScrolling),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                lazyListState.scrollToItem(currentLineIndex)
                                isScrolling = false
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.lyric_text_auto_mode),
                            style = typography.labelMedium
                        )
                    }
                }

            }

            if (lyrics == null && !isError) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    repeat(5) {
                        TextPlaceholder()
                    }
                }
            }
        }
    }
}