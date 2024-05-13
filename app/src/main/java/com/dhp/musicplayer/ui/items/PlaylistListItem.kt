package com.dhp.musicplayer.ui.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.GridThumbnailHeight
import com.dhp.musicplayer.constant.ListItemHeight
import com.dhp.musicplayer.constant.ListThumbnailSize
import com.dhp.musicplayer.constant.ThumbnailCornerRadius
import com.dhp.musicplayer.model.PlaylistPreview
import com.dhp.musicplayer.ui.IconApp

@Composable
fun PlaylistListItem(
    playlistPreview: PlaylistPreview,
    modifier: Modifier = Modifier,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = playlistPreview.playlist.name,
    subtitle = pluralStringResource(
        R.plurals.n_song,
        playlistPreview.songCount,
        playlistPreview.songCount
    ),
    thumbnailContent = {
        Icon(
            imageVector =
            IconApp.PlaylistPlay, contentDescription = null,
            modifier = Modifier.size(ListThumbnailSize)
        )
//        when (playlist.thumbnails.size) {
//            0 ->
//
//                Icon(
//                painter = painterResource(R.drawable.queue_music),
//                contentDescription = null,
//                modifier = Modifier.size(ListThumbnailSize)
//            )
//
//            1 -> AsyncImage(
//                model = playlist.thumbnails[0],
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(ListThumbnailSize)
//                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
//            )
//
//            else -> Box(
//                modifier = Modifier
//                    .size(ListThumbnailSize)
//                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
//            ) {
//                listOf(
//                    Alignment.TopStart,
//                    Alignment.TopEnd,
//                    Alignment.BottomStart,
//                    Alignment.BottomEnd
//                ).fastForEachIndexed { index, alignment ->
//                    AsyncImage(
//                        model = playlist.thumbnails.getOrNull(index),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .align(alignment)
//                            .size(ListThumbnailSize / 2)
//                    )
//                }
//            }
//        }
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = title,
    subtitle = {
        badges()

        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    },
    thumbnailContent = thumbnailContent,
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
inline fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    noinline subtitle: (@Composable RowScope.() -> Unit)? = null,
    thumbnailContent: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(ListItemHeight)
            .padding(horizontal = 6.dp),
    ) {
        Box(
            modifier = Modifier.padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            thumbnailContent()
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = title.orEmpty(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    subtitle()
                }
            }
        }

        trailingContent()
    }
}

@Composable
fun PlaylistGridItem(
    playlistPreview: PlaylistPreview,
    modifier: Modifier = Modifier,
    badges: @Composable RowScope.() -> Unit = { },
    fillMaxWidth: Boolean = false,
) = GridItem(
    title = playlistPreview.playlist.name,
    subtitle = pluralStringResource(
        R.plurals.n_song,
        playlistPreview.songCount,
        playlistPreview.songCount
    ),
    badges = badges,
    thumbnailContent = {
        val width = maxWidth
        Icon(
            imageVector = IconApp.PlaylistPlay, contentDescription = null, modifier = Modifier
                .size(width / 2)
                .align(Alignment.Center)
        )
//        when (playlist.thumbnails.size) {
//            0 -> Icon(
//                painter = painterResource(R.drawable.queue_music),
//                contentDescription = null,
//                tint = LocalContentColor.current.copy(alpha = 0.8f),
//                modifier = Modifier
//                    .size(width / 2)
//                    .align(Alignment.Center)
//            )
//
//            1 -> AsyncImage(
//                model = playlist.thumbnails[0],
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(width)
//                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
//            )
//
//            else -> Box(
//                modifier = Modifier
//                    .size(width)
//                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
//            ) {
//                listOf(
//                    Alignment.TopStart,
//                    Alignment.TopEnd,
//                    Alignment.BottomStart,
//                    Alignment.BottomEnd
//                ).fastForEachIndexed { index, alignment ->
//                    AsyncImage(
//                        model = playlist.thumbnails.getOrNull(index),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .align(alignment)
//                            .size(width / 2)
//                    )
//                }
//            }
//        }
    },
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius),
    fillMaxWidth = fillMaxWidth,
    modifier = modifier
)

@Composable
fun GridItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable BoxWithConstraintsScope.() -> Unit,
    thumbnailShape: Shape,
    thumbnailRatio: Float = 1f,
    fillMaxWidth: Boolean = false,
) {
    Column(
        modifier = if (fillMaxWidth) {
            modifier
                .padding(12.dp)
                .fillMaxWidth()
        } else {
            modifier
                .padding(12.dp)
                .width(GridThumbnailHeight * thumbnailRatio)
        }
    ) {
        BoxWithConstraints(
            modifier = if (fillMaxWidth) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.height(GridThumbnailHeight)
            }
                .aspectRatio(thumbnailRatio)
                .clip(thumbnailShape)
        ) {
            thumbnailContent()
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            badges()

            Text(
                text = subtitle.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}