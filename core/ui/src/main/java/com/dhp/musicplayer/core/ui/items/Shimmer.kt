package com.dhp.musicplayer.core.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.constant.AlbumThumbnailSizeDp
import com.dhp.musicplayer.core.designsystem.constant.Dimensions
import com.dhp.musicplayer.core.designsystem.constant.MoodAndGenresButtonHeight
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
    errorMessage: String = stringResource(id = R.string.error_message),
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage, style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        onRetry?.let {
            Button(onClick = onRetry, modifier = Modifier.clickable { onRetry() }) {
                Text(text = "Retry", style = MaterialTheme.typography.labelMedium)
            }
        }
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
    errorMessage: String = stringResource(id = R.string.error_message_paging_albums),
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .size(thumbnailSizeDp)
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage, style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        onRetry?.let {
            Button(onClick = onRetry, modifier = Modifier.clickable { onRetry() }) {
                Text(text = "Retry", style = MaterialTheme.typography.labelMedium)
            }
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
        alternative = alternative, thumbnailSizeDp = thumbnailSizeDp, modifier = modifier
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

@Composable
fun MoodAndGenresItemPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row {
        Box(
            modifier = modifier
                .padding(4.dp)
                .height(MoodAndGenresButtonHeight)
                .weight(1f)
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmer()
        )

        Box(
            modifier = modifier
                .padding(4.dp)
                .height(MoodAndGenresButtonHeight)
                .weight(1f)
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmer()
        )
    }
}