package com.dhp.musicplayer.core.ui.items

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dhp.musicplayer.core.designsystem.component.LoadingShimmerImageMaxSize
import com.dhp.musicplayer.core.designsystem.constant.GridThumbnailHeight
import com.dhp.musicplayer.core.designsystem.constant.ListItemHeight
import com.dhp.musicplayer.core.designsystem.constant.PlayListItemHeight
import com.dhp.musicplayer.core.designsystem.constant.ThumbnailCornerRadius
import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.model.music.PlaylistWithSongs
import com.dhp.musicplayer.core.ui.R

@Composable
fun DefaultListItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable () -> Unit,
) = ListItem(
    title = title,
    subtitle = subtitle,
    thumbnailContent =thumbnailContent,
    trailingContent = trailingContent,
    listItemHeight = PlayListItemHeight,
    modifier = modifier
)

@Composable
fun PlaylistListItem(
    playlistWithSongs: PlaylistWithSongs,
    modifier: Modifier = Modifier,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = playlistWithSongs.playlist.name,
    subtitle = pluralStringResource(
        R.plurals.n_song,
        playlistWithSongs.songs.size,
        playlistWithSongs.songs.size
    ),
    thumbnailContent = {
        CoverImagePlaylist(playlistWithSongs = playlistWithSongs)
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
    album: Album,
    modifier: Modifier = Modifier,
    badges: @Composable RowScope.() -> Unit = { },
    fillMaxWidth: Boolean = false,
) = GridItem(
    title = album.title.orEmpty(),
    subtitle = album.year.orEmpty(),
    badges = badges,
    thumbnailContent = {
        LoadingShimmerImageMaxSize(
            thumbnailUrl = album.thumbnailUrl,
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
    playlistWithSongs: PlaylistWithSongs,
    modifier: Modifier = Modifier,
    badges: @Composable RowScope.() -> Unit = { },
    fillMaxWidth: Boolean = false,
) = GridItem(
    title = playlistWithSongs.playlist.name,
    subtitle = pluralStringResource(
        R.plurals.n_song,
        playlistWithSongs.songs.size,
        playlistWithSongs.songs.size
    ),
    badges = badges,
    thumbnailContent = {
        CoverImagePlaylist(playlistWithSongs = playlistWithSongs, size = maxWidth)
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