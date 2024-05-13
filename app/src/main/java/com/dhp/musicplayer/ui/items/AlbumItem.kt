package com.dhp.musicplayer.ui.items

import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.thumbnail
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.utils.Logg

@Composable
fun AlbumItem(
    song: Song,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    AlbumItem(
        thumbnailUrl = song.thumbnailUrl,
        title = song.title,
        authors = song.artistsText,
        year = song.durationText,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier,
        isOffline = song.isOffline,
        bitmap = song.getBitmap(LocalContext.current)
    )
}

@Composable
fun AlbumItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    isOffline: Boolean,
    bitmap: Bitmap?,
) {
//    val (_, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {

        AsyncImage(
            model = if (isOffline) bitmap else thumbnailUrl?.thumbnail(thumbnailSizePx),
            contentDescription = null,
            error = painterResource(id = R.drawable.logo),
            onLoading = { Logg.d("AsyncImage onLoading")},
            onError = { Logg.d("AsyncImage onError")},
            contentScale = ContentScale.Crop,
            modifier = Modifier
//                .clip(thumbnailShape)
                .size(thumbnailSizeDp)
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

//            BasicText(
//                text = year ?: "",
//                style = typography.xxs.semiBold.secondary,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier
//                    .padding(top = 4.dp)
//            )
        }
    }
}