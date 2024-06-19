package com.dhp.musicplayer.core.ui.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhp.musicplayer.core.designsystem.constant.AlbumThumbnailSizeDp
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.extensions.shimmer

import kotlin.random.Random

@Composable
fun SongItemPlaceholder(
    modifier: Modifier = Modifier,
    thumbnailSizeDp: Dp = Dimensions.thumbnails.song,
) {

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
                .shimmer()
        )

        ItemInfoContainer {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}

@Composable
fun SongErrorPagingItem(
    modifier: Modifier = Modifier,
    message: String = stringResource(id = R.string.error_message),
    thumbnailSizeDp: Dp = Dimensions.thumbnails.song,
) {

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = message,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.errorContainer,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun TextPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(remember { 0.25f + Random.nextFloat() * 0.5f })
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .shimmer()
    )
}

@Composable
fun AlbumItemError(
    modifier: Modifier = Modifier,
    thumbnailSizeDp: Dp = AlbumThumbnailSizeDp,
    alternative: Boolean = false,
    message: String = stringResource(id = R.string.error_message),
) {

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
                .clip(RoundedCornerShape(8.dp))
                .shimmer()
        )

        ItemInfoContainer {
            Text(
                text = message,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.errorContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun AlbumItemPlaceholder(
    modifier: Modifier = Modifier,
    thumbnailSizeDp: Dp = AlbumThumbnailSizeDp,
    alternative: Boolean = false
) {

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
                .clip(RoundedCornerShape(8.dp))
                .shimmer()
        )

        ItemInfoContainer {
            TextPlaceholder()

            TextPlaceholder()
        }
    }
}