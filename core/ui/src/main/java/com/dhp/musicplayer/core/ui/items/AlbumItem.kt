package com.dhp.musicplayer.core.ui.items

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.designsystem.component.LoadingShimmerImage
import com.dhp.musicplayer.core.model.music.Album

@Composable
fun AlbumItem(
    album: Album,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnailUrl,
        title = album.title,
        authors = album.authorsText,
        year = album.year,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier,
    )
}

@Composable
fun AlbumItem(
    modifier: Modifier = Modifier,
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String? = null,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    alternative: Boolean = false,
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        LoadingShimmerImage(
            thumbnailSizeDp = thumbnailSizeDp,
            thumbnailUrl = thumbnailUrl.thumbnail(thumbnailSizePx),
        )

        ItemInfoContainer {
            Text(
                text = title ?: "",
                style = typography.titleMedium,
                maxLines = if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!alternative) {
                authors?.let {
                    Text(
                        text = authors,
                        style = typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            year?.let {
                Text(
                    text = year,
                    style = typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
        }
    }
}