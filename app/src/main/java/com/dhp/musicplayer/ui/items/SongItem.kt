package com.dhp.musicplayer.ui.items

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalDownloadUtil
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
        id = song.id,
        thumbnailUrl = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
        title = song.title,
        subtitle = song.artistsText,
        duration = song.durationText,
        isOffline = song.isOffline,
        bitmap = bitmap,
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@OptIn(UnstableApi::class)
@Composable
fun SongItem(
    id: String,
    thumbnailUrl: String?,
    title: String?,
    subtitle: String?,
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
        subtitle = subtitle,
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
        badges = {
            val download by LocalDownloadUtil.current.getDownload(id).collectAsState(initial = null)
            when (download?.state) {
                Download.STATE_COMPLETED -> Icon(
                    imageVector = IconApp.DownloadForOffline,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 2.dp)
                )

                Download.STATE_QUEUED, Download.STATE_DOWNLOADING -> CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 2.dp)
                )

                else -> {}
            }
        }
    )
}

@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    title: String?,
    subtitle: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    badges: @Composable RowScope.() -> Unit = {},
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    badges()
                    Text(
                        text = subtitle.orEmpty(),
                        style = typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                    )
                }

            }

            trailingContent?.let {
                it()
            }
        }
    }
}
