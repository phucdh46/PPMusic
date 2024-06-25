package com.dhp.musicplayer.core.ui.items

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.designsystem.R

@Composable
fun DeviceSongItem(
    song: Song,
    bitmap: Bitmap?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    DeviceSongItem(
        title = song.title,
        authors = song.artistsText,
        duration = song.durationText,
        bitmap = bitmap,
        thumbnailSizeDp = thumbnailSizeDp,
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@Composable
fun DeviceSongItem(
    title: String?,
    authors: String?,
    duration: String?,
    bitmap: Bitmap?,
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
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo_grayscale),
                    contentDescription = null
                )
            }
        }

        Row {
            ItemInfoContainer(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = title.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = authors.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
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