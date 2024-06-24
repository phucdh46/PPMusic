package com.dhp.musicplayer.core.designsystem.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp

@Composable
fun Artwork(
    modifier: Modifier = Modifier,
    url: String?,
    bitmap: Bitmap? = null,
    isLockAspect: Boolean = true,
    imageCoverLarge: @Composable (BoxWithConstraintsScope.(size: Dp) -> Unit)? = null
) {
    val artworkModifier = if (isLockAspect) modifier.aspectRatio(1f) else modifier
    BoxWithConstraints {
        bitmap?.let {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null
            )
        } ?: imageCoverLarge?.invoke(this, maxWidth)
        ?: LoadingShimmerImageMaxSize(
            modifier = artworkModifier,
            thumbnailUrl = url,
            contentScale = ContentScale.Crop
        )
    }
}