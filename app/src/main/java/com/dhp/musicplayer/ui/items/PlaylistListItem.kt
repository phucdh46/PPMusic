package com.dhp.musicplayer.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.GridThumbnailHeight
import com.dhp.musicplayer.constant.ListItemHeight
import com.dhp.musicplayer.constant.ListThumbnailSize
import com.dhp.musicplayer.constant.PlayListItemHeight
import com.dhp.musicplayer.constant.ThumbnailCornerRadius
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.model.PlaylistPreview
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.component.LoadingShimmerImageMaxSize

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
    },
    trailingContent = trailingContent,
    listItemHeight = PlayListItemHeight,
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
    listItemHeight: Dp = ListItemHeight,
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
    listItemHeight = listItemHeight,
    modifier = modifier
)

@Composable
inline fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    noinline subtitle: (@Composable RowScope.() -> Unit)? = null,
    thumbnailContent: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {},
    listItemHeight: Dp = ListItemHeight,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(listItemHeight)
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
fun AlbumGridItem(
    album: Innertube.AlbumItem,
    modifier: Modifier = Modifier,
    badges: @Composable RowScope.() -> Unit = { },
    fillMaxWidth: Boolean = false,
) = GridItem(
    title = album.info?.name.orEmpty(),
    subtitle = album.year.orEmpty(),
    badges = badges,
    thumbnailContent = {
        LoadingShimmerImageMaxSize(
            thumbnailUrl = album.thumbnail?.url,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
    },
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius),
    fillMaxWidth = fillMaxWidth,
    modifier = modifier
)

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
            imageVector = IconApp.PlaylistPlay, contentDescription = null,
            modifier = Modifier
                .size(width / 2)
                .align(Alignment.Center)
        )
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
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