package com.dhp.musicplayer.ui.items

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.dhp.musicplayer.constant.ListThumbnailSize
import com.dhp.musicplayer.constant.px
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.component.LoadingFiveLinesCenter
import com.dhp.musicplayer.ui.component.LoadingShimmerImage
import com.dhp.musicplayer.utils.joinByBullet
import com.dhp.musicplayer.utils.makeTimeString

@Composable
fun MediaMetadataListItem(
    song: Song,
    bitmap: Bitmap? = null,
    modifier: Modifier,
    isShow: Boolean = false,
    isPlaying: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = song.title,
    subtitle = joinByBullet(
        song.artistsText,
        makeTimeString(song.durationText?.toLongOrNull())
    ),
    thumbnailContent = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(ListThumbnailSize)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null
                )
            } else {
                LoadingShimmerImage(
                    thumbnailSizeDp = ListThumbnailSize,
                    thumbnailUrl = song.thumbnailUrl.thumbnail(ListThumbnailSize.px),
                )
            }
            LoadingFiveLinesCenter(isPlaying = isPlaying, isShow = isShow)
        }
    },
    trailingContent = trailingContent,
    modifier = modifier
)
