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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.utils.drawableToBitmap

@Composable
fun SongItem(
    song: Song,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    SongItem(
        thumbnailUrl = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
        title = song.title,
        authors = song.artistsText,
        duration = song.durationText,
        isOffline = song.isOffline,
        bitmap = song.getBitmap(LocalContext.current),
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
    bitmap: Bitmap?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Log.d("DHP","bitmap: $bitmap")
    SongItem(
        title = title,
        authors = authors,
        duration = duration,
        thumbnailSizeDp = thumbnailSizeDp,
        thumbnailContent = {
            if(isOffline) {
                Image(bitmap = (bitmap ?: drawableToBitmap(LocalContext.current)).asImageBitmap(), contentDescription = null)
            } else {
                AsyncImage(
                    model = thumbnailUrl,
                    error = painterResource(id = R.drawable.logo),
//                placeholder = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }


            onThumbnailContent?.invoke(this)
        },
        modifier = modifier,
        trailingContent = trailingContent
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

        ItemInfoContainer {
            trailingContent?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title.orEmpty(),
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                    )

                    it()
                }
            } ?: Text(
                text = title.orEmpty(),
                style = typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )


            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = authors.orEmpty(),
                    style = typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                )

//                duration?.let {
//                    BasicText(
//                        text = duration,
//                        style = typography.titleMedium,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        modifier = Modifier
//                            .padding(top = 4.dp)
//                    )
//                }
            }
        }
    }
}
