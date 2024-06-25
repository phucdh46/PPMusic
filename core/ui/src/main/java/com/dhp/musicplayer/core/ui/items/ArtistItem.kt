package com.dhp.musicplayer.core.ui.items

import com.dhp.musicplayer.core.common.extensions.thumbnail
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.component.LoadingShimmerImage
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.model.music.Artist

@Composable
fun ArtistItem(
    artist: Artist,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    ArtistItem(
        thumbnailUrl = artist.thumbnailUrl,
        name = artist.name,
//        subscribersCount = artist.subscribersCountText,
        subscribersCount = null,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative
    )
}

@Composable
fun ArtistItem(
    thumbnailUrl: String?,
    name: String?,
    subscribersCount: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        LoadingShimmerImage(
            thumbnailSizeDp = thumbnailSizeDp,
            thumbnailUrl = thumbnailUrl.thumbnail(thumbnailSizeDp.px),
            modifier = Modifier
                .clip(CircleShape)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            Text(
                text = name ?: "",
                style = typography.bodyMedium,
                maxLines = if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )

            subscribersCount?.let {
                Text(
                    text = subscribersCount,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
        }
    }
}