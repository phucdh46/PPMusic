package com.dhp.musicplayer.feature.player

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.core.common.extensions.move
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.animation.LoadingFiveLinesCenter
import com.dhp.musicplayer.core.designsystem.component.Artwork
import com.dhp.musicplayer.core.designsystem.component.ImageSongItem
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.extensions.marquee
import com.dhp.musicplayer.core.designsystem.theme.bold
import com.dhp.musicplayer.core.designsystem.theme.center
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.extensions.getBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.reorderable

@Composable
fun Queue(
    modifier: Modifier = Modifier,
    onClickDismiss: () -> Unit,
    songsWithBitmaps: SnapshotStateList<Pair<Song, Bitmap?>>
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val scope = rememberCoroutineScope()
    val currentMediaIndex by playerConnection.currentMediaItemIndex.collectAsState()
    val currentMediaItem by playerConnection.currentMediaItem.collectAsState()
    val reorderLazyListState = org.burnoutcrew.reorderable.rememberReorderableLazyListState(
        onMove = { from, to -> songsWithBitmaps.move(from.index, to.index) },
        onDragEnd = { fromIndex, toIndex ->
            playerConnection.player.moveMediaItem(
                fromIndex,
                toIndex
            )
        },
    )

    Column(modifier.fillMaxSize()) {
        QueueHeaderSection(
            modifier = Modifier
                .padding(
                    top = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateTopPadding()
                )
                .fillMaxWidth(),
            onClickDismiss = onClickDismiss,
        )

        QueueCurrentItemSection(modifier = Modifier.fillMaxWidth(),
            song = currentMediaItem?.toSong()!!,
            isPlaying = isPlaying,
            onClickHolder = {
                scope.launch {
                    reorderLazyListState.listState.animateScrollToItem(currentMediaIndex)
                }
            },
            onPlayOrPauseClick = {
                playerConnection.playOrPause()
            })

        QueueListSection(modifier = Modifier.fillMaxSize(),
            queue = songsWithBitmaps,
            currentMediaItem = currentMediaItem,
            reorderLazyListState = reorderLazyListState,
            lazyListState = reorderLazyListState.listState,
            onItemClick = { index ->
                scope.launch(Dispatchers.Main) {
                    if (index == currentMediaIndex) {
                        playerConnection.playOrPause()
                    } else {
                        playerConnection.player.seekToDefaultPosition(index)
                        playerConnection.player.playWhenReady = true
                    }
                }
            }
        )
    }
}

@Composable
internal fun QueueHeaderSection(
    onClickDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClickDismiss.invoke() }
                .padding(4.dp),
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.player_queue_text),
            style = MaterialTheme.typography.bodyLarge.bold().center(),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
        )
    }
}

@Composable
internal fun QueueCurrentItemSection(
    song: Song,
    isPlaying: Boolean,
    onClickHolder: () -> Unit,
    onPlayOrPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .clickable { onClickHolder.invoke() },
    ) {
        val (artwork, title, artist, icon) = createRefs()
        val context = LocalContext.current
        val bitmap = if (song.isOffline) {
            song.getBitmap(context)
        } else null
        Card(
            modifier = Modifier
                .size(56.dp)
                .constrainAs(artwork) {
                    top.linkTo(parent.top, 16.dp)
                    bottom.linkTo(parent.bottom, 16.dp)
                    start.linkTo(parent.start, 16.dp)
                },
            shape = RoundedCornerShape(8.dp),
        ) {
            Artwork(
                modifier = Modifier.fillMaxSize(),
                url = song.thumbnailUrl,
                bitmap = bitmap
            )
        }

        Text(
            modifier = Modifier
                .marquee()
                .constrainAs(title) {
                    top.linkTo(artwork.top)
                    bottom.linkTo(artist.top)
                    start.linkTo(artwork.end, 8.dp)
                    end.linkTo(icon.start, 8.dp)

                    width = Dimension.fillToConstraints
                },
            text = song.title,
            style = MaterialTheme.typography.bodyLarge.bold(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )

        Text(
            modifier = Modifier
                .marquee()
                .constrainAs(artist) {
                    top.linkTo(title.bottom)
                    bottom.linkTo(artwork.bottom)
                    start.linkTo(artwork.end, 8.dp)
                    end.linkTo(icon.start, 8.dp)

                    width = Dimension.fillToConstraints
                },
            text = song.artistsText.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        LoadingFiveLinesCenter(isPlaying = isPlaying,
            isShow = true,
            modifier = Modifier
                .size(50.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, 16.dp)
                }
                .clickable { onPlayOrPauseClick() })
    }
}

@Composable
internal fun QueueListSection(
    reorderLazyListState: org.burnoutcrew.reorderable.ReorderableLazyListState,
    lazyListState: LazyListState,
    queue: SnapshotStateList<Pair<Song, Bitmap?>>,
    currentMediaItem: MediaItem?,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {

    Box(modifier.background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .then(Modifier.reorderable(reorderLazyListState)),
            state = lazyListState,
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            itemsIndexed(
                items = queue,
                key = { _, item -> item.first.id },
            ) { index, item ->
                ReorderableItem(state = reorderLazyListState, key = item.first.id) {
                    val background = animateColorAsState(
                        targetValue = if (item.first.id == currentMediaItem?.mediaId) MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.2f
                        ) else MaterialTheme.colorScheme.surface,
                        label = "background",
                    )
                    IndexedSongHolder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(background.value),
                        modifierReorder = Modifier.detectReorder(reorderLazyListState),
                        song = item.first,
                        bitmap = item.second,
                        index = index,
                        onClickHolder = onItemClick,
                    )
                }
            }
        }
    }
}

@Composable
fun IndexedSongHolder(
    song: Song,
    bitmap: Bitmap?,
    index: Int,
    onClickHolder: (Int) -> Unit,
    modifier: Modifier = Modifier,
    modifierReorder: Modifier = Modifier,
    thumbnailSizeDp: Dp = Dimensions.thumbnails.song,
) {
    ConstraintLayout(modifier.clickable {
        onClickHolder.invoke(index)
    }
    ) {
        val (artwork, title, artist, duration, handle) = createRefs()

        createVerticalChain(
            title.withChainParams(bottomMargin = 2.dp),
            artist.withChainParams(topMargin = 2.dp),
            chainStyle = ChainStyle.Packed,
        )

        Card(
            modifier = Modifier
                .size(thumbnailSizeDp)
                .constrainAs(artwork) {
                    top.linkTo(parent.top, 12.dp)
                    bottom.linkTo(parent.bottom, 12.dp)
                    start.linkTo(parent.start, 16.dp)
                },
            shape = RoundedCornerShape(8.dp),
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(thumbnailSizeDp)
                )
            } else {
                ImageSongItem(
                    thumbnailUrl = song.thumbnailUrl.thumbnail(thumbnailSizeDp.px),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(thumbnailSizeDp)
                )
            }
//            Artwork(
//                modifier = Modifier.size(thumbnailSizeDp),
//                url = song.thumbnailUrl.thumbnail(thumbnailSizeDp.px),
//                bitmap = bitmap
//            )
        }

        Text(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(artwork.top)
                start.linkTo(artwork.end, 16.dp)
                end.linkTo(handle.start, 8.dp)

                width = Dimension.fillToConstraints
            },
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier.constrainAs(artist) {
                top.linkTo(title.bottom)
                start.linkTo(title.start)
                end.linkTo(duration.start, 16.dp)

                width = Dimension.fillToConstraints
            },
            text = song.artistsText.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier.constrainAs(duration) {
                top.linkTo(artist.top)
                bottom.linkTo(artist.bottom)
                end.linkTo(handle.start, 8.dp)
            },
            text = "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Icon(
            modifier = modifierReorder
                .size(32.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(50))
                .constrainAs(handle) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, 12.dp)
                }
                .padding(4.dp),
            imageVector = Icons.Default.DragHandle,
            contentDescription = null,
        )
    }
}
