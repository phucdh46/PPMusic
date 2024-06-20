package com.dhp.musicplayer.core.ui.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.designsystem.component.LoadingShimmerImage
import com.dhp.musicplayer.core.model.music.Playlist

@Composable
fun PlaylistItem(
    playlist: Playlist,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    PlaylistItem(
        thumbnailUrl = playlist.thumbnailUrl,
        songCount = playlist.songCount,
        name = playlist.name,
        channelName = playlist.channelName,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative
    )
}

@Composable
fun PlaylistItem(
    thumbnailUrl: String?,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    PlaylistItem(
        thumbnailContent = {
            LoadingShimmerImage(
                thumbnailSizeDp = thumbnailSizeDp,
                thumbnailUrl = thumbnailUrl?.thumbnail(thumbnailSizePx),
                modifier = it
            )
        },
        songCount = songCount,
        name = name,
        channelName = channelName,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
    )
}

@Composable
fun PlaylistItem(
    thumbnailContent: @Composable BoxScope.(modifier: Modifier) -> Unit,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
//    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) { centeredModifier ->
        Box(
            modifier = centeredModifier
//                .clip(thumbnailShape)
//                .background(color = colorPalette.background1)
                .requiredSize(thumbnailSizeDp)
        ) {
            thumbnailContent(Modifier.fillMaxSize())

//            songCount?.let {
//                Text(
//                    text = "$songCount",
//                    style = typography.bodyMedium,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier
//                        .padding(all = 4.dp)
//                        .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(2.dp))
//                        .padding(horizontal = 4.dp, vertical = 2.dp)
//                        .align(Alignment.BottomEnd)
//                )
//            }
        }

        ItemInfoContainer(
            horizontalAlignment = if (alternative && channelName == null) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            Text(
                text = name ?: "",
                style = typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            channelName?.let {
                Text(
                    text = channelName,
                    style = typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
