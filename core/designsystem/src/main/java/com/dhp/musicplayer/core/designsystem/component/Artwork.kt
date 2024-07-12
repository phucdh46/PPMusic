package com.dhp.musicplayer.core.designsystem.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun Artwork(
    modifier: Modifier = Modifier,
    url: String?,
    bitmap: Bitmap? = null,
    isLockAspect: Boolean = true,
    imageCoverLarge: @Composable (() -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val artworkModifier = if (isLockAspect) modifier.aspectRatio(1f) else modifier
    Box(contentAlignment = Alignment.Center) {
        bitmap?.let {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null
            )
        } ?: imageCoverLarge?.invoke()
        ?: LoadingShimmerImageMaxSize(
            modifier = artworkModifier,
            thumbnailUrl = url,
            contentScale = contentScale
        )
    }
}