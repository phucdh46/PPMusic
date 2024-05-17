package com.dhp.musicplayer.ui.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.ListThumbnailSize
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.utils.drawableToBitmap
import com.dhp.musicplayer.utils.joinByBullet
import com.dhp.musicplayer.utils.makeTimeString

@Composable
fun MediaMetadataListItem(
    song: Song,
    modifier: Modifier,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = song.title,
    subtitle = joinByBullet(
        song.artistsText,
        makeTimeString(song.durationText?.toLongOrNull())
    ),
    thumbnailContent = {
        if(song.isOffline) {
            Image(bitmap = (song.getBitmap(LocalContext.current) ?: drawableToBitmap(LocalContext.current)).asImageBitmap(), contentDescription = null)
        } else {
            AsyncImage(
                model = song.thumbnailUrl,
                error = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(ListThumbnailSize)
//                .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )
        }

//        PlayingIndicatorBox(
//            isActive = isActive,
//            playWhenReady = isPlaying,
//            modifier = Modifier
//                .size(ListThumbnailSize)
//                .background(
//                    color = Color.Black.copy(alpha = 0.4f),
//                    shape = RoundedCornerShape(ThumbnailCornerRadius)
//                )
//        )
    },
    trailingContent = trailingContent,
    modifier = modifier
)
