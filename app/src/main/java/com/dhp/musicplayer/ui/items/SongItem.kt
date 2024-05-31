package com.dhp.musicplayer.ui.items

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.component.LoadingShimmerImageMaxSize

@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    song: Song,
    bitmap: Bitmap? = null,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    SongItem(
        thumbnailUrl = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
        title = song.title,
        authors = song.artistsText,
        duration = song.durationText,
        isOffline = song.isOffline,
        bitmap = bitmap,
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@Composable
fun SongItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    duration: String?,
    isOffline: Boolean,
    bitmap: Bitmap? = null,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Log.d("DHP", "bitmap: $bitmap")
    SongItem(
        title = title,
        authors = authors,
        duration = duration,
        thumbnailSizeDp = thumbnailSizeDp,
        thumbnailContent = {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null
                )
            } else {
                LoadingShimmerImageMaxSize(
                    thumbnailUrl = thumbnailUrl,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            onThumbnailContent?.invoke(this)
        },
        modifier = modifier,
        trailingContent = trailingContent,
    )
}

@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    title: String?,
    authors: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {
            thumbnailContent()
        }

        Row {
            ItemInfoContainer(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = title.orEmpty(),
                    style = typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = authors.orEmpty(),
                    style = typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                )
            }

            trailingContent?.let {
                it()
            }
        }
    }
}
